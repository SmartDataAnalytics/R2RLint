package org.aksw.sparqlify.qa.metrics.consistency;

import java.sql.SQLException;
import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.aksw.sparqlify.qa.sinks.TriplePosition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.InvalidPropertyURIException;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;

@Component
public class NoDeprecatedClassesOrProperties extends MetricImpl implements
		DatasetMetric {
	
	@Autowired
	private Pinpointer pinpointer;
	
	// prefixes
	private final String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private final String owl = "http://www.w3.org/2002/07/owl#";
	// properties
	private final Property rdf_type = ResourceFactory.createProperty(rdf + "type");
	private final Property owl_equivalentClass = ResourceFactory
			.createProperty(owl + "equivalentClass");
	private final Property owl_equivalentProperty = ResourceFactory
			.createProperty(owl + "equivalentProperty");
	private final Property owl_deprecated = ResourceFactory.createProperty(owl
			+ "deprecated");
	private RDFNode trueLiteral = ResourceFactory.createTypedLiteral("true",
			XSDDatatype.XSDboolean);
	// classes
	private final Resource owl_DeprecatedClass = ResourceFactory
			.createResource(owl + "DeprecatedClass");
	private final Resource owl_DeprecatedProperty = ResourceFactory
			.createResource(owl + "DeprecatedProperty");

	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {

		OntModel ontModel = ModelFactory.createOntologyModel(
				OntModelSpec.OWL_LITE_MEM_TRANS_INF, dataset);
		
		/*
		 * OWL 2.0 deprecation property (owl:deprecated "true"^^xsd:boolean)
		 */
		StmtIterator owlDeprTrueIt = ontModel.listStatements(null,
				owl_deprecated, ResourceFactory.createTypedLiteral("true",
						XSDDatatype.XSDboolean));
		
		while (owlDeprTrueIt.hasNext()) {
			Statement statement = owlDeprTrueIt.next();
			Resource res = statement.getSubject();
			
			reportResource(res, dataset);
		}
			
		/*
		 * deprecated classes
		 */
		
		// <class> rdf:type owl:DeprecetedClass .
		StmtIterator rdfTypeDcIt = ontModel.listStatements((Resource) null,
				rdf_type, owl_DeprecatedClass);
		
		while (rdfTypeDcIt.hasNext()) {
			Statement statement = rdfTypeDcIt.next();
			Resource cls = statement.getSubject();
			
			reportClass(cls, dataset);
		}
		
		// owl:DeprecatedClass owl:equivalentClass <class>
		StmtIterator owlDcEqClsIt = ontModel.listStatements(
				owl_DeprecatedClass, owl_equivalentClass, (RDFNode) null);
		while (owlDcEqClsIt.hasNext()) {
			Statement statement = owlDcEqClsIt.next();
			RDFNode cls = statement.getObject();
			
			reportClass(cls, dataset);
		}
		
		// <class> owl:equivalentClass owl:DeprecatedClass
		StmtIterator owlEqClsDcIt = ontModel.listStatements(null,
				owl_equivalentClass, owl_DeprecatedClass);
		while (owlEqClsDcIt.hasNext()) {
			Statement statement = owlEqClsDcIt.next();
			Resource cls = statement.getSubject();
			
			reportClass(cls, dataset);
		}
		
		/*
		 * deprecated properties
		 */
		
		// <prop> rdf:type owl:DeprecatedProperty
		StmtIterator rdfTypeDpIt = ontModel.listStatements(null, rdf_type,
				owl_DeprecatedProperty);
		
		while (rdfTypeDpIt.hasNext()) {
			Statement statement = rdfTypeDpIt.next();
			Property prop = ResourceFactory.createProperty(statement
					.getSubject().getURI());
			
			reportProperty(prop, dataset);
		}
		
		// <prop> owl:equivalentProperty owl:DeprecatedProperty
		StmtIterator owlEqPropDpIt = ontModel.listStatements(null,
				owl_equivalentProperty, owl_DeprecatedProperty);
		
		while (owlEqPropDpIt.hasNext()) {
			Statement statement = owlEqPropDpIt.next();
			Property prop = ResourceFactory.createProperty(statement
					.getSubject().getURI());
			
			reportProperty(prop, dataset);
		}
		
		// owl:DeprecatedProperty owl:equivalentProperty <prop>
		StmtIterator owlDpEqPropIt = ontModel.listStatements(
				owl_DeprecatedProperty, owl_equivalentProperty, (RDFNode) null);
		
		while (owlDpEqPropIt.hasNext()) {
			Statement statement = owlDpEqPropIt.next();
			Property prop = ResourceFactory.createProperty(statement.getObject().asResource().getURI());
			
			reportProperty(prop, dataset);
		}
	}


	/**
	 * This method gets all statements that use a given deprecated property
	 * and reports statemens using it to the sink.
	 *  
	 * @param property
	 *         the deprecated property
	 * @param dataset
	 *         the Jena model to look for statements using the deprecated class
	 * @throws NotImplementedException
	 * @throws SQLException 
	 */
	private void reportProperty(Property property, SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {
		
		// property on subject position
		StmtIterator deprPropOnSubjPosIt = dataset.listStatements(property,
				null, (RDFNode) null);
		
		while (deprPropOnSubjPosIt.hasNext()) {
			Statement statement = deprPropOnSubjPosIt.next();
			
			if (!isDeprecationDeclaration(statement)) {
				Set<ViewQuad<ViewDefinition>> viewQuads = pinpointer
						.getViewCandidates(statement.asTriple());
				writeNodeTripleMeasureToSink(0, TriplePosition.SUBJECT,
						statement.asTriple(), viewQuads);
			}
		}
		
		// property on predicate position
		StmtIterator deprPropOnPredPosIt = dataset.listStatements(null, property,
				(RDFNode) null);
		
		while (deprPropOnPredPosIt.hasNext()) {
			Statement statement = deprPropOnPredPosIt.next();
			
			if (!isDeprecationDeclaration(statement)) {
				Set<ViewQuad<ViewDefinition>> viewQuads = pinpointer
						.getViewCandidates(statement.asTriple());
				writeNodeTripleMeasureToSink(0, TriplePosition.PREDICATE,
						statement.asTriple(), viewQuads);
			}
		}
		
		// property on object position
		StmtIterator deprPropOnObjPosIt = dataset.listStatements(null, null,
				property);
		
		while (deprPropOnObjPosIt.hasNext()) {
			Statement statement = deprPropOnObjPosIt.next();
			
			if (!isDeprecationDeclaration(statement)) {
				Set<ViewQuad<ViewDefinition>> viewQuads = pinpointer
						.getViewCandidates(statement.asTriple());
				writeNodeTripleMeasureToSink(0, TriplePosition.OBJECT,
						statement.asTriple(), viewQuads);
			}
		}
	}


	/**
	 * This method gets all statements that use a given deprecated class and
	 * reports statements using this class to the sink.
	 * 
	 * @param cls
	 *         the deprecated class
	 * @param dataset
	 *         the Jena model to look for statements using the deprecated class
	 * @throws NotImplementedException
	 * @throws SQLException 
	 */
	private void reportClass(RDFNode cls, SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {
		
		// class on object position
		StmtIterator deprClsOnObjPosIt = dataset.listStatements(null, null, cls);
		
		while (deprClsOnObjPosIt.hasNext()) {
			Statement statement = deprClsOnObjPosIt.next();
			
			if (!isDeprecationDeclaration(statement)) {
				Set<ViewQuad<ViewDefinition>> viewQuads = pinpointer
						.getViewCandidates(statement.asTriple());
							
				writeNodeTripleMeasureToSink(0, TriplePosition.OBJECT,
						statement.asTriple(), viewQuads);
			}
		}
		
		// class in subject position
		StmtIterator depClsOnSubjPosIt = dataset.listStatements(
				cls.asResource(), null, (RDFNode) null);
		
		while (depClsOnSubjPosIt.hasNext()) {
			Statement statement = depClsOnSubjPosIt.next();
			
			if (!isDeprecationDeclaration(statement)) {
				Set<ViewQuad<ViewDefinition>> viewQuads = pinpointer
						.getViewCandidates(statement.asTriple());
				
				writeNodeTripleMeasureToSink(0, TriplePosition.SUBJECT,
						statement.asTriple(), viewQuads);
			}
		}
			
	}


	private void reportResource(Resource resource, SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {
		
		// resource on subject position
		StmtIterator resOnSubjPosIt = dataset.listStatements(resource, null,
				(RDFNode) null);
		while (resOnSubjPosIt.hasNext()) {
			Statement statement = resOnSubjPosIt.next();
			
			if (!isDeprecationDeclaration(statement)) {
				Set<ViewQuad<ViewDefinition>> viewQuads = pinpointer
						.getViewCandidates(statement.asTriple());
				writeNodeTripleMeasureToSink(0, TriplePosition.SUBJECT,
						statement.asTriple(), viewQuads);
			}
		}
		
		// resource on predicate position
		try {
			StmtIterator resOnPredPosIt = dataset.listStatements(null,
					ResourceFactory.createProperty(resource.getURI()),
					(RDFNode) null);
			while (resOnPredPosIt.hasNext()) {
				Statement statement = resOnPredPosIt.next();
				
				if (!isDeprecationDeclaration(statement)) {
					Set<ViewQuad<ViewDefinition>> viewQuads = pinpointer
							.getViewCandidates(statement.asTriple());
					writeNodeTripleMeasureToSink(0, TriplePosition.PREDICATE,
							statement.asTriple(), viewQuads);
				}
			}
		} catch (InvalidPropertyURIException e) { /* just ignore */ };
		
		// resource on object position
		StmtIterator resOnObjPos = dataset.listStatements(null, null, resource);
		
		while (resOnObjPos.hasNext()) {
			Statement statement = resOnObjPos.next();
			
			if (!isDeprecationDeclaration(statement)) {
				Set<ViewQuad<ViewDefinition>> viewQuads = pinpointer
						.getViewCandidates(statement.asTriple());
				writeNodeTripleMeasureToSink(0, TriplePosition.OBJECT,
						statement.asTriple(), viewQuads);
			}
		}
	}
	
	
	/**
	 * Checks if a given statement is a deprecation declaration. If so, the
	 * statement is not a violation w.r.t. this metric, since the deprecation
	 * declaration of a resource is not considered as resource usage. Thus,
	 * if a resource is just declared to be deprecated, this is not a violation.
	 * Only if the so declared resource is actually used, this a violation will
	 * be reported.
	 * 
	 * @param statement A statement to check
	 * @return is the checked statement a deprecation declaration
	 */
	private boolean isDeprecationDeclaration(Statement statement) {
		Resource subj = statement.getSubject();
		Property pred = statement.getPredicate();
		RDFNode obj = statement.getObject();
		
		// owl:deprecated "true"^^xsd:boolean
		if (pred.equals(OWL2.deprecated) && obj.equals(trueLiteral)) {
			return true;
		}
		// <class> rdf:type owl:DeprecetedClass
		if (pred.equals(RDF.type) && obj.equals(OWL.DeprecatedClass)) {
			return true;
		}
		// owl:DeprecatedClass owl:equivalentClass <class>
		if (subj.equals(OWL.DeprecatedClass) && pred.equals(OWL.equivalentClass)) {
			return true;
		}
		// <class> owl:equivalentClass owl:DeprecatedClass
		if (pred.equals(OWL.equivalentClass) && obj.equals(OWL.DeprecatedClass)) {
			return true;
		}
		// <prop> rdf:type owl:DeprecatedProperty
		if (pred.equals(RDF.type) && obj.equals(OWL.DeprecatedProperty)) {
			return true;
		}
		// <prop> owl:equivalentProperty owl:DeprecatedProperty
		if (pred.equals(OWL.equivalentProperty) && obj.equals(OWL.DeprecatedProperty)) {
			return true;
		}
		// owl:DeprecatedProperty owl:equivalentProperty <prop>
		if (subj.equals(OWL.DeprecatedProperty) && pred.equals(OWL.equivalentProperty)) {
			return true;
		}
		
		return false;
	}
}
