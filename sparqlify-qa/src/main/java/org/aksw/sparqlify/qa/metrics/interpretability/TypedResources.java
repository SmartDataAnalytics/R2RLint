package org.aksw.sparqlify.qa.metrics.interpretability;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * This metric should find all resources that do not have a class assigned.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class TypedResources extends MetricImpl implements DatasetMetric {
	
	private static Logger logger = LoggerFactory.getLogger(TypedResources.class);

	private Set<Node> knownResources;
	
	private List<Node> types = new ArrayList<Node>(Arrays.asList(
			RDF.Property.asNode(), RDF.Statement.asNode(), RDF.Alt.asNode(),
			RDF.Bag.asNode(), RDF.Seq.asNode(), RDF.List.asNode(),
			RDFS.Resource.asNode(), RDFS.Class.asNode(),
			RDFS.Container.asNode(), RDFS.ContainerMembershipProperty.asNode(),
			OWL.AnnotationProperty.asNode(), OWL.Class.asNode(),
			OWL.DatatypeProperty.asNode(), OWL.DeprecatedClass.asNode(),
			OWL.DeprecatedProperty.asNode(), OWL.FunctionalProperty.asNode(),
			OWL.FunctionalProperty.asNode(), OWL.ObjectProperty.asNode(),
			OWL.Ontology.asNode(), OWL.OntologyProperty.asNode(),
			OWL.Restriction.asNode(), OWL.SymmetricProperty.asNode(),
			OWL.TransitiveProperty.asNode()));


	protected void clearCaches() {
		knownResources = new HashSet<Node>();
	}


	public TypedResources() {
		super();
		knownResources = new HashSet<Node>();
	}

	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {

		logger.debug("starting triple iteration...");
		int dbgTrplCnt = 0;
		
		for (Triple triple : dataset) {
			dbgTrplCnt++;
			if (dbgTrplCnt % 10000 == 0) {
				logger.debug(dbgTrplCnt + " triples assessed");
			}
			
			Node subject = triple.getSubject();
			Node object = triple.getObject();
			
			if (!knownResources.contains(subject)) {
				if (subject.isURI()) {
					// in case of an URI resource, only assess if local
					boolean isLocal = false;
					
					String uri = ((Node_URI) subject).getURI();
					for (String prefix : dataset.getPrefixes()) {
						if (uri.startsWith(prefix)){
							isLocal = true;
							break;
						}
					}
					
					if (isLocal) {
						checkIfTyped(subject, dataset);
					}
				
				} else {
					// blank node case
					checkIfTyped(subject, dataset);
				}
			}
			
			if (!object.isLiteral() && !knownResources.contains(object)) {
				if (object.isURI()) {
					// in case of an URI resource, only assess if local
					boolean isLocal = false;
					String uri = ((Node_URI) object).getURI();
					for (String prefix : dataset.getPrefixes()) {
						if (uri.startsWith(prefix)) {
							isLocal = true;
							break;
						}
					}
					
					if (isLocal) {
						checkIfTyped(object, dataset);
					}
				} else {
					// blank node case
					checkIfTyped(object, dataset);
				}
			}
				
		}
		logger.debug("finished triple iteration.");
	}
	
	private void checkIfTyped(Node node, SparqlifyDataset dataset)
			throws SQLException {
		
		boolean resourceTyped = false;
		
		knownResources.add(node);
		
		// res is a class itself
		if (types.contains(node)) {
			resourceTyped = true;
		}
		
		
		if (!resourceTyped) {
			// <res> rdf:type <sth>
			String typedQueryStr;
			if (node.isURI()) {
				typedQueryStr = "ASK { <" + node.getURI() + "> a ?type }";
			} else if (node.isBlank()) {
				// TODO: maybe this doesn't make sense...
				typedQueryStr = "ASK { ?blank a ?type . FILTER (isBlank(?blank)) }";
			} else {
				typedQueryStr = "ASK { <#foo> <#foo> <#foo> }";
			}
			Query typedQuery = QueryFactory.create(typedQueryStr);
			
			QueryExecution typedQe;
			if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
				typedQe = QueryExecutionFactory.createServiceRequest(
						dataset.getSparqlServiceUri(), typedQuery);
			} else {
				typedQe = QueryExecutionFactory.create(typedQuery, dataset);
			}
			
			resourceTyped = typedQe.execAsk();
			
			typedQe.close();
		}
		
		if (!resourceTyped) {
			// <res> rdfs:subClassOf <sth>
			
			String subClassQueryStr;
			if (node.isURI()) {
				subClassQueryStr = "ASK { <" + node.getURI() + "> <" + RDFS.subClassOf.getURI() + "> ?type }";
			} else if (node.isBlank()) {
				// TODO: maybe this doesn't make sense...
				subClassQueryStr = "ASK { ?blank <" + RDFS.subClassOf.getURI() + "> ?type . FILTER (isBlank(?blank)) }";
			} else {
				subClassQueryStr = "ASK { <#foo> <#foo> <#foo> }";
			}
			Query subClassQuery = QueryFactory.create(subClassQueryStr);
			
			QueryExecution subClassQe;
			if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
				subClassQe = QueryExecutionFactory.createServiceRequest(
						dataset.getSparqlServiceUri(), subClassQuery);
			} else {
				subClassQe = QueryExecutionFactory.create(subClassQuery, dataset);
			}
			
			resourceTyped = subClassQe.execAsk();
			subClassQe.close();
		}
		
		if (!resourceTyped) {
			// <res> rdfs:subPropertyOf <sth>
			String subPropQueryStr;
			if (node.isURI()) {
				subPropQueryStr = "ASK { <" + node.getURI() + "> <" + RDFS.subPropertyOf.getURI() + "> ?type }";
			} else if (node.isBlank()) {
				// TODO: maybe this doesn't make sense...
				subPropQueryStr = "ASK { ?blank <" + RDFS.subPropertyOf.getURI() + "> ?type . FILTER (isBlank(?blank)) }";
			} else {
				subPropQueryStr = "ASK { <#foo> <#foo> <#foo> }";
			}
			Query subPropQuery = QueryFactory.create(subPropQueryStr);
			
			QueryExecution subPropQe;
			if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
				subPropQe = QueryExecutionFactory.createServiceRequest(
						dataset.getSparqlServiceUri(), subPropQuery);
			} else {
				subPropQe = QueryExecutionFactory.create(subPropQuery, dataset);
			}
			
			resourceTyped = subPropQe.execAsk();
			subPropQe.close();
		}
		
		if (!resourceTyped) {
			// <res> owl:equivalentClass <sth>
			String eqClsQueryStr;
			if (node.isURI()) {
				eqClsQueryStr = "ASK { <" + node.getURI() + "> <" + OWL.equivalentClass.getURI() + "> ?type }";
			} else if (node.isBlank()) {
				// TODO: maybe this doesn't make sense...
				eqClsQueryStr = "ASK { ?blank <" + OWL.equivalentClass.getURI() + "> ?type . FILTER (isBlank(?blank)) }";
			} else {
				eqClsQueryStr = "ASK { <#foo> <#foo> <#foo> }";
			}
			Query eqClsQuery = QueryFactory.create(eqClsQueryStr);
			
			QueryExecution eqClsQe;
			if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
				eqClsQe = QueryExecutionFactory.createServiceRequest(
						dataset.getSparqlServiceUri(), eqClsQuery);
			} else {
				eqClsQe = QueryExecutionFactory.create(eqClsQuery, dataset);
			}
			
			resourceTyped = eqClsQe.execAsk();
			eqClsQe.close();
		}
		
		if (!resourceTyped) {
			// <res> owl:equivalentProperty <sth>
			String eqPropQueryStr;
			if (node.isURI()) {
				eqPropQueryStr = "ASK { <" + node.getURI() + "> <" + OWL.equivalentProperty.getURI() + "> ?type }";
			} else if (node.isBlank()) {
				// TODO: maybe this doesn't make sense...
				eqPropQueryStr = "ASK { ?blank <" + OWL.equivalentProperty.getURI() + "> ?type . FILTER (isBlank(?blank)) }";
			} else {
				eqPropQueryStr = "ASK { <#foo> <#foo> <#foo> }";
			}
			Query eqPropQuery = QueryFactory.create(eqPropQueryStr);
			
			QueryExecution eqPropQe;
			if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
				eqPropQe = QueryExecutionFactory.createServiceRequest(
						dataset.getSparqlServiceUri(), eqPropQuery);
			} else {
				eqPropQe = QueryExecutionFactory.create(eqPropQuery, dataset);
			}
			
			resourceTyped = eqPropQe.execAsk();
			eqPropQe.close();
		}

		if (!resourceTyped) {
			writeNodeMeasureToSink(0, node);
		}
	}

}
