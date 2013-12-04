package org.aksw.sparqlify.qa.metrics.accuracy;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
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
import org.aksw.sparqlify.qa.metrics.MappingMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

@Component
public class PreservedFunctionalAttributes extends MetricImpl implements
		MappingMetric {

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
	public void assessMappings(Collection<ViewDefinition> viewDefs)
			throws NotImplementedException, SQLException {
		
		for (ViewDefinition viewDef : viewDefs) {
			assessViewDef(viewDef);
		}
	}

	private void assessViewDef(ViewDefinition viewDef) throws SQLException, NotImplementedException {
		SqlOp tbl = viewDef.getMapping().getSqlOp();
		String tblName = null;
		QuadPattern quadPatterns = viewDef.getTemplate();
		
		if (tbl instanceof SqlOpTable) {
			tblName = ((SqlOpTable) tbl).getTableName();
		}
		// TODO: add support for SQL queries (not examined here due to the lack
		// of an SQL parser)
		
		if (tblName != null) {
			List<String> fkeyCols = getFkeys(tblName);
			Multimap<Var, RestrictedExpr> tcMap =
					viewDef.getVarDefinition().getMap();
			
			for (Var tcVar : tcMap.keySet()) {
				for (RestrictedExpr tc : tcMap.get(tcVar)) {
					Set<Var> cols = tc.getExpr().getVarsMentioned();
					
					for (Var col : cols) {
						if (!fkeyCols.contains(col.getName())) {
							List<Pair<Node, Quad>> funcProps =
									new ArrayList<Pair<Node, Quad>>();
							
							// get the property, used to assign the column value
							for (Quad quadPattern : quadPatterns.getList()) {
								Node predicate = quadPattern.getPredicate();
								Node object = quadPattern.getObject();
								
								if (tcVar.asNode().equals(object)) {
									funcProps.add(new Pair<Node, Quad>(predicate, quadPattern));
								}
							}
							
							// check if the owl:FunctionalPropery class is
							// assigned to funcProp
							for (Pair<Node, Quad> nodeQuad : funcProps) {
								Node funcProp = nodeQuad.first;
								
								boolean assignedFunctional = false;
								for (Quad quadPattern : quadPatterns.getList()) {
									Node subj = quadPattern.getSubject();
									Node pred = quadPattern.getPredicate();
									Node obj = quadPattern.getObject();
									
									if (subj.equals(funcProp)
											&& pred.equals(RDF.type.asNode())
											&& obj.equals(OWL.FunctionalProperty.asNode())) {
										assignedFunctional = true;
									}
								}
								if (!assignedFunctional) {
									// TODO: check against white list containing
									// properties known to be declared functional
									List<Pair<Quad, ViewDefinition>> quadViewDefs =
											new ArrayList<Pair<Quad,ViewDefinition>>();
									
									Pair<Quad, ViewDefinition> entry =
											new Pair<Quad, ViewDefinition>(
													nodeQuad.second, viewDef);
									
									quadViewDefs.add(entry);
									
									writeMappingQuadMeasureToSink(0, quadViewDefs);
								}
							}
						}
					}
				}
			}
		}
	}
	
	
	private List<String> getFkeys(String tableName) throws SQLException {
		DatabaseMetaData meta = conn.getMetaData();
		List<String> fkeys = new ArrayList<String>();
		// query foreign keys
		ResultSet fkRes = meta.getImportedKeys(conn.getCatalog(), null, tableName);
		
		while (fkRes.next()) {
			fkeys.add(fkRes.getString(8));
		}
		return fkeys;
	}
}
