package org.aksw.sparqlify.qa.metrics.completeness;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.main.VocabularyLoader;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

@Component
public class VocabularyClassCompleteness extends MetricImpl implements DatasetMetric {

	private VocabularyLoader vocabLoader;
	
	public VocabularyClassCompleteness() {
		super();
		
		vocabLoader = new VocabularyLoader();
	}
	
	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {

		for (String prefix : dataset.getUsedPrefixes()) {
			Model vocabulary;
			try {
				vocabulary = vocabLoader.getVocabulary(prefix);
				assessVocabClassCompleteness(vocabulary, prefix, dataset);
			} catch (FileNotFoundException e) {
				// TODO: log error
				continue;
			}
		}
	}
	
	private void assessVocabClassCompleteness(Model vocabulary, String prefix,
			SparqlifyDataset dataset) throws NotImplementedException, SQLException {
		
		String vocabUri;
		if (prefix.startsWith("http")) {
			vocabUri = prefix;
		} else {
			vocabUri = vocabulary.getNsPrefixURI(prefix);
		}
		Set<Node> datasetClasses = getClasses(dataset);
		
		Set<Node> vocabularyClasses = getClasses(vocabulary);
		Set<Node> vocabNSClasses = new HashSet<Node>();
		
		// filter classes found, that are from a 'foreign' namespace
		for (Node cls : vocabularyClasses) {
			if (cls.isURI() && cls.getURI().startsWith(vocabUri)) {
				vocabNSClasses.add(cls);
			}
		}
		
		int numVClasses = vocabNSClasses.size();
		int numUsedVClasses = 0;
		
		if (numVClasses > 0) {
			for (Node cls : datasetClasses) {
				if (vocabNSClasses.contains(cls)) {
					numUsedVClasses++;
				}
			}
			
			float val = numUsedVClasses / (float) numVClasses;
			if (threshold == 0 || val < threshold) {
				writeNodeMeasureToSink(val, NodeFactory.createURI(vocabUri));
			}
		}
	}
	
	/**
	 * @param dataset
	 * @return
	 */
	private Set<Node> getClasses(Model dataset) {
		
		if (dataset instanceof SparqlifyDataset) {
			dataset = (SparqlifyDataset) dataset;
		}
		
		Set<Node> classes = new HashSet<Node>();
		
		// ont model approach
		OntModel ontDataset = 
				ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF, dataset);
		ExtendedIterator<OntClass> ontClassesIt = ontDataset.listClasses();
		
		while (ontClassesIt.hasNext()) {
			OntClass cls = ontClassesIt.next();
			classes.add(cls.asNode());
		}
		
		// explicit query approach
		String queryStr =
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "SELECT distinct ?cls  {"
					+ "{?cls a rdfs:Class} "
					+ "UNION {?cls a owl:Class} "
					+ "UNION {[] a ?cls} }";
		Query query = QueryFactory.create(queryStr);
		
		QueryExecution qe;
		if (dataset instanceof SparqlifyDataset
				&& ((SparqlifyDataset) dataset).isSparqlService()
				&& ((SparqlifyDataset) dataset).getSparqlServiceUri() != null) {
			String serviceUri = ((SparqlifyDataset) dataset).getSparqlServiceUri();
			qe = QueryExecutionFactory.createServiceRequest(serviceUri, query);
		} else {
			qe = QueryExecutionFactory.create(query, dataset);
		}
		
		ResultSet res = qe.execSelect();
		
		while(res.hasNext()) {
			QuerySolution solution = res.nextSolution();
			RDFNode solNode = solution.get("cls");
			classes.add(solNode.asNode());
		}
		qe.close();
		
		return classes;
	}
}