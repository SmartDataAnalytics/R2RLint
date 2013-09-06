package org.aksw.sparqlify.qa.metrics.consistency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.Pair;
import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.PinpointMetric;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;


/**
 * This metric reports outliers or conflicts of literal datatypes. Since it is
 * meant to measure the homogeneity and not the validity possible rdfs:range
 * restrictions are not evaluated.
 * 
 * This metric makes two distinctions as far as homogeneity is concerned:
 * 1) if there are just a few triples of one predicate having a datatype
 *    different from all other triples, they are considered as outliers and
 *    are all reported
 * 2) if there is no obvious ratio of possibly wrong and possibly right triples
 *    the conflicting view candidates are reported
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 */
public class HomogeneousDatatypes extends PinpointMetric implements
		DatasetMetric {

	/*
	 *	{ 'ex:pred1': {
	 *			'xsd:int': [
	 *					<ex:foo1> <ex:pred1> "23"^^xsd:int,
	 *					<ex:foo3> <ex:pred1> "42"^^xsd:int,
	 *					<ex:foo4> <ex:pred1> "121"^^xsd:int
	 *				],
	 *			'xsd:string': [
	 *					<ex:foo2> <ex:pred1> "I am wrong!"^^xsd:string
	 *				]
	 *		},
	 *	 'ex:pred2': {
	 *			'xsd:int': [
	 *					...
	 *				] 
	 *		}
	 *	}
	 *
	 */
	private HashMap<String, HashMap<String, List<Triple>>> datatypes;
	private float outlierValue = (float) 0.5;
	private float conflictValue = 0;


	public void setOutlierValue(float value) {
		outlierValue = value;
	}

	public void setConflictValue(float value) {
		conflictValue = value;
	}


	public HomogeneousDatatypes() {
		super();
		datatypes = new HashMap<String, HashMap<String, List<Triple>>>();
	}


	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException {

		StmtIterator statementIt = dataset.listStatements();
		while (statementIt.hasNext()) {
			Statement statement = statementIt.next();
			if (statement.getObject().isLiteral()) {
				Node subjectNode = statement.getSubject().asNode();
				Node predicateNode = statement.getPredicate().asNode();
				String predicateStr = statement.getPredicate().getURI();
				Node objectNode = statement.getObject().asNode();
				String dtype= statement.getObject().asLiteral().getDatatypeURI();
				Triple triple = new Triple(subjectNode, predicateNode, objectNode);
				
				if (datatypes.containsKey(predicateStr)) {
					if (datatypes.get(predicateStr).containsKey(dtype)) {
						datatypes.get(predicateStr).get(dtype).add(triple);
					} else {
						List<Triple> triples = new ArrayList<Triple>();
						triples.add(triple);
						datatypes.get(predicateStr).put(dtype, triples);
					}
				} else {
					HashMap<String, List<Triple>> dtypeMap =
							new HashMap<String, List<Triple>>();
					List<Triple> triples = new ArrayList<Triple>();
					triples.add(triple);
					dtypeMap.put(dtype, triples);
					datatypes.put(predicateStr, dtypeMap);
				}
			}
		}
		
		for (HashMap<String, List<Triple>> dtypeMap : datatypes.values()) {
			if (dtypeMap.keySet().size() > 1) {
				/*
				 * The predicate has at least two differently datatyped
				 * literals.
				 * The question now is which of the triples to report.
				 * The approach is as follows:
				 * 1) if there is a certain ratio with a great portion of
				 *    objects of one type and a small one of objects with other
				 *    types, the objects of the small parts are considered an
				 *    error and reported
				 * 2) if there isn't such a obvious ratio the conflicting view
				 *    definitions (a.k.a. "mappings") as such are reported to
				 *    avoid writing back thousands of triples all having the
				 *    same error cause
				 * 
				 * The 'certain ratio' is taken from this.treshold .
				 * With this approach, the second case will slow down things
				 * because the pinpointer has to be called every time
				 * eventually returning the same view definitions ever and ever
				 * again. But that's life and the calls are necessary since all
				 * view definitions have to be found. And since the triples
				 * themselves are not likely to be the same for every call it
				 * makes less sense to implement caching here.
				 */
				int numTriples = 0;
				int biggestNumTriples = 0;
				for (List<Triple> triples : dtypeMap.values()) {
					int thisNum = triples.size();
					numTriples += thisNum;
					biggestNumTriples  = Math.max(biggestNumTriples, thisNum);
				}
				
				int numTriplesThreshold = (int) (numTriples * threshold);
				System.out.println("biggest num: " + biggestNumTriples);
				System.out.println("threshold: " + numTriplesThreshold);
				System.out.println("-----");
				if (biggestNumTriples >= numTriplesThreshold) {
					// plan 1)
					reportOutliers(numTriplesThreshold, dtypeMap.values());
				} else {
					// plan 2)
					reportConflicts(dtypeMap.values());
				}
			}
		}
	}


	private void reportOutliers(int numTriplesThreshold,
			Collection<List<Triple>> dtypeMapVals)
			throws NotImplementedException {
		
		for (List<Triple> triples : dtypeMapVals) {
			if (triples.size() < numTriplesThreshold) {
				for (Triple triple : triples) {
					Set<ViewQuad<ViewDefinition>> candidates =
							pinpointer.getViewCandidates(triple);
					
					Pair<Triple, Set<ViewQuad<ViewDefinition>>> tmp =
							new Pair<Triple, Set<ViewQuad<ViewDefinition>>>(triple, candidates);
					
					List<Pair<Triple, Set<ViewQuad<ViewDefinition>>>> pinpointResult =
							new ArrayList<Pair<Triple, Set<ViewQuad<ViewDefinition>>>>();
					
					pinpointResult.add(tmp);
					
					writeTriplesMeasureToSink(outlierValue, pinpointResult);
				}
			}
		}
	}


	private void reportConflicts(Collection<List<Triple>> dtypeMapVals)
			throws NotImplementedException {
		
		Set<ViewQuad<ViewDefinition>> candidates =
				new HashSet<ViewQuad<ViewDefinition>>();
		
		for (List<Triple> triples : dtypeMapVals) {
			for (Triple triple : triples) {
				candidates.addAll(pinpointer.getViewCandidates(triple));
			}
		}
		
		writeMappingMeasureToSink(conflictValue, candidates);
	}
}
