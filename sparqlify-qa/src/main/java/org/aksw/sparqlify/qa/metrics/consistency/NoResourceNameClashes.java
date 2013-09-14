package org.aksw.sparqlify.qa.metrics.consistency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.PinpointMetric;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.InvalidPropertyURIException;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * This metric reports resource name clashes. These clashes can be:
 * 1) a class name used as
 *    - a property
 *    - subject of a datatype/object property
 *    - object of a datatype/object property
 * 2) a property name used as
 *    - subject of a datatype/object property
 *    - object of a datatype/object property
 * 3) a class instance's name used as
 *    - a property
 *    - subject of certain ontology properties (rdfs:subClassOf, ...)
 *    - object of an ontology property ( rdf:type ex:instance23)
 * 
 * 1) is referred to as class name clashes.
 * 2) is referred to as property name clashes.
 * 3) is referred to as instance name clashes.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
public class NoResourceNameClashes extends PinpointMetric implements
		DatasetMetric {
	private final String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private final String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
	private final String owl = "http://www.w3.org/2002/07/owl#";
	
	// classes
	private final Resource rdfs_Property =
			ResourceFactory.createResource("http://www.w3.org/2000/01/rdf-schema#Property");
	
	// rdf properties
	private final Property rdf_type = ResourceFactory.createProperty(rdf + "type");
	private final Property rdf_first = ResourceFactory.createProperty(rdf + "first");
	private final Property rdf_rest = ResourceFactory.createProperty(rdf + "rest");
	private final Property rdf_subject =
			ResourceFactory.createProperty(rdf + "subject");
	private final Property rdf_predicate =
			ResourceFactory.createProperty(rdf + "predicate");
	private final Property rdf_object =
			ResourceFactory.createProperty(rdf + "object");
	// rdfs properties
	private final Property rdfs_subPropertyOf =
			ResourceFactory.createProperty(rdfs + "subPropertyOf");
	private final Property rdfs_domain =
			ResourceFactory.createProperty(rdfs + "domain");
	private final Property rdfs_range =
			ResourceFactory.createProperty(rdfs + "range");
	private final Property rdfs_subClassOf =
			ResourceFactory.createProperty(rdfs + "subClassOf");
	// owl properties
	private final Property owl_equivalentProperty =
			ResourceFactory.createProperty(owl + "equivalentProperty");
	private final Property owl_inverseOf =
			ResourceFactory.createProperty(owl + "inverseOf");
	private final Property owl_disjointWith =
			ResourceFactory.createProperty(owl + "disjointWith");
	private final Property owl_equivalentClass =
			ResourceFactory.createProperty(owl + "equivalentClass");
	private final Property owl_intersectionOf =
			ResourceFactory.createProperty(owl + "intersectionOf");
	private final Property owl_maxCardinality =
			ResourceFactory.createProperty(owl + "maxCardinality");
	private final Property owl_minCardinality =
			ResourceFactory.createProperty(owl + "minCardinality");
	private final Property owl_cardinality =
			ResourceFactory.createProperty(owl + "cardinality");
	private final Property owl_oneOf = ResourceFactory.createProperty(owl + "oneOf");
	private final Property owl_onProperty =
			ResourceFactory.createProperty(owl + "onProperty");
	private final Property owl_someValuesFrom =
			ResourceFactory.createProperty(owl + "someValuesFrom");
	private final Property owl_unionOf =
			ResourceFactory.createProperty(owl + "unionOf");
	
	// whitelist for statements like <individual> <predicate> <given class> .
	private final List<Property> indivPredicateClassWhitelist =
			new ArrayList<Property>(Arrays.asList(rdf_type));
	
	// whitelist for statements like <given property> <predicate> <object> .
	private final List<Property> propPredicateObjWhitelist =
			new ArrayList<Property>(Arrays.asList(rdfs_subPropertyOf,
					rdfs_domain, rdfs_range, owl_equivalentProperty,
					owl_inverseOf, rdf_type));
	
	// whitelist for statements like <subject> <predicate> <given property> .
	private final List<Property> subjPredicatePropWhitelist =
			new ArrayList<Property>(Arrays.asList(rdfs_subPropertyOf,
					owl_equivalentProperty, owl_inverseOf));
	
	// blacklist for properties that cannot be used together with an individual
	// as subject
	private final List<Property> indivPredicateObjBlacklist =
			new ArrayList<Property>(Arrays.asList(rdf_first, rdf_rest,
					rdf_subject, rdf_predicate, rdf_object, rdfs_subClassOf,
					rdfs_subPropertyOf, rdfs_domain, rdfs_range,
					owl_equivalentProperty, owl_inverseOf, owl_disjointWith,
					owl_equivalentClass, owl_intersectionOf, owl_maxCardinality,
					owl_minCardinality, owl_cardinality, owl_oneOf,
					owl_onProperty, owl_someValuesFrom, owl_unionOf));
	
	// blacklist for properties that cannot be used together with an individual
	// as object
	private final List<Property> subjPredicateIndivBlacklist =
			new ArrayList<Property>(Arrays.asList(rdf_type, rdf_first, rdf_rest,
					rdf_subject, rdf_predicate, rdf_object, rdfs_subClassOf,
					rdfs_subPropertyOf, rdfs_domain, rdfs_range,
					owl_equivalentProperty, owl_inverseOf, owl_disjointWith,
					owl_equivalentClass, owl_intersectionOf, owl_maxCardinality,
					owl_minCardinality, owl_cardinality, owl_oneOf,
					owl_onProperty, owl_someValuesFrom, owl_unionOf));
	
	Set<Set<ViewQuad<ViewDefinition>>> candidatesResults;
	
	private Set<Resource> classes;
	private Set<Property> properties;
	private Set<Resource> individuals;
	// helper sets
	private Set<Property> datatypeProperties;
	private Set<Property> objectProperties;


	public NoResourceNameClashes() {
		super();
		candidatesResults = new HashSet<Set<ViewQuad<ViewDefinition>>>();
		classes = new HashSet<Resource>();
		properties = new HashSet<Property>();
		individuals = new HashSet<Resource>();
		datatypeProperties = new HashSet<Property>();
		objectProperties = new HashSet<Property>();
	}


	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException {
		
		OntModel ontModel = ModelFactory.createOntologyModel(
				OntModelSpec.RDFS_MEM_TRANS_INF, dataset);

		initClasses(ontModel);
		initProperties(ontModel);
		initIndividuals(ontModel);
		initHelperSets(dataset);

		StmtIterator statementsIt = dataset.listStatements();
		
		while (statementsIt.hasNext()) {
			Statement statement = statementsIt.next();
			lookForClassNameClashes(statement);
			lookForPropertyNameClashes(statement);
			lookForInstanceNameClashes(statement);
		}
		
		for (Set<ViewQuad<ViewDefinition>> candidates : candidatesResults) {
			writeMappingMeasureToSink(0, candidates);
		}
	}


	private void initClasses(OntModel ontModel) {
		ExtendedIterator<OntClass> classesIt = ontModel.listClasses();
		while (classesIt.hasNext()) classes.add(classesIt.next().asResource());
	}
	
	private void lookForClassNameClashes(Statement statement) {
		boolean violationFound = false;
		
		Property predicate = statement.getPredicate();
		Resource subject = statement.getSubject();
		RDFNode object = statement.getObject();
		
		// class names used as property
		if (classes.contains(predicate.asResource())) {
			violationFound = true;
		}
		
		// class names used as subject of a datatype/object property
		if (classes.contains(subject)) {
			
			/*
			 * Note: the first check of an implicit object property is kind of
			 * weak since there could be properties having
			 * rdfs:domain rdfs:Class and an individual range. But I think in
			 * most cases it is rather an error that design decision to somehow
			 * assign individuals to classes via <class> <prop> <individual>.
			 * statements.
			 */
					// implicit object property (object is individual)
			if ((object.isResource() && individuals.contains(object.asResource()))
					// implicit datatype property
					|| object.isLiteral()
					// explicit object property
					|| objectProperties.contains(predicate)
					// explicit datatype property
					|| datatypeProperties.contains(predicate)) {
				violationFound = true;
			}
			
		}

		// class names used as object of a datatype/object property
		
				// object is a class
		if (object.isURIResource() && classes.contains(object.asResource())
				// subject is an individual
				&& individuals.contains(subject)
				// predicate is a datatype- or object property...
				&& (datatypeProperties.contains(predicate)
						|| objectProperties.contains(predicate)
						// ...or at least not whitelisted
						|| !indivPredicateClassWhitelist.contains(predicate))) {
			
			violationFound = true;
		}
		
		if (violationFound) {
			Set<ViewQuad<ViewDefinition>> candidates =
					pinpointer.getViewCandidates(statement.asTriple());
			candidatesResults.add(candidates);
		}
	}


	private void initProperties(OntModel ontModel) {
		// listAllOntProperties would in principle be the better choice here
		// but does not return any properties if they are not explicitly
		// declared as property
		ExtendedIterator<Statement> statementsIt = ontModel.listStatements();
		
		while (statementsIt.hasNext())
			properties.add(statementsIt.next().getPredicate());
		
		statementsIt = ontModel.listStatements(null, rdf_type, rdfs_Property);
		
		while (statementsIt.hasNext()) {
			Resource subject = statementsIt.next().getSubject();
			if (subject.isURIResource()) {
				String uri = subject.getURI();
				Property property = ResourceFactory.createProperty(uri);
				properties.add(property);
			}
		}
	}

	private void lookForPropertyNameClashes(Statement statement) {
		boolean violationFound = false;
		
		Resource subject = statement.getSubject();
		Property predicate = statement.getPredicate();
		RDFNode object = statement.getObject();
		
		// property name used as subject of a datatype/object property
		try {
			Property subjectAsProperty =
					ResourceFactory.createProperty(subject.getURI());
			
					// subject is known to be also a property
			if (properties.contains(subjectAsProperty)
					// and the predicate is a known datatype- or object property...
					&& (datatypeProperties.contains(predicate)
							|| objectProperties.contains(predicate)
							// ...or the predicate is at least not whitelisted for
							// use with a property as subject
							|| !propPredicateObjWhitelist.contains(predicate))) {
				violationFound = true;
			}
		} catch (InvalidPropertyURIException e) {
			// do nothing
		}

		// property name used as object of a datatype/object property
		if (object.isURIResource()) {
			try {
				Property objectAsProperty =
						ResourceFactory.createProperty(object.asResource().getURI());
				
				if (properties.contains(objectAsProperty)
						// and the predicate is a known datatype- or object property...
						&& (datatypeProperties.contains(predicate)
								|| objectProperties.contains(predicate)
								// ...or the predicate is at least not whitelisted for
								// use with a property as subject
								|| !subjPredicatePropWhitelist.contains(predicate))) {
					violationFound = true;
				}
			} catch (InvalidPropertyURIException e) {
				// do nothing
			}
		}
		
		if (violationFound) {
			Set<ViewQuad<ViewDefinition>> candidates =
					pinpointer.getViewCandidates(statement.asTriple());
			candidatesResults.add(candidates);
		}
	}


	private void initIndividuals(OntModel ontModel) {
		ExtendedIterator<Individual> indivIt = ontModel.listIndividuals();
		while (indivIt.hasNext()) individuals.add(indivIt.next());
	}

	private void lookForInstanceNameClashes(Statement statement) {
		boolean violationFound = false;
		
		Resource subject = statement.getSubject();
		Property predicate = statement.getPredicate();
		RDFNode object = statement.getObject();
		
		// individual used as property
		if (individuals.contains(predicate.asResource())) {
			violationFound = true;
		}
		// individual used as subject of certain ontology properties
			// subject is an individual... 
		if (individuals.contains(subject)
				// ...and the predicate is a known ontology defining property
				// (neither datatype- nor object property)
				&& indivPredicateObjBlacklist.contains(predicate)) {
			violationFound = true;
		}
		
		// instance name is used as object of an ontology property
			// object is an individual...
		if (object.isResource() && individuals.contains(object.asResource())
				// ...and the predicate is a known ontology defining property
				// (neither datatype- nor object property)
				&& subjPredicateIndivBlacklist.contains(predicate)){
			violationFound = true;
		}
		
		if (violationFound) {
			Set<ViewQuad<ViewDefinition>> candidates =
					pinpointer.getViewCandidates(statement.asTriple());
			candidatesResults.add(candidates);
		}
	}


	private void initHelperSets(SparqlifyDataset dataset) {
		
		OntModel ontModel = ModelFactory.createOntologyModel(
				OntModelSpec.OWL_LITE_MEM_TRANS_INF, dataset);
		
		ExtendedIterator<DatatypeProperty> dtPropsIt =
				ontModel.listDatatypeProperties();
		
		while (dtPropsIt.hasNext())
			datatypeProperties.add(dtPropsIt.next().asProperty());
		
		ExtendedIterator<ObjectProperty> objPropsIt = ontModel.listObjectProperties();
		while (objPropsIt.hasNext())
			objectProperties.add(objPropsIt.next().asProperty());
	}
}
