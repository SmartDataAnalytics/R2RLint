package org.aksw.sparqlify.qa.metrics.reprconsistency;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * This metric should find a measure of the degree to which established terms
 * are re-used.
 * Since it is hard to find out if a given term is established, there is an
 * assumption made here, to make things easier:
 * If an external URI is used to express something it is assumed, that this
 * URI does exist and so is re-used. This weaker approach also assumes, that
 * such re-used resources/properties must in some way be established, since the
 * respective author preferred re-using it instead of creating an own one.
 * 
 * So the measure of term re-use would be the ratio of external
 * resources to all resources.
 * 
 * This measure is hard to compare amongst different datasets since it also
 * depends on the TBox-ABox ratio: A dataset with a lot of ABox triples will
 * at least use local URIs to describe the individuals.
 * 
 * Just referring to the TBox does not reflect the actual rate of use of
 * defined properties and classes. Another issue here is to detect the TBox
 * statements. In case of a poor ontology terms may be used but not declared
 * and so would also not be measured.
 * 
 * Another question is whether using a distinct or total count: Since this
 * metric refers to how often terms are re-used in relation to local resources
 * the total count is used. Otherwise the measure would too much depend on the
 * ABox size.
 * 
 * Blank nodes are not considered a re-used nor a local resorce
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
public class TermReUse extends MetricImpl implements DatasetMetric {
	
	long numExternalResources;
	long numLocalResources;

	public TermReUse() {
		numExternalResources = 0;
		numLocalResources = 0;
	}


	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException {
		
		StmtIterator statementsIt = dataset.listStatements(null, null, (RDFNode) null);
		
		while (statementsIt.hasNext()) {
			Statement statement = statementsIt.next();
			
			/* subject */
			Resource subject = statement.getSubject();
			if (subject.isURIResource()) {
				if (subject.getURI().startsWith(dataset.getPrefix()))
					numLocalResources++;
				else numExternalResources++;
			}
					
			
			/* predicate */
			Property predicate = statement.getPredicate();
			if (predicate.getURI().startsWith(dataset.getPrefix())) numLocalResources++;
			else numExternalResources++;
			
			/* object */
			RDFNode object = statement.getObject();
			if (object.isURIResource()) {
				if (object.asResource().getURI().startsWith(dataset.getPrefix()))
					numLocalResources++;
				else numExternalResources++;
			}
		}
		
		float ratio = numExternalResources / (float) (numExternalResources + numLocalResources);
		if (threshold == 0 || ratio <= threshold) {
			writeDatasetMeasureToSink(ratio);
		}
	}

}
