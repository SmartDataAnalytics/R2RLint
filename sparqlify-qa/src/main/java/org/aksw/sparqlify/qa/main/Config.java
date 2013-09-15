package org.aksw.sparqlify.qa.main;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.aksw.commons.collections.Pair;
import org.aksw.sparqlify.qa.dimensions.Dimension;
import org.aksw.sparqlify.qa.exceptions.DimensionUnknownException;
import org.aksw.sparqlify.qa.exceptions.MetricUnknownException;
import org.aksw.sparqlify.qa.metrics.Metric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.aksw.sparqlify.qa.metrics.accuracy.ValidLanguageTag;
import org.aksw.sparqlify.qa.metrics.accuracy.XSDDatatypeCompatibleLiterals;
import org.aksw.sparqlify.qa.metrics.availability.DereferenceableForwardLinks;
import org.aksw.sparqlify.qa.metrics.believability.DatasetMetadata;
import org.aksw.sparqlify.qa.metrics.completeness.InterlinkingCompleteness;
import org.aksw.sparqlify.qa.metrics.completeness.PopulationCompleteness;
import org.aksw.sparqlify.qa.metrics.completeness.PropertyCompleteness;
import org.aksw.sparqlify.qa.metrics.completeness.SchemaCompleteness;
import org.aksw.sparqlify.qa.metrics.conciseness.ExtensionalConciseness;
import org.aksw.sparqlify.qa.metrics.conciseness.IntensionalConciseness;
import org.aksw.sparqlify.qa.metrics.conciseness.NoDuplicateStatements;
import org.aksw.sparqlify.qa.metrics.consistency.BasicOntologyConformance;
import org.aksw.sparqlify.qa.metrics.consistency.DefinedClassesAndProperties;
import org.aksw.sparqlify.qa.metrics.consistency.HomogeneousDatatypes;
import org.aksw.sparqlify.qa.metrics.consistency.NoAmbiguousMappings;
import org.aksw.sparqlify.qa.metrics.consistency.NoBogusInverseFunctionalProperties;
import org.aksw.sparqlify.qa.metrics.consistency.NoDeprecatedClassesOrProperties;
import org.aksw.sparqlify.qa.metrics.consistency.NoResourceNameClashes;
import org.aksw.sparqlify.qa.metrics.consistency.TypedResources;
import org.aksw.sparqlify.qa.metrics.consistency.WellPlacedClassesAndProperties;
import org.aksw.sparqlify.qa.metrics.interlinking.ExternalSameAsLinks;
import org.aksw.sparqlify.qa.metrics.performance.NoHashUris;
import org.aksw.sparqlify.qa.metrics.reprconciseness.NoProlixFeatures;
import org.aksw.sparqlify.qa.metrics.reprconciseness.QueryParamFreeUri;
import org.aksw.sparqlify.qa.metrics.reprconciseness.ShortUri;
import org.aksw.sparqlify.qa.metrics.reprconsistency.TermReUse;
import org.aksw.sparqlify.qa.metrics.understandability.HttpUri;
import org.aksw.sparqlify.qa.metrics.understandability.SoundingUri;


class DimensionFactory {
	
	private final static HashMap<String, Class<? extends MetricImpl>> metricClasses =
			new HashMap<String, Class<? extends MetricImpl>>() {
		private static final long serialVersionUID = -8496214118714182820L;

		{
			// accuracy
			put(Config.metricDatatypeCompatibleLiterals, XSDDatatypeCompatibleLiterals.class);
			put(Config.metricValidLanguageTag, ValidLanguageTag.class);
			// availability
			put(Config.metricDereferenceableForwardLinks, DereferenceableForwardLinks.class);
			// believability
			put(Config.metricDatasetMetadata, DatasetMetadata.class);
			// completeness
			put(Config.metricInterlinkingCompleteness, InterlinkingCompleteness.class);
			put(Config.metricPopulationCompleteness, PopulationCompleteness.class);
			put(Config.metricPropertyCompleteness, PropertyCompleteness.class);
			put(Config.metricSchemaCompleteness, SchemaCompleteness.class);
			// conciseness
			put(Config.metricExtensionalConciseness, ExtensionalConciseness.class);
			put(Config.metricIntensionalConciseness, IntensionalConciseness.class);
			put(Config.metricNoDuplicateStatements, NoDuplicateStatements.class);
			// consistency
			put(Config.metricBasicOntologyConformance, BasicOntologyConformance.class);
			put(Config.metricDefinedClassesAndProperties, DefinedClassesAndProperties.class);
			put(Config.metricHomogeneousDatatypes, HomogeneousDatatypes.class);
			put(Config.metricNoAmbiguousMappings, NoAmbiguousMappings.class);
			put(Config.metricNoBogusInverseFunctionalProperty, NoBogusInverseFunctionalProperties.class);
			put(Config.metricNoDeprecatedClassesAndProperties, NoDeprecatedClassesOrProperties.class);
			put(Config.metricNoResourceNameClashes, NoResourceNameClashes.class);
			put(Config.metricTypedResources, TypedResources.class);
			put(Config.metricWellPlacedClassesAndProperties, WellPlacedClassesAndProperties.class);
			// interlinking
			put(Config.metricExternalSameAsLinks, ExternalSameAsLinks.class);
			// performance
			put(Config.metricNoHashUris, NoHashUris.class);
			// representational conciseness
			put(Config.metricNoProlixRdfFeatures, NoProlixFeatures.class);
			put(Config.metricQueryParamFreeUri, QueryParamFreeUri.class);
			put(Config.metricShortUris, ShortUri.class);
			// representational consistency
			put(Config.metricTermReUse, TermReUse.class);
			// understandability
			put(Config.metricHttpUris, HttpUri.class);
			put(Config.metricSoundingUri, SoundingUri.class);
		}
	};
	
	public static Dimension get(String dimensionName,
			List<Pair<String, Float>> metricsInfo)
			throws DimensionUnknownException, InstantiationException,
			IllegalAccessException, MetricUnknownException {
		
		Dimension dim = new Dimension(dimensionName);
		
		for (Pair<String, Float> metricInfo : metricsInfo) {
			String name = (String) metricInfo.first;
			Class<? extends MetricImpl> metricClass = metricClasses.get(name);
			
			if (metricClass == null) throw new MetricUnknownException();
			
			float threshold = (float) metricInfo.second;
			Metric metric = (Metric) metricClass.newInstance();
			metric.setThreshold(threshold);
			metric.setParentDimension(dimensionName);
			metric.setName(name);
			dim.addMetric(metric);
		}
		return dim;
	}
}


public class Config {
	private final String enabled = "yes";
	private final String threshold = "threshold";
	
	final static String dimAccuracy = "accuracy";
	final static String dimAmountOfData = "amount_of_data";
	final static String dimAvailability = "availability";
	final static String dimBelievability = "believability";
	final static String dimCompleteness = "completeness";
	final static String dimConciseness = "conciseness";
	final static String dimConsistency = "consistency";
	final static String dimInterlinking = "interlinking";
	final static String dimInterpretability = "interpretability";
	final static String dimLicensing = "licensing";
	final static String dimPerformance = "performance";
	final static String dimRelevancy = "relevancy";
	final static String dimRepresentationalConciseness = "representational_conciseness";
	final static String dimRepresentationalConsistency = "representational_consistency";
	final static String dimSecurity = "security";
	final static String dimUnderstandability = "understandability";
	final static String dimVerifiability = "verifiability";
	final static String dimVersatility = "versatility";

	// <accuracy>
	final static String metricDatatypeCompatibleLiterals = "datatype-compatible_literals";
	final static String metricValidLanguageTag = "valid_language_tag";
	// </accuracy>
	// <amountOfData></amountOfData>
	// <availability>
	final static String metricDereferenceableForwardLinks = "dereferenceable_forward_links";
	// </availability>
	// <believability>
	final static String metricDatasetMetadata = "dataset_metadata";
	// </believability>
	// <completeness>
	final static String metricSchemaCompleteness = "schema_completeness";
	final static String metricPropertyCompleteness = "property_completeness";
	final static String metricPopulationCompleteness = "population_completeness";
	final static String metricInterlinkingCompleteness = "interlinking_completeness";
	// </completeness>
	// <conciseness>
	final static String metricIntensionalConciseness = "intensional_conciseness";
	final static String metricExtensionalConciseness = "extensional_conciseness";
	final static String metricNoDuplicateStatements = "no_duplicate_statements";
	// </conciseness>
	// <consistency>
	final static String metricBasicOntologyConformance = "basic_ontology_conformance";
	final static String metricDefinedClassesAndProperties = "defined_classes_and_properties";
	final static String metricHomogeneousDatatypes = "homogeneous_datatypes";
	final static String metricNoAmbiguousMappings = "no_ambiguous_mappings";
	final static String metricNoBogusInverseFunctionalProperty = "no_bogus_inverse-functional_properties";
	final static String metricNoDeprecatedClassesAndProperties = "no_deprecated_classes_and_properties";
	final static String metricNoResourceNameClashes = "no_resource_name_clashes";
	final static String metricTypedResources = "typed_resources";
	final static String metricWellPlacedClassesAndProperties = "well-placed_classes_and_properties";
	// </consistency>
	// <interlinking>
	final static String metricExternalSameAsLinks = "external_sameas_links";
	// </interlinking>
	// <interpretability></interpretability>
	// <performance>
	final static String metricNoHashUris = "no_hash_uris";
	// </performance>
	// <relevancy></relevancy>
	// <representationalConciseness>
	final static String metricShortUris = "short_uris";
	final static String metricNoProlixRdfFeatures = "no_prolix_rdf_features";
	final static String metricQueryParamFreeUri = "query_parameter-free_uri";
	// </representationalConciseness>
	// <representationalConsistency>
	final static String metricTermReUse = "term_re-use";
	// </representationalConsistency>
	// <understandability>
	final static String metricHttpUris = "http_uris";
	final static String metricSoundingUri = "sounding_uri";
	// </understandability>
	
	private final List<String> knownDimensions = Arrays.asList(dimAccuracy,
			dimAmountOfData, dimAvailability, dimBelievability, dimCompleteness,
			dimConciseness,	dimConsistency, dimInterlinking,
			dimInterpretability, dimLicensing, dimPerformance, dimRelevancy,
			dimRepresentationalConciseness,	dimRepresentationalConsistency,
			dimSecurity, dimUnderstandability, dimVerifiability,
			dimVersatility);

	private Reader reader;
	private Properties confData;
	
	
	public Config(String filePath) throws IOException {
		// load config data from file
		reader = new FileReader(filePath);
		confData = new Properties();
		confData.load(reader);
		reader.close();
	}
	
	
	public String get(String key) {
		return confData.getProperty(key);
	}
	
	
	public boolean dimensionEnabled(String dimension) {
		String dim = confData.getProperty(dimension.toLowerCase());
		
		return (dim != null && dim.equalsIgnoreCase(this.enabled)) ? true : false;
	}
	
	
	public List<String> getDimensionNames() {
		List<String> dimensionNames = new ArrayList<String>();
		
		for (Object keyObj : confData.keySet()) {
			String key = ((String) keyObj).toLowerCase();
			String val = confData.getProperty(key); 
			if (val != null && val.equalsIgnoreCase(enabled)
					&& knownDimensions.contains(key)) {
				dimensionNames.add((String) key);
			}
		}
		
		return dimensionNames;
	}
	
	
	public List<Dimension> getDimensions() {
		List<Dimension> dims = new ArrayList<Dimension>();
		Collection<Object> confKeys = confData.keySet();
		
		for (String dimName : getDimensionNames()) {
			if (!confData.get(dimName).equals(enabled)) continue;
			
			List<Pair<String, Float>> metricsInfo = new ArrayList<Pair<String, Float>>();

			String metricEnabledPattern = "^" + dimName + "\\.[A-Za-z0-9_-]*$";
			for (Object confKey : confKeys) {
				if (((String) confKey).matches(metricEnabledPattern)) {
					if (!confData.get(confKey).equals(enabled)) continue;
					Object thresholdRaw = confData.get(confKey + "." + threshold);
					float thresholdVal = 0;
					if (thresholdRaw != null) thresholdVal = Float.parseFloat((String) thresholdRaw); 
					
					String measureName = ((String) confKey).split("\\.")[1];
					metricsInfo.add(new Pair<String, Float>(measureName, thresholdVal));
				}
			}

			Dimension dim = null;
			try {
				dim = DimensionFactory.get(dimName, metricsInfo);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (DimensionUnknownException e) {
				e.printStackTrace();
			} catch (MetricUnknownException e) {
				e.printStackTrace();
			}
			
			dims.add(dim);
		}
		
		return dims;
	}
	
}
