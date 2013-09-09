package org.aksw.sparqlify.qa.metrics.consistency;

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

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * This metric should find resources that are
 * - defined as a property but also appear on subject or object positions in
 *   other triples (except cases like ex:prop rdf:type rdfs:Property, ex:prop
 *   rds:subPropetyOf)
 * - defined as a class but also appear on predicate position in other triples
 * 
 * To find such properties for every triple predicate it is checked if it also
 * appears in subject position in connection with a not explicitly allowed
 * property. Such allowed properties can be found in the
 * propertyOnSubjPosWhitelist class attribute. Another whitelist check is
 * performed if the considered property appears on object position of other
 * triples (see propertiesOnObjPosWhitelist).
 * So this explicit whitelist approach does not cover properties that would
 * also be allowed and could be found through inference.
 * 
 * For all classes it is checked if any of them appears in predicate position:
 * If so an error is reported.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
public class WellPlacedClassesAndProperties extends PinpointMetric implements
		DatasetMetric {

	private final List<String> propertyOnSubjPosWhitelist = new ArrayList<String>(
			Arrays.asList(
					"http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
					"http://www.w3.org/2000/01/rdf-schema#subPropertyOf",
					"http://www.w3.org/2002/07/owl#equivalentProperty",
					"http://www.w3.org/2002/07/owl#inverseOf",
					"http://www.w3.org/2000/01/rdf-schema#domain",
					"http://www.w3.org/2000/01/rdf-schema#range"));
	
	private final List<String> propertyOnObjPosWhitelist = new ArrayList<String>(
			Arrays.asList(
					"http://www.w3.org/2000/01/rdf-schema#subPropertyOf",
					"http://www.w3.org/2002/07/owl#equivalentProperty",
					"http://www.w3.org/2002/07/owl#inverseOf",
					"http://www.w3.org/2002/07/owl#onProperty"));

	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException {

		/*
		 * checks for wrong-placed properties
		 */
		OntModel ontModel = ModelFactory.createOntologyModel(
				OntModelSpec.OWL_LITE_MEM_RULES_INF, dataset);
		
		// get all properties
		ExtendedIterator<OntProperty> propIt = ontModel.listOntProperties();
		
		// loop over them
		while (propIt.hasNext()) {
			OntProperty prop = propIt.next();
			
			// property is on subject position...
			StmtIterator propInSubjPosIt = dataset.listStatements(
					prop.asResource(), (Property) null, (RDFNode) null);
			while (propInSubjPosIt.hasNext()) {
				Statement statement = propInSubjPosIt.next();
				
				Property statementPred = statement.getPredicate();
				if (!propertyOnSubjPosWhitelist.contains(statementPred.getURI())) {
					// ...and used in connection with a predicate that is not
					// whitelisted explicitly
					
					Set<ViewQuad<ViewDefinition>> viewQuads =
							pinpointer.getViewCandidates(statement.asTriple());
					
					writeTripleMeasureToSink(0, statement.asTriple(), viewQuads);
				}
			}
			
			// property is on object position...
			StmtIterator propInObjPosIt = dataset.listStatements(
					(Resource) null, (Property) null, prop);
			
			while (propInObjPosIt.hasNext()) {
				Statement statement = propInObjPosIt.next();
				
				Property statementPred = statement.getPredicate();
				if (!propertyOnObjPosWhitelist.contains(statementPred.getURI())) {
					// ...and used in connection with a predicate that is not
					// whitelisted explicitly
					
					Set<ViewQuad<ViewDefinition>> viewQuads =
							pinpointer.getViewCandidates(statement.asTriple());
					
					writeTripleMeasureToSink(0, statement.asTriple(), viewQuads);
				}
			}
		}
		
		/*
		 * checks for wrong-placed classes
		 */
		
		// get classes
		ExtendedIterator<OntClass> classIt = ontModel.listClasses();
		
		// loop over them
		while (classIt.hasNext()) {
			OntClass cls = classIt.next();
			
			Property clsAsProperty = ResourceFactory.createProperty(cls.getURI());
			// statements where class appears in predicate position
			StmtIterator statementsIt = dataset.listStatements(null,
					clsAsProperty, (RDFNode) null);
			
			// loop over them and report
			while (statementsIt.hasNext()) {
				Statement statement = statementsIt.next();
				
				Set<ViewQuad<ViewDefinition>> viewQuads =
						pinpointer.getViewCandidates(statement.asTriple());
				
				writeTripleMeasureToSink(0, statement.asTriple(), viewQuads);
			}
		}
		
	}

}
