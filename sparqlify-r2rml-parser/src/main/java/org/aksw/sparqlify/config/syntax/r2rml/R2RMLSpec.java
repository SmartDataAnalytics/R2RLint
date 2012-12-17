/**
 * 
 */
package org.aksw.sparqlify.config.syntax.r2rml;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class R2RMLSpec {
	private Model model;

	/**
	 * @param model
	 *@author sherif
	 */
	public R2RMLSpec(Model model) {
		super();
		this.model = model;
	}
	
	public Set<TriplesMap> getTripleMaps() {
		Set<TriplesMap> result = new HashSet<TriplesMap>();
		
		//Property logicalTable = ResourceFactory.createProperty("http://www.w3.org/ns/r2rml#logicalTable");
		Set<Resource> tripleMaps = model.listSubjectsWithProperty(RR.logicalTable).toSet();
		
		for(Resource resource : tripleMaps) {
			TriplesMap item = new TriplesMap(model, resource);
			
			result.add(item);
		}
		
		return result;
	}
	
	
}