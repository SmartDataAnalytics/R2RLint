package org.aksw.sparqlify.qa.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.aksw.commons.util.MapReader;
import org.aksw.commons.util.slf4j.LoggerCount;
import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProvider;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProviderImpl;
import org.aksw.sparqlify.config.v0_2.bridge.SyntaxBridge;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.core.cast.NewWorldTest;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.exceptions.TripleParseException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MappingMetric;
import org.aksw.sparqlify.qa.metrics.Metric;
import org.aksw.sparqlify.qa.metrics.NodeMetric;
import org.aksw.sparqlify.qa.metrics.TripleMetric;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.hp.hpl.jena.graph.Triple;



/**
 * FIXME: update doc
 * 
 * The QualityAssessment class is meant to do the actual quality assessment. All
 * it needs to do that are
 * - a Sparqlify dump (SparqlifyDump)
 * - the view definitions (Collection<ViewDefinition>)
 * - access the to database (Connection)
 * - data quality dimensions holding certain metrics to be applied to the data
 *   of the Sparqlify dump (Collection<Dimension> and Collection<Metric>
 *   respectively)
 * - a target to write relevant measure data to (MeasureDataSink)
 * 
 * The following steps are done in the given order:
 * 1) group all metrics according to the interfaces they implement; these are
 *    used detect if the metrics should be applied to
 *    - the whole dataset
 *    - one triple
 *    - one RDF node (resource or literal)
 *    - mappings defined in a Sparqlify view
 * 2) initialize the structures needed to write measured data to the measure
 *    data sink
 * 3) run all metrics to be applied to the whole dataset and write back
 *    relevant results
 * 4) iterate triple-wise over the dataset
 *    4.1) apply all triple metrics to the current triple and write back
 *         relevant results
 *    4.2) iterate over all triple parts (i.e. subject, predicate, object)
 *         4.2.1) apply all node metrics (if applicable) to the
 *                current triple part and write back relevant results
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
public class QualityAssessment {
	
	private static Logger logger = LoggerFactory.getLogger(QualityAssessment.class);
	private LoggerCount loggerCount;
	
	@Autowired
	private ApplicationContext appContext;
	
	// dataset related attributes
	private String datasetServiceUri;
	private String datasetGraphIri;
	private String datasetDumpFilePath;
	private String datasetPrefix;
	private List<String> datasetUsedPrefixes;
	private SparqlifyDataset dataset;
	
	// metrics related attributes
	private String metricsSettingsFilePath;
	private List<Metric> datasetMetrics;
	private List<Metric> tripleMetrics;
	private List<Metric> nodeMetrics;
	private List<Metric> mappingMetrics;
	
	// view defs related attributes
	private String viewDefsFilePath;
	private String typeAliasFilePath;
	private Collection<ViewDefinition> viewDefs;
	
	// source rdb related attributes
	@Autowired
	private DataSource rdb;
	
	@Autowired
	Pinpointer pinpointer;
	
	
	public QualityAssessment() {
		loggerCount = new LoggerCount(logger);
		datasetMetrics = new ArrayList<Metric>();
		tripleMetrics = new ArrayList<Metric>();
		nodeMetrics = new ArrayList<Metric>();
		mappingMetrics = new ArrayList<Metric>();
		viewDefs = new ArrayList<ViewDefinition>();
	}
	
	public void exec() throws TripleParseException, NotImplementedException,
			SQLException, IOException, RecognitionException {
		setUpMetrics();
		setUpDataset();
		readViewDefs();
		run();
	}
	
	private void readViewDefs() throws SQLException, IOException,
			RecognitionException {
		/*
		 *  init sparqlify views
		 */
		RdfViewSystemOld.initSparqlifyFunctions();
		TypeSystem typeSystem = NewWorldTest.createDefaultDatatypeSystem();
		
		File file = new File(typeAliasFilePath);
		InputStreamReader reader = new FileReader(file);
		Map<String, String> typeAlias = MapReader.read(reader);

		File viewsFile = new File(viewDefsFilePath);

		if (!viewsFile.exists()) {
			loggerCount.error("File does not exist: " + viewDefsFilePath);
		}

		ConfigParser parser = new ConfigParser();
		InputStream in = new FileInputStream(viewsFile);
		org.aksw.sparqlify.config.syntax.Config views;
		try {
			views = parser.parse(in, loggerCount);
		} finally {
			in.close();
		}

		List<org.aksw.sparqlify.config.syntax.ViewDefinition> syntaxViewDefs =
				views.getViewDefinitions();

		SchemaProvider schemaProvider = new SchemaProviderImpl(rdb.getConnection(),
				typeSystem, typeAlias);

		// since all we got until now are view definitions in a wrong format,
		// we have to convert them using a so called syntax bridge
		SyntaxBridge synBridge = new SyntaxBridge(schemaProvider);
		// the target collection holding the view definitions in the right
		//format
		//Collection<ViewDefinition> viewDefs = new ArrayList<ViewDefinition>();
		// conversion loop
		for (org.aksw.sparqlify.config.syntax.ViewDefinition viewDef : syntaxViewDefs) {
			viewDefs.add(synBridge.create(viewDef));
		}
		
		pinpointer.registerViewDefs(viewDefs);
	}
	
	
	private void setUpMetrics() throws NotImplementedException {
		// read metrics settings (whether enabled or not, thresholds)
		Properties metricsSettings = new Properties();
		Reader reader;
		try {
			reader = new FileReader(metricsSettingsFilePath);
			metricsSettings.load(reader);
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (Object key : metricsSettings.keySet()) {
			String[] settingParts = ((String) key).split("\\.");
				
			if (settingParts.length == 2) {
				// it's a metric setting (enabled/disabled)
				String dimensionName = settingParts[0];
				String metricName = settingParts[1];
				if (metricsSettings.containsKey(dimensionName)
						&& metricsSettings.get(dimensionName).equals("yes")
						&& metricsSettings.get(key).equals("yes")) {
					
					if (appContext.containsBean(metricName)) {
						Metric metric = (Metric) appContext.getBean(metricName);
						String thresholdKey = key + ".threshold";
						if (metricsSettings.containsKey(thresholdKey)) {
							float threshold = Float.parseFloat((String) metricsSettings.get(thresholdKey));
							metric.setThreshold(threshold);
						}
						metric.setName(metricName);
						metric.setParentDimension(dimensionName);
						metric.initMeasureDataSink();
						metric.setPrefix(datasetPrefix);
						if (metric instanceof DatasetMetric) {
							datasetMetrics.add(metric);
						} else if (metric instanceof TripleMetric) {
							tripleMetrics.add(metric);
						} else if (metric instanceof NodeMetric) {
							nodeMetrics.add(metric);
						} else if (metric instanceof MappingMetric) {
							mappingMetrics.add(metric);
						}
					}
				}
			}
		}
		
	}
	
	
	/**
	 * TODO: check if the values make sense
	 * @throws IOException
	 */
	private void setUpDataset() throws IOException {
		if (!datasetServiceUri.startsWith("$")
				&& !datasetGraphIri.startsWith("$")) {
			/*
			 *  a SPARQL endpoint is configured and thus used for dataset scope
			 *  access
			 */
			SparqlGraph g = new SparqlGraph(datasetServiceUri, datasetGraphIri);
			dataset = new SparqlifyDataset(g);
			// dump is only used for iteration and hence does not have to fit
			// in the availabla RAM
			dataset.registerDump(datasetDumpFilePath);
		} else {
			/*
			 *  no SPARQL endpoint is configured and the dump is used for
			 *  dataset scope access
			 */
			dataset = new SparqlifyDataset();
			dataset.readFromDump(datasetDumpFilePath);
		}
		dataset.setPrefix(datasetPrefix);
		dataset.setUsedPrefixes(datasetUsedPrefixes);
	}
	
	
	private void run() throws TripleParseException, NotImplementedException {
		logger.info("run() called");
		boolean runDatasetAssessment = datasetMetrics.size() > 0;
		logger.info("runDatasetAssessment: " + runDatasetAssessment);
		boolean runTripleAssessment = tripleMetrics.size() > 0;
		logger.info("runTripleAssessment: " + runTripleAssessment);
		boolean runNodeAssessment = nodeMetrics.size() > 0;
		logger.info("runNodeAssessment: " + runNodeAssessment);
		boolean runMappingAssessment = mappingMetrics.size() > 0;

		if (runMappingAssessment) {
			assessMappings();
		}
		if (runDatasetAssessment) {
			assessDataset();
		}
		if (runTripleAssessment || runNodeAssessment) {
			
			logger.info("starting triple assessment");
			int counter = 0;
			for (Triple triple : dataset) {
				if (runTripleAssessment) assessTriple(triple);
				if (runNodeAssessment) assessNodes(triple);
				counter++;
				
				if (counter%1000 == 0) {
					logger.info(counter + "triples processed");
				}
			}
			logger.info("finished assessTriple()");
		}
	}
	
	
	private void assessTriple(Triple triple) throws NotImplementedException {
		
		for (Metric metric : tripleMetrics) {
			((TripleMetric) metric).assessTriple(triple);
		}
	}
	
	
	private void assessNodes(Triple triple) throws NotImplementedException {
		for (Metric metric : nodeMetrics) {
			((NodeMetric) metric).assessNodes(triple);
		}
	}
	
	
	private void assessDataset() throws NotImplementedException {
		logger.info("assessDataset() called");
		for (Metric metric : datasetMetrics) {
			logger.info("start assessment with metric " + metric.getName());
			((DatasetMetric) metric).assessDataset(dataset);
			logger.info("finished assessment with metric " + metric.getName());
		}
		logger.info("finished assessDataset()");
	}
	
	
	private void assessMappings() throws NotImplementedException {
		logger.info("assessMappings() called");
		for (Metric metric : mappingMetrics) {
			logger.info("start assessment with metric " + metric.getName());
			((MappingMetric) metric).assessMappings(viewDefs);
			logger.info("finished assessment with metric " + metric.getName());
		}
		logger.info("finished assessMappings()");
	}
	
	
	public void setViewDefsFilePath(String filePath) {
		viewDefsFilePath = filePath;
	}
	
	public void setMetricsSettingsFilePath(String filePath) {
		metricsSettingsFilePath = filePath;
	}
	
	public void setTypeAliasFilePath(String filePath) {
		typeAliasFilePath = filePath;
	}
	
	public void setDatasetServiceUri(String serviceUri) {
		this.datasetServiceUri = serviceUri;
	}
	
	public void setDatasetGraphIri(String graphIri) {
		datasetGraphIri = graphIri;
	}
	
	public void setDatasetDumpFilePath(String dumpFilePath) {
		datasetDumpFilePath = dumpFilePath;
	}
	
	public void setDatasetUsedPrefixes(String csPrefixes) {
		datasetUsedPrefixes = new ArrayList<String>(Arrays.asList(csPrefixes
				.split(",")));
	}
	
	public void setDatasetPrefix(String prefix) {
		datasetPrefix = prefix;
	}
}
