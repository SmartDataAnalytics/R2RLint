package org.aksw.sparqlify.qa.metrics.interpretability;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
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
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * This metric checks if containers are used according to the RDF container
 * syntax rules.
 * 
 * It is looked for statements like "sth rdf:_1 sthElse ." and checked, if
 * - there is a statement assigning a type to the container, i.e.
 *   "sth rdf:type rdf:Bag ." or "sth rdf:type rdf:Seq ." or
 *   "sth rdf:type rdf:Alt ."
 * - member is a resource
 * - there are duplicate membership properties like in
 *   "sth rdf:_23 sthCompletelyDifferent ." and "sth rdf:_23 againSthElse ."
 * - there is gap in the consecutive numbering between two members,
 *   i.e. the rdf:_n's are not numbered consecutively, e.g.
 *   "sth rdf:_23 sthCompletelyDifferent ." followed by
 *   "sth rdf:_25 againSthElse ."
 * - there is a statement starting with rdf:_0
 * - there are statements that use container membership properties with leading
 *   zeros, e.g. rdf:_023
 * 
 * So one assumption is, that at least the first container member has a valid
 * container membership property (i.e. rdf:_1) to have an entry point.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class CorrectContainerUse extends MetricImpl implements
		DatasetMetric {

	@Autowired
	private Pinpointer pinpointer;
	private float noTypeAssignedVal = (float) 0;
	private float multipleTypesAssignedVal = (float) 0;
	private float duplicateContMembPropsVal = (float) 0;
	private float noConsecutiveNumberingVal = (float) 0.5;
	private float leadingZeroVal = (float) 0;
	private float memberIsLiteralVal = 0;

	// setter mainly used for testing
	public void setNoTypeAssignedVal(float val) {
		noTypeAssignedVal = val;
	}
	public void setMultipleTypesAssignedVal(float val) {
		multipleTypesAssignedVal = val;
	}
	public void setDuplicateContMembPropsVal(float val) {
		duplicateContMembPropsVal = val;
	}
	public void setNoConsecutiveNumberingVal(float val) {
		noConsecutiveNumberingVal = val;
	}
	public void setLeadingZeroVal(float val) {
		leadingZeroVal = val;
	}
	public void setMemberIsLiteralVal(float val) {
		memberIsLiteralVal = val;
	}


	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {
		
		checkTypeConsNumberingUriAndDuplictates(dataset);
		ckeckLeadingZeros(dataset);
	}


	private void checkTypeConsNumberingUriAndDuplictates(SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {
		
		StmtIterator rdf_1StatementsIt =
				dataset.listStatements(null, RDF.li(1), (RDFNode) null);
		
		while (rdf_1StatementsIt.hasNext()) {
			Statement statement = rdf_1StatementsIt.next();
			Resource subject = statement.getSubject();
			StmtIterator succIt =
					dataset.listStatements(subject, RDF.li(2), (RDFNode) null);
			
			checkIfTyped(subject, dataset);
			checkConsecutiveNumberingAndUri(statement, 1, succIt, dataset);
		}
	}


	private void checkConsecutiveNumberingAndUri(Statement statement, int num,
			StmtIterator succIt, SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {

		Resource subject = statement.getSubject();
		Property predicate = statement.getPredicate();
		RDFNode object = statement.getObject();
		
		// check if object is resource
		if (!object.isResource()) {
			Set<ViewQuad<ViewDefinition>> viewQuads =
					pinpointer.getViewCandidates(statement.asTriple());
			writeTripleMeasureToSink(memberIsLiteralVal, statement.asTriple(), viewQuads);
		}
		
		// check if same member number exists more than once
		StmtIterator sameMemNumberStmntsIt =
				dataset.listStatements(subject, predicate, (RDFNode) null);
		List<Statement> sameMemNumberStmnts = sameMemNumberStmntsIt.toList();
		if (sameMemNumberStmnts.size() > 1) {
			// there is more than one statement with same subject and same
			// membership property
			reportStatements(duplicateContMembPropsVal, sameMemNumberStmnts, dataset);
		}
		
		// check consecutive numbering
		StmtIterator succSuccIt = dataset.listStatements(subject, RDF.li(num+2), (RDFNode) null);
		if (!succIt.hasNext()) {
			// there is a gap or we're done
			if (succSuccIt.hasNext()) {
				// there is a gap --> violation
				
				// create dummy triple representing the missing statement
				Triple dummy = new Triple(subject.asNode(),
						RDF.li(num + 1).asNode(), NodeFactory.createAnon());
				writeTripleMeasureToSink(noConsecutiveNumberingVal, dummy, null);
				
				// go on with successor of successor
				StmtIterator succSuccSuccIt =
						dataset.listStatements(subject, RDF.li(num+3), (RDFNode) null);
				checkConsecutiveNumberingAndUri(succSuccIt.next(), num+2, succSuccSuccIt, dataset);
			}
			
		} else // there is no gap and we can continue with the next member
			checkConsecutiveNumberingAndUri(succIt.next(), num+1, succSuccIt, dataset);
	}


	private void checkIfTyped(Resource subject, SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {
		
		// get all type statements for the considered resource ('subject')
		StmtIterator typeStmntsIt =
				dataset.listStatements(subject, RDF.type, (RDFNode) null);
		
		// init list that should hold all assigned types to later check if
		// it is empty or contains different entries (contradictory types)
		Set<Statement> assignmentStatements = new HashSet<Statement>();
		
		// get assigned types...
		while (typeStmntsIt.hasNext()) {
			Statement typeStmnt = typeStmntsIt.next();
			RDFNode object = typeStmnt.getObject();
			
			// ... of course we're just interested in container types
			if (object.isResource()) {
				if (object.asResource().equals(RDF.Bag)
						|| object.asResource().equals(RDF.Seq)
						|| object.asResource().equals(RDF.Alt))
					assignmentStatements.add(typeStmnt);
			}
		}
		
		if (assignmentStatements.isEmpty()) {
			// no type assigned --> violation
			
			// dummy triple representing the missing type statement
			Triple triple =  new Triple(subject.asNode(),
					RDF.type.asNode(), NodeFactory.createAnon());
			writeTripleMeasureToSink(noTypeAssignedVal, triple, null);
			
		} else if (assignmentStatements.size() > 1) {
			// multiple container types assigned --> violation
			ArrayList<Statement> statementsList =
					new ArrayList<Statement>(assignmentStatements);
			reportStatements(multipleTypesAssignedVal, statementsList, dataset);
		}
	}


	private void ckeckLeadingZeros(SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {
		
		String queryStr =
			"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
			"SELECT * {" +
				"?s ?p ?o . " +
				"FILTER(regex(str(?p), \"^http://www.w3.org/1999/02/22-rdf-syntax-ns#_0\"))" +
			"}";
		
		Query query = QueryFactory.create(queryStr);
		
		QueryExecution qe = QueryExecutionFactory.create(query, dataset);
		ResultSet res = qe.execSelect();
		
		List<Triple> leadingZeroTriples = new ArrayList<Triple>();
		while(res.hasNext())
		{
			QuerySolution solution = res.nextSolution();
			Resource subj = solution.getResource("s");
			Resource pred = solution.getResource("p");
			RDFNode obj = solution.get("o");
			Triple leadingzeroTriple = new Triple(subj.asNode(), pred.asNode(), obj.asNode());
			leadingZeroTriples.add(leadingzeroTriple);
		}
		qe.close(); 
		
		for (Triple triple : leadingZeroTriples) {
			Set<ViewQuad<ViewDefinition>> viewQuads = pinpointer.getViewCandidates(triple);
			writeTripleMeasureToSink(leadingZeroVal, triple, viewQuads);
		}
	}


	private void reportStatements(float val, List<Statement> statements,
			SparqlifyDataset dataset) throws NotImplementedException,
			SQLException {
		
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
