package org.aksw.sparqlify.qa.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;

import org.aksw.commons.collections.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class VocabularyLoader {
	
	private Logger logger = LoggerFactory.getLogger(VocabularyLoader.class);
	private final String pathPrefix = "src/main/resources/vocabularies/";
	private final HashMap<String, Pair<String, String>> knownVocabularies =
			new HashMap<String, Pair<String, String>>(){
				private static final long serialVersionUID = -4601039038754504181L;
			{
				put("http://www.w3.org/1999/02/22-rdf-syntax-ns#",
						new Pair<String, String>("22-rdf-syntax-ns.rdf", "RDF/XML"));
				put("http://www.w3.org/2000/01/rdf-schema#",
						new Pair<String, String>("rdf-schema.rdf", "RDF/XML"));
				put("http://www.w3.org/2002/07/owl#",
						new Pair<String, String>("owl.rdf", "RDF/XML"));
				put("http://xmlns.com/foaf/0.1/",
						new Pair<String, String>("foaf.rdf", "RDF/XML"));
				put("http://geovocab.org/geometry#",
						new Pair<String, String>("geometry.rdf", "RDF/XML"));
				put("http://geovocab.org/spatial#",
						new Pair<String, String>("spatial.rdf", "RDF/XML"));
				put("http://purl.org/dc/terms/",
						new Pair<String, String>("dcterms.rdf", "RDF/XML"));
				put("http://purl.org/dc/elements/1.1/",
						new Pair<String, String>("dcelements.rdf", "RDF/XML"));
				put("http://www.opengis.net/ont/geosparql#",
						new Pair<String, String>("geosparql_vocab_all.rdf", "RDF/XML"));
				put("http://www.w3.org/2003/01/geo/wgs84_pos#",
						new Pair<String, String>("wgs84_pos", "RDF/XML"));
				put("http://www.w3.org/2004/02/skos/core#",
						new Pair<String, String>("core", "RDF/XML"));
				put("http://purl.org/NET/c4dm/event.owl#",
						new Pair<String, String>("event.rdf", "RDF/XML"));
				put("http://purl.org/ontology/mo/",
						new Pair<String, String>("mo.rdf","RDF/XML"));
				put("http://open.vocab.org/terms/",
						new Pair<String, String>("terms.rdf", "RDF/XML"));
				put("http://purl.org/linked-data/cube#",
						new Pair<String, String>("cube.rdf", "RDF/XML"));
				put("http://purl.org/linked-data/sdmx/2009/attribute#",
						new Pair<String, String>("sdmx-attribute.rdf", "RDF/XML"));
				put("http://purl.org/linked-data/sdmx/2009/concept#",
						new Pair<String, String>("sdmx-concept.rdf", "RDF/XML"));
				put("http://purl.org/linked-data/sdmx/2009/dimension#",
						new Pair<String, String>("sdmx-dimension.rdf", "RDF/XML"));
				put("http://purl.org/linked-data/sdmx/2009/measure#",
						new Pair<String, String>("sdmx-measure", "RDF/XML"));
			}};
	
	private final HashMap<String, String> knownPrefixes =
			new HashMap<String, String>(){
				private static final long serialVersionUID = 7477631573380330379L;
			{
				put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
				put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
				put("owl", "http://www.w3.org/2002/07/owl#");
				put("foaf", "http://xmlns.com/foaf/0.1/");
				put("ngeo", "http://geovocab.org/geometry#");
				put("spatial", "http://geovocab.org/spatial#");
				put("dcterms", "http://purl.org/dc/terms/");
				put("dc", "http://purl.org/dc/elements/1.1/");
				put("ogc", "http://www.opengis.net/ont/geosparql#");
				put("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
				put("skos", "http://www.w3.org/2004/02/skos/core#");
				put("event", "http://purl.org/NET/c4dm/event.owl#");
				put("mo", "http://purl.org/ontology/mo/");
				put("terms", "http://open.vocab.org/terms/");
				put("cube", "http://purl.org/linked-data/cube#");
				put("sdmx-attribute", "http://purl.org/linked-data/sdmx/2009/attribute#");
				put("sdmx-concept", "http://purl.org/linked-data/sdmx/2009/concept#");
				put("sdmx-dimension", "http://purl.org/linked-data/sdmx/2009/dimension#");
				put("sdmx-measure", "http://purl.org/linked-data/sdmx/2009/measure#");
			}};
	
	// model cache for already loaded vocabularies; key is the whole namespace
	private HashMap<String, Model> cache;


	public VocabularyLoader() {
		cache = new HashMap<String, Model>();
	}


	public Model getVocabulary(String prefixOrNamespace) throws FileNotFoundException {
		String namespace;
		if (knownPrefixes.containsKey(prefixOrNamespace))
			namespace = knownPrefixes.get(prefixOrNamespace);
		else
			namespace = prefixOrNamespace;
		
		// check if already cached
		if (cache.containsKey(namespace)) return cache.get(namespace);
		else return loadVocabulary(namespace);
	}


	private Model loadVocabulary(String namespace) throws FileNotFoundException {
		if (knownVocabularies.get(namespace) == null) return null;
		
		String filePath = pathPrefix + knownVocabularies.get(namespace).first;
		String type = knownVocabularies.get(namespace).second;
		File file = new File(filePath);
		Reader reader = new FileReader(file);
		Model vocab = ModelFactory.createDefaultModel();
		vocab.read(reader, type);
		
		if (!cache.containsKey(namespace)) cache.put(namespace, vocab);
		
		return vocab;
	}


	public Model getVocabularies(Collection<String> prefixesOrNamespaces)
			throws FileNotFoundException {
		
		Model vocabs = ModelFactory.createDefaultModel();
		for (String prefixOrNamespace : prefixesOrNamespaces)
			vocabs.add(getVocabulary(prefixOrNamespace));
		
		return vocabs;
	}


	public Model getAllKnownVocabulariesOf(Collection<String> prefixesOrNamespaces) {
		
		Model vocabs = ModelFactory.createDefaultModel();
		for (String prefixOrNamespace : prefixesOrNamespaces) {
			Model vocab;
			try {
				vocab = getVocabulary(prefixOrNamespace);
			} catch (FileNotFoundException e) {
				continue;
			}
			if (vocab != null) vocabs.add(vocab);
			else logger.warn("Could not load " + prefixOrNamespace + "!!!!!");
		}
		
		return vocabs;
	}
}
