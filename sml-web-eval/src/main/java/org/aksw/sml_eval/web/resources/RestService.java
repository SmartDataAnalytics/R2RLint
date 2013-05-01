package org.aksw.sml_eval.web.resources;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.util.MapReader;
import org.aksw.commons.util.slf4j.LoggerCount;
import org.aksw.sml_eval.core.ModelUtils;
import org.aksw.sml_eval.core.Store;
import org.aksw.sml_eval.core.TaskRepo;
import org.aksw.sml_eval.mappers.Adapter;
import org.aksw.sml_eval.mappers.MapResult;
import org.aksw.sml_eval.mappers.MapperFactory;
import org.aksw.sml_eval.mappers.MapperFactorySparqlify;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.core.algorithms.CandidateViewSelectorImpl;
import org.aksw.sparqlify.core.cast.NewWorldTest;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.CandidateViewSelector;
import org.aksw.sparqlify.core.interfaces.OpMappingRewriter;
import org.aksw.sparqlify.core.interfaces.SparqlSqlRewriter;
import org.aksw.sparqlify.core.sparql.QueryExecutionFactorySparqlifyDs;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;
import org.aksw.sparqlify.web.ProcessQuery;
import org.aksw.sparqlify.web.SparqlFormatterUtils;
import org.aksw.sparqlify.web.StreamingOutputString;
import org.antlr.runtime.RecognitionException;
import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.http.HttpParams;

/**
 * Jersey resource for the QA Dashboard transition backend.
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 * 
 */
@Component
@Path("/0.1")
public class RestService {
	
	private static final Logger logger = LoggerFactory.getLogger(RestService.class);

	@Resource(name="smlEval.taskRepo")
	private TaskRepo taskRepo;
	
//	@Resource(name="smlEval.dataSource")
//	private DataSource dataSource;
	
	@Resource(name="smlEval.store")
	private Store store;

	
	@Resource(name="smlEval.mapperFactory.sml")
	private MapperFactorySparqlify mapperSml;
	
	private MapperRepo mapperRepo;
	
	@PostConstruct
	private void init() {
		Map<String, MapperFactory> toolToFactory = new HashMap<String, MapperFactory>();
		toolToFactory.put("sml", mapperSml);
		
		mapperRepo = MapperRepo.create(taskRepo, toolToFactory);
	}
	
	//private @Context ServletContext context;
	
	/**
	 * 
	 * @param context
	 *            The servlet context.
	 */
	public RestService() {
	}

	
	@POST
	@Path("/logout")
	public void logout(@Context HttpServletRequest req) {
		HttpSession session = req.getSession();

		try {
			Object tmpUserId = session.getAttribute("userId");
			if(tmpUserId == null) {
				throw new RuntimeException("Not logged in");
			}
		} finally {
			session.invalidate();
		}
	}
		

	
	/*
	 * User management
	 */
	@POST
	@Path("/login")
	@Produces(MediaType.APPLICATION_JSON)
	public String loginClassic(@Context HttpServletRequest req, @FormParam("username") String username, @FormParam("password") String password) throws SQLException {
		// Create a WebEval object, and put it into the session
		HttpSession session = req.getSession();
		
		Object tmpUserId = session.getAttribute("userId");
		if(tmpUserId != null) {
			throw new RuntimeException("Already logged in");
		}
		
		Integer userId = store.authenticate(username, password);
		if(userId == null) {
			throw new RuntimeException("Could not authenticate");
		}

		session.setAttribute("userId", userId);
		//session.get
		
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("success", true);
		
		return toJsonString(response);
	}
	
	/*
	 * 
	 * @param username
	 * @param password
	 */
	@POST
	@Path("/register")
	@Produces(MediaType.APPLICATION_JSON)
	public String registerClassic(
			@Context HttpServletRequest req,
			@FormParam("username") String username,
			@FormParam("password") String password,
			@FormParam("passwordConfirm")String passwordConfirm,
			@FormParam("email")String email,
			@FormParam("emailConfirm")String emailConfirm) throws SQLException
	{
		Map<String, Object> response = new HashMap<String, Object>();
		
		// TODO Validation
//		if(password == null) {
//			response.put("isPasswordValid", false);
//		}
				
		Integer userId = store.registerUser(username, password, email);
		if(userId == null) {
			throw new RuntimeException("Could not register user");
		}
		
		response.put("userId", userId);
		//response.put("success", value);
		
		
		return toJsonString(response);
	}
	
	@POST
	@Path("/fetchState")
	@Produces(MediaType.APPLICATION_JSON)
	public String fetchState(@Context HttpServletRequest req) throws SQLException {
		
		HttpSession session = req.getSession();
		Integer userId = (Integer)session.getAttribute("userId");
		
		Map<String, Object> response = new HashMap<String, Object>();
		
		boolean isLoggedIn = userId != null;
		String evalMode = store.getEvalMode(userId);
		response.put("isLoggedIn", isLoggedIn);
		
		// Note: The client has no control of the eval mode
		// Its purely informational
		response.put("evalMode", evalMode);
		
		// For each task, fetch the appropriate mapping
		
		return toJsonString(response);
	}

	
	/**
	 * Informs the client about which tasks are open / finished
	 * Also contains whether it is possible to advance to the next eval stage
	 * or whether the survey has been completed
	 * 
	 * @return
	 */
	@Path("/fetchSummary")
	@Produces(MediaType.APPLICATION_JSON)
	public String fetchSummary() {
		return "{}";
	}
	
	/**
	 * Advances to the next eval mode (if it exists)
	 * 
	 * @return
	 */
	@Path("/advance")
	@Produces(MediaType.APPLICATION_JSON)
	public String advance() {
		return "{}";
	}
	
	
	/**
	 * Advances to the next eval mode (if it exists)
	 * 
	 * @return
	 */
	@POST
	@Path("/runMapping")
	@Produces(MediaType.APPLICATION_JSON)
	public String runMapping(@Context HttpServletRequest req, @FormParam("taskId") String taskId, @FormParam("mapping") String mappingStr) {
		
		Integer userId = requireUserId(req);
		
		MapResult mr = runMappingCore(userId, taskId, mappingStr);
		
		Map<String, Object> response = createJson(taskId, mr, null);
		
		return toJsonString(response);
	}
	
	
	public static Map<String, Object> createJson(String taskId, MapResult mr, Boolean isSolution) {

		Object o = ModelUtils.toJsonObject(mr.getModel());
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("task", taskId);
		result.put("model", o);
		//result.put("isSolution", false);
		result.put("messages", mr.getMessages());
		
		if(isSolution != null) {
			result.put("isSolution", isSolution);
		}
		
		return result;
	}
	
	
	public String requireEvalMode(Integer userId) throws SQLException {
		String result = store.getEvalMode(userId);
		
		if(result == null) {
			throw new RuntimeException("Eval mode required");
		}
		
		return result;
	}

	public Integer requireUserId(@Context HttpServletRequest req) {
		HttpSession session = req.getSession();
		
		Integer userId = (Integer)session.getAttribute("userId");

		if(userId == null) {
			throw new RuntimeException("Must be logged in");
		}
		
		return userId;
	}
		
	public MapResult runMappingCore(Integer userId, String taskId, String mappingStr) {
		
				
		MapResult mr;
		try {
			
			//boolean isLoggedIn = userId != null;
			String evalMode = store.getEvalMode(userId);
			
			if(evalMode == null) {
				throw new RuntimeException("No eval mode exists. Either the survey was completed or we have an internal error");
			}
	
			Adapter mapper = mapperRepo.getMapper(evalMode, taskId);
			mr = mapper.map(mappingStr);
			
			
			//mr = adapterSml.map(mappingStr);
		} catch(Exception e) {
			logger.error("Something went wrong", e);
			throw new RuntimeException(e);			
		}
		
		return mr;
	}
	
	
	
	
	
	public static String toJsonString(Object o) {
		Gson gson = new Gson();
		String result = gson.toJson(o);
		return result;
	}
	
	/**
	 * Return the user's mapping that solved the task (null if the task was not solved)
	 */
	public void getSolutionMapping(String taskName) {
		
	}
	
	
	/**
	 * Sets the user's mapping for the given task.
	 * Logs the action
	 * 
	 * 
	 * @param taskName
	 * @param mapping
	 * @throws SQLException 
	 */
	@POST
	@Path("/submitMapping")
	@Produces(MediaType.APPLICATION_JSON)
	public String submitMapping(@Context HttpServletRequest req, @FormParam("taskId") String taskId, @FormParam("mapping") String mappingStr) throws SQLException {
		//HttpSession session = req.getSession();

		Integer userId = requireUserId(req);
		String evalMode = requireEvalMode(userId);
		
		
		Model expected = taskRepo.getReferenceModel(taskId);

		// store.writeMapping(...)

		
		Integer submissionId = store.writeMapping(userId, evalMode, taskId, mappingStr);
		
		MapResult mr = runMappingCore(userId, taskId, mappingStr);
		
		// Compate the result
		Model actual = mr.getModel();
		boolean isTaskSolved = expected.isIsomorphicWith(actual);
		
		if(isTaskSolved) {
			store.setSolution(submissionId);
		}
		
		Map<String, Object> response = createJson(taskId, mr, isTaskSolved);
		
		return toJsonString(response);
	}
	
	
	/**
	 * Returns the difference to the reference set
	 * 
	 * TODO Maybe this should already generate HTML mimicing colored turtle.
	 * 
	 * @param taskName
	 */
	public void getDiff(String taskName) {
		
	}
	
	
	public void getTables(String taskName) {
		
	}

	
	@GET
	@Path("/fetchTasks")
	@Produces(MediaType.APPLICATION_JSON)
	public String fetchTasks() {
		String result = taskRepo.toJson();
		return result;
	}
	
//	public void setEval(String) {
//		
//	}
	
	
	
	/**
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
	@Path("/setSql")
	public String setSql(@Context HttpServletRequest req,
			@QueryParam("sql") String sqlString) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			HttpSession session = req.getSession(true);

			String sessionId = session.getId();
			System.out.println(sessionId);

			JdbcDataSource dataSource = (JdbcDataSource) session
					.getAttribute("database");
			
			if (dataSource != null) {
				Connection conn;
				conn = dataSource.getConnection();
				conn.createStatement().execute("SHUTDOWN");

			}
			
			Connection conn;

			// Lade eine H2 Datenbank mit dem angegebenen SQL
			dataSource = new JdbcDataSource();
			dataSource.setURL("jdbc:h2:mem:" + sessionId + ";DB_CLOSE_DELAY=-1");
			dataSource.setUser("sa");
			dataSource.setPassword("sa");

			conn = dataSource.getConnection();

			session.setAttribute("database", dataSource);

			conn.createStatement().executeUpdate(sqlString);

			map.put("success", true);

		} catch (SQLException e) {
			String msg = e.getMessage(); 
			
			map.put("success", false);
			map.put("errormsg", msg);
			
		}
		Gson gson = new Gson();
		String result = gson.toJson(map);
		return result;
	}

//	// TODO: setMapping(String mappingStr)
//	@GET
//	@Path("/setMapping")
//	@Produces("text/plain")
//	public String setMapping(@Context HttpServletRequest req,
//			@QueryParam("mapping") String mapping){
//		Map<String, Object> map = new HashMap<String, Object>();
//		 try{
//			HttpSession session = req.getSession(true);
//
//			String sessionId = session.getId();
//			System.out.println(sessionId);
//			JdbcDataSource dataSource = (JdbcDataSource) session
//					.getAttribute("database");
//			Connection conn = null;
//
//			// Erstellen der Mappings
//			Map<String, String> typeAlias = MapReader.readFromResource("/type-map.h2.tsv");
//
//			ViewDefinitionFactory vdf = SparqlifyUtils.createViewDefinitionFactory(conn, typeAlias);
//
//			ViewDefinition vd = vdf.create(mapping);
//			
//			map.put("success", true);
//		 }
//		 catch (IOException e){
//				String msg = e.getMessage(); 
//				
//				map.put("success", false);
//				map.put("errormsg", msg);
//		 }
//			
//		Gson gson = new Gson();
//		String result = gson.toJson(map);
//		return result;
//	}
	
	// TODO: getTriples() Wenn Fehler: dann z.B. success = false (die ist in getMapping() zu definieren)
	
	/**
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

		// Erstellen der Mappings
		Map<String, String> typeAlias = MapReader
				.readFromResource("/type-map.h2.tsv");

		ViewDefinitionFactory vdf = SparqlifyUtils.createViewDefinitionFactory(
				conn, typeAlias);

		ViewDefinition personView = vdf
				.create("Prefix ex:<http://ex.org/> Create View person As Construct { ?s a ex:Person ; ex:name ?t } With ?s = uri(concat('http://ex.org/person/', ?ID) ?t = plainLiteral(?NAME) From person");

		
		// Laden der Mappings in die Sparqlify engine
		CandidateViewSelector<ViewDefinition> candidateViewSelector = new CandidateViewSelectorImpl();
		candidateViewSelector.addView(personView);

		RdfViewSystemOld.initSparqlifyFunctions();
		TypeSystem typeSystem = NewWorldTest.createDefaultDatatypeSystem();
		OpMappingRewriter opMappingRewriter = SparqlifyUtils.createDefaultOpMappingRewriter(typeSystem);

		// Initialisieren von Sparqlify

		SparqlSqlRewriter rewriter = SparqlifyUtils.createTestRewriter(
				candidateViewSelector, opMappingRewriter, typeSystem);
		QueryExecutionFactory qef = new QueryExecutionFactorySparqlifyDs(
				rewriter, dataSource);

		// Execute the SPARQL query
		QueryExecution qe = qef
				.createQueryExecution("Select ?s ?p ?o { ?s ?p ?o }");
		ResultSet rs = qe.execSelect();
		String rsStr = ResultSetFormatter.asText(rs);

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
	@Path("/listTables")
	@Produces(MediaType.TEXT_PLAIN)
	public String getTables(@Context HttpServletRequest req) throws Exception {
		HttpSession session = req.getSession(true);
		JdbcDataSource dataSource = (JdbcDataSource)session.getAttribute("database");
		if(dataSource == null) {
			throw new RuntimeException("No data source exists - probably no or errornous sql was sent");
		}

		
		Connection conn = dataSource.getConnection();
		List<String> tables;
		try {
			tables = SparqlifyUtils.listTables(conn);
		}
		finally {
			conn.close();
		}
		
		Gson gson = new Gson();
		String result = gson.toJson(tables);

		return result;
	}
	
	@GET
	@Path("/getTriples")
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput getTriples(@Context HttpServletRequest req) throws Exception {
		HttpSession session = req.getSession(true);

		String queryString = "Construct { ?s ?p ?o } { ?s ?p ?o }";
		StreamingOutput result = processQuery(req, queryString, SparqlFormatterUtils.FORMAT_Json);
		return result;
	}
	
	@GET
	@Path("/setMapping")
	@Produces(MediaType.APPLICATION_JSON)
	public String setMapping2(@Context HttpServletRequest req, @QueryParam("mapping") String sparqlifyMapping) throws IOException, RecognitionException 
	{
		HttpSession session = req.getSession(true);

		LoggerCount loggerCount = new LoggerCount(logger);
		Config config = SparqlifyUtils.createConfig(sparqlifyMapping, loggerCount);
		
		if(loggerCount.getErrorCount() > 0) {
			throw new RuntimeException("Errors in the input");
		}
		
		session.setAttribute("sparqlifyConfig", config);
		
		
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("success", true);
		
		Gson gson = new Gson();
		String result = gson.toJson(map);

		return result;
	}
	
	
	
	public QueryExecutionFactory buildQueryExecutionFactory(HttpServletRequest req)
			throws SQLException, IOException
	{
		HttpSession session = req.getSession(true);

		JdbcDataSource dataSource = (JdbcDataSource)session.getAttribute("database");
		if(dataSource == null) {
			throw new RuntimeException("No data source exists - probably no or errornous sql was sent");
		}

		
		Config config = (Config)session.getAttribute("sparqlifyConfig");
		if(config == null) {
			throw new RuntimeException("No Sparqlify mapping exists - probably no or errornous data was sent");
		}
		
		//Config config = SparqlifyUtils.readConfig(bundle.getMapping().getInputStream());
		//DataSource ds = SparqlifyUtils.createDefaultDatabase("test", bundle.getSql().getInputStream());
		
		
		QueryExecutionFactory qef = SparqlifyUtils.createDefaultSparqlifyEngine(dataSource, config, null, null);
		
		return qef;
	}
	
	
	
	public StreamingOutput processQuery(HttpServletRequest req, String queryString, String format)
			throws Exception
	{
		QueryExecutionFactory qef = buildQueryExecutionFactory(req);
		
		return ProcessQuery.processQuery(queryString, format, qef);
	}

	/*
	@GET
	public String executeQueryXml()
			throws Exception {
		String example = "<?xml version='1.0' encoding='ISO-8859-1'?><xml>Select * { ?s ?p ?o } Limit 10</xml>";
		return "No query specified. Example: ?query=" + StringUtils.urlEncode(example);
	}
	*/

	/*
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String executeQuery()
			throws Exception {
		return "No query specified";
	}
	*/
	

	/*
	@GET
	public StreamingOutput executeQueryXml(@QueryParam("query") String queryString)
			throws Exception {
		return processQuery(queryString, SparqlFormatterUtils.FORMAT_XML);
	}
	*/

	
	//@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public StreamingOutput executeQueryXml(@Context HttpServletRequest req, @QueryParam("query") String queryString)
			throws Exception {

		if(queryString == null) {
			return StreamingOutputString.create("<error>No query specified. Append '?query=&lt;your SPARQL query&gt;'</error>");
		}
		
		return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_XML);
	}

	
	//@Produces(MediaType.APPLICATION_XML)
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public StreamingOutput executeQueryXmlPost(@Context HttpServletRequest req, @FormParam("query") String queryString)
			throws Exception {

		if(queryString == null) {
			return StreamingOutputString.create("<error>No query specified. Append '?query=&lt;your SPARQL query&gt;'</error>");
		}
		
		return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_XML);
	}

	@GET
	@Produces({MediaType.APPLICATION_JSON, "application/sparql-results+json"})
	public StreamingOutput executeQueryJson(@Context HttpServletRequest req, @QueryParam("query") String queryString)
			throws Exception {
		return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_Json);
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_JSON, "application/sparql-results+json"})
	public StreamingOutput executeQueryJsonPost(@Context HttpServletRequest req, @FormParam("query") String queryString)
			throws Exception {
		return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_Json);
	}
	
	//@Produces("application/rdf+xml")
	//@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@GET
	@Produces(HttpParams.contentTypeRDFXML)
	public StreamingOutput executeQueryRdfXml(@Context HttpServletRequest req, @QueryParam("query") String queryString)
			throws Exception {
		return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_RdfXml);
	}	
	

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(HttpParams.contentTypeRDFXML)
	public StreamingOutput executeQueryRdfXmlPost(@Context HttpServletRequest req, @FormParam("query") String queryString)
			throws Exception {
		return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_RdfXml);
	}

	@GET
	@Produces("application/sparql-results+xml")
	public StreamingOutput executeQueryResultSetXml(@Context HttpServletRequest req, @QueryParam("query") String queryString)
			throws Exception {
		return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_XML);
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces("application/sparql-results+xml")
	public StreamingOutput executeQueryResultSetXmlPost(@Context HttpServletRequest req, @FormParam("query") String queryString)
			throws Exception {
		return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_XML);
	}	
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public StreamingOutput executeQueryText(@Context HttpServletRequest req, @QueryParam("query") String queryString)
			throws Exception {
		return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_Text);
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	public StreamingOutput executeQueryTextPost(@Context HttpServletRequest req, @FormParam("query") String queryString)
			throws Exception {
		return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_Text);
	}
}
