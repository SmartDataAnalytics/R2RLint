package org.aksw.sparqlify.qa.dataset;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.io.LineIterator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


/*
 * TODO:
 * - make this a subclass of Model
 * - performance and memory tweaks can be applied later; for now a naive model
 *   based approach is sufficient
 * 
 * notes:
 * - Model-based approach would allow to use a SPARQL endpoint or a Sparqlify
 *   dump
 */

public class SparqlifyDump implements Iterable<String> {
	
	private String dumpFilePath;
	FileReader fileRead;
	BufferedReader buff;
	
	public SparqlifyDump(String dumpFilePath) throws FileNotFoundException {
		this.dumpFilePath = dumpFilePath;
		
		fileRead = new FileReader(dumpFilePath);
		buff = new BufferedReader(fileRead);
	}
	
	
	public Model read() throws FileNotFoundException {
		Model model = ModelFactory.createDefaultModel();
		
		InputStream in = new FileInputStream   (dumpFilePath); 
		model.read(in, null, "TTL");
		
		return model;
	}

	
	@Override
	public Iterator<String> iterator() {
		Iterator<String> it = new LineIterator(buff);
		return it;
	}

}
