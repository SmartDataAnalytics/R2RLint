package org.aksw.sparqlify.qa.metrics.consistency;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.main.VocabularyLoader;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Ontology hijacking refers to cases where external ontologigal concepts are
 * re-defined in a local ontology. So if one states that
 * rdfs:subPropertyOf rdf:type rdfs:Class she/he hijacks the RDFS vocabulary.
 * 
 * Since it is hard to define, what ontological or at least vocabulary
 * properties are, this metric is based on the following assumptions:
 * - In a dataset having a local URI prefix all statements having an external
 *   resource as subject are expressing sth. that refers to external resources
 *   and is considered as a more general kind of resource hijacking. Such
 *   resource hijacking cases are
 *   - a violation if
 *     - the hijacked vocabulary is known and can be looked up via a
 *       VocabularyLoader instance *and*
 *     - the hijacking statement differs from the statement made in the original
 *       vocabulary
 *   - bad smell if
 *     - the hijacked vocabulary is not known and cannot be looked up via a
 *       VocabularyLoader instance
 * 
 * Further assumptions:
 * - the vocabularies used in the dataset are known and retrievable via
 *   dataset.getUsedPrefixes()
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class NoOntologyHijacking extends MetricImpl implements DatasetMetric {

	@Autowired
	private Pinpointer pinpointer;
	
	private final float badSmellValue = (float) 0.5;
	private final float errorValue = 0;
	
	// FIXME: provide the loader via autowiring or something else to avoid
	// multiple instantiations
	private VocabularyLoader vocabLoader;
	
	public NoOntologyHijacking() {
		super();
		vocabLoader = new VocabularyLoader();
	}
	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {
		
		Model vocabularies = null;
		Collection<String> usedPrefixes = dataset.getUsedPrefixes();
		try {
			vocabularies = vocabLoader.getVocabularies(usedPrefixes);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// iterate over all triples of the dataset
		for (Triple triple : dataset) {
			Node subject = triple.getSubject();
			Node predicate = triple.getPredicate();
			Node object = triple.getObject();
			
			if (subject.isURI()) {
				boolean isLocal = false;
				String subjectUri = subject.getURI();
				for (String prefix : dataset.getPrefixes()) {
					if (subjectUri.startsWith(prefix)) {
						isLocal = true;
						break;
					}
				}
				
				if (!isLocal) {
					// statement is a hijacking candidate
					boolean subjectPrefixKnown = false;
					for (String extPrefix : usedPrefixes) {
						if (subjectUri.startsWith(extPrefix)) {
							subjectPrefixKnown = true;
							break;
						}
					}
					if (subjectPrefixKnown) {
						// build statement
						Resource subjRes = ResourceFactory.createResource(subject.getURI());
					
						Property predRes = ResourceFactory.createProperty(predicate.getURI());
					
						RDFNode objRes = convertObjectNodeToRes(object);
						
						if (!vocabularies.listStatements(subjRes, predRes, objRes).hasNext()) {
					
							// subject is from a known vocabulary but the statement
							// about this subject is not part of the corresponding
							// vocabulary definition --> violation
							Set<ViewQuad<ViewDefinition>> viewQuads =
									pinpointer.getViewCandidates(triple);
							writeTripleMeasureToSink(errorValue, triple, viewQuads);
						}
					} else {
						// non-local subject is from a vocabulary not known by the
						// VocabularyLoader instance --> it cannot be confirmed if
						// this is an ontology hijacking case or not --> bad smell
						Set<ViewQuad<ViewDefinition>> viewQuads =
								pinpointer.getViewCandidates(triple);
						writeTripleMeasureToSink(badSmellValue, triple, viewQuads);
					}
				}
			}
		}

	}
	
	private RDFNode convertObjectNodeToRes(Node object) {
		RDFNode objRes;
		
		// URI
		if (object.isURI()) {
			objRes = ResourceFactory.createResource(object.getURI());
			
		// literal
		} else if (object.isLiteral()) {
			// typed literal
			if (object.getLiteralDatatypeURI() != null
					&& !object.getLiteralDatatypeURI().isEmpty()) {
				objRes = ResourceFactory.createTypedLiteral(object.getLiteralLexicalForm(), object.getLiteralDatatype());
			} else {
				// plain literal with lang tag
				if (object.getLiteralLanguage() != null
						&& !object.getLiteralLanguage().isEmpty()) {
					objRes = ResourceFactory.createLangLiteral(object.getLiteralLexicalForm(), object.getLiteralLanguage());
				// plain literal without lang tag
				} else {
					objRes = ResourceFactory.createPlainLiteral(object.getLiteralLexicalForm());
				}
			}
		} else {
			// blank node
			objRes = ResourceFactory.createResource();
		}
		
		return objRes;
	}

}
