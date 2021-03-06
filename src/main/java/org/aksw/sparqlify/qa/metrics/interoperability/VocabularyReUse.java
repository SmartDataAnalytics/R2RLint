package org.aksw.sparqlify.qa.metrics.interoperability;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * This metric measures the ratio the appearance of properties and classes that
 * are re-used from established vocabularies compared to the whole number of
 * properties and classes.
 * 
 * "established" here means they are listed on one of the top 100 ranks of
 * prefix.cc namespace popularity overview (http://prefix.cc/popular/all). If
 * there are alternative URIs for a namespace abbreviation, all URIs were used
 * (except if they didn't make sense, e.g. dbpprop -->
 * http://dbpedia.org/property/years/)
 * 
 * This ranking is not updated dynamically and so the given ranking refers
 * to Sep 16, 2013.
 * 
 * No distinction is made, if classes or properties are local or not -- it is
 * only of interest if established vocabularies are used.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class VocabularyReUse extends MetricImpl implements DatasetMetric {
	
	private static Logger logger = LoggerFactory.getLogger(VocabularyReUse.class);
	
	private long numAll;
	private long numEstablishedReUsed;
	private List<Node> seenProperties;
	private List<Node> seenClasses;
	
	private List<String> top100Prefixes = new ArrayList<String>(Arrays.asList(
			"http://yago-knowledge.org/resource/",
			"http://dbpedia.org/class/yago/",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
			"http://xmlns.com/foaf/0.1/", "http://xmlns.com/foaf/",
			"http://www.w3.org/2002/07/owl#",
			"http://purl.org/dc/elements/1.1/",
			"http://dublincore.org/documents/dces/",
			"http://www.w3.org/2000/01/rdf-schema#",
			"http://purl.org/rss/1.0/", "http://www.w3.org/2004/02/skos/core#",
			"http://purl.org/science/owl/sciencecommons/",
			"http://umbel.org/umbel/sc/",
			"http://www.w3.org/2003/01/geo/wgs84_pos#",
			"http://www.opengis.net/ont/geosparql#",
			"http://www.opengis.net/geosparql#", "http://rdf.freebase.com/ns/",
			"http://rdf.freebase.com/rdf/", "http://ogp.me/ns/fb#",
			"http://www.geonames.org/ontology#", "http://www.w3.org/ns/people#",
			"http://rdfs.org/sioc/ns#", "http://purl.org/goodrelations/v1#",
			"http://www.w3.org/2001/XMLSchema#",
			"http://www.aktors.org/ontology/portal#",
			"http://sw.opencyc.org/concept/", "http://dbpedia.org/resource/",
			"http://dbpedia.org/ontology/", "http://dbpedia.org/property/",
			"http://purl.org/dc/terms/", "http://dbpedia.org/property/",
			"http://swrc.ontoware.org/ontology#",
			"http://ontoware.org/swrc/swrc/SWRCOWL/swrc_updated_v0.7.1.owl#",
			"http://rdfs.org/ns/void#", "http://purl.org/ontology/bibo/",
			"http://search.yahoo.com/searchmonkey/commerce/",
			"http://purl.org/commerce#",
			"http://purl.org/rss/1.0/modules/content/",
			"http://www.w3.org/2001/04/roadmap/org#",
			"http://www.w3.org/ns/org#", "http://usefulinc.com/ns/doap#",
			"http://webns.net/mvcb/", "http://www.w3.org/2006/vcard/ns#",
			"http://www.w3.org/2001/vcard-rdf/3.0#",
			"http://www.w3.org/1999/xhtml#",
			"http://purl.org/gen/0.1#", "http://www.w3.org/2006/gen/ont#",
			"http://purl.org/vocab/aiiso/schema#",
			"http://purl.org/linked-data/cube#",
			"http://www.rdfabout.com/rdf/schema/usbill/ ",
			"http://xmlns.com/wot/0.1/",
			"http://www.semanticdesktop.org/ontologies/2007/01/19/nie#",
			"http://www.semanticdesktop.org/ontologies/nie/",
			"http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#",
			"http://purl.org/vocab/relationship/",
			"http://this.invalid/test2#", "http://creativecommons.org/ns#",
			"http://purl.oclc.org/NET/ssnx/ssn#", "http://web.resource.org/cc/",
			"http://www.w3.org/2006/http#", "http://www.w3.org/2011/http#",
			"http://purl.org/dc/dcmitype/", "http://purl.org/vocab/vann/",
			"http://ogp.me/ns#", "http://opengraphprotocol.org/schema/",
			"http://example.com/", "http://example.org/",
			"http://purl.org/net/ns/ex#",
			"http://www4.wiwiss.fu-berlin.de/factbook/ns#",
			"http://purl.org/vocab/bio/0.1/",
			"http://vocab.org/bio/0.1/", "http://purl.org/ontology/mo/",
			"http://schemas.talis.com/2005/address/schema#",
			"http://purl.org/NET/c4dm/event.owl#", "http://www.w3.org/ns/earl#",
			"http://purl.org/captsolo/resume-rdf/0.2/cv#",
			"http://ramonantonio.net/doac/0.1/",
			"http://rdfs.org/resume-rdf/", "http://ontologi.es/colour/vocab#",
			"http://purl.org/NET/book/vocab#",
			"http://purl.org/microformat/hmedia/", "http://purl.org/media#",
			"http://search.yahoo.com/searchmonkey/media/",
			"http://purl.org/NET/biol/botany#",
			"http://dig.csail.mit.edu/TAMI/2007/amord/air#",
			"http://www.w3.org/2002/12/cal/icaltzd#",
			"http://www.holygoat.co.uk/owl/redwood/0.1/tags/",
			"http://data.semanticweb.org/ns/swc/ontology#",
			"http://purl.org/dc/qualifiers/1.0/",
			"http://purl.org/ontology/similarity/",
			"http://purl.org/ontology/musim#", "http://purl.org/cld/terms/",
			"http://purl.org/net/biblio#", "http://rdf.data-vocabulary.org/#",
			"http://rdf.data-vocabulary.org/", "http://commontag.org/ns#",
			"http://schemas.talis.com/2005/dir/schema#",
			"http://purl.org/ontology/af/", "http://purl.org/reco#",
			"http://ontologies.ezweb.morfeo-project.org/reco/ns#",
			"http://purl.org/stuff/rev#", "http://www. purl.org/stuff/rev#",
			"http://ontologi.es/days#", "http://www.w3.org/2000/10/swap/log#",
			"http://www.w3.org/ns/sparql-service-description#",
			"http://purl.org/ontology/daia/",
			"http://rhizomik.net/ontologies/copyrightonto.owl#",
			"http://purl.org/co/", "http://purl.org/ontology/co/core#",
			"http://purl.org/ontology/chord/",
			"http://www.w3.org/1999/xhtml/vocab#",
			"http://www.w3.org/1999/xhtml/vocab/",
			"http://purl.org/vocab/changeset/schema#",
			"http://purl.org/ontomedia/core/expression#",
			"http://www.ordnancesurvey.co.uk/ontology/AdministrativeGeography/v2.0/AdministrativeGeography.rdf#",
			"http://www.ontologydesignpatterns.org/cp/owl/componency.owl#",
			"http://www.w3.org/2004/03/trix/rdfg-1/",
			"http://www.w3.org/2005/xpath-functions#",
			"http://ontologies.smile.deri.ie/2009/02/27/memo#",
			"http://purl.oclc.org/NET/sism/0.1/",
			"http://www.kanzaki.com/ns/music#",
			"http://www.w3.org/2002/12/cal/ical#",
			"http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/",
			"http://wifo5-04.informatik.uni-mannheim.de/drugbank/resource/drugbank/",
			"http://jena.hpl.hp.com/ARQ/function#", "http://okkam.org/terms#",
			"http://sw.deri.org/2005/08/conf/cfp.owl#",
			"http://gmpg.org/xfn/11#", "http://vocab.sindice.com/xfn#",
			"http://www.ontotext.com/trree/owlim#",
			"http://www.owl-ontologies.com/task.owl#",
			"http://www.ontologydesignpatterns.org/cp/owl/informationrealization.owl#",
			"http://ontologi.es/giving#", "http://www.w3.org/2002/xforms/"
			));
	
	protected void clearCaches() {
		seenProperties = new ArrayList<Node>();
		seenClasses = new ArrayList<Node>();
		numAll = 0;
		numEstablishedReUsed = 0;
	}
	
	
	public VocabularyReUse() {
		super();
		numAll = 0;
		numEstablishedReUsed = 0;
		seenProperties = new ArrayList<Node>();
		seenClasses = new ArrayList<Node>();
	}


	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {

		/*
		 * check all properties
		 */
		
		int dbgCounter = 0;
		
		for (Triple triple : dataset) {
			if (dbgCounter++ % 10000 == 0) logger.debug(""+dbgCounter);

			Node pred = triple.getPredicate();
			
			if (!seenProperties.contains(pred)) {
				
				checkPrefix(pred.getURI());
				numAll++;
				seenProperties.add(pred);
			}
		}
		logger.debug("finished property part");
		
		/*
		 * check all classes
		 * 
		 * Since the OntModel.listClasses() method only gets classes that are
		 * defined explicitly via rdf:type rdfs:Class/owl:Class or via the use
		 * of certain properties (owl:equivalentClass, ...), not explicitly
		 * defined classes are not covered. So to check all classes a three
		 * step approach is taken:
		 * 1) get all classes from OntModel.listClasses() and loop over them
		 * 2) check if the considered class is used in the original
		 *    SparqlifyDataset (or if it is inferred by the reasoner)
		 *    --> not in the SparqlifyDataset: skip it
		 *    --> in the SparqlifyDataset: check if established, ...
		 * 3) additionally get all <sth> rdf:type <a class> statements to also
		 *    get classes not explicitly defined; check if established, ...
		 */
		
		// 1)
		logger.debug("starting class part 1");
		String rdfsClsQueryStr = "SELECT ?s { ?s a <" + RDFS.Class.getURI() + "> }";
		Query rdfsClsQuery = QueryFactory.create(rdfsClsQueryStr);
		
		QueryExecution rdfsClsQe;
		if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
			rdfsClsQe = QueryExecutionFactory.createServiceRequest(
					dataset.getSparqlServiceUri(), rdfsClsQuery);
		} else {
			rdfsClsQe = QueryExecutionFactory.create(rdfsClsQuery, dataset);
		}
		
		ResultSet rdfsClsRes = rdfsClsQe.execSelect();
		
		while (rdfsClsRes.hasNext()) {
			QuerySolution sol = rdfsClsRes.next();
			Node cls = sol.get("s").asNode();
			
			if (!seenClasses.contains(cls) && cls.isURI()) {
				checkPrefix(cls.getURI());
				numAll++;
				seenClasses.add(cls);
			}
		}
		rdfsClsQe.close();
		
		String owlClsQueryStr = "SELECT ?s { ?s a <" + OWL.Class.getURI() + "> }";
		Query owlClsQuery = QueryFactory.create(owlClsQueryStr);
		
		QueryExecution owlClsQe;
		if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
			owlClsQe = QueryExecutionFactory.createServiceRequest(
					dataset.getSparqlServiceUri(), owlClsQuery);
		} else {
			owlClsQe = QueryExecutionFactory.create(owlClsQuery, dataset);
		}
		
		ResultSet owlClsRes = owlClsQe.execSelect();
		
		while (owlClsRes.hasNext()) {
			QuerySolution sol = owlClsRes.next();
			Node cls = sol.get("s").asNode();
			
			if (!seenClasses.contains(cls) && cls.isURI()) {
				checkPrefix(cls.getURI());
				numAll++;
				seenClasses.add(cls);
			}
		}
		rdfsClsQe.close();
		
		logger.debug("finished class part 1; starting class part 2");
		// 3) get all implicit classes
		
		String typeQueryStr = "SELECT ?o { ?s a ?o }";
		Query typeQuery = QueryFactory.create(typeQueryStr);
		
		QueryExecution typeQe;
		if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
			typeQe = QueryExecutionFactory.createServiceRequest(
					dataset.getSparqlServiceUri(), typeQuery);
		} else {
			typeQe = QueryExecutionFactory.create(typeQuery, dataset);
		}
		
		ResultSet res = typeQe.execSelect();
		
		while (res.hasNext()) {
			QuerySolution sol = res.next();
			Node cls = sol.get("o").asNode();
			
			if (!seenClasses.contains(cls) && cls.isURI()) {
				checkPrefix(cls.getURI());
				numAll++;
				seenClasses.add(cls);
			}
		}
		typeQe.close();
		
		
		logger.debug("finished class part 2");
		float ratio = numEstablishedReUsed / (float) numAll;
		if (threshold == 0 || ratio <= threshold) {
			writeDatasetMeasureToSink(ratio);
		}
	}
	
	private void checkPrefix(String uri) {
		for (String prefix : top100Prefixes) {
			if (uri.startsWith(prefix)){
				numEstablishedReUsed++;
				break;
			}
		}
	}

}
