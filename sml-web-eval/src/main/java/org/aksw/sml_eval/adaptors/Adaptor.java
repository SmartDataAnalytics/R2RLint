package org.aksw.sml_eval.adaptors;

/**
 * Perform a RDB-RDF mapping based on the given string.
 * 
 * @author raven
 *
 */
public interface Adaptor {
	MapResult map(String mapping);
}
