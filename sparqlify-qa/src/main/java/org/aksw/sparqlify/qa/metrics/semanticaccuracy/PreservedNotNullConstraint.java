package org.aksw.sparqlify.qa.metrics.semanticaccuracy;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.aksw.commons.collections.Pair;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.ViewMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.vocabulary.OWL;

/**
 * This metric checks for all used term constructors, if the columns involved
 * have any NOT NULL constraints defined in the relational schema. If so, to
 * preserve this semantic constraint, there should also be an owl:cardinality
 * constraint as follows:
 * - there is a term constructor assignment, e.g. ?nnc = uri(?notNUllCol)
 *   whereas ?notNullCol refers to a column having a NOT NULL constraint
 * - ?nnc is used in a triple pattern like: ?sth ex:prop ?nnc .
 * - there should be patterns like
 *     _:r a owl:Restriction .
 *     _:r owl:onProperty ex:prop .
 *     _:r owl:cardinality 1^^xsd:int .
 *   or
 *     _:r a owl:Restriction .
 *     _:r owl:onProperty ex:prop .
 *     _:r owl:minCardinality 1^^xsd:int .
 *     
 * Since a full fledged SQL parser would be needed to get the columns of an SQL
 * expression used as logical table, they are not considered for now.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class PreservedNotNullConstraint extends MetricImpl implements ViewMetric {

	@Autowired
	DataSource rdb;
	Connection conn;
	
	@PostConstruct
	private void init() throws SQLException {
		conn = rdb.getConnection();
	}
	
	@PreDestroy
	private void cleanUp() throws SQLException {
		conn.close();
	}
	
	
	@Override
	public void assessViews(Collection<ViewDefinition> viewDefs)
			throws NotImplementedException, SQLException {
		
		for (ViewDefinition viewDef : viewDefs) {
			assessViewDef(viewDef);
		}
	}

	private void assessViewDef(ViewDefinition viewDef) throws SQLException,
			NotImplementedException {
		
		SqlOp tbl = viewDef.getMapping().getSqlOp();
		String tblName;
		if (tbl instanceof SqlOpTable) {
			tblName = ((SqlOpTable) tbl).getTableName();
			
		// TODO: add support for SQL expressions
		} else {
			tblName = null;
		}
		
		if (tblName != null) {
			Multimap<Var, RestrictedExpr> tcMap =
					viewDef.getMapping().getVarDefinition().getMap();
			
			for (Var tcVar : tcMap.keySet()) {
				for (RestrictedExpr tc : tcMap.get(tcVar)) {
					Set<Var> cols = tc.getExpr().getVarsMentioned();
					
					for (Var col : cols) {
						if (isConstrained(col, tblName)) {
							// there is a NOT NULL constraint for column col in table tbl
							
							if (!hasCardinalityConstraint(tcVar, viewDef.getTemplate())) {
								List<Pair<Node_Variable, ViewDefinition>> nodeViewDefs =
										new ArrayList<Pair<Node_Variable, ViewDefinition>>();
								
								Pair<Node_Variable, ViewDefinition> res =
										new Pair<Node_Variable, ViewDefinition>(
												(Node_Variable) tcVar.asNode(), viewDef);
								
								nodeViewDefs.add(res);
								writeMappingVarMeasureToSink(0, nodeViewDefs);
							}
						}
					}
				}
			}
		}
	}
	
	
	private boolean hasCardinalityConstraint(Var var, QuadPattern quadPatterns) {
		
		List<Node> predicates = new ArrayList<Node>();
		
		// 1st time looping: find the properties used to assign var
		for (Quad quadPattern : quadPatterns.getList()) {
			if (quadPattern.getObject().equals(var)) {
				predicates.add(quadPattern.getPredicate());
			}
		}
		
		if (!predicates.isEmpty()) {
			HashMap<Node, HashMap<String, Node>> constraints =
					new HashMap<Node, HashMap<String, Node>>();
			
			// 2nd time looping: check if there are cardinality constraints for pred
			for (Quad quadPattern : quadPatterns.getList()) {
				Node subj = quadPattern.getSubject();
				Node pred = quadPattern.getPredicate();
				Node obj = quadPattern.getObject();
				
				if (pred.equals(OWL.onProperty.asNode())) {
					for (Node predicate : predicates) {
						if (obj.equals(predicate)) {
							if (constraints.containsKey(subj)) {
								constraints.get(subj).put("prop", pred);
								
							} else {
								
								HashMap<String, Node> value =
										new HashMap<String, Node>();
								
								value.put("prop", pred);
								constraints.put(subj, value);
							}
							break;
						}
					}
				
				} else if (pred.equals(OWL.cardinality.asNode())
						|| pred.equals(OWL.minCardinality.asNode())) {
					// check if the actual cardinality constraint is sth like >= 1
					if (obj.isLiteral()) {
						String rawVal = obj.getLiteralLexicalForm();
						int val;
						try {
							val = Integer.parseInt(rawVal);
						} catch (NumberFormatException e) {
							val = -1;
						}
						if (val >= 1) {
							if (constraints.containsKey(subj)) {
								constraints.get(subj).put("val", obj);
							} else {
								HashMap<String, Node> value =
										new HashMap<String, Node>();
								value.put("val", obj);
								constraints.put(subj, value );
							}
						}
					}
				}
			}
			
			for (Node constr : constraints.keySet()) {
				if (constraints.get(constr).containsKey("prop")
						&& constraints.get(constr).containsKey("val")) {
					return true;
				}
			}
		
		} else {
			// since the considered resource is not used as object there is
			// no need for having an explicit cardinality constraint
			return true;
		}
		
		return false;
	}
	
	
	private boolean isConstrained(Var column, String tblName) throws SQLException {
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet res = meta.getColumns(conn.getCatalog(), null, tblName, column.getName());
		if (!res.next()) {
			return false;
		} else {
			if (res.getBoolean("NULLABLE")) return false;
			else return true;
		}
	}
}
