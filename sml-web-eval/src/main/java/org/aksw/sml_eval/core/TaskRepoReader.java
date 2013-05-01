package org.aksw.sml_eval.core;


import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.aksw.commons.util.StreamUtils;
import org.aksw.commons.util.jdbc.DataSourceConfig;
import org.aksw.commons.util.jdbc.DataSourceConfigDefault;
import org.aksw.commons.util.jdbc.JdbcUtils;
import org.aksw.commons.util.jdbc.Relation;
import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.jolbox.bonecp.BoneCPDataSource;


class ResourceComparator
	implements Comparator<Resource>
{
	@Override
	public int compare(Resource a, Resource b) {
		return a.getFilename().compareTo(b.getFilename());
	}
}

public class TaskRepoReader
{
	private static final Logger logger = LoggerFactory.getLogger(TaskRepoReader.class);	

	private static final Comparator<Resource> resourceComparator = new ResourceComparator();	
	private static final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
	
	private String taskBasePath = "/org/aksw/sparqlify-web-eval/tasks/";

	public TaskRepo getTaskRepo() throws IOException, SQLException {
		List<TaskBundle> taskBundles = getTaskBundles();
		TaskRepo result = new TaskRepo(taskBundles);
		return result;
	}
	
	public List<TaskBundle> getTaskBundles() throws IOException, SQLException {
		List<TaskBundle> result = new ArrayList<TaskBundle>();
			
		
		Resource defaultsRes = resolver.getResource(taskBasePath + "defaults.properties");
		Properties defaults = new Properties();
		defaults.load(defaultsRes.getInputStream());

		
		Resource[] resources = resolver.getResources(taskBasePath + "task*");
		Arrays.sort(resources, resourceComparator);
	
		
		for (Resource r : resources) {
			TaskBundle tmp = process(defaults, r);
			result.add(tmp);
		}

		return result;
	}
	
	// Note treat file pattern r2rml(*).ttl
	public TaskBundle process(Properties defaults, Resource r) throws IOException, SQLException {
		//System.out.println(basePath + r.getFilename() + "/create.sql");

		
		String taskName = r.getFilename();
		
		String testPathStr = taskBasePath + taskName + "/";
		
		Resource taskPathRes = resolver.getResource(testPathStr);
		if(!taskPathRes.exists()) {
			logger.warn("Resource does not exist " + taskPathRes);
			return null;
		}

		// task.properties, mapping.sparqlify.sml, ref.nt, database.sql 
		Resource taskPropertiesRes = resolver.getResource(testPathStr + "task.properties");
		Resource mappingSparqlifyRes = resolver.getResource(testPathStr + "mapping.sparqlify.sml");
		Resource mappingSparqlMapRes = resolver.getResource(testPathStr + "mapping.sparqlmap.ttl");
		Resource databaseRes = resolver.getResource(testPathStr + "database.sql");
		Resource refSetRes = resolver.getResource(testPathStr + "ref.nt");
		
		Properties taskProperties = new Properties();
		taskProperties.load(taskPropertiesRes.getInputStream());
		
		String mappingSparqlify = StreamUtils.toString(mappingSparqlifyRes.getInputStream()); 
		String mappingSparqlMap = StreamUtils.toString(mappingSparqlMapRes.getInputStream());
		
		Map<String, String> mappings = new HashMap<String, String>();
		mappings.put("sparqlify", mappingSparqlify);
		mappings.put("sparqlmap", mappingSparqlMap);
		
		String database = StreamUtils.toString(databaseRes.getInputStream());
		
		taskProperties.put("sql", database);
		
		Model refSet = ModelFactory.createDefaultModel();
		refSet.read(refSetRes.getInputStream(), "http://example.org/", "N-TRIPLES");
		
		//List<Table> tables = getTablesFromSql(database);

		
		
		DataSourceConfigDefault dsc = new DataSourceConfigDefault();
		
		String host = defaults.getProperty("tasks.db.host");
		String name = taskName;
		String user = defaults.getProperty("tasks.db.user");
		String pass = defaults.getProperty("tasks.db.pass");
		
		String jdbcUrl = "jdbc:postgresql://" + host + "/" + name;
		
		dsc.setJdbcUrl(jdbcUrl);
		dsc.setUsername(user);
		dsc.setPassword(pass);
		
		
		DataSource ds = createDataSource(dsc);
		List<Table> tables = getTables(ds, "public");
		
		TaskBundle result = new TaskBundle(taskName, taskProperties, mappings, refSet, tables, dsc);

		return result;
		//System.out.println(createRes.getURI());
		//System.out.println(createRes.getFilename());
		//System.out.println("create exists? " + createRes.exists());
		
	}
	
	
//	public static List<Table> getTableListFromSql(String sql)
//			throws SQLException
//	{
//		List<Relation> result = new ArrayList<Relation>();
//	
//		Map<String, Relation> map = getTablesFromSql(sql); 
//		result.addAll(map.values());
//		
//		return result;
//	}
	
	public static DataSource createDataSource(DataSourceConfig dsc) {
		BoneCPDataSource result = new BoneCPDataSource();
		result.setJdbcUrl(dsc.getJdbcUrl());
		result.setUsername(dsc.getUsername());
		result.setPassword(dsc.getPassword());
		
		return result;
	}
	
	public static List<Table> getTables(DataSource dataSource, String schemaName) throws SQLException {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			List<Table> result = getTables(conn, schemaName);
			return result;
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
	}
	
	public static List<Table> getTables(Connection conn, String schemaName) throws SQLException {
		Set<String> tableNames = JdbcUtils.fetchRelationNames(conn);

		List<Table> result = new ArrayList<Table>();

		for(String tableName : tableNames) {
			Map<String, Relation> relations = JdbcUtils.fetchColumns(conn, schemaName, tableName);
			
			Relation relation = relations.values().iterator().next();
			
			Table table = createTable(conn, relation);
			result.add(table);
		}
		
		return result;
	}
	
	public static Table createTable(Connection conn, Relation relation) throws SQLException { 
		
		//logger.info("Relations: " + relations.keySet());
		
		List<Map<String, Object>> rowData = new ArrayList<Map<String, Object>>();
		
		String tableName = relation.getName();
		
		ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM \"" + tableName + "\"");
		ResultSetMetaData meta = rs.getMetaData();
		
		while(rs.next()) {
			Map<String, Object> row = new HashMap<String, Object>();
	
			for(int i = 1; i <= meta.getColumnCount(); ++i) {
				String columnName = meta.getColumnName(i);
				
				Object value = rs.getString(i); //rs.getObject(i);
				row.put(columnName, value);
			}
			
			rowData.add(row);
		}

		Table result = new Table(relation, rowData);
					
		return result;
	}
	
	public static List<Table> getTablesFromSql(String sql) throws SQLException {
		DataSource ds = createDefaultDatabase("testdb");
		Connection conn = ds.getConnection();
		String schemaName = "TEST";
		conn.createStatement().execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
		conn.createStatement().execute("SET SCHEMA " + schemaName);
		
		conn.createStatement().execute(sql);

		List<Table> result = getTables(conn, schemaName);
		
		conn.createStatement().execute("SHUTDOWN");
		
		return result;
	}
	
	
	public static DataSource createDefaultDatabase(String name) {
		JdbcDataSource ds = new JdbcDataSource();
		//ds.setURL("jdbc:h2:mem:" + name + ";mode=postgres");
		ds.setURL("jdbc:h2:mem:" + name + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
		ds.setUser("sa");
		ds.setPassword("sa");

		
		return ds;
	}
}