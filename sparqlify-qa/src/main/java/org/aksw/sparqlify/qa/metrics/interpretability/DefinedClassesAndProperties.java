package org.aksw.sparqlify.qa.metrics.interpretability;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
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

@Component
public class DefinedClassesAndProperties extends MetricImpl implements
		DatasetMetric {

	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {
		
		/* ====================================================================
		 * assess classes
		 */
		Query rdfTypeQuery = QueryFactory.create(
				"SELECT DISTINCT ?o { ?s a ?o }");
		QueryExecution rdfTypeQe;
		if (dataset.isSparqlService() && dataset.getSparqlServiceUri() != null) {
			rdfTypeQe = QueryExecutionFactory.createServiceRequest(
					dataset.getSparqlServiceUri(), rdfTypeQuery);
		} else {
			rdfTypeQe = QueryExecutionFactory.create(rdfTypeQuery, dataset);
		}
		
		ResultSet typeRes = rdfTypeQe.execSelect();
		
		List<String> seenClasses = new ArrayList<String>();
		
		while (typeRes.hasNext()) {
			QuerySolution sol = typeRes.next();
			Node cls = sol.get("o").asNode();
			if (!seenClasses.contains(cls.getURI())) {
				
				// check if cls is local
				boolean clsIsLocal = false;
				for (String prefix : dataset.getPrefixes()) {
					if (cls.getURI().startsWith(prefix)) {
						clsIsLocal = true;
						break;
					}
				}
				
				if (clsIsLocal) {
				
					// check if cls a rdfs:Class
					Query rdfsQuery = QueryFactory.create(
							"ASK { <" + cls.getURI() + "> a <" + RDFS.Class.getURI() + "> }");
				
					QueryExecution qe;
					if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
						qe = QueryExecutionFactory.createServiceRequest(dataset.getSparqlServiceUri(), rdfsQuery);
					} else {
						qe = QueryExecutionFactory.create(rdfsQuery, dataset);
					}
					
					boolean isRdfsCls = qe.execAsk();
					
					if (!isRdfsCls) {
						
						// check if owl:Class
						Query owlQuery = QueryFactory.create(
								"ASK { <" + cls.getURI() + "> a <" + OWL.Class.getURI() + "> }");
						
						if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
							qe = QueryExecutionFactory.createServiceRequest(
									dataset.getSparqlServiceUri(), owlQuery);
						} else {
							qe = QueryExecutionFactory.create(owlQuery, dataset);
						}
						
						boolean isOwlCls = qe.execAsk();
						
						if (!isOwlCls) {
							writeTripleMeasureToSink(0, new Triple(cls,
									RDF.type.asNode(), RDFS.Class.asNode()), null);
						}
					}
				}
				seenClasses.add(cls.getURI());
			}
		}
		
		
		/* ====================================================================
		 * assess properties
		 */
		Query query = QueryFactory.create("SELECT DISTINCT ?p { ?s ?p ?o }");
		
		QueryExecution qe;
		if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
			qe = QueryExecutionFactory.createServiceRequest(dataset.getSparqlServiceUri(), query);
		} else {
			qe = QueryExecutionFactory.create(query, dataset);
		}
		
		List<String> seenProperties = new ArrayList<String>();
		
		ResultSet res = qe.execSelect();
		
		while(res.hasNext())
		{
			QuerySolution sol = res.nextSolution();
			Node prop = sol.get("p").asNode();

			if (!seenProperties.contains(prop.getURI())) {
				
				// check if property is local
				boolean propIsLocal = false;
				for (String prefix : dataset.getPrefixes()) {
					if (prop.getURI().startsWith(prefix)) {
						propIsLocal = true;
						break;
					}
				}
				
				if (propIsLocal) {
				
					Query pDefAsk = QueryFactory.create(
							"ASK { <" + prop.getURI() + "> a <" + RDF.Property.getURI() + "> }");
					
					QueryExecution qeAsk;
					if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
						qeAsk = QueryExecutionFactory.createServiceRequest(
								dataset.getSparqlServiceUri(), pDefAsk);
					} else {
						qeAsk = QueryExecutionFactory.create(pDefAsk, dataset);
					}
					boolean propDefined = qeAsk.execAsk();
					qeAsk.close();
					
					if (!propDefined) {
						writeTripleMeasureToSink(0, new Triple(prop,
								RDF.type.asNode(), RDF.Property.asNode()), null);
					}
				}
				seenProperties.add(prop.getURI());
			}
		}
		qe.close();
	}

}
