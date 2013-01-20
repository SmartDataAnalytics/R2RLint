package org.aksw.sparqlify.export.r2rml;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.aksw.sparqlify.algebra.sql.datatype.DatatypeSystem;
import org.aksw.sparqlify.algebra.sql.datatype.DatatypeSystemDefault;
import org.aksw.sparqlify.config.syntax.ViewDefinition;
import org.aksw.sparqlify.core.RdfView;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.sparqlview.SparqlView;

import com.hp.hpl.jena.query.QueryFactory;


public class R2RMLExporter {
	
	public R2RMLExporter() {
		RdfViewSystemOld.initSparqlifyFunctions();
		DatatypeSystem datatypeSystem = new DatatypeSystemDefault();
		DataSource dataSource = new DummyDataSource();
		try {
			Connection conn = dataSource.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		RdfView rdfview = RdfView.create("Construct { ?s a ex:Person ; ex:name ?t } " +
				"with ?s = uri(concat('http://ex.org/person/', ?ID) " +
				"?t = plainLiteral(?NAME) From person");
		SparqlView view = SparqlView.create("foo",
				QueryFactory.create("Construct { ?s a ex:Person ; ex:name ?t } " + 
						"With ?s = uri(concat('http://ex.org/person/', ?ID) " +
						"?t = plainLiteral(?NAME) From person")
		);
		
		ViewDefinition viewDef = new ViewDefinition();
	}
	
	

}
