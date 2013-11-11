package org.aksw.sparqlify.qa.main;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

public class SparqlGraph extends GraphBase implements Graph {


	private String serviceURI ;
	@SuppressWarnings("unused")
	private String graphIRI = null ;
	private String queryStr;
	
	public SparqlGraph(String serviceURI, String graphIRI) {
		this.serviceURI = serviceURI ;
		this.graphIRI = graphIRI ;
		queryStr = "SELECT * { GRAPH <" + graphIRI + "> {?s ?p ?o }}";
	}
	
	public SparqlGraph(String serviceURI) {
		this.serviceURI = serviceURI ;
		this.graphIRI = null ;
		queryStr = "SELECT * { ?s ?p ?o }";
	}
	
	@Override
	protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m) {
		
		Query query = QueryFactory.create(queryStr);
		QueryExecution qe = QueryExecutionFactory.sparqlService(serviceURI, query);

		ResultSet res = qe.execSelect();
		List<Triple> triples = new ArrayList<Triple>() ;
		while(res.hasNext())
		{
			QuerySolution sol = res.nextSolution();
			Triple resTriple = new Triple(sol.get("s").asNode(),
					sol.get("p").asNode(), sol.get("o").asNode());
			
			triples.add(resTriple);
		}
		qe.close(); 
		return WrappedIterator.createNoRemove(triples.iterator()) ;
	}
	
	public String getServiceURI() {
		return serviceURI;
	}
}
