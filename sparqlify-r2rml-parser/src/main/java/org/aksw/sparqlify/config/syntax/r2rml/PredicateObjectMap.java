/**
 * 
 */
package org.aksw.sparqlify.config.syntax.r2rml;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author sherif
 * 
 */
public class PredicateObjectMap {
	private Model model;
	private Resource subject;

	/**
	 * @param model
	 * @param subject
	 * @author sherif
	 */
	public PredicateObjectMap(Model model, Resource subject) {
		super();
		this.model = model;
		this.subject = subject;
	}

	public RDFNode getPredicate() {
		Set<RDFNode> objects = model.listObjectsOfProperty(subject,
				RR.predicate).toSet();

		if (objects.isEmpty()) {
			return null;
		}

		RDFNode node = RRUtils.getFirst(objects);

		return node;
	}

	public Set<ObjectMap> getObjectMap() {

		Set<ObjectMap> result = new HashSet<ObjectMap>();

		// list all predicate object maps
		Set<RDFNode> objects = model.listObjectsOfProperty(subject,
				RR.objectMap).toSet();

		for (RDFNode object : objects) {
			Resource r = (Resource) object;

			ObjectMap item = new ObjectMap(model, r);

			result.add(item);
		}
		return result;
	}

	public String getDataType() {
		Set<RDFNode> objects = model
				.listObjectsOfProperty(subject, RR.datatype).toSet();

		if (objects.isEmpty()) {
			return null;
		}

		RDFNode node = RRUtils.getFirst(objects);

		String result = "" + node.asNode().getLiteralValue();

		return result;

	}

	/**
	 * @return the model
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * @return the subject
	 */
	public Resource getSubject() {
		return subject;
	}

}
