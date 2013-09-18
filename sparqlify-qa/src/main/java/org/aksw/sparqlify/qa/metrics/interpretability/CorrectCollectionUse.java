package org.aksw.sparqlify.qa.metrics.interpretability;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.Pair;
import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.PinpointMetric;
import org.aksw.sparqlify.qa.sinks.TriplePosition;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * This metric checks if there are any collections created and if they are
 * valid as far as the corresponding syntax rules are concerned.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
public class CorrectCollectionUse extends PinpointMetric implements
		DatasetMetric {
	
	private float listNodeHasNoRdfFirstVal = 0;
	private float listNodeHasMultipleRdfFirstStmntsVal = 0;
	private float rdfRestIsLiteralVal = 0;
	private float multiplePredecessorsVal = 0;
	private float multipleSuccessorsVal = 0;
	private float listEndedWithoutRdfNilVal = 0;
	private float nilHasSuccessorVal = 0;
	private List<Resource> seenListNodes;


	// setter mainly used for testing purposes
	public void setListNodeHasNoRdfFirstVal(float val) {
		listNodeHasNoRdfFirstVal = val;
	}
	public void setListNodeHasMultipleRdfFirstStmntsVal(float val) {
		listNodeHasMultipleRdfFirstStmntsVal = val;
	}
	public void setRdfRestIsLiteralVal(float val) {
		rdfRestIsLiteralVal = val;
	}
	public void setMultiplePredecessorsVal(float val) {
		multiplePredecessorsVal = val;
	}
	public void setMultipleSuccessorsVal(float val) {
		multipleSuccessorsVal = val;
	}
	public void setListEndedWithoutRdfNilVal(float val) {
		listEndedWithoutRdfNilVal = val;
	}
	public void setNilHasSuccessorVal (float val) {
		nilHasSuccessorVal = val;
	}
	
	
	public CorrectCollectionUse()  {
		seenListNodes = new ArrayList<Resource>();
	}

	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException {
		System.out.println("starting (" + name + ")");
		/*
		 * entry point: find statements like <sth> rdf:rest <sthElse> .
		 * since this statement does not necessarily have to be the head of the
		 * list the further search has to be performed in both directions:
		 * towards the head and towards the end of the list
		 */
		StmtIterator listStatementsIt = dataset.listStatements(null, RDF.rest,
				(RDFNode) null);
		while(listStatementsIt.hasNext()) {
			Statement statement = listStatementsIt.next();
			
			searchHeadwards(statement, dataset);
			searchTailwards(statement, dataset);
		}
		
		System.out.println(seenListNodes);
	}


	/**
	 * This method is intended to check if nodes of a list are valid as far as
	 * the RDF collection syntax is concerned. This checking is done iteratively
	 * stepping towards the head of the given list, only regarding one
	 * "subject rdf:rest object ." statement at a time. Doing so, only the
	 * subject is of interest, since it is assumed, that the object (which was
	 * the subject in the former step) is already checked.
	 * 
	 * What it actually does is
	 * - checking if the given subject is rdf:nil (this would be a violation
	 *   since rdf:nil is always the last entry of a collection)
	 * - calling a check for valid rdf:first use
	 * - calling a check for multiple successors
	 * - performing one step towards the list head
	 * 
	 * @param statement
	 *         A Statement expressing the currently considered part of the
	 *         collection ("pred rdf:rest succ .")
	 * @param dataset
	 *         The SparqlifyDataset under assessment
	 * @throws NotImplementedException
	 */
	private void searchHeadwards(Statement statement, SparqlifyDataset dataset)
			throws NotImplementedException {

		Resource subject = statement.getSubject();
		
		if (subject.equals(RDF.nil)) {
			// rdf:nil has a successor (== the object of 'statement')
			// --> report error
			Set<ViewQuad<ViewDefinition>> viewQuads =
					pinpointer.getViewCandidates(statement.asTriple());
			writeTripleMeasureToSink(nilHasSuccessorVal, statement.asTriple(),
					viewQuads);
		}
		
		if (!seenListNodes.contains(subject)) {
			seenListNodes.add(subject);
			
			// check if there is no or more than one
			// "subject rdf:first sth ." triple
			checkRdfFirst(subject, dataset);
			// check if there is more than one "subject rdf:rest sth ." triple
			checkForMultipleSuccessors(subject, dataset);
			// go on headwards
			goHeadwards(subject, dataset);
		}
	}


	/**
	 * This methods checks if there is no or if there are multiple statements
	 * like "listNode rdf:first sth ." in the dataset.
	 * 
	 * @param listNode
	 *         The subject of a "subject rdf:rest sth ." triple that will be
	 *         checked.
	 * @param dataset
	 *         The Sparqlify dataset under assessment
	 * @throws NotImplementedException
	 */
	private void checkRdfFirst(Resource listNode,
			SparqlifyDataset dataset) throws NotImplementedException {
		
		StmtIterator nodeRdfFirstStatetementsIt = dataset.listStatements(
				listNode, RDF.first, (RDFNode) null);
		List<Statement> nodeRdfFirstStatements = nodeRdfFirstStatetementsIt.toList();
		
		if (nodeRdfFirstStatements.isEmpty()) {
			// node has no rdf:first statement which is a violation
			
			// dummy triple that represents the missing statement
			Triple triple = new Triple(listNode.asNode(),
					RDF.first.asResource().asNode(), NodeFactory.createAnon());
			
			writeNodeTripleMeasureToSink(listNodeHasNoRdfFirstVal,
					TriplePosition.SUBJECT, triple, null);
			
		} else if (nodeRdfFirstStatements.size() > 1) {
			// there are multiple rdf:first statements which is a violation
			reportStatements(nodeRdfFirstStatements, listNodeHasMultipleRdfFirstStmntsVal);
		}
		
	}
	
	
	private void checkForMultipleSuccessors(Resource listNode,
			SparqlifyDataset dataset) throws NotImplementedException {
		
		StmtIterator nodeRdfRestStatementsIt =
				dataset.listStatements(listNode, RDF.rest, (RDFNode) null);
		List<Statement> nodeRdfRestStatements = nodeRdfRestStatementsIt.toList();
		
		if (nodeRdfRestStatements.size() > 1) {
			// there is more than one statement "subject rdf:rest sth ."
			// which is a violation
			reportStatements(nodeRdfRestStatements, multipleSuccessorsVal);
		}
		
	}
	
	
	/**
	 * This method takes a node of a collection and tries to find its
	 * predecessor. When doing so it also checks if
	 * - there is no predecessor: then we're at the list head (nothing left to
	 *   do)
	 * - there is exactly one predecessor: expected case; call searchHeadwards()
	 *   on the predecessor statement
	 * - there are multiple predecessors: this is a violation and will be
	 *   reported; afterwards searchHeadwards() is called for all predecessors
	 * 
	 * @param subject
	 * @param dataset
	 * @throws NotImplementedException
	 */
	private void goHeadwards(Resource subject, SparqlifyDataset dataset)
			throws NotImplementedException {
		System.out.println("went headwards");
		/*
		 * make one step headwards (== find triple like "sth rdf:rest subject")
		 * and check how many predecessor triples were found
		 */
		StmtIterator predStatementsIt =
				dataset.listStatements(null, RDF.rest, subject);
		List<Statement> predStatements = predStatementsIt.toList();
		
		if (predStatements.isEmpty()) {
			// 0: we're at the list head --> nothing to do
			
		} else if (predStatements.size() == 1) {
			// 1: normal case --> go ahead (== headwards)
			searchHeadwards(predStatements.get(0), dataset);
		
		} else {
			// >1: there is more than one predecessor --> report error and go
			// ahead
			reportStatements(predStatements, multiplePredecessorsVal);
			
			for (Statement statement : predStatements) {
				searchHeadwards(statement, dataset);
			}
		}
		
	}


	/**
	 * This method is intended to check if nodes of a list are valid as far as
	 * the RDF collection syntax is concerned. This checking is done iteratively
	 * stepping towards the end of the list, only regarding one
	 * "subject rdf:rest object ." statement at a time. Doing so, only the
	 * object is of interest, since it is assumed, that the subject (which was
	 * the object in the former step) is already checked.
	 * 
	 * What it actually does is
	 * - checking if the given object is a literal (this would be a violation
	 *   and will be reported)
	 * - checking if the object was already processed (nothing left to do in
	 *   this case)
	 * - checking if the object is rdf:nil, which means, we're at the end of
	 *   the list (nothing left to do)
	 * - performing one step towards the end of the list if object not rdf:nil
	 *   and not already processed
	 * 
	 * @param statement
	 * @param dataset
	 * @throws NotImplementedException
	 */
	private void searchTailwards(Statement statement, SparqlifyDataset dataset)
			throws NotImplementedException {
		
		RDFNode object = statement.getObject();
		
		// check the object node
		if(!object.isResource()) {
			// object node is a literal which is a violation --> report triple

			Triple triple = statement.asTriple();
			Set<ViewQuad<ViewDefinition>> viewQuads =
					pinpointer.getViewCandidates(triple);
			writeTripleMeasureToSink(rdfRestIsLiteralVal, triple, viewQuads);
		
		} else {
			// object node is resource (== blank node or URI node) as expected
			
			// do further checks if object was not already processed and
			// we're not at the end of the list
			if (!seenListNodes.contains(object.asResource()) 
					&& !object.asResource().equals(RDF.nil)) {
				seenListNodes.add(object.asResource());
				
				checkForMultiplePredecessors(object.asResource(), dataset);
				
				goTailwards(object.asResource(), dataset);
			}
		}
	}


	private void checkForMultiplePredecessors(Resource listNode,
			SparqlifyDataset dataset) throws NotImplementedException {
		
		// check if listNode is used as rdf:rest more than once
		StmtIterator rdfRestNodeStmntsIt =
				dataset.listStatements(null, RDF.rest, listNode);
		List<Statement> rdfRestNodeStmnts = rdfRestNodeStmntsIt.toList();
		
		if (rdfRestNodeStmnts.size() > 1) {
			// used more than once --> multiple predecessors
			reportStatements(rdfRestNodeStmnts, multiplePredecessorsVal);
		}
	}


	/**
	 * This method takes a node of a collection and tries to find its
	 * successors. When doing so it also checks if
	 * - there is no successor: then we're at the end of the list; but since we
	 *   checked if the object of the predecessor statement was rdf:nil one
	 *   step before (and would have stopped if so) there should be a successor;
	 *   so if there is no successor this is a violation since the collection
	 *   ended abruptly, which will be reported
	 * - there is exactly one successor as expected:
	 *   - check if the rdf:first property of the successor is correct (call
	 *     checkRdfFirst() )
	 *   - call searchTailwards() on the successor statement
	 * - there is more than one successor: this is a violation and will be
	 *   reported
	 * 
	 * @param resource
	 * @param dataset
	 * @throws NotImplementedException
	 */
	private void goTailwards(Resource resource, SparqlifyDataset dataset)
			throws NotImplementedException {
		System.out.println("went tailwards");
		/*
		 * make one step tailwards (== find triple like "resource rdf:rest sth")
		 * and check how many successor triples were found
		 */
		StmtIterator succStatementsIt =
				dataset.listStatements(resource, RDF.rest, (RDFNode) null);
		List<Statement> succStatements = succStatementsIt.toList();
		
		if (succStatements.isEmpty()) {
			// 0: list ended abruptly (without rdf:nil) --> report error
			
			// build dummy triple that represents the missing statement
			Triple triple = new Triple(resource.asNode(), RDF.rest.asNode(), RDF.nil.asNode());
			writeTripleMeasureToSink(listEndedWithoutRdfNilVal, triple, null);
		
		} else if (succStatements.size() == 1) {
			// 1: there is just on successor (as expected) --> go on tailwards
			Statement succStatement = succStatements.get(0);
			checkRdfFirst(succStatement.getSubject(), dataset);
			searchTailwards(succStatement, dataset);
		
		} else {
			// >1: there are multiple successors which is a violation --> report
			// violation and go ahead
			reportStatements(succStatements, multipleSuccessorsVal);
			
			for (Statement statement : succStatements) {
				checkRdfFirst(statement.getSubject(), dataset);
				searchTailwards(statement, dataset);
			}
		}
	}


	private void reportStatements(List<Statement> statements, float value)
			throws NotImplementedException {
		
		List<Pair<Triple, Set<ViewQuad<ViewDefinition>>>> pinpointResults =
				new ArrayList<Pair<Triple,Set<ViewQuad<ViewDefinition>>>>();
		
		for (Statement statement : statements) {
			
			Set<ViewQuad<ViewDefinition>> quadViewDefs =
					pinpointer.getViewCandidates(statement.asTriple());
			
			pinpointResults.add(
					new Pair<Triple, Set<ViewQuad<ViewDefinition>>>(
							statement.asTriple(), quadViewDefs));
		}
		
		writeTriplesMeasureToSink(value, pinpointResults);
	}
}
