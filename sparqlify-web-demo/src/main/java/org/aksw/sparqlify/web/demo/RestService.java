package org.aksw.sparqlify.web.demo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.util.MapReader;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.core.algorithms.CandidateViewSelectorImpl;
import org.aksw.sparqlify.core.datatypes.TypeSystem;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.CandidateViewSelector;
import org.aksw.sparqlify.core.interfaces.SparqlSqlRewriter;
import org.aksw.sparqlify.core.sparql.QueryExecutionFactorySparqlifyDs;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;
import org.h2.jdbcx.JdbcDataSource;

import com.google.gson.Gson;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

/**
 * Jersey resource for the QA Dashboard transition backend.
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 * 
 */
@Path("/service")
// @Produces("application/rdf+xml")
// @Produces("text/plain")
public class RestService {

	/**
	 * 
	 * @param context
	 *            The servlet context.
	 */
	public RestService(@Context ServletContext context) {
	}

	/**
	 * TODO Diese Methode mit POST Requests zum funktionieren zu bringen.
	 * 
	 * 
	 * @param sql
	 * @param mapping
	 * @param queryString
	 * @return
	 * @throws Exception
	 */
	@GET
	@Path("/sparqlify-demo-backend-test")
	@Produces(MediaType.APPLICATION_JSON)
	public String executeTest(@QueryParam("sql") String sql,
			@QueryParam("mapping") String mapping,
			@QueryParam("query") String queryString) throws Exception {
		// String rsStr = "test"; // TODO Zur Not hier ein Dummy RDF result set
		// eintragen
		// - am Besten wohl doch RDF Talis JSON:
		// http://docs.api.talis.com/platform-api/output-types/rdf-json
		String rsStr = "<http://example.org/A> <http://example.org/B> <http://example.org/C>";

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("sql", sql);
		map.put("mapping", mapping);
		map.put("queryString", queryString);
		map.put("result", rsStr);

		Gson gson = new Gson();
		String result = gson.toJson(map);

		return result;
	}

	@GET
	@Produces("text/plain")
	public String hello(@Context HttpServletRequest req,
			@QueryParam("sql") String sqlString) {
		try {
			HttpSession session = req.getSession(true);

			String sessionId = session.getId();
			System.out.println(sessionId);

			// Object oldVal = session.getAttribute("foo");

			JdbcDataSource dataSource = (JdbcDataSource) session
					.getAttribute("database");
			Connection conn;

			if (dataSource == null) {

				// Lade eine H2 Datenbank mit dem angegebenen SQL
				dataSource = new JdbcDataSource();
				dataSource.setURL("jdbc:h2:mem:" + sessionId);
				dataSource.setUser("sa");
				dataSource.setPassword("sa");

				conn = dataSource.getConnection();
				conn.createStatement().executeUpdate(
						"CREATE TABLE person (name VARCHAR)");

				session.setAttribute("database", dataSource);
			} else {
				conn = dataSource.getConnection();
			}

			conn.createStatement().executeUpdate(
					"INSERT INTO person VALUES ('" + sqlString + "');");

			String result = "";
			java.sql.ResultSet rs = conn.createStatement().executeQuery(
					"Select name AS c FROM person");
			while (rs.next()) {
				result += " " + rs.getString("name");
			}

			return result;
		} catch (SQLException e) {
			String msg = e.getMessage(); 
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("success", false);
			map.put("errormsg", msg);
			
			Gson gson = new Gson();
			String result = gson.toJson(map);
			
			return result;
		}

		// ExceptionUtils.getFullStackTrace(e);

	}

	/**
	 * 
	 * 
	 * 
	 * @param sql
	 * @param mapping
	 * @param queryString
	 * @return TODO RDF output
	 * @throws Exception
	 */
	@GET
	@Path("/sparqlify-demo-backend")
	@Produces(MediaType.APPLICATION_JSON)
	public String execute(@QueryParam("sql") String sql,
			@QueryParam("mapping") String mapping,
			@QueryParam("query") String queryString) throws Exception {
		// Lade eine H2 Datenbank mit dem angegebenen SQL
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setURL("jdbc:h2:mem:test_mem;mode=postgres");
		dataSource.setUser("sa");
		dataSource.setPassword("sa");

		Connection conn = dataSource.getConnection();
		conn.createStatement()
				.executeUpdate(
						"CREATE TABLE person (id INT PRIMARY KEY, name VARCHAR, age INT)");

		conn.createStatement().executeUpdate(
				"INSERT INTO person VALUES (1, 'Anne', 20)");

		// conn.createStatement().executeQuery("Select * From person");

		// Erstellen der Mappings
		Map<String, String> typeAlias = MapReader
				.readFromResource("/type-map.h2.tsv");

		ViewDefinitionFactory vdf = SparqlifyUtils.createViewDefinitionFactory(
				conn, typeAlias);

		ViewDefinition personView = vdf
				.create("Prefix ex:<http://ex.org/> Create View person As Construct { ?s a ex:Person ; ex:name ?t } With ?s = uri(concat('http://ex.org/person/', ?ID) ?t = plainLiteral(?NAME) From person");

		// Laden der Mappings in die Sparqlify engine
		CandidateViewSelector candidateViewSelector = new CandidateViewSelectorImpl();
		candidateViewSelector.addView(personView);

		// Initialisieren von Sparqlify
		RdfViewSystemOld.initSparqlifyFunctions();
		TypeSystem datatypeSystem = SparqlifyUtils
				.createDefaultDatatypeSystem();
		// SqlTranslator sqlTranslator = new SqlTranslatorImpl(datatypeSystem);

		SparqlSqlRewriter rewriter = SparqlifyUtils.createTestRewriter(
				candidateViewSelector, datatypeSystem);
		QueryExecutionFactory qef = new QueryExecutionFactorySparqlifyDs(
				rewriter, dataSource);

		// Execute the SPARQL query
		QueryExecution qe = qef
				.createQueryExecution("Select ?s ?p ?o { ?s ?p ?o }");
		ResultSet rs = qe.execSelect();
		String rsStr = ResultSetFormatter.asText(rs);

		// QueryExecution qe =
		// qef.createQueryExecution("Construct { ?s ?p ?o } WHERE { ?s ?p ?o }");
		// Model model = qe.execConstruct();
		// String rsStr = ModelUtils.toString(model);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("sql", sql);
		map.put("mapping", mapping);
		map.put("queryString", queryString);
		map.put("result", rsStr);

		Gson gson = new Gson();
		String result = gson.toJson(map);

		return result;
	}
}
