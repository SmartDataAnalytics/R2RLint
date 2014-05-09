package org.aksw.sparqlify.qa.vocabularies;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class VoID {
	
	private static String void_ = "http://rdfs.org/ns/void#";
	
	/*
	 * classes
	 */

	/** A set of RDF triples that are published, maintained or aggregated by a
	 * single provider.
	 */
	public final static Resource Dataset =
			ResourceFactory.createResource(void_ + "Dataset");
	/** A web resource whose foaf:primaryTopic or foaf:topics include
	 * void:Datasets.
	 */
	public final static Resource DatasetDescription =
			ResourceFactory.createResource(void_ + "DatasetDescription");
	/** A collection of RDF links between two void:Datasets. */
	public final static Resource Linkset =
			ResourceFactory.createResource(void_ + "Linkset");
	/** A technical feature of a void:Dataset, such as a supported RDF
	 * serialization format.
	 */
	public final static Resource TechnicalFeature =
			ResourceFactory.createResource(void_ + "TechnicalFeature");
	
	/*
	 * properties
	 */
	
	/** The rdfs:Class that is the rdf:type of all entities in a class-based
	 * partition.
	 */
	public final static Property class_ =
			ResourceFactory.createProperty(void_, "class");
	/** A subset of a void:Dataset that contains only the entities of a
	 * certain rdfs:Class.
	 */
	public final static Property classPartition =
			ResourceFactory.createProperty(void_, "classPartition");
	/** The total number of distinct classes in a void:Dataset. In other words,
	 * the number of distinct resources occuring as objects of rdf:type triples
	 * in the dataset.
	 */
	public final static Property classes =
			ResourceFactory.createProperty(void_, "classes");
	/** An RDF dump, partial or complete, of a void:Dataset. */
	public final static Property dataDump =
			ResourceFactory.createProperty(void_, "dataDump");
	/** The total number of distinct objects in a void:Dataset. In other words,
	 * the number of distinct resources that occur in the object position of
	 * triples in the dataset. Literals are included in this count.
	 */
	public final static Property distinctObjects =
			ResourceFactory.createProperty(void_, "distinctObjects");
	/** The total number of distinct subjects in a void:Dataset. In other words,
	 * the number of distinct resources that occur in the subject position of
	 * triples in the dataset.
	 */
	public final static Property distinctSubjects =
			ResourceFactory.createProperty(void_, "distinctSubjects");
	/** The total number of documents, for datasets that are published as a set
	 * of individual documents, such as RDF/XML documents or RDFa-annotated web
	 * pages. Non-RDF documents, such as web pages in HTML or images, are
	 * usually not included in this count. This property is intended for
	 * datasets where the total number of triples or entities is hard to
	 * determine. void:triples or void:entities should be preferred where
	 * practical.
	 */
	public final static Property documents =
			ResourceFactory.createProperty(void_, "documents");
	/** The total number of entities that are described in a void:Dataset. */
	public final static Property entities =
			ResourceFactory.createProperty(void_, "entities");
	public final static Property exampleResource =
			ResourceFactory.createProperty(void_, "exampleResource");
	public final static Property feature =
			ResourceFactory.createProperty(void_, "feature");
	/** Points to the void:Dataset that a document is a part of. */
	public final static Property inDataset =
			ResourceFactory.createProperty(void_, "inDataset");
	public final static Property linkPredicate =
			ResourceFactory.createProperty(void_, "linkPredicate");
	/**  The dataset describing the objects of the triples contained in the Linkset. */
	public final static Property objectsTarget =
			ResourceFactory.createProperty(void_, "objectsTarget");
	/** An OpenSearch description document for a free-text search service over
	 * a void:Dataset.
	 */
	public final static Property openSearchDescription =
			ResourceFactory.createProperty(void_, "openSearchDescription");
	/** The total number of distinct properties in a void:Dataset. In other
	 * words, the number of distinct resources that occur in the predicate
	 * position of triples in the dataset.
	 */
	public final static Property properties =
			ResourceFactory.createProperty(void_, "properties");
	/** The rdf:Property that is the predicate of all triples in a
	 * property-based partition.
	 */
	public final static Property property =
			ResourceFactory.createProperty(void_, "property");
	/** A subset of a void:Dataset that contains only the triples of a certain
	 * rdf:Property.
	 */
	public final static Property propertyPartition =
			ResourceFactory.createProperty(void_, "propertyPartition");
	/** A top concept or entry point for a void:Dataset that is structured in a
	 * tree-like fashion. All resources in a dataset can be reached by
	 * following links from its root resources in a small number of steps.
	 */
	public final static Property rootResource =
			ResourceFactory.createProperty(void_, "rootResource");
	public final static Property sparqlEndpoint =
			ResourceFactory.createProperty(void_, "sparqlEndpoint");
	/** The dataset describing the subjects of triples contained in the Linkset. */
	public final static Property subjectsTarget =
			ResourceFactory.createProperty(void_, "subjectsTarget");
	public final static Property subset =
			ResourceFactory.createProperty(void_, "subset");
	/** One of the two datasets linked by the Linkset. */
	public final static Property target =
			ResourceFactory.createProperty(void_, "target");
	/** The total number of triples contained in a void:Dataset. */
	public final static Property triples =
			ResourceFactory.createProperty(void_, "triples");
	/** URI look-up endpoint at – Defines a simple URI look-up protocol for
	 * accessing a dataset.
	 */
	public final static Property uriLookupEndpoint =
			ResourceFactory.createProperty(void_, "uriLookupEndpoint");
	/** Defines a regular expression pattern matching URIs in the dataset. */
	public final static Property uriRegexPattern =
			ResourceFactory.createProperty(void_, "uriRegexPattern");
	/** A URI that is a common string prefix of all the entity URIs in a
	 * void:Dataset.
	 */
	public final static Property uriSpace =
			ResourceFactory.createProperty(void_, "uriSpace");
	/** A vocabulary that is used in the dataset. */
	public final static Property vocabulary =
			ResourceFactory.createProperty(void_, "vocabulary");
}
