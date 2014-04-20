package org.aksw.sparqlify.qa.metrics.interpretability;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.aksw.sparqlify.qa.sinks.TriplePosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * This metric checks if a given resource has a certain ontological context,
 * meaning that its role in an ontology is well defined.
 * Such an ontological context is assigned via ontology properties like
 * rdf:type, rdfs:subClassOf, owl:equivalentProperty, ... (see ontProperties
 * list)
 * 
 * This metric considers only local resources.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class OWLOntologyDeclarations extends MetricImpl implements
		DatasetMetric {
	
	private Logger logger = LoggerFactory.getLogger(OWLOntologyDeclarationsTest.class);

	@Autowired
	private Pinpointer pinpointer;
//	List<Resource> seenResources;
	List<Node> seenNodes;
	List<Property> ontDefProperties = new ArrayList<Property>(Arrays.asList(
			// rdf(s)
			RDF.type, RDFS.subClassOf, RDFS.subPropertyOf, RDFS.domain,
			RDFS.range,
			// owl
			OWL.complementOf, OWL.disjointWith, OWL.equivalentClass,
			OWL.equivalentProperty, OWL.intersectionOf, OWL.inverseOf,
			OWL.oneOf, OWL.unionOf
			));

	protected void clearCaches() {
		seenNodes = new ArrayList<Node>();
	}
	
	
	public OWLOntologyDeclarations() {
		super();
		seenNodes = new ArrayList<Node>();
	}

	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {
		
		int dbgCount = 0;
		for (Triple triple : dataset) {
			dbgCount++;
			if (dbgCount % 1000 == 0) {
				logger.debug(dbgCount + "tripples assessed for OWL Ontology Declarations");
			}
			Node subject = triple.getSubject();
			Node predicate = triple.getPredicate();
			Node object = triple.getObject();
			
			/* subject */
			if (!seenNodes.contains(subject) && subject.isURI()) {
				
				// only consider local resources
				for (String prefix : dataset.getPrefixes()) {
					if (subject.getURI().startsWith(prefix)) {
						// resource is local...
						checkResourceInterpretability(subject,
								TriplePosition.SUBJECT, triple, dataset);
						break;
					}
				}
				seenNodes.add(subject);
			}
			
			
			/* predicate */
			if (!seenNodes.contains(predicate)) {
				
				// only consider local resources
				for (String prefix : dataset.getPrefixes()) {
					if (predicate.getURI().startsWith(prefix)) {
						// resource is local...
						checkResourceInterpretability(predicate,
								TriplePosition.PREDICATE, triple, dataset);
						break;
					}
					
				}
				seenNodes.add(predicate);
			}
			
			
			/* object */
			if (object.isURI() && !seenNodes.contains(object)) {
				
				// only consider local resources
				for (String prefix : dataset.getPrefixes()) {
					if (object.getURI().startsWith(prefix)) {
						
						// resource is local...
						checkResourceInterpretability(object,
								TriplePosition.OBJECT, triple, dataset);
						break;
					}
				}
				seenNodes.add(object);
			}
		}
	}


	private void checkResourceInterpretability(Node node,
			TriplePosition pos, Triple triple, SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {
		
		boolean ontPropStatementFound = false;
		
		// check if resource is further described using one of the proposed
		// properties
		for (Property ontProp : ontDefProperties) {
			String queryStr = "ASK { <" + node.getURI() + "> <" + ontProp.getURI() + "> ?o }";
			
			Query query = QueryFactory.create(queryStr);
			
			QueryExecution qe;
			if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
				qe = QueryExecutionFactory.createServiceRequest(
						dataset.getSparqlServiceUri(), query);
			} else {
				qe = QueryExecutionFactory.create(query, dataset);
			}
			
			ontPropStatementFound = qe.execAsk();
			qe.close();
			if (ontPropStatementFound) break;
		}
		
		if (!ontPropStatementFound) {
			Set<ViewQuad<ViewDefinition>> viewQuads = pinpointer
					.getViewCandidates(triple.asTriple());
			
			writeNodeTripleMeasureToSink(0, pos, triple.asTriple(), viewQuads);
		}
	}
}
