package org.aksw.sparqlify.qa.metrics.consistency;

import java.io.FileNotFoundException;
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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

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
			throws NotImplementedException {
		
		Model vocabularies = null;
		Collection<String> usedPrefixes = dataset.getUsedPrefixes();
		try {
			vocabularies = vocabLoader.getVocabularies(usedPrefixes);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		String prefix;
		if (dataset.getPrefix() != null) prefix = dataset.getPrefix();
		else prefix = "";
		
		// iterate over all triples of the dataset
		StmtIterator stmntsIt = dataset.listStatements(null, null, (RDFNode) null);
		while (stmntsIt.hasNext()) {
			Statement statement = stmntsIt.next();
			Resource subject = statement.getSubject();
			
			if (!subject.getURI().startsWith(prefix)) {
				// statement is a hijacking candidate
				boolean subjectPrefixKnown = false;
				for (String extPrefix : usedPrefixes) {
					if (subject.getURI().startsWith(extPrefix)) {
						subjectPrefixKnown = true;
						break;
					}
				}
				if (subjectPrefixKnown) {
					if (!vocabularies.listStatements(subject,
							statement.getPredicate(), statement.getObject())
							.hasNext()) {
					
						// subject is from a known vocabulary but the statement
						// about this subject is not part of the corresponding
						// vocabulary definition --> violation
						Set<ViewQuad<ViewDefinition>> viewQuads =
								pinpointer.getViewCandidates(statement.asTriple());
						writeTripleMeasureToSink(errorValue,
								statement.asTriple(), viewQuads);
					}
					
				} else {
					// non-local subject is from a vocabulary not known by the
					// VocabularyLoader instance --> it cannot be confirmed if
					// this is an ontology hijacking case or not --> bad smell
					Set<ViewQuad<ViewDefinition>> viewQuads =
							pinpointer.getViewCandidates(statement.asTriple());
					writeTripleMeasureToSink(badSmellValue,
							statement.asTriple(), viewQuads);
				}
			}
		}

	}

}
