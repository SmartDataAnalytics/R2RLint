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
import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
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
import com.hp.hpl.jena.sparql.core.Var;

@Component
public class PreservedFkeyConstraint extends MetricImpl implements ViewMetric {

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
		String tblName = null;
		if (tbl instanceof SqlOpTable) {
			tblName = ((SqlOpTable) tbl).getTableName();
		}
		// TODO: add support for logical tables based on SQL query (not
		// possible now due to the lack of a handy SQL parser)
		
		if (tblName != null) {
			List<String> fkCols = getFkCols(tblName);
			
			// has map to resolve the quad pattern variables by column name
			HashMap<String, List<Var>> col2patternVar =
					getCol2PatternVar(viewDef.getMapping());
			
			// list needed to later check if a foreign key node is referenced
			// form a non-foreign key node
			List<Node> fkColNodes = new ArrayList<Node>();
			for (String fkCol : fkCols) {
				if (col2patternVar.containsKey(fkCol)) {
					List<Var> nodes = col2patternVar.get(fkCol);
					for (Var node : nodes) {
						fkColNodes.add(node.asNode());
					}
				}
			}
			
			
			for (String fkCol : fkCols) {
				
				List<Var> fkVars = col2patternVar.get(fkCol);
				
				for (Var fkVar : fkVars) {
					Node fkNode = fkVar.asNode();
					
					boolean fkNodeReferencedAsObject = false;
					boolean fkNodeUsedAsSubject = false;
					for (Quad quadPattern : viewDef.getTemplate()) {
						Node subj = quadPattern.getSubject();
						Node obj = quadPattern.getObject();
						
						if (subj.isVariable() && !fkColNodes.contains(subj)
								&& obj.equals(fkNode)) {
							fkNodeReferencedAsObject = true;
						}
						
						if (subj.equals(fkNode)) {
							fkNodeUsedAsSubject = true;
						}
					}
					
					if (fkNodeUsedAsSubject && !fkNodeReferencedAsObject) {
						
						List<Pair<Node_Variable, ViewDefinition>> nodeViewDefs =
								new ArrayList<Pair<Node_Variable,ViewDefinition>>();
						
						Pair<Node_Variable, ViewDefinition> entry =
								new Pair<Node_Variable, ViewDefinition>(
										(Node_Variable) fkNode, viewDef);
						
						nodeViewDefs.add(entry);
						
						writeMappingVarMeasureToSink(0, nodeViewDefs);
					}
				}
			}
		}
	}
	
	
	private HashMap<String, List<Var>> getCol2PatternVar(Mapping mapping) {
		HashMap<String, List<Var>> col2patternVar = new HashMap<String, List<Var>>();
		VarDefinition varDef = mapping.getVarDefinition();
		Multimap<Var, RestrictedExpr> tcVars = varDef.getMap();
		
		for (Var var : tcVars.keySet()) {
			// hint: tc stands for term constructor
			Collection<RestrictedExpr> tcs = tcVars.get(var);
			
			for (RestrictedExpr tc : tcs) {
				Set<Var> cols = tc.getExpr().getVarsMentioned();
				for (Var col : cols) {
					String colName = col.getName();
					if (col2patternVar.containsKey(colName)) {
						col2patternVar.get(colName).add(var);
					} else {
						List<Var> vars = new ArrayList<Var>();
						vars.add(var);
						col2patternVar.put(colName, vars);
					}
				}
			}
		}
		
		return col2patternVar;
	}
	
	
	private List<String> getFkCols(String tableName) throws SQLException {
		
		List<String> fkCols = new ArrayList<String>();
		DatabaseMetaData meta = conn.getMetaData();
		
		// query foreign keys
		ResultSet fkRes = meta.getImportedKeys(conn.getCatalog(), null, tableName);
		
		while (fkRes.next()) {
			String fkeyCol = fkRes.getString(8);
			fkCols.add(fkeyCol);
		}
		
		return fkCols;
	}
}
