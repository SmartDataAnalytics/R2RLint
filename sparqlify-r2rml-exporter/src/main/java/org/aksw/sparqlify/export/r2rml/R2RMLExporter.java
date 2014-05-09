package org.aksw.sparqlify.export.r2rml;

import java.util.Collection;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class R2RMLExporter {

	Collection<ViewDefinition> viewDefs;

	ViewExporter viewExporter;

	
	/**
	 * @param viewDefs
	 *            a Collection<ViewDefinition> containing view definition data
	 */
	public R2RMLExporter(final Collection<ViewDefinition> viewDefs) {
		this.viewExporter = new ViewExporter();
		this.viewDefs = viewDefs;

	}

	
	/**
	 * The actual export method returning an RDF model
	 * 
	 * @return a com.hp.hpl.jena.rdf.model.Model representing the R2RML
	 *         structure
	 * @throws ResultNumberMismatchError 
	 */
	public Model export() throws ResultNumberMismatchError {
		Model r2rml = ModelFactory.createDefaultModel();

		for (ViewDefinition viewDef : viewDefs) {
			viewExporter.export(viewDef, r2rml);
		}
		return r2rml;
	}

}
