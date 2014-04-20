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
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
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
public class VocabularyPropertyCompleteness extends MetricImpl implements
		DatasetMetric {

	private VocabularyLoader vocabLoader;
	
	
	public VocabularyPropertyCompleteness() {
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
		Set<Node> datasetProperties = getProperties(dataset);
		Set<Node> vocabularyProperties = getProperties(vocabulary);
		Set<Node> vocabNSProperties = new HashSet<Node>();
		
		// filter classes found, that are from a 'foreign' namespace
		for (Node prop : vocabularyProperties) {
			if (prop.isURI() && prop.getURI().startsWith(vocabUri)) {
				vocabNSProperties.add(prop);
			}
		}
		
		int numVProperties = vocabNSProperties.size();
		int numUsedVProperties = 0;
		
		if (numVProperties > 0) {
			for (Node prop : datasetProperties) {
				if (vocabNSProperties.contains(prop)) {
					numUsedVProperties++;
				}
			}
			
			float val = numUsedVProperties / (float) numVProperties;
			if (threshold == 0 || val < threshold) {
				writeNodeMeasureToSink(val, NodeFactory.createURI(vocabUri));
			}
		}
	}
	
	/**
	 * @param dataset
	 * @return
	 */
	private Set<Node> getProperties(Model dataset) {
		
		if (dataset instanceof SparqlifyDataset) {
			dataset = (SparqlifyDataset) dataset;
		}
		
		Set<Node> properties = new HashSet<Node>();
		
		// ont model approach
		OntModel ontDataset = 
				ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF, dataset);
		ExtendedIterator<OntProperty> ontPropIt = ontDataset.listAllOntProperties();
		
		while (ontPropIt.hasNext()) {
			OntProperty prop = ontPropIt.next();
			properties.add(prop.asNode());
		}
		
		// explicit query approach
		String queryStr =
				"SELECT distinct ?p  {" +
					"?s ?p ?o " +
				"}";
		Query query = QueryFactory.create(queryStr);
		
		QueryExecution qe;
		if (dataset instanceof SparqlifyDataset
				&& ((SparqlifyDataset) dataset).isSparqlService()
				&& ((SparqlifyDataset) dataset).getSparqlServiceUri() != null) {
			String serviceUri = ((SparqlifyDataset) dataset).getSparqlServiceUri();
			qe = QueryExecutionFactory.sparqlService(serviceUri, query);
		} else {
			qe = QueryExecutionFactory.create(query, dataset);
		}
		
		ResultSet res = qe.execSelect();
		
		while(res.hasNext()) {
			QuerySolution solution = res.nextSolution();
			RDFNode solNode = solution.get("p");
			properties.add(solNode.asNode());
		}
		qe.close();
		
		return properties;
	}
}
