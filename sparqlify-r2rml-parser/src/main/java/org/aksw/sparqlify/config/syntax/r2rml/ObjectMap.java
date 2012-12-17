/**
 * 
 */
package org.aksw.sparqlify.config.syntax.r2rml;

import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class ObjectMap {
	private Model model;
	private Resource subject;
	
	/**
	 * @param model
	 * @param subject
	 *@author sherif
	 */
	public ObjectMap(Model model, Resource subject) {
		super();
		this.model = model;
		this.subject = subject;
	}
	
	public String getColumnName() {
	Set<RDFNode> objects = model.listObjectsOfProperty(subject, RR.column).toSet();
	
	if(objects.isEmpty()) {
		return null;
	}
	
	RDFNode node = RRUtils.getFirst(objects);
	
	String result = "" + node.asNode().getLiteralValue();
	
	return result;
	
	//Resource resource = RRUtils.getResourceFromSet(objects);
}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "" + subject;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ObjectMap other = (ObjectMap) obj;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject))
			return false;
		return true;
	}
	/**
	 * @return the model
	 */
	public Model getModel() {
		return model;
	}
	/**
	 * @param model the model to set
	 */
	public void setModel(Model model) {
		this.model = model;
	}
	/**
	 * @return the subject
	 */
	public Resource getSubject() {
		return subject;
	}
	/**
	 * @param subject the subject to set
	 */
	public void setSubject(Resource subject) {
		this.subject = subject;
	}

}