package org.aksw.sparqlify.qa.metrics.consistency;

import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class DefinedClassesAndProperties extends MetricImpl implements
		DatasetMetric {

	Property rdfType = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	Resource owlClass = ResourceFactory.createResource("http://www.w3.org/2002/07/owl#Class");
	Resource rdfsClass = ResourceFactory.createResource("http://www.w3.org/2000/01/rdf-schema#Class");
	Resource rdfsProperty = ResourceFactory.createResource("http://www.w3.org/2000/01/rdf-schema#Property");

	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException {
		
		OntModel ontModel =
				ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF, dataset);
		
		ExtendedIterator<OntClass> classesIt = ontModel.listClasses();
		
		List<String> reportedClasses = new ArrayList<String>();
		while (classesIt.hasNext()) {
			OntClass cls = classesIt.next();
			if (!reportedClasses.contains(cls.getURI())
					&& (dataset.getPrefix() == null || cls.getURI().startsWith(
							dataset.getPrefix()))) {
				
				StmtIterator rdfsClsIt = dataset.listStatements(cls,
						rdfType, rdfsClass);
				StmtIterator owlClsIt = dataset.listStatements(cls, rdfType,
						owlClass);
				
				if (!rdfsClsIt.hasNext() && !owlClsIt.hasNext()) {
					writeTripleMeasureToSink(0, new Triple(cls.asNode(),
							rdfType.asNode(), rdfsClass.asNode()), null);
					reportedClasses.add(cls.getURI());
				}
				
			}
			
		}
		
		StmtIterator statementsIt = dataset.listStatements();
		
		List<String> reportedProperties = new ArrayList<String>();
		while (statementsIt.hasNext()) {
			Statement statement = statementsIt.next();
			Property prop = statement.getPredicate();
			if (!reportedProperties.contains(prop.getURI())
					&& (dataset.getPrefix() == null || prop.getURI()
							.startsWith(dataset.getPrefix()))) {
		
				StmtIterator propDefs =
						dataset.listStatements(prop, rdfType, rdfsProperty);
				
				if (!propDefs.hasNext()) {
					writeTripleMeasureToSink(0, new Triple(prop.asNode(),
							rdfType.asNode(), rdfsProperty.asNode()), null);
					reportedProperties.add(prop.getURI());
				}
			}
			
		}
	}

}
