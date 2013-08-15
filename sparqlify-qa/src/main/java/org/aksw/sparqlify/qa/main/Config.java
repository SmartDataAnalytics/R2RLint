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
import org.aksw.sparqlify.qa.metrics.HttpUri;
import org.aksw.sparqlify.qa.metrics.Metric;
import org.aksw.sparqlify.qa.metrics.NoProlixFeatures;
import org.aksw.sparqlify.qa.metrics.QueryParamFreeUri;
import org.aksw.sparqlify.qa.metrics.ShortUri;
import org.aksw.sparqlify.qa.metrics.SoundingUri;
import org.aksw.sparqlify.qa.metrics.availability.DereferenceableForwardLinks;
import org.aksw.sparqlify.qa.metrics.performance.NoHashUris;


class DimensionFactory {
	
	private final static HashMap<String, Class> metricClasses = new HashMap<String, Class>() {
		private static final long serialVersionUID = -8496214118714182820L;

		{
			// availability
			put(Config.metricDereferenceableForwardLinks, DereferenceableForwardLinks.class);
			// performance
			put(Config.metricNoHashUris, NoHashUris.class);
			// representational conciseness
			put(Config.metricNoProlixRdfFeatures, NoProlixFeatures.class);
			put(Config.metricShortUris, ShortUri.class);
			put(Config.metricQueryParamFreeUri, QueryParamFreeUri.class);
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
			Class metricClass = metricClasses.get(name);
			
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

	// <accuracy></accuracy>
	// <amountOfData></amountOfData>
	// <availability>
	final static String metricDereferenceableForwardLinks = "dereferenceable_forward_links";
	// </availability>
	// <believability></believability>
	// <completeness></completeness>
	// <conciseness></conciseness>
	// <consistency></consistency>
	// <interlinking></interlinking>
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
	// <representationalConsistency></representationalConsistency>
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
			int a = 23;
			Dimension dim = null;
			try {
				dim = DimensionFactory.get(dimName, metricsInfo);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DimensionUnknownException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MetricUnknownException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			dims.add(dim);
		}
		
		return dims;
	}
	
}
