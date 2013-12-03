package org.aksw.sparqlify.qa.metrics.interlinking;

import java.sql.SQLException;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

@Component
public class ExternalSameAsLinks extends MetricImpl implements DatasetMetric {

	private final String owl = "http://www.w3.org/2002/07/owl#";
	private final Property owl_sameAs = ResourceFactory.createProperty(owl + "sameAs");


	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {
		
		StmtIterator sameAsIt = dataset.listStatements(null, owl_sameAs, (RDFNode) null);
		
		int sameAsCount = 0;
		while (sameAsIt.hasNext()) {
			Statement statement = sameAsIt.next();
			String subjectUri = statement.getSubject().getURI();
			
			RDFNode object = statement.getObject();
			if (!object.isURIResource()) continue;
			String objectUri = object.asResource().getURI();
			
			String prfx = dataset.getPrefix();
			// if <local> owl:sameAs <external> or <external> owl:sameAs <local>
			if ((subjectUri.startsWith(prfx) && !objectUri.startsWith(prfx))
					||(!subjectUri.startsWith(prfx) && objectUri.startsWith(prfx))) {
				sameAsCount ++;
			}
		}
		long wholeCount = dataset.size();
		float ratio;
		if (wholeCount == 0) ratio = 0;
		else ratio = sameAsCount / (float) wholeCount;
		writeDatasetMeasureToSink(ratio);
	}

}
