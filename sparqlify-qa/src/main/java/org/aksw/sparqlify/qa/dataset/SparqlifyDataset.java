package org.aksw.sparqlify.qa.dataset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.aksw.commons.jena.reader.NTripleIterator;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
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

	Iterator<Triple> it;


	public SparqlifyDataset() {
		super(GraphFactory.createDefaultGraph());
	}


	public void readFromDump(String dumpFilePath) throws IOException {
		
		File dumpFile = new File(dumpFilePath);
		InputStream modelStream = new FileInputStream(dumpFile);
		read(modelStream, null, "TTL");
		modelStream.close();
		
		InputStream iteratorStream = new FileInputStream(dumpFile);
		it = new NTripleIterator(iteratorStream, null, null);
	}


	@Override
	public Iterator<Triple> iterator() {
		return it;
	}

}
