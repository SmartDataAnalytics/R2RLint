package org.aksw.sml_eval.adapters;

/**
 * Perform a RDB-RDF mapping based on the given string.
 * 
 * @author raven
 *
 */
public interface Adapter {
	MapResult map(String mapping);
}
