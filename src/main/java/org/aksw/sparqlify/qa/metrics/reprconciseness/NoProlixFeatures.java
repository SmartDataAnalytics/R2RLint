package org.aksw.sparqlify.qa.metrics.reprconciseness;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.aksw.sparqlify.qa.metrics.TripleMetric;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * This metric should find statements using RDF features that are more or less
 * deprecated. These are reification, containers and collections:
 * 
 * - RDF reification:
 *   - predicates:
 *     - rdf:subject
 *     - rdf:predicate
 *     - rdf:object
 *   - classes:
 *     - rdf:Statement
 * 
 * - RDF containers:
 *   - predicates:
 *     - rdf:_n (n in natural numbers)
 *     - rdfs:member
 *   - classes:
 *     - rdf:Alt
 *     - rdf:Bag
 *     - rdf:Seq
 *     - rdfs:Container
 * 
 * - RDF collections:
 *   - predicates:
 *     - rdf:first
 *     - rdf:rest
 *   - classes
 *     - rdf:List
 * 
 * TODO: to also consider sub classes of the predicates and all
 * ContainerMembershipProperties (rdf:_2, rdf:_3, ...) this would have to be a
 * dataset measure 
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class NoProlixFeatures extends MetricImpl implements TripleMetric {

	@Autowired
	private Pinpointer pinpointer;

	List<Node> predicates = new ArrayList<Node>(Arrays.asList(
			RDF.subject.asNode(), RDF.predicate.asNode(), RDF.object.asNode(),
			RDF.li(1).asNode(), RDFS.member.asNode(), RDF.first.asNode(),
			RDF.rest.asNode()));

	List<Node> classes = new ArrayList<Node>(Arrays.asList(
			RDF.Statement.asNode(), RDF.Alt.asNode(), RDF.Bag.asNode(),
			RDF.Seq.asNode(), RDFS.Container.asNode(), RDF.List.asNode()));


	@Override
	public void assessTriple(Triple triple) throws NotImplementedException,
			SQLException {
		findPredicates(triple);
		findClasses(triple);
		
	}


	private void findPredicates(Triple triple) throws NotImplementedException,
			SQLException {
		for (Node predicate : predicates) {
			if (triple.getPredicate().equals((Node_URI) predicate)) {
				
				Set<ViewQuad<ViewDefinition>> viewQuads =
						pinpointer.getViewCandidates(triple);
				
				writeTripleMeasureToSink(0, triple, viewQuads);
			}
		}
		
	}


	private void findClasses(Triple triple) throws NotImplementedException,
			SQLException {
		
		for (Node cls : classes) {
			if (triple.getObject().equals(cls)
					&& triple.getPredicate().equals(RDF.type.asNode())) {
				
				Set<ViewQuad<ViewDefinition>> viewQuads =
						pinpointer.getViewCandidates(triple);
				
				writeTripleMeasureToSink(0, triple, viewQuads);
			}
		}
	}
}
