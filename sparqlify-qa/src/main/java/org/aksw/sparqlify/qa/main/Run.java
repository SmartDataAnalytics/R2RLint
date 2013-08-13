package org.aksw.sparqlify.qa.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
import org.aksw.sparqlify.qa.dimensions.Dimension;
import org.aksw.sparqlify.qa.exceptions.DimensionUnknownException;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.exceptions.TripleParseException;
import org.aksw.sparqlify.qa.sinks.DummySink;
import org.aksw.sparqlify.qa.sinks.MeasureDataSink;
import org.antlr.runtime.RecognitionException;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Run {
	
	private static Logger logger = LoggerFactory.getLogger("QA");

	/**
	 * @param args
	 * @throws IOException 
	 * @throws DimensionUnknownException 
	 * @throws SQLException 
	 * @throws RecognitionException 
	 * @throws TripleParseException 
	 * @throws NotImplementedException 
	 */
	public static void main(String[] args) throws IOException, DimensionUnknownException, SQLException, RecognitionException, TripleParseException, NotImplementedException {
		// TODO: things to put in a config file:
		String resDirPrefix = "src/main/resources/";
		String configFilePath = resDirPrefix + "dimensions.properties";
		String dumpFilePath = resDirPrefix + "dump.ttl";
		String viewDefFilePath = resDirPrefix + "views.sparqlify";
		String h5FilePath = resDirPrefix + "measure_data.h5";
		String typeAliasFilePath = "../../Sparqlify/sparqlify-core/src/" +
				"main/resources/type-map.h2.tsv";
		
		String databaseName = "qa1";
		String databaseUser = "postgres";
		String databasePassword = "postgres";
		String databaseHost = "10.23.0.2";
		int databasePort = 5432;

		
		Config confReader;
		LoggerCount loggerCount = new LoggerCount(logger);
		
		
		/*
		 *  get Sparqlify dump
		 */
		SparqlifyDataset dataset = new SparqlifyDataset();
		dataset.readFromDump(dumpFilePath);

		
		/*
		 *  get quality assessment config
		 */
		confReader = new Config(configFilePath);
		List<Dimension> dimensions = confReader.getDimensions();
		
		/*
		 *  init database connection
		 */
		PGSimpleDataSource dataSource = new PGSimpleDataSource();
		dataSource.setDatabaseName(databaseName);
		dataSource.setUser(databaseUser);
		dataSource.setPassword(databasePassword);
		dataSource.setServerName(databaseHost);
		dataSource.setPortNumber(databasePort);
		Connection conn = dataSource.getConnection();
		// TODO: check if I need this
		// Schema schema = Schema.create(conn);

		
		/*
		 *  init sparqlify views
		 */
		RdfViewSystemOld.initSparqlifyFunctions();
		TypeSystem typeSystem = NewWorldTest.createDefaultDatatypeSystem();
		
		// TODO: check if I really need this
		// SqlTranslator sqlTranslator = SparqlifyUtils.createSqlRewriter();
		
		File file = new File(typeAliasFilePath);
		Map<String, String> typeAlias = MapReader.readFile(file);
		

        File viewsFile = new File(viewDefFilePath);

        if (!viewsFile.exists()) {
            loggerCount.error("File does not exist: " + viewDefFilePath);

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

        
		SchemaProvider schemaProvider = new SchemaProviderImpl(conn,
				typeSystem, typeAlias);

        // since all we got until now are view definitions in a wrong format,
        // we have to convert them using a so called syntax bridge
        SyntaxBridge synBridge = new SyntaxBridge(schemaProvider);
        // the target collection holding the view definitions in the right
        //format
        Collection<ViewDefinition> viewDefs = new ArrayList<ViewDefinition>();
        // conversion loop
        for (org.aksw.sparqlify.config.syntax.ViewDefinition viewDef : syntaxViewDefs) {
            viewDefs.add(synBridge.create(viewDef));
        }
		
        
        /*
         * create measure data sink 
         */
//        MeasureDataSink sink = new H5Sink(h5FilePath);
        MeasureDataSink sink = new DummySink();
        
        
        
        /*
         * init quality assessment
         */
		QualityAssessment qa = new QualityAssessment(dataset, viewDefs, conn,
				dimensions, sink);
        
		qa.run();
        
		int foo = 23;
	}

}
