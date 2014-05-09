package org.aksw.sparqlify.qa.metrics.interoperability;

import java.net.HttpURLConnection;
import java.net.URL;
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
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;

/**
 * This metric should find a measure of the degree to which established terms
 * are re-used.
 * Resources are considered established if they stem from an established
 * vocabulary and are dereferenceable.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class TermReUse extends MetricImpl implements DatasetMetric {
	
	private long numEstResources;
	private long numLocalResources;
	private List<Node> seenResources;
	
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

	public TermReUse() {
		super();
		numEstResources = 0;
		numLocalResources = 0;
		seenResources = new ArrayList<Node>();
	}
	private static Logger logger = LoggerFactory.getLogger(TermReUse.class);

	@Override
	public void assessDataset(SparqlifyDataset dataset)
			throws NotImplementedException, SQLException {
		
		int dbgCount = 0;
		for (Triple triple : dataset) {
			dbgCount++;
			if (dbgCount % 10000 == 0) logger.debug(""+dbgCount);
			
			
			/* subject */
			Node subject = triple.getSubject();
			if (!seenResources.contains(subject)) {
				
				
				if (subject.isURI()) {
					boolean isLocal = false;
					for (String prefix : dataset.getPrefixes()) {
						if (subject.getURI().startsWith(prefix)) {
							isLocal = true;
							break;
						}
					}
					if (isLocal) {
						numLocalResources++;
					} else {
						if (established(subject)) numEstResources++;
					}
				}
				
				seenResources.add(subject);
			}
					
			
			/* predicate */
			Node predicate = triple.getPredicate();
			if (!seenResources.contains(predicate)) {
				
				boolean isLocal = false;
				for (String prefix : dataset.getPrefixes()) {
					if (predicate.getURI().startsWith(prefix)) {
						isLocal = true;
						break;
					}
				}
				if (isLocal) {
					numLocalResources++;
				} else {
					if (established(predicate)) numEstResources++;
				}
				seenResources.add(predicate);
			}
			
			/* object */
			Node object = triple.getObject();
			if (!seenResources.contains(object)) {
				if (object.isURI()) {
					
					boolean isLocal = false;
					for (String prefix : dataset.getPrefixes()) {
						if (object.getURI().startsWith(prefix)) {
							isLocal = true;
							break;
						}
					}
					
					if (isLocal)
						numLocalResources++;
					else {
						if (established(object))
							numEstResources++;
					}
					seenResources.add(object);
				}
			}
		}
		
		float ratio = numEstResources / (float) (numEstResources + numLocalResources);
		if (threshold == 0 || ratio <= threshold) {
			writeDatasetMeasureToSink(ratio);
		}
	}

	private boolean established(Node node) {
		boolean fromEstablishedVoc = false;
		boolean dereferenceable = false;
		String uri = ((Node_URI) node).getURI();
		for (String prefix : top100Prefixes) {
			if (uri.startsWith(prefix)) {
				fromEstablishedVoc = true;
				break;
			}
		}
		
		if (fromEstablishedVoc) {
			URL url;
			try {
				url = new URL(uri);
				HttpURLConnection urlConn;
				urlConn = (HttpURLConnection) url.openConnection();
				urlConn.setRequestMethod("HEAD");
				int responseCode = 0;
				urlConn.connect();
				responseCode = urlConn.getResponseCode();
				if (responseCode >= 200 && responseCode < 400)
					dereferenceable= true;
			} catch (Exception e) {
				// not dereferenceable
			}
		}
		
		return fromEstablishedVoc && dereferenceable;
	}
}
