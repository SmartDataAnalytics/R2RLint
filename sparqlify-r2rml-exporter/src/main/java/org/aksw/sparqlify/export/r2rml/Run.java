package org.aksw.sparqlify.export.r2rml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.aksw.commons.util.MapReader;
import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProvider;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProviderDummy;
import org.aksw.sparqlify.config.v0_2.bridge.SyntaxBridge;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;
import org.aksw.sparqlify.validation.LoggerCount;
import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;

public class Run {
	
	private static final Logger logger = LoggerFactory.getLogger("R2RMLExporter");
	
	
	public static void main(String[] args) throws IOException, SQLException,
			ResultNumberMismatchError, RecognitionException {
		
		LoggerCount loggerCount = new LoggerCount(logger);
		Map<String, String> typeAlias = MapReader.read(
				new File("src/main/resources/type-map.h2.tsv"));
		/*
		 * In case you want to define the view definitions in place (as a
		 * string), uncomment the following lines
		 */
		
//		ViewDefinitionFactory vdf =
//				SparqlifyUtils.createDummyViewDefinitionFactory(typeAlias);
//		
//		ViewDefinition personView =
//				vdf.create(
//					"Prefix ex:<http://ex.org/> " +
//					"Prefix xsd:<http://www.w3.org/2001/XMLSchema#> " +
//					"Create View person As " +
//						"Construct {" +
//							"?s a ex:Person." +
//							"?u ex:worksFor ?s. " +
//							"?s ex:name \"Hugo\"@en. " +
//							"?s ex:blank []. " +
//						"} " +
//						"With " +
//							"?s = uri(concat('http://ex.org/person/', ?id) " +
//							"?t = typedLiteral(?name, xsd:string) " +
//							"?u = uri(concat(ex:emp, ?id)) " +
//						"From [[SELECT * FROM person]]"
//				);
//		
//		Collection<ViewDefinition> viewDefs = Arrays.asList(personView);

		
		/*
		 * Alternatively an SML view definition file can be specified by file
		 * path as shown below 
		 */
		String filePath = "/tmp/sparqlify/views.sparqlify";
		
		File configFile = new File(filePath);
		
		if (!configFile.exists()) {
			loggerCount.error("File does not exist: " + filePath);

		}

		
		ConfigParser parser = new ConfigParser();

		InputStream in = new FileInputStream(configFile);
		Config config;
		try {
			config = parser.parse(in, loggerCount);
		} finally {
			in.close();
		}
		
		List<org.aksw.sparqlify.config.syntax.ViewDefinition> syntaxViewDefs =
				config.getViewDefinitions();
		
		// there is no need to have a real typeSystem, since a dummy schema
		// provider is used later
		TypeSystem typeSystem = null;
		SchemaProvider schemaProvider =
				new SchemaProviderDummy(typeSystem, typeAlias);
		
		
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
		
		R2RMLExporter exporter = new R2RMLExporter(viewDefs);
		Model r2rml = exporter.export();
		
		// print the result to stdout
		r2rml.write(System.out, "TURTLE", "<http://foo.org/resources>");
	}

}
