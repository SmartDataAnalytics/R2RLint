package org.aksw.sparqlify.qa.metrics.understandability;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.aksw.sparqlify.qa.vocabularies.SIOC;
import org.aksw.sparqlify.qa.vocabularies.VoID;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * This metric checks if there are metadata about the given dataset. A dataset
 * is defined as a resource being typed as void:Dataset
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class DatasetMetadata extends MetricImpl implements DatasetMetric {
	
	float noDatasetStatementsValue = 0;
	float noSuggestedPropertiesUsedValue = (float) 0.5;
	
	List<Property> datasetTitleProperties = new ArrayList<Property>(
			Arrays.asList(DCTerms.alternative, DCTerms.title, DC.title,
					SIOC.name));
	
	List<Property> datasetContentProperties = new ArrayList<Property>(
			Arrays.asList(DCTerms.abstract_, DCTerms.accrualMethod,
					DCTerms.accrualPeriodicity, DCTerms.accrualPolicy,
					DCTerms.audience, DCTerms.available, DCTerms.coverage,
					DCTerms.description, DCTerms.language, DCTerms.provenance,
					DCTerms.source, DCTerms.spatial, DCTerms.subject,
					DCTerms.tableOfContents, DCTerms.type, DC.coverage,
					DC.description, DC.language, DC.source, DC.subject, DC.type,
					SIOC.about, SIOC.has_space, SIOC.topic, FOAF.primaryTopic,
					FOAF.topic, VoID.class_, VoID.classPartition, VoID.classes,
					VoID.dataDump, VoID.distinctObjects, VoID.distinctSubjects,
					VoID.documents, VoID.entities, VoID.exampleResource,
					VoID.feature, VoID.inDataset, VoID.linkPredicate,
					VoID.objectsTarget, VoID.openSearchDescription,
					VoID.properties, VoID.property, VoID.propertyPartition,
					VoID.rootResource, VoID.sparqlEndpoint, VoID.subjectsTarget,
					VoID.subset, VoID.target, VoID.triples,
					VoID.uriLookupEndpoint, VoID.uriRegexPattern, VoID.uriSpace,
					VoID.vocabulary));
	
	List<Property> datasetCreatorProperties = new ArrayList<Property>(
			Arrays.asList(DCTerms.contributor, DCTerms.creator,
					DCTerms.publisher, DC.contributor, DC.creator, DC.publisher,
					SIOC.has_creator, FOAF.maker ));
	
	
	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {

		// TODO: maybe use a SPARQL query here
		// get all defined datasets
		StmtIterator datasetIt = dataset.listStatements(null, RDF.type, VoID.Dataset);
		
		if (!datasetIt.hasNext() && (noDatasetStatementsValue >= threshold)) {
			// report a value of <noDatasetStatementsValue> since there are is
			// not even a dataset resource defined
			Node datasetNode;
			try {
				// create dummy resource representing the dataset
				datasetNode = NodeFactory.createURI(dataset.getPrefixes().get(0));
			} catch (JenaException e) {
				datasetNode = NodeFactory.createURI(dataset.getPrefixes() + "#");
			} catch (NullPointerException e) {
				// ...in case there are no local prefixes defined
				datasetNode = NodeFactory.createURI("#");
			}
				
			writeNodeMeasureToSink(noDatasetStatementsValue, datasetNode);
		}

		while (datasetIt.hasNext()) {
			// check for every dataset if any properties from the dataset
			// properties lists are used
			Resource datasetResource = datasetIt.next().getSubject();
			
			StmtIterator datasetStatementsIt = dataset.listStatements(
					datasetResource, null, (RDFNode) null);
			
			boolean suggestedPropertiesUsed = false;
			boolean onlyRdfTypeVoidDatasetStatement = true;
			while (datasetStatementsIt.hasNext()) {
				Statement datasetStatement = datasetStatementsIt.next();
				Property pred = datasetStatement.getPredicate();
				
				if (!pred.equals(RDF.type))
					// the is not just the <..> rdf:type void:Dataset statement
					onlyRdfTypeVoidDatasetStatement = false;
				
				if (datasetTitleProperties.contains(pred)
						|| datasetContentProperties.contains(pred)
						|| datasetCreatorProperties.contains(pred)) {
					suggestedPropertiesUsed = true;
				}
			}
			if (!suggestedPropertiesUsed) {
				
				if (onlyRdfTypeVoidDatasetStatement
						&& (noDatasetStatementsValue >= threshold))
					// report a value of <noDatasetStatementsValue> since there
					// are no statements (other than rdf:type void:Dataset)
					// made about the given dataset
					writeNodeMeasureToSink(noDatasetStatementsValue,
							datasetResource.asNode());
				
				else if (noSuggestedPropertiesUsedValue >= threshold)
					// report a value of <noSuggestedPropertiesUsedValue> since
					// there are statements made but not with the dataset
					// properties suggested above
					writeNodeMeasureToSink(noSuggestedPropertiesUsedValue,
							datasetResource.asNode());
			}
		}
	}


	// mainly for testing purposes
	public void setNoDatasetStatementsValue(float value) {
		noDatasetStatementsValue = value;
	}
	public void setNoSuggestedPropertiesUsedValue(float value) {
		noSuggestedPropertiesUsedValue = value;
	}

}
