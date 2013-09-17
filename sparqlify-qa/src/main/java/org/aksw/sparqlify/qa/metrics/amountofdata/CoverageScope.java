package org.aksw.sparqlify.qa.metrics.amountofdata;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * This metric should calculate the coverage of a dataset referring to the
 * covered scope. This covered scope is expressed as the number of 'instances'
 * statements are made about.
 * 'instance' is specified as follows: "The members of a class are known as
 * instances of the class." [0]
 * Another definition made there is, that "All things described by RDF are
 * called resources, and are instances of the class rdfs:Resource." [1]
 * So, as a consequence, everything is an instance and the scope, defined as
 * 
 *       number of instances
 *      ---------------------
 *        number of triples
 * 
 * would always be > 1.
 * To avoid this, a more restrictive definition of the term 'instance' would be
 * preferred here.
 * 
 * An artifact defined in the OWL specs, that is in some way similar to
 * an instance, is the term 'individual'. To be an individual a resource has to
 * have a type that is an owl:Class. Since in the most RDB2RDF real world
 * examples, I have seen, OWL wasn't used at all, calculating the scope based
 * on individuals wouldn't be very meaningful. In that sense, using the
 * individuals as instances would be too restrictive.
 * 
 * The definition of an instance used here is as follows:
 * 
 *     instance rdf:type someClass && someClass rdf:type rdfs:Class
 * 
 * i.e. an instance (as used here) is a resource, that is of a certain type that
 * is an rdfs:Class (or an owl:Class).
 * 
 * 
 * [0] http://www.w3.org/TR/rdf-schema/#ch_classes
 * [1] http://www.w3.org/TR/rdf-schema/#ch_resource
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
public class CoverageScope extends MetricImpl implements DatasetMetric {

	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException {

		long numTriples = dataset.size();
		long numInstances = 0;
		
		StmtIterator typeStatementsIt = dataset.listStatements(null, RDF.type,
				(RDFNode) null);
		
		while (typeStatementsIt.hasNext()) {
			Statement statement = typeStatementsIt.next();
			RDFNode object = statement.getObject();
			
			if (object.isURIResource() && isClass(object.asResource(), dataset))
				numInstances++;
		}
		
		float val;
		if (numTriples == 0) val = 0;
		else val = numInstances / (float) numTriples;

		writeDatasetMeasureToSink(val);
	}

	/**
	 * This method checks if a given resource is a class, i.e. if there is a
	 * statement 
	 *   resource rdf:type rdfs:Class
	 * or
	 *   resource rdf:type owl:Class
	 */
	private boolean isClass(Resource res, SparqlifyDataset dataset) {
		
		if (dataset.listStatements(res, RDF.type, RDFS.Class).hasNext()
				|| dataset.listStatements(res, RDF.type, OWL.Class).hasNext())
			return true;
		
		else return false;
	}

}
