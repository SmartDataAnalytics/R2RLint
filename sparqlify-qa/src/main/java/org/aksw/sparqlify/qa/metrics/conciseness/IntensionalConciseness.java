package org.aksw.sparqlify.qa.metrics.conciseness;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;



/**
 * This metric measures how often redundant predicate are used. Redundant here
 * means that for a given predicate there is at least another one that
 * expresses the same property but having a different name. One example could
 * be a property foo:height expressing the size of sth. and foo:size expressing
 * the same.
 * 
 * Finding such redundant predicates in general would mean checking the domain
 * and range of a predicate. These could be stated explicitly via rdfs:domain
 * and rdfs:range. If there are no explicit specifications, another way is to
 * look at the classes subjects using the considered predicate are assigned to.
 * The object classes or types could be checked accordingly.
 * 
 * In both cases, if domain and range of different predicates are equal it is
 * likely that they express the same property.
 * 
 * In the RDB2RDF case things are even more explicit. Since the data is
 * retrieved from a database referring to certain relation attributes one has
 * to check if there are triple (or quad) patterns using
 * 1) the same pattern variables within one view definition
 * 2) a different pattern variable within one view definition, but using the
 *    same term constructor and refer to the same relational attribute
 * 3) the same term constructor referring to the same relation attribute in
 *    different view definitions
 * 
 * Case 3) is quite tricky to implement since such a relation could be an SQL
 * query being composed of nested sub-queries. So to really check if to such
 * queries refer to the same attribute and really ensure that none of the used
 * attributes result from an alias a full fledged SQL parser would be needed.
 * So 3) is not checked in the required depth here but relaxed to cover the
 * following things:
 * 3a) check if the same term constructor refer to the same relation attribute
 *     of a table in different view definitions
 * 3b) check if the same term constructor refer to the same relation attribute
 *     of a simple query (no sub-queries, no aliases) in different view
 *     definitions
 * 
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
public class IntensionalConciseness extends MetricImpl implements MappingMetric {
	
	@Override
	public void assessMappings(Collection<ViewDefinition> viewDefs)
			throws NotImplementedException {

		ViewDefsInfos vdInfos = new ViewDefsInfos();
		
		for (ViewDefinition viewDef : viewDefs) {
			vdInfos.read(viewDef);
		}
		
		List<Pair<Float, List<ViewDefinition>>> redundantVDsOccurrences =
				vdInfos.getRedundantVDOccurrences();
		
		for (Pair<Float, List<ViewDefinition>> redundantVDsOccurrence : redundantVDsOccurrences) {
			float val = redundantVDsOccurrence.first;
			List<ViewDefinition> vDs = redundantVDsOccurrence.second;
			writeMappingMeasureToSink(val, vDs);
		}
	}
	
}


class ViewDefsInfos {
	
	//                                 term constructors
	//         relation name             subject object
	private HashMap<String, HashMap<Pair<String, String>, List<Pair<Quad, ViewDefinition>>>> data; 

	public ViewDefsInfos() {
		data = new HashMap<String, HashMap<Pair<String, String>, List<Pair<Quad, ViewDefinition>>>>();
		
	}


	void read(ViewDefinition viewDef) {
		String relationName = readRelationName(viewDef);
		HashMap<Pair<String, String>, List<Pair<Quad, ViewDefinition>>> termConstructorInfo = 
				readTCInfos(viewDef);
		if (!data.containsKey(relationName)) {
			data.put(relationName, termConstructorInfo);
		} else {
			HashMap<Pair<String, String>, List<Pair<Quad, ViewDefinition>>> tcInfo = data.get(relationName);
			
			for (Pair<String, String> key : termConstructorInfo.keySet()) {
				if (!tcInfo.containsKey(key)) {
					tcInfo.put(key, termConstructorInfo.get(key));
				} else {
					
					tcInfo.get(key).addAll(termConstructorInfo.get(key));
				}
				
			}
		}
	}


	private HashMap<Pair<String, String>, List<Pair<Quad, ViewDefinition>>> readTCInfos(
			ViewDefinition viewDef) {
		
		HashMap<Pair<String, String>, List<Pair<Quad, ViewDefinition>>> tcInfos =
				new HashMap<Pair<String, String>, List<Pair<Quad, ViewDefinition>>>();
		
		
		VarDefinition varDefs = viewDef.getMapping().getVarDefinition();
		
		QuadPattern quadPatterns = viewDef.getTemplate();
		for (Quad quadPattern : quadPatterns.getList()) {
			
			Node subject = quadPattern.getSubject();
			Node object = quadPattern.getObject();
			
			if (!subject.isVariable() || !object.isVariable()) continue;
			
			// get string of the subjects term constructor
			Var subjVar = Var.alloc(subject);
			Collection<RestrictedExpr> subjTermConstructor =
					varDefs.getDefinitions(subjVar);
			String subjTCString = "";
			
			// collection can only contain one item --> dummy loop
			for (RestrictedExpr restrExpr : subjTermConstructor) {
				subjTCString = restrExpr.getExpr().toString();
			}
			
			// get string of the objects term constructor
			Var objVar = Var.alloc(object);
			Collection<RestrictedExpr> objTermConstructor =
					varDefs.getDefinitions(objVar);
			
			String objTCString = "";
			
			// collection can only contain one item --> dummy loop
			for (RestrictedExpr restrExpr : objTermConstructor) {
				objTCString = restrExpr.getExpr().toString();
			}
			
			Pair<String, String> termConstructors =
					new Pair<String, String>(subjTCString, objTCString);
			Pair<Quad, ViewDefinition> quadViewDef =
					new Pair<Quad, ViewDefinition>(quadPattern, viewDef);
			if (tcInfos.containsKey(termConstructors)) {
				tcInfos.get(termConstructors).add(quadViewDef);
			} else {
				List<Pair<Quad, ViewDefinition>> tmp =
						new ArrayList<Pair<Quad, ViewDefinition>>();
				tmp.add(quadViewDef);
				
				tcInfos.put(termConstructors, tmp);
			}
		}
		
		return tcInfos;
	}

	public List<Pair<Float, List<ViewDefinition>>> getRedundantVDOccurrences() {
		List<Pair<Float, List<ViewDefinition>>> res =
				new ArrayList<Pair<Float, List<ViewDefinition>>>();
		
		for (String relKey : data.keySet()) {
			HashMap<Pair<String, String>, List<Pair<Quad, ViewDefinition>>> relVal = data.get(relKey);
			for (Pair<String, String> tcStrings : relVal.keySet()) {
				List<Pair<Quad, ViewDefinition>> tcVal = relVal.get(tcStrings);
				int size = tcVal.size(); 
				if (size > 1) {
					float measure = 1 / (float) size;
					List<ViewDefinition> viewDefs = new ArrayList<ViewDefinition>();
					for (Pair<Quad, ViewDefinition> quadViewDef : tcVal) {
//						if (!viewDefs.contains(quadViewDef.second)) {
							viewDefs.add(quadViewDef.second);
//						}
					}
					
					res.add(new Pair<Float, List<ViewDefinition>>(measure, viewDefs));
				}
			}
		}
		return res;
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
}