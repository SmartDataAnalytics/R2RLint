package org.aksw.sparqlify.qa.metrics.conciseness;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.aksw.commons.collections.Pair;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.MappingMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;


/**
 * This metric measures how often redundant identifiers of database entities
 * occur in a view definition. "Redundant identifiers" means any resources that
 * are (in some way) built based in a certain DB entity or one of its
 * attributes.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class ExtensionalConciseness extends MetricImpl implements MappingMetric {

	@Override
	public void assessMappings(Collection<ViewDefinition> viewDefs)
			throws NotImplementedException, SQLException {

		ViewDefsInfosEC vdInfos = new ViewDefsInfosEC();
		
		vdInfos.read(viewDefs);

		List<Pair<Float, List<Pair<Node_Variable, ViewDefinition>>>> redNodeOccs =
				vdInfos.getRedundantNodeOccurrences();

		for (Pair<Float, List<Pair<Node_Variable, ViewDefinition>>> redNodeOcc : redNodeOccs) {
			writeMappingVarMeasureToSink(redNodeOcc.first, redNodeOcc.second);
		}
	}

}


class ViewDefsInfosEC {
	//                               col                             view def
	//         relation name        var(s)                            name
	private HashMap<String, HashMap<String, List<Pair<Node_Variable, String>>>> data;
	private HashMap<String, List<String>> aliases;
	private HashMap<String, ViewDefinition> viewDefsMap;


	public ViewDefsInfosEC() {
		data = new HashMap<String, HashMap<String, List<Pair<Node_Variable, String>>>>();
		aliases = new HashMap<String, List<String>>();
		viewDefsMap = new HashMap<String, ViewDefinition>();
	}


	void read(Collection<ViewDefinition> viewDefs) {
		
		for (ViewDefinition viewDef : viewDefs) {
			viewDefsMap.put(viewDef.getName(), viewDef);
			
			String relationName = readRelationName(viewDef);
			
			// get info data
			HashMap<String, List<Pair<Node_Variable, String>>> ctInfos = readCTInfos(viewDef);
			
			// merge it into the existing data
			if (data.containsKey(relationName)) {
				for (String varKey : ctInfos.keySet()) {
					List<Pair<Node_Variable, String>> varVals = ctInfos.get(varKey);
					if (data.get(relationName).containsKey(varKey)) {
						
						for (Pair<Node_Variable, String> varVal : varVals) {
							if (!data.get(relationName).get(varKey).contains(varVal)) {
								data.get(relationName).get(varKey).add(varVal);
							}
						}
					} else {
						data.get(relationName).put(varKey, varVals);
					}
				}
				
			} else {
				data.put(relationName, ctInfos);
			}
			
		}
	}


	private HashMap<String, List<Pair<Node_Variable, String>>> readCTInfos(
			ViewDefinition viewDef) {
		
		HashMap<String, List<Pair<Node_Variable, String>>> ctInfos =
				new HashMap<String, List<Pair<Node_Variable,String>>>();
		
		VarDefinition varDefs = viewDef.getMapping().getVarDefinition();
		
		QuadPattern quadPatterns = viewDef.getTemplate();
		for (Quad quadPattern : quadPatterns.getList()) {
			
			List<Node> nodes = new ArrayList<Node>(Arrays.asList(
					quadPattern.getSubject(), quadPattern.getPredicate(),
					quadPattern.getObject()));
			
			for (Node node : nodes) {
				if (node.isVariable()) {
					Var nodevar = Var.alloc((Node_Variable) node);
					Collection<RestrictedExpr> termConstructors =
							varDefs.getDefinitions(nodevar);
					
					Set<Var> colVars = null;
					for (RestrictedExpr termConstructor : termConstructors) {
						colVars =
								termConstructor.getExpr().getVarsMentioned();
					}
					
					String key;
					if (colVars.size() > 1) {
						key = aliasOf(colVars);
					} else {
						if (colVars.size() == 0) continue;
						
						key = colVars.iterator().next().getName();
					}
					
					Pair<Node_Variable, String> varViewDef =
							new Pair<Node_Variable, String>(nodevar, viewDef.getName());
							
					
					if (ctInfos.containsKey(key)) {
						if (!ctInfos.get(key).contains(varViewDef)){
							ctInfos.get(key).add(varViewDef);
						}
					} else {
						List<Pair<Node_Variable, String>> value =
								new ArrayList<Pair<Node_Variable,String>>();
						value.add(varViewDef);
						ctInfos.put(key, value );
					}
				}
			}
		}
		
		
		return ctInfos;
	}


	List<Pair<Float, List<Pair<Node_Variable, ViewDefinition>>>> getRedundantNodeOccurrences() {
		
		List<Pair<Float, List<Pair<Node_Variable, ViewDefinition>>>> redNodeOcc =
				new ArrayList<Pair<Float, List<Pair<Node_Variable, ViewDefinition>>>>();
		
		for (HashMap<String, List<Pair<Node_Variable, String>>> colVarInfo : data.values()){
			
			for (List<Pair<Node_Variable, String>> nodeViewDefNames : colVarInfo.values()) {
				
				// redundant if list contains more than one entry
				if (nodeViewDefNames.size() > 1) {
					
					// get measure
					float measure = 1 / (float) nodeViewDefNames.size();
					
					// get redundant node variables and the view definitions
					// they belong to
					List<Pair<Node_Variable, ViewDefinition>> nodeViewDefs =
							new ArrayList<Pair<Node_Variable, ViewDefinition>>();
					
					for (Pair<Node_Variable, String> nodeViewDefName : nodeViewDefNames) {
						ViewDefinition viewDef = viewDefsMap.get(nodeViewDefName.second);
						
						if (viewDef == null) {
							throw new RuntimeException("View definition not found");
						}
						
						nodeViewDefs.add(new Pair<Node_Variable, ViewDefinition>(nodeViewDefName.first, viewDef));
					}
					
					redNodeOcc.add(new Pair<Float, List<Pair<Node_Variable, ViewDefinition>>>(measure, nodeViewDefs));
					
				}
			}
		}
		
		return redNodeOcc;
	}
	


	private String readRelationName(ViewDefinition viewDef) {
		SqlOp relation = viewDef.getMapping().getSqlOp();
		String relationName = "";
		
		if (relation instanceof SqlOpTable) {
			relationName = ((SqlOpTable) relation).getTableName();
		} else if (relation instanceof SqlOpQuery) {
			relationName = ((SqlOpQuery) relation).getQueryString();
		}
		return relationName;
	}


	/*
	 * needed to get a string representation of a list of variables that is
	 * order-agnostic, so [a, b] and [b, a] will result in the same string
	 * representation "[a, b]"
	 */
	private String aliasOf(Collection<Var> vars) {
		SortedSet<String> varNames = new TreeSet<String>();
		
		for (Var var : vars) {
			varNames.add(var.getName());
		}
		
		String alias = varNames.toString();
		
		List<String> tmp = new ArrayList<String>();
		aliases.put(alias, tmp);
		for (Var var : vars) {
			aliases.get(alias).add(var.getName());
		}
		
		return alias;
	}
	
	public String toString() {
		return data.toString();
	}
}