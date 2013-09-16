package org.aksw.sparqlify.qa.metrics.interpretability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.PinpointMetric;
import org.aksw.sparqlify.qa.sinks.TriplePosition;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ResourceInterpretability extends PinpointMetric implements
		DatasetMetric {

	List<Resource> seenResources;
	List<Property> ontProperties = new ArrayList<Property>(Arrays.asList(
			// rdf(s)
			RDF.type, RDFS.subClassOf, RDFS.subPropertyOf, RDFS.domain,
			RDFS.range,
			// owl
			OWL.complementOf, OWL.disjointWith, OWL.equivalentClass,
			OWL.equivalentProperty, OWL.intersectionOf, OWL.inverseOf,
			OWL.oneOf, OWL.unionOf
			));


	public ResourceInterpretability() {
		super();
		seenResources = new ArrayList<Resource>();
	}

	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException {

		StmtIterator statementsIt = dataset.listStatements();
		
		while (statementsIt.hasNext()) {
			Statement statement = statementsIt.next();
			
			Resource subject = statement.getSubject();
			Resource predicate = statement.getPredicate().asResource();
			RDFNode object = statement.getObject();
			
			/* subject */
			if (!seenResources.contains(subject) && subject.isURIResource()
					&& subject.getURI().startsWith(dataset.getPrefix())) {
				
				checkResourceInterpretability(subject,
						TriplePosition.SUBJECT, statement, dataset);
				seenResources.add(subject);
			}
			/* predicate */
			if (!seenResources.contains(predicate)
					&& predicate.getURI().startsWith(dataset.getPrefix())) {
				
				checkResourceInterpretability(predicate,
						TriplePosition.PREDICATE, statement, dataset);
				seenResources.add(predicate);
			}
			
			/* object */
			if (object.isURIResource() && !seenResources.contains(object.asResource())
					&& object.asResource().getURI().startsWith(dataset.getPrefix())) {
				
				checkResourceInterpretability(object.asResource(),
						TriplePosition.OBJECT, statement, dataset);
				seenResources.add(object.asResource());
			}
		}
	}


	private void checkResourceInterpretability(Resource resource,
			TriplePosition pos, Statement statement, SparqlifyDataset dataset)
			throws NotImplementedException {
		
		boolean ontPropStatementFound = false;
		
		// check if resource is further described using one of the proposed
		// properties
		
		for (Property ontProp : ontProperties) {
			if (dataset.listStatements(resource, ontProp, (RDFNode) null).hasNext()) {
				ontPropStatementFound = true;
				break;
			}
		}
		
		if (!ontPropStatementFound) {
			Set<ViewQuad<ViewDefinition>> viewQuads = pinpointer
					.getViewCandidates(statement.asTriple());
			
			writeNodeTripleMeasureToSink(0, pos, statement.asTriple(), viewQuads);
		}
	}
}
