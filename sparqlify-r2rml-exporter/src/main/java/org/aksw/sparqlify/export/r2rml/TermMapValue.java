package org.aksw.sparqlify.export.r2rml;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.core.Var;


/**
 * A Term Map value is the part of a Term Map that contains all information
 * about the actual value, so
 * - the rr:template/rr:column/rr:constant value
 * - the language tag (rr:language) if one exists
 * - the datatype (rr:datatype) if specified
 * - the term type (rr:termType) if given
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
public class TermMapValue {
	private RDFNode value;
	private Property mappingProperty;
	private Resource termType;
	private Literal langTag;
	private Resource datatype;
	private Var colRef;
	
	
	public TermMapValue(String mappingPropertyStr, String valueStr) {
		mappingProperty = ResourceFactory.createProperty(mappingPropertyStr);
		value = ResourceFactory.createPlainLiteral(valueStr);
	}
	
	
	public TermMapValue(String mappingPropertyNamespace,
			String mappingPropertyStr, String valueStr) {
		
		mappingProperty = ResourceFactory.createProperty(
								mappingPropertyNamespace, mappingPropertyStr);
		
		value = ResourceFactory.createPlainLiteral(valueStr);
	}
	
	
	public TermMapValue(String mappingPropertyStr, Node resource) {
		mappingProperty = ResourceFactory.createProperty(mappingPropertyStr);
		// FIXME: this looks goofy
		value = ResourceFactory.createResource(resource.getURI());
	}
	
	
	public TermMapValue(String mappingPropertyNamespace,
			String mappingPropertyStr, Node resource) {
		
		mappingProperty = ResourceFactory.createProperty(
								mappingPropertyNamespace, mappingPropertyStr);
		value = (RDFNode) resource;
	}
	
	
	public TermMapValue(String mappingPropertyStr) {
		mappingProperty = ResourceFactory.createProperty(mappingPropertyStr);
		value = ResourceFactory.createResource();
	}
		
	
	public Property getMappingProperty() { return mappingProperty; }
	
	
	public RDFNode getValue() { return value; }
	
	
	public void setTermType(String type) {
		termType = ResourceFactory.createResource(type);
	}
	
	
	public boolean hastTermType() {
		if (termType != null) return true;
		else return false;
	}
	
	
	public Resource getTermType() {
		return termType;
	}

	
	public void setLanguage(String languageTag) {
		langTag = ResourceFactory.createPlainLiteral(languageTag);
	}
	
	
	public boolean hasLanguage() {
		if (langTag != null) return true;
		else return false;
	}
	
	
	public Literal getLanguage() {
		return langTag;
	}
	
	
	public void setDatatype(String datatypeUri) {
		datatype = ResourceFactory.createResource(datatypeUri);
	}
	
	
	public boolean hasDatatype() {
		if (datatype != null) return true;
		else return false;
	}
	
	
	public Resource getDatatype() {
		return datatype;
	}
	
	
	public boolean hasColumnReference() {
		if (colRef != null) return true;
		else return false;
	}
	
	
	public void setColumnReference(Var colRef) {
		this.colRef = colRef;
	}
	
	
	public Var getColumnReference() {
		return colRef;
	}
}