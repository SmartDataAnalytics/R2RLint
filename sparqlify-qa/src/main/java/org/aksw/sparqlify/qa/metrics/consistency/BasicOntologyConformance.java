package org.aksw.sparqlify.qa.metrics.consistency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.Pair;
import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.reasoner.ValidityReport.Report;

/**
 * This metric reports several ontology violations of a given dataset.
 * 
 * Since the normal reports gotten from the model.validate() method provide
 * the information of errors and warnings in heterogeneously structured strings
 * there must be dedicated methods to parse these strings to get the information
 * needed to create a measure datum to be sent to the sink.
 * 
 * Currently the following errors/warnings are supported:
 * - datatype property violations
 * - object property violations
 * - disjoint class violations
 * - range violations
 * 
 * According to the OWLMicroReasonerFactory the reasoner itself has the
 * following features:
 * - RDFS.subClassOf
 * - RDFS.subPropertyOf
 * - RDFS.member
 * - RDFS.range
 * - RDFS.domain
 * - TransitiveReasoner.directSubClassOf
 * - TransitiveReasoner.directSubPropertyOf
 * - ReasonerVocabulary.individualAsThingP
 * - OWL.ObjectProperty
 * - OWL.DatatypeProperty
 * - OWL.FunctionalProperty
 * - OWL.SymmetricProperty
 * - OWL.TransitiveProperty
 * - OWL.InverseFunctionalProperty
 * - OWL.hasValue
 * - OWL.intersectionOf
 * - OWL.unionOf
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class BasicOntologyConformance extends MetricImpl implements DatasetMetric {

	@Autowired
	private Pinpointer pinpointer;
	
	final static String correctDtPropValue = "correct_datatype_property_value";
	final static String correctObjPropValue = "correct_object_property_value";
	final static String disjointClassesConformance = "disjoint_classes_conformance";
	final static String validRange = "valid_range";
	final static List<String> supportedMetrics =
			new ArrayList<String>(Arrays.asList(
					correctDtPropValue,
					correctObjPropValue,
					disjointClassesConformance,
					validRange
				));


	@Override
	public void initMeasureDataSink() throws NotImplementedException {
		for (String metricName : supportedMetrics) {
			sink.initMeasure(metricName, getClass(), parentDimension);
		}
	}


	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException {

		Reasoner reasoner = ReasonerRegistry.getOWLMicroReasoner();
		InfModel infModel = ModelFactory.createInfModel(reasoner, dataset);
		
		ValidityReport valReport = infModel.validate();
		Iterator<Report> reportIt = valReport.getReports();
		
		while (reportIt.hasNext()) {
			Report report = reportIt.next();
			List<Triple> badTriples = getErroneousTriples(report);
			List<Pair<Triple, Set<ViewQuad<ViewDefinition>>>> pinpointRes =
					new ArrayList<Pair<Triple, Set<ViewQuad<ViewDefinition>>>>();
			
			for (Triple triple : badTriples) {
				if (tripleExists(triple, dataset)) {
				
					Set<ViewQuad<ViewDefinition>> candidates =
							pinpointer.getViewCandidates(triple);
					
					Pair<Triple, Set<ViewQuad<ViewDefinition>>> tmp =
							new Pair<Triple, Set<ViewQuad<ViewDefinition>>>(triple, candidates);
				
					pinpointRes.add(tmp);
				}
			}
			float val;
			if (report.isError()) val = 0;
			else val = (float) 0.5;
			
			writeTriplesMeasureToSink(report.getType(), val, pinpointRes);
		}
	}


	private boolean tripleExists(Triple triple, SparqlifyDataset dataset) {
		StringBuilder queryStr = new StringBuilder(); 
		queryStr.append("ASK { ");
		
		queryStr.append(String.format("<%s> ", triple.getSubject().getURI()));
		queryStr.append(String.format("<%s> ", triple.getPredicate().getURI()));
		
		Node obj = triple.getObject();
		if (obj.isLiteral()) {
			// literal
			Node_Literal objNodeLiteral = (Node_Literal) obj;
			RDFDatatype dataType = objNodeLiteral.getLiteralDatatype();
			if (dataType != null) {
				// typed literal
				queryStr.append(String.format("\"%s\"^^<%s> . }",
						objNodeLiteral.getLiteralLexicalForm(),
						objNodeLiteral.getLiteralDatatypeURI()));
				
			} else {
				// plain literal
				String lang = objNodeLiteral.getLiteralLanguage();
				if (lang != null && lang != "") {
					
					queryStr.append(String.format("\"%s\"@%s . }",
							objNodeLiteral.getLiteralLexicalForm(),
							objNodeLiteral.getLiteralLanguage()));
				} else {
					queryStr.append(String.format("\"%s\" . }",
							objNodeLiteral.getLiteralLexicalForm()));
				}
			}
		} else if (obj.isURI()) {
			// resource
			Node_URI objNodeURI = (Node_URI) obj;
			queryStr.append(String.format("<%s> . }", objNodeURI.getURI().trim()));
			
		} else if (obj.isBlank()) {
			// blank node
			queryStr.append("?b . FILTER (isBlank(?b)) }");
		} else {
			// should never happen
			return false;
		}
		
		Query query = QueryFactory.create(queryStr.toString());
		
		QueryExecution qe = QueryExecutionFactory.create(query, dataset);
		boolean res = qe.execAsk();
		
		qe.close(); 
		
		return res;
	}


	private List<Triple> getErroneousTriples(Report report) {
		
		if (report.getType().equals("dtRange"))
			return  parseDtRange(report);
		else if (report.getType().equals("\"conflict\""))
			return parseConflict(report);
		else if (report.getType().equals("\"range check\""))
			return parseRangeCheck(report);
		else
			return new ArrayList<Triple>();
	}


	/*
	 * extension:
	 * http://ex.org/employee/B./Bela @http://ex.org/objProp "42"
	 * 
	 * description:
	 * Property http://ex.org/objProp has a typed range Datatype[http://www.w3.org/2001/XMLSchema#int -> class java.lang.Integer]that is not compatible with "42"
	 * 
	 * type:
	 * dtRange
	 * 
	 * report.toString():
	 * Error (dtRange): Property http://ex.org/objProp has a typed range Datatype[http://www.w3.org/2001/XMLSchema#int -> class java.lang.Integer]that is not compatible with "42"
	 */
	private List<Triple> parseDtRange(Report report) {
		List<Triple> triples = new ArrayList<Triple>();
		report.type = "correct_datatype_range";
		Triple triple =  (Triple) report.getExtension();
		triples.add(triple);
		return triples;
	}


	/*
	 * extension:
	 * http://ex.org/1
	 * 
	 * description:
	 * "Individual a member of disjoint classes"
	 * Culprit = http://ex.org/1
	 * Implicated node: http://ex.org/Department
	 * Implicated node: http://ex.org/Foo
	 * 
	 * type:
	 * "conflict"
	 * 
	 * report.toString():
	 * Error ("conflict"): "Individual a member of disjoint classes"
	 * Culprit = http://ex.org/1
	 * Implicated node: http://ex.org/Department
	 * Implicated node: http://ex.org/Foo
	 */
	private List<Triple> parseConflict(Report report) {
		final String separator = "Implicated node: ";
		final String disjDescStart = "\"Individual a member of disjoint classes\"";
		List<Triple> triples = new ArrayList<Triple>();
		
		Resource subj = (Resource) report.getExtension();
		
		/*
		 * sth like this:
		 * 
		 * "Individual a member of disjoint classes"
		 * Culprit = http://ex.org/1
		 * Implicated node: http://ex.org/Department
		 * Implicated node: http://ex.org/Foo
		 */
		String description = report.getDescription();
		String[] parts = description.split(separator);
		if (parts[0].startsWith(disjDescStart)) {
			report.type = disjointClassesConformance;
			Node pred = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
			Node obj1 = NodeFactory.createURI(parts[1].trim());
			Node obj2 = NodeFactory.createURI(parts[2].trim());
			
			Triple triple1 = new Triple(subj.asNode(), pred, obj1);
			triples.add(triple1);
			Triple triple2 = new Triple(subj.asNode(), pred, obj2);
			triples.add(triple2);
		}
		return triples;
	}


	/*
	 * extension:
	 * http://ex.org/employee/B./Bela
	 * description: "Literal value for object property (prop, value)"
	 * Culprit = http://ex.org/employee/B./Bela
	 * Implicated node: http://ex.org/objProp
	 * Implicated node: '42'
	 * 
	 * type:
	 * "range check"
	 * 
	 * report.toString():
	 * Warning ("range check"): "Literal value for object property (prop, value)"
	 * Culprit = http://ex.org/employee/B./Bela
	 * Implicated node: http://ex.org/objProp
	 * Implicated node: '42'
	 *
	 * 
	 * description:
	 * "Literal value for object property (prop, value)"
	 * Culprit = http://ex.org/employee/Defoe/Daniel
	 * Implicated node: http://ex.org/objProp
	 * Implicated node: 'lalalal'^^http://www.w3.org/2001/XMLSchema#string
	 * 
	 * type:
	 * "range check"
	 * 
	 * report.toString():
	 * Warning ("range check"): "Literal value for object property (prop, value)"
	 * Culprit = http://ex.org/employee/Defoe/Daniel
	 * Implicated node: http://ex.org/objProp
	 * Implicated node: 'lalalal'^^http://www.w3.org/2001/XMLSchema#string
	 * 
	 * 
	 * type:
	 * "range check"
	 * 
	 * report.toString():
	 * "Object value for datatype property (prop, value)"
	 * Culprit = http://ex.org/Department
	 * Implicated node: http://ex.org/dtProp
	 * Implicated node: http://ex.org/Foo
	 */
	private List<Triple> parseRangeCheck(Report report) {
		// range violation
		final String rangeDescStart = "\"Incorrectly typed literal due to range";
		// object property literal value
		final String objPropDescStart = "\"Literal value for object property";
		// datatype property resource value
		final String dtPropDescStart = "\"Object value for datatype property";
		
		List<Triple> triples = new ArrayList<Triple>();
		
		Resource subj = (Resource) report.getExtension();
		
		String description = report.getDescription();
		
		if (description.startsWith(rangeDescStart)) report.type = validRange;
		else if (description.startsWith(objPropDescStart)) report.type = correctObjPropValue;
		else if (description.startsWith(dtPropDescStart)) report.type = correctDtPropValue;
		
		triples.add(buildTripleFromDescFor(subj, description));
		
		return triples;
	}


	private Triple buildTripleFromDescFor(Resource subj, String description) {
		final String separator = "Implicated node: ";
		CharSequence dt = "^^";
		CharSequence at = "@";
		CharSequence clnSlshSlsh = "://";
		
		String[] parts = description.split(separator);
		
		Node pred = NodeFactory.createURI(parts[1].trim());
		Node obj;
		
		if (parts[2].contains(dt)) {
			// typed literal
			String[] tLitParts = parts[2].split("\\^\\^");
			String litVal = tLitParts[0].trim();
			if (litVal.startsWith("\"") || litVal.startsWith("'")) {
				litVal = litVal.substring(1);
			}
			if (litVal.endsWith("\"") || litVal.endsWith("'")) {
				litVal = litVal.substring(0, litVal.length()-1);
			}
			String litDt = tLitParts[1].trim();
			RDFDatatype dtype = new BaseDatatype(litDt);
			obj = NodeFactory.createLiteral(litVal, dtype);
		
		} else if (parts[2].contains(clnSlshSlsh)) {
			// a uri
			obj = NodeFactory.createURI(parts[2].trim());
			
		} else if (parts[2].contains(at)) {
			// plain literal with language tag
			String[] pLitParts = parts[2].split((String) dt);
			String litVal = pLitParts[0].trim();
			if (litVal.startsWith("\"") || litVal.startsWith("'")) {
				litVal = litVal.substring(1);
			}
			if (litVal.endsWith("\"") || litVal.endsWith("'")) {
				litVal = litVal.substring(0, litVal.length()-1);
			}
			String litLang = pLitParts[1].trim();
			
			obj = NodeFactory.createLiteral(litVal, litLang, false);
			
		} else {
			// plain literal without language tag
			String litVal = parts[2].trim();
			if (litVal.startsWith("\"") || litVal.startsWith("'")) {
				litVal = litVal.substring(1);
			}
			if (litVal.endsWith("\"") || litVal.endsWith("'")) {
				litVal = litVal.substring(0, litVal.length()-1);
			}
			obj = NodeFactory.createLiteral(litVal);
		}
		Triple triple = new Triple(subj.asNode(), pred, obj);
		
		return triple;
	}
}
