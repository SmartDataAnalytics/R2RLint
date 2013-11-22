package org.aksw.sparqlify.qa.metrics.interpretability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.Pair;
import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * This metric should find violations of the syntax and semantics of reification
 * statements.
 * It is checked if
 * - the reification statement is typed as rdf:Statement
 * - more than one subject/predicate/object is used
 * - the statement is complete (rdf:subject, rdf:predicate, rdf:object present)
 * - a blank node or literal is used as rdf:predicate value
 * - a literal is used as rdf:subject value
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class CorrectReificationUse extends MetricImpl implements
		DatasetMetric {

	@Autowired
	private Pinpointer pinpointer;
	private float noReificationTypeVal = (float) 0.5;
	private float missingReificationPartVal = 0;
	private float multipleReificationPartsVal = 0;
	private float wrongValueTypeVal = 0;
	
	// setter mainly intended for testing purposes
	public void setNoReificationTypeVal(float val) {
		noReificationTypeVal = val;
	}
	public void setMissingReificationPartVal(float val) {
		missingReificationPartVal = val;
	}
	public void setMultipleReificationPartsVal(float val) {
		multipleReificationPartsVal = val;
	}
	public void setWrongValueTypeVal(float val) {
		wrongValueTypeVal = val;
	}
	
	private List<Resource> seenReificationResources;
	private List<Property> reificationProperties = new ArrayList<Property>(
			Arrays.asList(RDF.subject, RDF.predicate, RDF.object));

	protected void clearCaches() {
		seenReificationResources = new ArrayList<Resource>();
	}
	
	public CorrectReificationUse() {
		seenReificationResources = new ArrayList<Resource>();
	}


	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException {
		
		for (Property reifProp : reificationProperties) {
			StmtIterator reifStmntsIt = dataset.listStatements(null, reifProp,
					(RDFNode) null);
			
			while(reifStmntsIt.hasNext()) {
				Statement reifStmnt = reifStmntsIt.next();
				Resource reificationResource = reifStmnt.getSubject();
				if (!seenReificationResources.contains(reificationResource)) {
					checkIfTyped(reificationResource, dataset);
					checkReificationParts(reificationResource, dataset);
				}
			}
		}
		
	}


	private void checkIfTyped(Resource reificationResource,
			SparqlifyDataset dataset) throws NotImplementedException {
		
		StmtIterator typeStmntsIt = dataset.listStatements(reificationResource,
				RDF.type, (RDFNode) null);
		
		boolean isReificationTyped = false;
		while (typeStmntsIt.hasNext()) {
			Statement statement = typeStmntsIt.next();
			RDFNode object = statement.getObject();
			if (object.isResource() && object.asResource().equals(RDF.Statement)) {
				isReificationTyped = true;
				break;
			}
		}
		
		if (!isReificationTyped) {
			// create dummy triple representing the missing statement
			Triple triple = new Triple(reificationResource.asNode(),
					RDF.type.asNode(), RDF.Statement.asNode());
			
			writeTripleMeasureToSink(noReificationTypeVal, triple, null);
		}
	}


	private void checkReificationParts(Resource reificationResource,
			SparqlifyDataset dataset) throws NotImplementedException {
		
		StmtIterator subjPartsIt = dataset.listStatements(reificationResource,
				RDF.subject, (RDFNode) null);
		checkSubjectPart(reificationResource, subjPartsIt, dataset);
		
		StmtIterator predPartsIt = dataset.listStatements(reificationResource,
				RDF.predicate, (RDFNode) null);
		checkPredicatePart(reificationResource, predPartsIt, dataset);
		
		StmtIterator objPartsIt = dataset.listStatements(reificationResource,
				RDF.object, (RDFNode) null);
		checkObjectPart(reificationResource, objPartsIt, dataset);
	}
	/**
	 * This method checks
	 * - if there is exactly one rdf:subject part of the reification statement
	 * - for all rdf:subject parts of the reification statement if the value
	 *   is a resource
	 */
	private void checkSubjectPart(Resource reificationResource,
			StmtIterator subjPartsIt, SparqlifyDataset dataset)
			throws NotImplementedException {
		
		List<Statement> subjParts = subjPartsIt.toList();
		
		if (subjParts.isEmpty()) {
			// no subject part given --> reification statement incomplete
			// --> violation
			
			// build dummy triple representing the missing reification part
			Triple triple = new Triple(reificationResource.asNode(),
					RDF.subject.asNode(), NodeFactory.createAnon());
			writeTripleMeasureToSink(missingReificationPartVal, triple, null);
			
		} else if (subjParts.size() == 1) {
			// expected case --> no violation; look if the value type is correct
			Statement subjPart = subjParts.get(0);
			RDFNode subjPartVal = subjPart.getObject();
			if (!subjPartVal.isResource()) {
				Set<ViewQuad<ViewDefinition>> viewQuads = pinpointer
						.getViewCandidates(subjPart.asTriple());
				writeTripleMeasureToSink(wrongValueTypeVal, subjPart.asTriple(),
						viewQuads);
			}
			
		} else {
			// multiple subject parts given --> violation
			
			// TODO: also check if the types of all statements are correct
			reportStatements(multipleReificationPartsVal, subjParts, dataset);
		}
		
	}


	/**
	 * This method checks
	 * - if there is exactly one rdf:predicate part of the reification statement
	 * - for all rdf:predicate parts of the reification statement if the value
	 *   is a URI resource
	 */
	private void checkPredicatePart(Resource reificationResource,
			StmtIterator predPartsIt, SparqlifyDataset dataset)
			throws NotImplementedException {
		
		List<Statement> predParts = predPartsIt.toList();
		
		if (predParts.isEmpty()) {
			// no predicate part given --> reification statement incomplete
			// --> violation
			
			// build dummy triple representing the missing reification part
			Triple triple = new Triple(reificationResource.asNode(),
					RDF.predicate.asNode(), NodeFactory.createAnon());
			writeTripleMeasureToSink(missingReificationPartVal, triple, null);
			
		} else if (predParts.size() == 1) {
			// expected case --> no violation; look if the value type is correct
			Statement predPart = predParts.get(0);
			RDFNode predPartVal = predPart.getObject();
			if (!predPartVal.isURIResource()) {
				Set<ViewQuad<ViewDefinition>> viewQuads = pinpointer
						.getViewCandidates(predPart.asTriple());
				writeTripleMeasureToSink(wrongValueTypeVal, predPart.asTriple(),
						viewQuads);
			}
			
		} else {
			// multiple predicate parts given --> violation
			
			// TODO: also check if the types of all statements are correct
			reportStatements(multipleReificationPartsVal, predParts, dataset);
		}
	}


	/**
	 * This method checks
	 * - if there is exactly one rdf:object part of the reification statement
	 */
	private void checkObjectPart(Resource reificationResource,
			StmtIterator objPartsIt, SparqlifyDataset dataset)
			throws NotImplementedException {

		List<Statement> objParts = objPartsIt.toList();
		
		if (objParts.isEmpty()) {
			// no object part given --> reification statement incomplete
			// --> violation
			
			// build dummy triple representing the missing reification part
			Triple triple = new Triple(reificationResource.asNode(),
					RDF.object.asNode(), NodeFactory.createAnon());
			writeTripleMeasureToSink(missingReificationPartVal, triple, null);
			
		} else if (objParts.size() > 1) {
			// multiple predicate parts given --> violation
			
			// TODO: also check if the types of all statements are correct
			reportStatements(multipleReificationPartsVal, objParts, dataset);
		}
	}


	private void reportStatements(float val, List<Statement> statements,
			SparqlifyDataset dataset) throws NotImplementedException {
		List<Pair<Triple, Set<ViewQuad<ViewDefinition>>>> res =
				new ArrayList<Pair<Triple,Set<ViewQuad<ViewDefinition>>>>();
		
		for (Statement statement : statements) {
			Set<ViewQuad<ViewDefinition>> viewQuads =
					pinpointer.getViewCandidates(statement.asTriple());
			res.add(new Pair<Triple, Set<ViewQuad<ViewDefinition>>>(
							statement.asTriple(), viewQuads));
		}
		
		writeTriplesMeasureToSink(val, res);
	}
}
