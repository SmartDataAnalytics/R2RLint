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
@Component
public class WellPlacedClassesAndProperties extends MetricImpl implements
		DatasetMetric {

	@Autowired
	private Pinpointer pinpointer;
	
	private final List<String> propertyOnSubjPosWhitelist = new ArrayList<String>(
			Arrays.asList( RDF.type.getURI(), RDFS.subPropertyOf.getURI(),
					OWL.equivalentProperty.getURI(), OWL.inverseOf.getURI(),
					RDFS.domain.getURI(), RDFS.range.getURI(),
					RDFS.label.getURI(), RDFS.comment.getURI()));
	
	private final List<String> propertyOnObjPosWhitelist = new ArrayList<String>(
			Arrays.asList(RDFS.subPropertyOf.getURI(),
					OWL.equivalentProperty.getURI(), OWL.inverseOf.getURI(),
					OWL.onProperty.getURI()));

	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {

		/* ====================================================================
		 * checks for wrong-placed properties
		 */
		List<String> propertyClsUris = new ArrayList<String>(Arrays.asList(
				RDF.Property.getURI(), OWL.AnnotationProperty.getURI(),
				OWL.DatatypeProperty.getURI(), OWL.DeprecatedProperty.getURI(),
				OWL.FunctionalProperty.getURI(),
				OWL.InverseFunctionalProperty.getURI(),
				OWL.ObjectProperty.getURI(), OWL.OntologyProperty.getURI(),
				OWL.SymmetricProperty.getURI(), OWL.TransitiveProperty.getURI()
			));
		
		Set<Node> properties = new HashSet<Node>();


		
		String genPropQueryStr = "SELECT distinct ?p { ?s ?p ?o }";
		Query genPropQuery = QueryFactory.create(genPropQueryStr);
		QueryExecution genPropQe;
		if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
			genPropQe = QueryExecutionFactory.createServiceRequest(dataset.getSparqlServiceUri(), genPropQuery);
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
		
		for(String propUri : propertyClsUris) {
			String propQueryStr =
				"SELECT ?p {" +
					"{ ?p a <" + propUri + "> } UNION " +
					"{ ?p a ?cls. ?cls <" + RDFS.subClassOf.getURI() + ">+ <" + propUri + "> }" +
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
				Node pred = sol.get("p").asNode();
				properties.add(pred);
			}
			propQe.close();
		}
		
		for (Node prop : properties) {
			
			// property is on subject position...
			String propSubjQueryStr = "SELECT * { <" + prop.getURI() + "> ?p ?o }";
			Query propSubjQuery = QueryFactory.create(propSubjQueryStr);
			
			QueryExecution propSubjQe;
			if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
				propSubjQe = QueryExecutionFactory.createServiceRequest(
						dataset.getSparqlServiceUri(), propSubjQuery);
			} else {
				propSubjQe = QueryExecutionFactory.create(propSubjQuery, dataset);
			}
			
			ResultSet propSubjRes = propSubjQe.execSelect();

			while(propSubjRes.hasNext()) {
				QuerySolution sol = propSubjRes.nextSolution();
				Node pred = sol.get("p").asNode();
				Node obj = sol.get("o").asNode();
				
				if (!propertyOnSubjPosWhitelist.contains(pred.getURI())) {
					// ...and used in connection with a predicate that is not
					// whitelisted explicitly
					
					Triple triple = new Triple(prop, pred, obj);
					Set<ViewQuad<ViewDefinition>> viewQuads =
							pinpointer.getViewCandidates(triple);
					
					writeTripleMeasureToSink(0, triple, viewQuads);
				}
				
			}
			
			// property is on object position...
			String propObjQueryStr = "SELECT * { ?s ?p <" + prop.getURI() + "> }";
			Query propObjQuery = QueryFactory.create(propObjQueryStr);
			
			QueryExecution propObjQe;
			if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
				propObjQe = QueryExecutionFactory.createServiceRequest(
						dataset.getSparqlServiceUri(), propObjQuery);
			} else {
				propObjQe = QueryExecutionFactory.create(propObjQuery, dataset);
			}
			
			ResultSet propObjRes = propObjQe.execSelect();

			while(propObjRes.hasNext())
			{
				QuerySolution sol = propObjRes.nextSolution();
				Node subj = sol.get("s").asNode();
				Node pred = sol.get("p").asNode();
				
				if (!propertyOnObjPosWhitelist.contains(pred.getURI())) {
					// ...and used in connection with a predicate that is not
					// whitelisted explicitly
					
					Triple triple = new Triple(subj, pred, prop);
					Set<ViewQuad<ViewDefinition>> viewQuads =
							pinpointer.getViewCandidates(triple);
					
					writeTripleMeasureToSink(0, triple, viewQuads);
				}
				
			}
		}
		
		/*
		 * In a second step a more throughout query would be required, e.g.
		 * using the query below. But this query was not used due to
		 * performance problems
		 */
//		String propQueryStr =
//				"SELECT distinct ?p {" +
//					"{ ?s ?p ?o } UNION " +
					// is (subclass of) rdf:Property
//					"{ ?p a <" + RDF.Property.getURI() + "> } UNION " +
//					"{ ?p a ?cls. ?cls <" + RDFS.subClassOf.getURI() + ">+ <" + RDF.Property.getURI() + "> } UNION " +
					// is (subclass of) owl:AnnotationProperty
//					"{ ?p a <" + OWL.AnnotationProperty.getURI() + "> } UNION " +
//					"{ ?p a ?cls. ?cls <" + RDFS.subClassOf.getURI() + ">+ <" + OWL.AnnotationProperty.getURI() + "> } UNION " +
					// is (subclass of) owl:DatatypeProperty
//					"{ ?p a <" + OWL.DatatypeProperty.getURI() + "> } UNION " +
//					"{ ?p a ?cls. ?cls <" + RDFS.subClassOf.getURI() + ">+ <" + OWL.DatatypeProperty.getURI() + "> } UNION " +
					// is (subclass of) owl:DeprecatedProperty
//					"{ ?p a <" + OWL.DeprecatedProperty.getURI() + "> } UNION " +
//					"{ ?p a ?cls. ?cls <" + RDFS.subClassOf.getURI() + ">+ <" + OWL.DeprecatedProperty.getURI() + "> } UNION " +
					// is (subclass of) owl:FunctionalProperty
//					"{ ?p a <" + OWL.FunctionalProperty.getURI() + "> } UNION " +
//					"{ ?p a ?cls. ?cls <" + RDFS.subClassOf.getURI() + ">+ <" + OWL.FunctionalProperty.getURI() + "> } UNION " +
					// is (subclass of) owl:InverseFunctionalProperty
//					"{ ?p a <" + OWL.InverseFunctionalProperty.getURI() + "> } UNION " +
//					"{ ?p a ?cls. ?cls <" + RDFS.subClassOf.getURI() + ">+ <" + OWL.InverseFunctionalProperty.getURI() + "> } UNION " +
					// is (subclass of) owl:ObjectProperty
//					"{ ?p a <" + OWL.ObjectProperty.getURI() + "> } UNION " +
//					"{ ?p a ?cls. ?cls <" + RDFS.subClassOf.getURI() + ">+ <" + OWL.ObjectProperty.getURI() + "> } UNION " +
					// is (subclass of) owl:OntologyProperty
//					"{ ?p a <" + OWL.OntologyProperty.getURI() + "> } UNION " +
//					"{ ?p a ?cls. ?cls <" + RDFS.subClassOf.getURI() + ">+ <" + OWL.OntologyProperty.getURI() + "> } UNION " +
					// is (subclass of) owl:SymmetricProperty
//					"{ ?p a <" + OWL.SymmetricProperty.getURI() + "> } UNION " +
//					"{ ?p a ?cls. ?cls <" + RDFS.subClassOf.getURI() + ">+ <" + OWL.SymmetricProperty.getURI() + "> } UNION " +
					// is (subclass of) owl:TransitiveProperty
//					"{ ?p a <" + OWL.TransitiveProperty.getURI() + "> } UNION " +
//					"{ ?p a ?cls. ?cls <" + RDFS.subClassOf.getURI() + ">+ <" + OWL.TransitiveProperty.getURI() + "> } " +
//				"}";
		
		
		
		/* ====================================================================
		 * checks for wrong-placed classes
		 */
		
		// get classes
		List<String> clsUris = new ArrayList<String>(Arrays.asList(
				RDFS.Class.getURI(), OWL.Class.getURI()
			));
		
		Set<Node> classes = new HashSet<Node>();
		
		String genClsQueryStr = "SELECT distinct ?cls { ?s a ?cls }";
		Query genClsQuery = QueryFactory.create(genClsQueryStr);
		QueryExecution genClsQe;
		if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
			genClsQe = QueryExecutionFactory.createServiceRequest(dataset.getSparqlServiceUri(), genClsQuery);
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
		
		for(String clsUri : clsUris) {
			String clsQueryStr =
				"SELECT ?cls {" +
					"{ ?cls a <" + clsUri + "> } UNION " +
					"{ ?cls a ?sth. ?sth <" + RDFS.subClassOf.getURI() + ">+ <" + clsUri + "> }" +
				"}";
			Query clsQuery = QueryFactory.create(clsQueryStr);
			
			QueryExecution clsQe;
			if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
				clsQe = QueryExecutionFactory.createServiceRequest(dataset.getSparqlServiceUri(), clsQuery);
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
		
		
		for (Node cls : classes) {
			
			if (cls.isBlank()) continue;
			String clsPredQueryStr = "SELECT * { ?s <" + cls.getURI() + "> ?o }";
			
			Query clsPredQuery = QueryFactory.create(clsPredQueryStr);
			
			QueryExecution clsPredQe;
			if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
				clsPredQe = QueryExecutionFactory.createServiceRequest(
						dataset.getSparqlServiceUri(), clsPredQuery);
			} else {
				clsPredQe = QueryExecutionFactory.create(clsPredQuery, dataset);
			}
			
			ResultSet clsPredRes = clsPredQe.execSelect();
			
			while (clsPredRes.hasNext()) {
				QuerySolution sol = clsPredRes.nextSolution();
				Node subj = sol.get("s").asNode();
				Node obj = sol.get("o").asNode();
				Triple triple = new Triple(subj, cls, obj);
				
				Set<ViewQuad<ViewDefinition>> viewQuads =
						pinpointer.getViewCandidates(triple);
				
				writeTripleMeasureToSink(0, triple, viewQuads);
			}
		}
		
	}

}
