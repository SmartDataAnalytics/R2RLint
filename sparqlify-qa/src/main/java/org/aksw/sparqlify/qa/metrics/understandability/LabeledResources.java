package org.aksw.sparqlify.qa.metrics.understandability;

import java.util.ArrayList;
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
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * This metric should find individuals, classes and properties that are not
 * labeled and report them.
 * 
 * For performance reasons this metric currently reports only the first
 * occurrence of a not labeled resource. First this avoids additional scans on
 * the dataset (to find all occurrences) and also prevents reporting the same
 * resource appearing on different positions.
 * The drawback of this approach is, that not all quad candidates (in the view
 * definitions) are found but only these that match the considered triple.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
public class LabeledResources extends PinpointMetric implements DatasetMetric {
	
	List<Resource> seenResources;
	List<Property> seenProperties;


	public LabeledResources() {
		super();
		seenResources = new ArrayList<Resource>();
		seenProperties = new ArrayList<Property>();
	}


	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException {

		StmtIterator statementsIt = dataset.listStatements();
		
		while (statementsIt.hasNext()) {
			Statement statement = statementsIt.next();
			
			Resource subject = statement.getSubject();
			Property predicate = statement.getPredicate();
			RDFNode object = statement.getObject();
			
			/* subject */
			
			// seenProperties would have to be checked here as well but this
			// would require extra exception handling in case the URI of the
			// given resource look to 'un-propertily' for Jena to build a
			// property based on it
			if (!seenResources.contains(subject)) {
				
				// subject is a local URI resource 
				if ((subject.isURIResource() && subject.getURI().startsWith(dataset.getPrefix())) 
						// there is no statement <subj> rdfs:label "sth"
						&& !dataset.listStatements(subject, RDFS.label, (RDFNode) null).hasNext()) {
					
					Set<ViewQuad<ViewDefinition>> viewQuads = pinpointer
							.getViewCandidates(statement.asTriple());
					writeNodeTripleMeasureToSink(0, TriplePosition.SUBJECT,
							statement.asTriple(), viewQuads);
				}
				seenResources.add(subject);
			}
			
			/* predicate */
			
			if (!seenProperties.contains(predicate)) {
				// predicate is a local URI resource
				if(predicate.getURI().startsWith(dataset.getPrefix())
						// there is no statement <pred> rdfs:label "sth"
						&& !dataset.listStatements(predicate.asResource(), RDFS.label, (RDFNode) null).hasNext()) {
					
					Set<ViewQuad<ViewDefinition>> viewQuads = pinpointer
							.getViewCandidates(statement.asTriple());
					writeNodeTripleMeasureToSink(0, TriplePosition.PREDICATE,
							statement.asTriple(), viewQuads);
				}
				seenProperties.add(predicate);
			}
			
			
			/* object */
			
			// object is a URI resource, not seen yet...
			if (object.isURIResource() &&!seenResources.contains(object.asResource())) {
				
				// object is a local resource...
				if(object.asResource().getURI().startsWith(dataset.getPrefix())
					// ...and there is no statement <obj> rdfs:label "sth"
					&& !dataset.listStatements(object.asResource(), RDFS.label, (RDFNode) null).hasNext()) {
					
					Set<ViewQuad<ViewDefinition>> viewQuads = pinpointer
							.getViewCandidates(statement.asTriple());
				
					writeNodeTripleMeasureToSink(0, TriplePosition.OBJECT,
							statement.asTriple(), viewQuads);
				}
				seenResources.add(object.asResource());
			}
		}
		
	}
}
