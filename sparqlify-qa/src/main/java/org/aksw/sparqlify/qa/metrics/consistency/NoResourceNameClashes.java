package org.aksw.sparqlify.qa.metrics.consistency;

import java.sql.SQLException;
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
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * This metric reports resource name clashes. These clashes can be:
 * 1) a class name used as
 *    a) a property
 *    b) subject of a datatype/object property
 *    c) object of a datatype/object property
 * 2) a property name used as
 *    a) subject of a datatype/object property
 *    b) object of a datatype/object property
 * 3) an individual's name used as
 *    a) a property
 * 
 * 1) is referred to as class name clashes.
 * 2) is referred to as property name clashes.
 * 3) is referred to as individual name clashes.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class NoResourceNameClashes extends MetricImpl implements DatasetMetric {

	private static Logger logger = LoggerFactory.getLogger(NoResourceNameClashes.class);
	
	@Autowired
	private Pinpointer pinpointer;
	
	// whitelist for statements like <individual> <predicate> <given class> .
	private final List<Node> indivPredicateClassWhitelist =
			new ArrayList<Node>(Arrays.asList(RDF.type.asNode()));
	
	// whitelist for statements like <given property> <predicate> <object> .
	private final List<Node> propPredicateObjWhitelist =
			new ArrayList<Node>(Arrays.asList(RDFS.subPropertyOf.asNode(),
					RDFS.domain.asNode(), RDFS.range.asNode(),
					OWL.equivalentProperty.asNode(), OWL.inverseOf.asNode(),
					RDF.type.asNode()));
	
	// whitelist for statements like <subject> <predicate> <given property> .
	private final List<Node> subjPredicatePropWhitelist =
			new ArrayList<Node>(Arrays.asList(RDFS.subPropertyOf.asNode(),
					OWL.equivalentProperty.asNode(), OWL.inverseOf.asNode()));
	
	private Set<Set<ViewQuad<ViewDefinition>>> candidatesResults;
	
	private List<String> ontPrefixes = new ArrayList<String>(Arrays.asList(RDF.getURI(),
			RDFS.getURI(), OWL.getURI(), XSD.getURI()));
	
	private Set<Node> classes;
	private Set<Node> properties;
	private Set<Node> individuals;
	// helper sets
	private Set<Node> datatypeProperties;
	private Set<Node> objectProperties;

	// for testing
	protected void clearCaches() {
		candidatesResults = new HashSet<Set<ViewQuad<ViewDefinition>>>();
		classes = new HashSet<Node>();
		properties = new HashSet<Node>();
		individuals = new HashSet<Node>();
		datatypeProperties = new HashSet<Node>();
		objectProperties = new HashSet<Node>();
	}
	
	
	public NoResourceNameClashes() {
		super();
		candidatesResults = new HashSet<Set<ViewQuad<ViewDefinition>>>();
		classes = new HashSet<Node>();
		properties = new HashSet<Node>();
		individuals = new HashSet<Node>();
		datatypeProperties = new HashSet<Node>();
		objectProperties = new HashSet<Node>();
	}


	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {
		
		logger.debug("going to init classes...");
		initClasses(dataset);
		logger.debug("inited classes");
		logger.debug("going to init properties...");
		initProperties(dataset);
		logger.debug("inited properties");
		logger.debug("going to init individuals...");
		initIndividuals(dataset);
		initPossIndividuals(dataset);
		logger.debug("inited individuals");
		
		for (Triple triple : dataset) {
			lookForClassNameClashes(triple);
			lookForPropertyNameClashes(triple);
			lookForInstanceNameClashes(triple);
		}
		
		for (Set<ViewQuad<ViewDefinition>> candidates : candidatesResults) {
			writeMappingMeasureToSink(0, candidates);
		}
	}


	/**
	 * Method that finds all classes contained in a given dataset.
	 * The classes are detected using the following SPARQL queries:
	 * 
	 * 1)
	 *   SELECT distinct ?cls { ?s a ?cls }.
	 * 
	 * 2)
	 *   SELECT ?cls {
	 *     { ?cls a rdfs:Class }
	 *     UNION { ?cls a ?sth . ?sth rdfs:subClassOf+ rdfs:Class }
	 *   }
	 * 
	 * 3)
	 *   SELECT ?cls {
	 *     { ?cls a owl:Class }
	 *     UNION { ?cls a ?sth . ?sth rdfs:subClassOf+ owl:Class }
	 *   }
	 * 
	 * @param dataset
	 */
	private void initClasses(SparqlifyDataset dataset) {
		
		List<String> clsUris = new ArrayList<String>();
		clsUris.add(RDFS.Class.getURI());
		clsUris.add(OWL.Class.getURI());
		
		// 1)
		String genClsQueryStr = "SELECT distinct ?cls { ?s a ?cls }";
		Query genClsQuery = QueryFactory.create(genClsQueryStr);
		
		QueryExecution genClsQe;
		if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
			genClsQe = QueryExecutionFactory.createServiceRequest(
					dataset.getSparqlServiceUri(), genClsQuery);
		} else {
			genClsQe = QueryExecutionFactory.create(genClsQuery, dataset);
		}
		
		ResultSet genClsRes = genClsQe.execSelect();
		while (genClsRes.hasNext()) {
			QuerySolution sol = genClsRes.next();
			Node cls = sol.get("cls").asNode();
			classes.add(cls);
		}
		genClsQe.close();
		
		// 2, 3)
		for(String clsUri : clsUris) {
			String clsQueryStr =
				"SELECT ?cls {" +
					"{ ?cls a <" + clsUri + "> } UNION " +
					"{ ?cls a ?sth. ?sth <" + RDFS.subClassOf.getURI() + ">+ <" + clsUri + "> }" +
				"}";
			Query clsQuery = QueryFactory.create(clsQueryStr);
			
			QueryExecution clsQe;
			if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
				clsQe = QueryExecutionFactory.createServiceRequest(
						dataset.getSparqlServiceUri(), clsQuery);
			} else {
				clsQe = QueryExecutionFactory.create(clsQuery, dataset);
			}
			
			ResultSet clsRes = clsQe.execSelect();

			while(clsRes.hasNext()) {
				QuerySolution sol = clsRes.next();
				Node cls = sol.get("cls").asNode();
				classes.add(cls);
			}
			clsQe.close();
		}
	}
	
	private void lookForClassNameClashes(Triple triple) {
		boolean violationFound = false;
		
		Node subject = triple.getSubject();
		Node predicate = triple.getPredicate();
		Node object = triple.getObject();
		
		// 1a) class names used as property
		if (classes.contains(predicate)) {
			violationFound = true;
		}
		
		// class names used as subject of a datatype/object property
		if (classes.contains(subject)) {
			
			/*
			 * Note: the first check of an implicit object property is kind of
			 * weak since there could be properties having
			 * rdfs:domain rdfs:Class and an individual range. But I think in
			 * most cases it is rather an error than design decision to somehow
			 * assign individuals to classes via <class> <prop> <individual>.
			 * statements.
			 */
					// implicit object property (object is individual)
			if ((!object.isLiteral() && individuals.contains(object))
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
		if (object.isURI() && classes.contains(object)
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
					pinpointer.getViewCandidates(triple);
			candidatesResults.add(candidates);
		}
	}


	/**
	 * Method that finds all properties contained in a dataset.
	 * Properties are detected using the following SPARQL queries:
	 * 
	 * 1)
	 *   SELECT distinct ?p { ?s ?p ?o }
	 * 
	 * 2)
	 *   SELECT ?p {
	 *     { ?p a rdf:Property }
	 *     UNION { ?p a ?sth . ?sth rdfs:subPropertyOf+ rdf:Property }
	 *   }
	 * 
	 * 3)
	 *   SELECT ?p {
	 *     { ?p a owl:AnnotationProperty }
	 *     UNION { ?p a ?sth . ?sth rdfs:subPropertyOf+ owl:AnnotationProperty }
	 *   }
	 * 
	 * 4)
	 *   SELECT ?p {
	 *     { ?p a owl:DatatypeProperty }
	 *     UNION { ?p a ?sth . ?sth rdfs:subPropertyOf+ owl:DatatypeProperty }
	 *   }
	 * 
	 * 5)
	 *   SELECT ?p {
	 *     { ?p a owl:DeprecatedProperty }
	 *     UNION { ?p a ?sth . ?sth rdfs:subPropertyOf+ owl:DeprecatedProperty }
	 *   }
	 * 
	 * 6)
	 *   SELECT ?p {
	 *     { ?p a owl:FunctionalProperty }
	 *     UNION { ?p a ?sth . ?sth rdfs:subPropertyOf+ owl:FunctionalProperty }
	 *   }
	 * 
	 * 7)
	 *   SELECT ?p {
	 *     { ?p a owl:InverseFunctionalProperty }
	 *     UNION { ?p a ?sth . ?sth rdfs:subPropertyOf+ owl:InverseFunctionalProperty }
	 *   }
	 * 
	 * 8)
	 *   SELECT ?p {
	 *     { ?p a owl:ObjectProperty }
	 *     UNION { ?p a ?sth . ?sth rdfs:subPropertyOf+ owl:ObjectProperty }
	 *   }
	 * 
	 * 9)
	 *   SELECT ?p {
	 *     { ?p a owl:OntologyProperty }
	 *     UNION { ?p a ?sth . ?sth rdfs:subPropertyOf+ owl:OntologyProperty }
	 *   }
	 * 
	 * 10)
	 *   SELECT ?p {
	 *     { ?p a owl:SymmetricProperty }
	 *     UNION { ?p a ?sth . ?sth rdfs:subPropertyOf+ owl:SymmetricProperty }
	 *   }
	 * 
	 * 11)
	 *   SELECT ?p {
	 *     { ?p a owl:TransitiveProperty }
	 *     UNION { ?p a ?sth . ?sth rdfs:subPropertyOf+ owl:TransitiveProperty }
	 *   }
	 * 
	 * Besides this, datatype and object properties are collected separately.
	 * 
	 * @param dataset
	 */
	private void initProperties(SparqlifyDataset dataset) {
		
		List<String> propertyClsUris = new ArrayList<String>(Arrays.asList(
				RDF.Property.getURI(), OWL.AnnotationProperty.getURI(),
				OWL.DatatypeProperty.getURI(), OWL.DeprecatedProperty.getURI(),
				OWL.FunctionalProperty.getURI(),
				OWL.InverseFunctionalProperty.getURI(),
				OWL.ObjectProperty.getURI(), OWL.OntologyProperty.getURI(),
				OWL.SymmetricProperty.getURI(), OWL.TransitiveProperty.getURI()
			));
		
		// 1)
		String genPropQueryStr = "SELECT distinct ?p { ?s ?p ?o }";
		Query genPropQuery = QueryFactory.create(genPropQueryStr);
		
		QueryExecution genPropQe;
		if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
			genPropQe = QueryExecutionFactory.createServiceRequest(
					dataset.getSparqlServiceUri(), genPropQuery);
		} else {
			genPropQe = QueryExecutionFactory.create(genPropQuery, dataset);
		}
		
		ResultSet genPropRes = genPropQe.execSelect();
		
		while (genPropRes.hasNext()) {
			QuerySolution sol = genPropRes.next();
			Node prop = sol.get("p").asNode();
			properties.add(prop);
		}
		genPropQe.close();
		
		
		// 2)-11)
		for(String propUri : propertyClsUris) {
			
			String propQueryStr =
				"SELECT ?p {" +
					"{ ?p a <" + propUri + "> } UNION " +
					"{ ?p a ?sth. ?sth <" + RDFS.subPropertyOf.getURI() + ">+ <" + propUri + "> }" +
				"}";
			Query propQuery = QueryFactory.create(propQueryStr);
			
			QueryExecution propQe;
			if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
				propQe = QueryExecutionFactory.createServiceRequest(dataset.getSparqlServiceUri(), propQuery);
			} else {
				propQe = QueryExecutionFactory.create(propQuery, dataset);
			}
			
			ResultSet propRes = propQe.execSelect();

			while(propRes.hasNext()) {
				QuerySolution sol = propRes.next();
				Node prop = sol.get("p").asNode();
				properties.add(prop);
				
				// also add prop to the datatype/object properties list if it is
				// a datatype/object property
				if (propUri.equals(OWL.DatatypeProperty.getURI())) {
					datatypeProperties.add(prop);
				} else if (propUri.equals(OWL.ObjectProperty.getURI())) {
					objectProperties.add(prop);
				}
			}
			propQe.close();
		}
	}

	private void lookForPropertyNameClashes(Triple triple) {
		boolean violationFound = false;
		
		Node subject = triple.getSubject();
		Node predicate = triple.getPredicate();
		Node object = triple.getObject();
		
		// 2a) property name used as subject of a datatype/object property
		
				// subject is known to be also a property
		if (properties.contains(subject)
				// and the predicate is a known datatype- or object property...
				&& (datatypeProperties.contains(predicate)
					|| objectProperties.contains(predicate)
					// ...or the predicate is at least not whitelisted for
					// use with a property as subject
					|| !propPredicateObjWhitelist.contains(predicate))) {
			violationFound = true;
		}

		// 2b) property name used as object of a datatype/object property
		if (object.isURI()) {
			if (properties.contains(object)
					// and the predicate is a known datatype- or object property...
					&& (datatypeProperties.contains(predicate)
							|| objectProperties.contains(predicate)
							// ...or the predicate is at least not whitelisted for
							// use with a property as subject
							|| !subjPredicatePropWhitelist.contains(predicate))) {
				violationFound = true;
			}
		}
		
		if (violationFound) {
			Set<ViewQuad<ViewDefinition>> candidates =
					pinpointer.getViewCandidates(triple);
			candidatesResults.add(candidates);
		}
	}

	/**
	 * This method finds all individuals (= instances of owl:Class)
	 * 
	 * @param dataset
	 */
	private void initIndividuals(SparqlifyDataset dataset) {
		String indivQueryStr =
				"SELECT distinct ?indiv {" +
					"?indiv a ?cls . ?cls a <" + OWL.Class.getURI() + "> }";
		
		Query indivQuery = QueryFactory.create(indivQueryStr);
		QueryExecution indivQe;
		if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
			indivQe = QueryExecutionFactory.createServiceRequest(
					dataset.getSparqlServiceUri(), indivQuery);
		} else {
			indivQe = QueryExecutionFactory.create(indivQuery, dataset);
		}
		
		ResultSet indivRes = indivQe.execSelect();
		while (indivRes.hasNext()) {
			QuerySolution sol = indivRes.next();
			Node indiv = sol.get("indiv").asNode();
			individuals.add(indiv);
		}
	}

	/**
	 * This method finds resources that are *possibly* individuals. The
	 * collection of possible individuals is necessary since there were no cases
	 * where instances of owl:Class appeared in real world datasets. Thus, this
	 * heuristic way is followed here. Resources are possible individuals if
	 * they are no declared as a known class or property.
	 * 
	 * @param dataset
	 */
	private void initPossIndividuals(SparqlifyDataset dataset) {
		
		// subject, not being of type rdfs:Class/owl:Class or a subclass of
		// rdfs:Class/owl:Class
		String sClassQueryStr =
				"SELECT distinct ?indiv {" +
					"?indiv ?p ?o . " +
					"FILTER (" + 
						"(not exists {?indiv a <" + RDF.Property.getURI() + "> } ) " +
						"&& (not exists {?indiv a <" + OWL.AnnotationProperty.getURI() + "> } ) " +
						"&& (not exists {?indiv a <" + OWL.DatatypeProperty.getURI() + "> } ) " +
						"&& (not exists {?indiv a <" + OWL.DeprecatedProperty.getURI() + "> } ) " +
						"&& (not exists {?indiv a <" + OWL.FunctionalProperty.getURI() + "> } ) " +
						"&& (not exists {?indiv a <" + OWL.InverseFunctionalProperty.getURI() + "> } ) " +
						"&& (not exists {?indiv a <" + OWL.ObjectProperty.getURI() + "> } ) " +
						"&& (not exists {?indiv a <" + OWL.OntologyProperty.getURI() + "> } ) " +
						"&& (not exists {?indiv a <" + OWL.SymmetricProperty.getURI() + "> } ) " +
						"&& (not exists {?indiv a <" + OWL.TransitiveProperty.getURI() + "> } ) " +
						"&& (not exists {?indiv a <" + RDFS.Class.getURI() + "> } ) " +
						"&& (not exists {?indiv a <" + OWL.Class.getURI() + "> } ) " +
						"&& (not exists {?inst a ?indiv } ) " +
						"&& (not exists {" +
							"?indiv <" + RDFS.subClassOf.getURI() + ">+ <" + RDFS.Class.getURI() + "> }) " +
						"&& (not exists {" +
							"?indiv <" + RDFS.subClassOf.getURI() + ">+ <" + OWL.Class.getURI() + "> } ))" +
				"}";
		Query sClassQuery = QueryFactory.create(sClassQueryStr);
		
		QueryExecution sClassQe;
		if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
			sClassQe = QueryExecutionFactory.createServiceRequest(
					dataset.getSparqlServiceUri(), sClassQuery);
		} else {
			sClassQe = QueryExecutionFactory.create(sClassQuery, dataset);
		}
		
		ResultSet sClassRes = sClassQe.execSelect();
		logger.debug("got subject results");
		while (sClassRes.hasNext()) {
			QuerySolution sol = sClassRes.next();
			Node indiv = sol.get("indiv").asNode();
			
			// check if potential individual starts with a prefix of a known
			// ontology/vocabulary not containing any individuals 
			boolean skip = false;
			if (indiv.isURI()) {
				String indivUri = indiv.getURI();
				for (String ontPrefix : ontPrefixes) {
					if (indivUri.startsWith(ontPrefix)) {
						skip = true;
						break;
					}
				}
			}
			if (!skip) individuals.add(indiv);
		}
		
		// object not being literal and not being of type rdfs:Class/owl:Class
		// or a subclass of rdfs:Class/owl:Class
		String oClassQueryStr =
				"SELECT distinct ?indiv {" +
					"?s ?p ?indiv . " +
					"FILTER ( ! isLiteral(?indiv) " +
						"&& not exists {?indiv a <" + RDF.Property.getURI() + "> } " +
						"&& not exists {?indiv a <" + OWL.AnnotationProperty.getURI() + "> } " +
						"&& not exists {?indiv a <" + OWL.DatatypeProperty.getURI() + "> } " +
						"&& not exists {?indiv a <" + OWL.DeprecatedProperty.getURI() + "> } " +
						"&& not exists {?indiv a <" + OWL.FunctionalProperty.getURI() + "> } " +
						"&& not exists {?indiv a <" + OWL.InverseFunctionalProperty.getURI() + "> } " +
						"&& not exists {?indiv a <" + OWL.ObjectProperty.getURI() + "> } " +
						"&& not exists {?indiv a <" + OWL.OntologyProperty.getURI() + "> } " +
						"&& not exists {?indiv a <" + OWL.SymmetricProperty.getURI() + "> } " +
						"&& not exists {?indiv a <" + OWL.TransitiveProperty.getURI() + "> } " +
						"&& not exists { ?indiv a <" + RDFS.Class.getURI() + "> } " +
						"&& not exists {" +
							"?indiv <" + RDFS.subClassOf.getURI() + ">+ <" + RDFS.Class.getURI() + "> } " +
						"&& not exists { ?indiv a <" + OWL.Class.getURI() + "> } " +
						"&& not exists {" +
							"?indiv <" + RDFS.subClassOf.getURI() + ">+ <" + OWL.Class.getURI() + "> } ) " +
				"}";
		Query oClassQuery = QueryFactory.create(oClassQueryStr);
		
		QueryExecution oClassQe;
		if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
			oClassQe = QueryExecutionFactory.createServiceRequest(dataset.getSparqlServiceUri(), oClassQuery);
		} else {
			oClassQe = QueryExecutionFactory.create(oClassQuery, dataset);
		}
		
		ResultSet oClassRes = oClassQe.execSelect();
		
		while (oClassRes.hasNext()) {
			QuerySolution sol = oClassRes.next();
			Node indiv = sol.get("indiv").asNode();
			
			// check if potential individual starts with a prefix of a known
			// ontology/vocabulary not containing any individuals 
			boolean skip = false;
			if (indiv.isURI()) {
				String indivUri = indiv.getURI();
				for (String ontPrefix : ontPrefixes) {
					if (indivUri.startsWith(ontPrefix)) {
						skip = true;
						break;
					}
				}
			}
			if (!skip) individuals.add(indiv);
		}
	}

	private void lookForInstanceNameClashes(Triple triple) {
		
		Node predicate = triple.getPredicate();
		
		if (individuals.contains(predicate)) {
			Set<ViewQuad<ViewDefinition>> candidates =
					pinpointer.getViewCandidates(triple);
			candidatesResults.add(candidates);
		}
	}
}
