package org.aksw.sparqlify.qa.metrics.consistency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.PinpointMetric;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * This metric should find all resources that do not have a class assigned.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
public class TypedResources extends PinpointMetric implements DatasetMetric {

	private Set<Resource> knownResources;
	
	private List<Resource> types = new ArrayList<Resource>(Arrays.asList(
			RDF.Property, RDF.Statement, RDF.Alt, RDF.Bag, RDF.Seq, RDF.List,
			RDFS.Resource, RDFS.Class, RDFS.Container,
			RDFS.ContainerMembershipProperty, OWL.AnnotationProperty, OWL.Class,
			OWL.DatatypeProperty, OWL.DeprecatedClass, OWL.DeprecatedProperty,
			OWL.FunctionalProperty, OWL.FunctionalProperty, OWL.ObjectProperty,
			OWL.Ontology, OWL.OntologyProperty, OWL.Restriction,
			OWL.SymmetricProperty, OWL.TransitiveProperty));


	public TypedResources() {
		super();
		knownResources = new HashSet<Resource>();
	}

	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException {

		StmtIterator statementsIt = dataset.listStatements();
		
		while (statementsIt.hasNext()) {
			Statement statement = statementsIt.next();
			
			Resource subject = statement.getSubject();
			RDFNode object = statement.getObject();
			
			if (!knownResources.contains(subject)) {
				checkIfTyped(subject, dataset);
			}
			
			if (object.isResource() && !knownResources.contains(object.asResource())) {
				checkIfTyped(object.asResource(), dataset);
			}
				
		}
	}
	
	private void checkIfTyped(Resource res, SparqlifyDataset dataset) {
		boolean resourceTyped = false;
		
		knownResources.add(res);
		
		// res is a type itself
		if (types.contains(res)) {
			resourceTyped = true;
		}
		
		
		if (!resourceTyped) {
			// <res> rdf:type <sth>
			StmtIterator typeStatementsIt = dataset.listStatements(
					res, RDF.type, (RDFNode) null);
			if (typeStatementsIt.hasNext()) {
				resourceTyped = true;
			}
		}
		
		if (!resourceTyped) {
			// <res> rdfs:subClassOf <sth>
			StmtIterator subClassStatementsIt = dataset.listStatements(res,
					RDFS.subClassOf, (RDFNode) null);
			if (subClassStatementsIt.hasNext()) {
				resourceTyped = true;
			}
		}
		
		if (!resourceTyped) {
			// <res> rdfs:subPropertyOf <sth>
			StmtIterator subPropStatementsIt = dataset.listStatements(res,
					RDFS.subPropertyOf, (RDFNode) null);
			if (subPropStatementsIt.hasNext()) {
				resourceTyped = true;
			}
		}
		
		if (!resourceTyped) {
			// <res> owl:equivalentClass <sth>
			StmtIterator equClassStatementsIt = dataset.listStatements(res,
					OWL.equivalentClass, (RDFNode) null);
			if (equClassStatementsIt.hasNext()) {
				resourceTyped = true;
			}
		}
		
		if (!resourceTyped) {
			// <res> owl:equivalentProperty <sth>
			StmtIterator equPropStatementsIt = dataset.listStatements(res,
					OWL.equivalentProperty, (RDFNode) null);
			if (equPropStatementsIt.hasNext()) {
				resourceTyped = true;
			}
		}

		if (!resourceTyped) {
			writeNodeMeasureToSink(0, res.asNode());
		}
	}

}
