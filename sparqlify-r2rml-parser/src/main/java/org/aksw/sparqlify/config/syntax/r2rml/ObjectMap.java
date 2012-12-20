/**
 * 
 */
package org.aksw.sparqlify.config.syntax.r2rml;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

enum TermMapSpec {
	COLUMN, CONSTANT, TEMPLATE, JOIN
}

public class ObjectMap {
	private Model model;
	private Resource subject;

	/**
	 * @param model
	 * @param subject
	 * @author sherif
	 */
	public ObjectMap(Model model, Resource subject) {
		super();
		this.model = model;
		this.subject = subject;
	}


	/**
	 * It is only valid to have exactly one term type associated with a term
	 * map. Return the valid one, or throw an exception otherwise
	 * 
	 * @return
	 * @author sherif
	 */
	public TermMapSpec getTermSpec() {
		EnumSet<TermMapSpec> set = getTermSpecs();

		if (set.isEmpty()) {
			throw new RuntimeException("No term type found");
		} else if (set.size() > 1) {
			throw new RuntimeException("Multiple term types found: " + set);
		}

		TermMapSpec result = set.iterator().next();

		return result;
	}

	public EnumSet<TermMapSpec> getTermSpecs() {
		EnumSet<TermMapSpec> result = EnumSet.noneOf(TermMapSpec.class);

		if (this.containColumn()) {
			result.add(TermMapSpec.COLUMN);
		}
		else if (!this.containConstant()) {
			result.add(TermMapSpec.CONSTANT);
		}
		else if (!this.containTemplate()) {
			result.add(TermMapSpec.TEMPLATE);
		}
		else if (!this.containJoinCondition()) {
			result.add(TermMapSpec.JOIN);
		}
		else{
			return null;
		}

		return result;
	}

	public String getColumnName() {
		Set<RDFNode> objects = model.listObjectsOfProperty(subject, RR.column)
				.toSet();

		if (objects.isEmpty()) {
			return null;
		}

		RDFNode node = RRUtils.getFirst(objects);

		String result = "" + node.asNode().getLiteralValue();

		return result;
	}
	
	public boolean containColumn() {
		Set<RDFNode> objects = model.listObjectsOfProperty(subject, RR.column).toSet();

		if (objects.isEmpty()) {
			return false;
		}
		return true;
	}

	public String getConstant() {
		Set<RDFNode> objects = model.listObjectsOfProperty(subject, RR.constant).toSet();

		if (objects.isEmpty()) {
			return null;
		}

		RDFNode node = RRUtils.getFirst(objects);

		String result = "" + node.asNode().getLiteralValue();

		return result;
	}
	
	public boolean containConstant() {
		Set<RDFNode> objects = model.listObjectsOfProperty(subject, RR.constant).toSet();

		if (objects.isEmpty()) {
			return false;
		}
		return true;
	}
	
	public String getTemplate() {
		Set<RDFNode> objects = model.listObjectsOfProperty(subject, RR.template).toSet();
		
		if(objects.isEmpty()) {
			return null;
		}
		
		RDFNode node = RRUtils.getFirst(objects);
		
		String result = "" + node.asNode().getLiteralValue();
		
		return result;
	}
	
	public boolean containTemplate() {
		Set<RDFNode> objects = model.listObjectsOfProperty(subject, RR.template).toSet();

		if (objects.isEmpty()) {
			return false;
		}
		return true;
	}
	
	RDFNode getDatatype() {
		Set<RDFNode> objects = model
				.listObjectsOfProperty(subject, RR.datatype).toSet();

		if (objects.isEmpty()) {
			return null;
		}

		RDFNode node = RRUtils.getFirst(objects);
		return node;
	}

	RDFNode getLanguageTag() {
		Set<RDFNode> objects = model
				.listObjectsOfProperty(subject, RR.language).toSet();

		if (objects.isEmpty()) {
			return null;
		}

		RDFNode node = RRUtils.getFirst(objects);
		return node;
	}

	RDFNode getTermType() {
		Set<RDFNode> objects = model
				.listObjectsOfProperty(subject, RR.language).toSet();

		if (objects.isEmpty()) {
			return null;
		}

		RDFNode node = RRUtils.getFirst(objects);
		return node;
	}
	
	public Set<JoinCondition> getJoinCondition() {

		Set<JoinCondition> result = new HashSet<JoinCondition>();
		Set<RDFNode> objects = model.listObjectsOfProperty(subject,RR.joinCondition).toSet();
		
		if(objects.isEmpty()){
			return null;
		}
		
		for (RDFNode object : objects) {
			Resource r = (Resource) object;
			JoinCondition item = new JoinCondition(model, r);
			result.add(item);
		}
		return result;
	}
	
	public boolean containJoinCondition() {
		Set<RDFNode> objects = model.listObjectsOfProperty(subject, RR.joinCondition).toSet();

		if (objects.isEmpty()) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "" + subject;
	}

	/*
	 * (non-Javadoc)
	 * 
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

	/*
	 * (non-Javadoc)
	 * 
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
	 * @param model
	 *            the model to set
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
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(Resource subject) {
		this.subject = subject;
	}

}