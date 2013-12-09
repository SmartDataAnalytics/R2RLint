package org.aksw.sparqlify.qa.main;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;


public class SparqlGraph extends GraphBase implements Graph {


	private String serviceURI ;
	@SuppressWarnings("unused")
	private String graphIRI = null ;
	private long tripleSliceSize = 10000;
	
	public SparqlGraph(String serviceURI, String graphIRI) {
		this.serviceURI = serviceURI ;
		this.graphIRI = graphIRI ;
	}
	
	public SparqlGraph(String serviceURI) {
		this.serviceURI = serviceURI ;
		this.graphIRI = null ;
	}
	
	@Override
	protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m) {
		
		Node s = m.getMatchSubject() ;
		Var sVar = null ;
		if ( s == null )
		{
			sVar = Var.alloc("s") ;
			s = sVar ;
		}
		
		Node p = m.getMatchPredicate() ;
		Var pVar = null ;
		if ( p == null )
		{
			pVar = Var.alloc("p") ;
			p = pVar ;
		}
		
		Node o = m.getMatchObject() ;
		Var oVar = null ;
		if ( o == null )
		{
			oVar = Var.alloc("o") ;
			o = oVar ;
		}
		
		Triple triple = new Triple(s, p ,o) ;
		
		BasicPattern pattern = new BasicPattern() ;
		pattern.add(triple) ;
		ElementTriplesBlock element = new ElementTriplesBlock(pattern);
		Query query = new Query();
		query.setQuerySelectType(); 
		query.setQueryResultStar(true);
		query.setQueryPattern(element);
		query.setDistinct(true);
		
		boolean resNotEmpty = true;
		query.setLimit(tripleSliceSize);
		long offsetCounter = 0;
		Set<Triple> triples = new HashSet<Triple>() ;
		while (resNotEmpty) {
			long offset = tripleSliceSize * offsetCounter++;
			query.setOffset(offset);
			QueryExecution qe = QueryExecutionFactory.sparqlService(serviceURI, query);
	
			ResultSet res = qe.execSelect();
			if (!res.hasNext()) resNotEmpty = false;
			
			while(res.hasNext()) {
				QuerySolution sol = res.nextSolution();
				Node subj;
				if (s.isVariable()) {
					subj = sol.get("s").asNode();
				} else {
					subj= s;
				}
				
				Node pred;
				if (p.isVariable()) {
					pred = sol.get("p").asNode();
				} else {
					pred = p;
				}
				
				Node obj;
				if (o.isVariable()) {
					obj = sol.get("o").asNode();
				} else {
					obj = o;
				}
				Triple resTriple = new Triple(subj, pred, obj);
				
				triples.add(resTriple);
			}
			qe.close();
		}
		return WrappedIterator.createNoRemove(triples.iterator()) ;
	}
	
	public String getServiceURI() {
		return serviceURI;
	}
}
