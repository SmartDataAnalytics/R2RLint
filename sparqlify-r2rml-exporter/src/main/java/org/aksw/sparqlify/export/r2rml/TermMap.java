package org.aksw.sparqlify.export.r2rml;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * A Class representing an R2RML Term Map just containing a Term Map predicate
 * and a Term Map value (TermMapValue) instance
 * 
 * TODO: This class seems totally useless and should be removed if not needed
 */
public class TermMap {
	
	private TermMapValue termMapValue;
	private Property predicate;
	
	
	public TermMap(String predicateStr) {
		this.predicate = ResourceFactory.createProperty(predicateStr);
	}

	
	public Property getPredicate() {
		return predicate;
	}

	
	public void setTermMapValue(TermMapValue termMapValue) {
		this.termMapValue = termMapValue;
	}
	
	
	public TermMapValue getTermMapValue() {
		return termMapValue;
	}

}
