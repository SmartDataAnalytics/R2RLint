package org.aksw.sparqlify.qa.metrics.consistency;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@Component
public class DefinedClassesAndProperties extends MetricImpl implements
		DatasetMetric {

	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {
		
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
						RDF.type, RDFS.Class);
				StmtIterator owlClsIt = dataset.listStatements(cls, RDF.type,
						OWL.Class);
				
				if (!rdfsClsIt.hasNext() && !owlClsIt.hasNext()) {
					writeTripleMeasureToSink(0, new Triple(cls.asNode(),
							RDF.type.asNode(), RDFS.Class.asNode()), null);
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
						dataset.listStatements(prop, RDF.type, RDF.Property);
				
				if (!propDefs.hasNext()) {
					writeTripleMeasureToSink(0, new Triple(prop.asNode(),
							RDF.type.asNode(), RDF.Property.asNode()), null);
					reportedProperties.add(prop.getURI());
				}
			}
			
		}
	}

}
