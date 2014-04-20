package org.aksw.sparqlify.qa.dataset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aksw.commons.jena.reader.NTripleIterator;
import org.aksw.sparqlify.qa.main.SparqlGraph;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.sparql.graph.GraphFactory;


/*
 * TODO:
 * - performance and memory tweaks can be applied later; for now a naive model
 *   based approach is sufficient
 * 
 * notes:
 * - Model-based approach would allow to use a SPARQL endpoint or a Sparqlify
 *   dump
 */

public class SparqlifyDataset extends ModelCom implements Model, Iterable<Triple> {

	private Iterator<Triple> it;
	// to be able to differentiate between "re-used" and own resources
	private List<String> prefixes = null;
	private List<String> usedPrefixes = null;
	private boolean sparqlWrapping = false;
	private long size = -1;
	private boolean cachingEnabled = true;

	public SparqlifyDataset() {
		super(GraphFactory.createDefaultGraph());
	}


	public SparqlifyDataset(Graph g) {
		super(g);
		sparqlWrapping = true;
		prefixes = new ArrayList<String>();
	}

	public boolean isSparqlService() {
		return sparqlWrapping;
	}
	
	public String getSparqlServiceUri() {
		if (sparqlWrapping) {
			return ((SparqlGraph) getGraph()).getServiceUri();
		} else return null;
	}
	
	public String getGraphIri() {
		if (sparqlWrapping) {
			return ((SparqlGraph) getGraph()).getGraphIri();
		} else return null;
	}
	
	public void readFromDump(String dumpFilePath) throws IOException {
		
		File dumpFile = new File(dumpFilePath);
		
		InputStream iteratorStream = new FileInputStream(dumpFile);
		it = new NTripleIterator(iteratorStream, null, null);
	}
	
	public void setPrefixes(String prefixes) {
		for (String prefix : prefixes.split(",")) {
			this.prefixes.add(prefix.trim());
		}
	}


	public List<String> getPrefixes() {
		return prefixes;
	}


	public void setUsedPrefixes(List<String> prefixes) {
		usedPrefixes = prefixes;
	}


	public List<String> getUsedPrefixes() {
		return usedPrefixes;
	}
	@Override
	public Iterator<Triple> iterator() {
		return it;
	}
	
	public void registerDump(String dumpFilePath) throws FileNotFoundException {
		File dumpFile = new File(dumpFilePath);
		InputStream iteratorStream = new FileInputStream(dumpFile);
		it = new NTripleIterator(iteratorStream, null, null);
		
	}
	
	@Override
	public long size() {
		if (sparqlWrapping) {
			if (size < 0 || !cachingEnabled) {
				Query query = QueryFactory.create(
						"SELECT DISTINCT (count(*) AS ?count) {?s ?p ?o}");
				QueryExecution qe = QueryExecutionFactory.sparqlService(
						((SparqlGraph) getGraph()).getServiceUri(),
						query);
				ResultSet res = qe.execSelect();
				RDFNode count = null;
				while(res.hasNext())
				{
					QuerySolution sol = res.nextSolution();
					count = sol.get("count").asLiteral();
				}
				qe.close();
				
				return count.asLiteral().getLong();
			} else {
				return size;
			}
		} else {
			return super.size();
		}
	}
}
