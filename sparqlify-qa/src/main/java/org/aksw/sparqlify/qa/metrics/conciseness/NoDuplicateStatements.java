package org.aksw.sparqlify.qa.metrics.conciseness;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.commons.collections.Pair;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DbMetric;
import org.aksw.sparqlify.qa.metrics.MappingMetric;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;

/*
 * This metric should check if there are duplicates in a dataset and pinpoint
 * to the quad patterns that cause them. Unfortunately the Jena Model class
 * used, already eliminates duplicates so another (more complex) approach is
 * used here. So instead of just finding duplicates and calling the pinpointer
 * possible duplicates have to be computed. This is done in two steps:
 * 
 * 1) it is checked if there are duplicates created by one quad pattern
 *    In this case the duplicates result from duplicate values in the (logical)
 *    table (maybe because of awkward joins etc.). This is done by the
 *    assessViewDefinition method.
 * 
 * 2) it is checked if there are duplicates resulting from different quad
 *    patterns (from possibly different view definitions)
 *    First the power set of all possible combinations of all view definitions
 *    is created and then all these combinations are checked for duplicates
 *    resulting from the combination (not considering duplicates, that are
 *    caused by duplicate database values and are already detected in 1)).
 * 
 * So, the call workflow of this metric is:
 * 
 * - assessMappings
 *   - assessViewDefinition  // pattern duplicates check
 *   - exhaustivelyAssessViewDefinitions  // power set creation
 *     - assessViewDefinitions  // duplicate candidates for every power set entry
 *     - assessQuads  // actual duplication checks for candidates from above
 */
public class NoDuplicateStatements extends DbMetric implements MappingMetric {
	
	private boolean exhaustively = true;
	
	// Caching to avoid calculating the distinct counts of quad patterns twice:
	// this count values will be needed once when checking for pattern caused
	// duplicates and later when checking for duplicates created by multiple
	// quad patterns
	private HashMap<String, Integer> distinctCounts;
	private HashMap<String, String> relationNames;
	
	// needed for view def power set creation that will not work with
	// ViewDefinition objects since they are not comparable
	private HashMap<String, ViewDefinition> viewDefMap;
	
	private String separator = "//";
	

	public NoDuplicateStatements() {
		super();
		distinctCounts = new HashMap<String, Integer>();
		relationNames = new HashMap<String, String>();
		viewDefMap = new HashMap<String, ViewDefinition>();
	}


	@Override
	public void assessMappings(Collection<ViewDefinition> viewDefs)
			throws NotImplementedException {

		for (ViewDefinition viewDef : viewDefs) {
			// 1) checked if there are duplicates created by one quad pattern
			assessViewDefinition(viewDef);
		}
		if (exhaustively) {
			// 2) 
			exhaustivelyAssessViewDefinitions(viewDefs);
		}
	}


	/**
	 * Finds duplicates of one quad pattern that is caused by duplicate
	 * database entries.
	 * 
	 * @param viewDef
	 *         the considered view definition
	 * @throws NotImplementedException
	 */
	private void assessViewDefinition(ViewDefinition viewDef)
			throws NotImplementedException {
		
		String relation = readRelation(viewDef);
		
		// needed for later power set creation that will not work with
		// ViewDefinition objects since they are not comparable
		viewDefMap.put(viewDef.getName(), viewDef);
		
		VarDefinition varDefs = viewDef.getMapping().getVarDefinition();
		
		for (Quad quadPattern : viewDef.getTemplate()) {
			
			Set<String> referencedColumns = readReferencedColumns(quadPattern, varDefs);
			
			String countDistinctQuery =  buildCountDistinctQuery(relation, referencedColumns);
			String countAllQuery = buildCountAllQuery(relation, referencedColumns);
			
			int distinctCount = -1;
			int allCount = -1;
			ResultSet res = null;
			
			try {
				res = conn.createStatement().executeQuery(countDistinctQuery);
				res.next();
				distinctCount = res.getInt("count");
				
				res = conn.createStatement().executeQuery(countAllQuery);
				res.next();
				allCount = res.getInt("count");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			// Caching to avoid calling all this twice: the distinct count
			// values will be needed later when checking for duplicates created
			// by multiple quad patterns
			String key = viewDef.getName() + separator + quadPattern.toString();
			distinctCounts.put(key, distinctCount);
			
			int diff = allCount - distinctCount;
			if (diff > 0 ) {	
				float metricValue = 1 - (diff / (float) allCount);
				if (metricValue < threshold || threshold == 0) {
					List<Pair<Quad, ViewDefinition>> quadViewDefs =
							new ArrayList<Pair<Quad, ViewDefinition>>();
					
					quadViewDefs.add(new Pair<Quad, ViewDefinition>(quadPattern, viewDef));
					
					writeMappingQuadMeasureToSink(metricValue, quadViewDefs);
				}
			}
		}
	}


	/**
	 * This method creates the power set of all view definitions and calls
	 * assessViewDefinitions to check for duplicates in any of the powerset
	 * combinations
	 * 
	 * @param viewDefs
	 *         collection of view definitions
	 * @throws NotImplementedException
	 */
	private void exhaustivelyAssessViewDefinitions(
			Collection<ViewDefinition> viewDefs) throws NotImplementedException {
		
		// since ViewDefinition cannot be cast to java.lang.Comparable (and so
		// there cannot be a Set of ViewDefinitions) the view definition names
		// are used here and mapped back to the view definitions via the
		/// viewDefMap created previously
		Set<String> viewDefNames = viewDefMap.keySet();
		Set<Set<String>> pSet = Sets.powerSet(viewDefNames);

		for (Set<String> viewDefSubSet : pSet) {
			assessViewDefinitions(viewDefSubSet);
		}
	}


	/**
	 * Warning: this method assumes to be called after the assessViewDefinition
	 * method and relies on an already set up
	 * - distinctCounts hash map
	 * - viewDefMap hash map
	 * and would do nothing otherwise (or break).
	 */
	private void assessViewDefinitions(Set<String> viewDefNames) throws NotImplementedException {
		
		HashMap<String, List<Pair<Quad, ViewDefinition>>> patterns =
				new HashMap<String, List<Pair<Quad, ViewDefinition>>>();
		
		/*
		 *  find candidates to assess
		 *  These candidates are quad patterns that share the same signature,
		 *  e.g. variable rdf:type ex:Department or variable ex:foo variable.
		 *  These signatures are put into a hash map to be able to check for
		 *  candidates later.
		 */
		for (String viewDefName : viewDefNames) {
			// assume that all view defs are already in the hash map
			ViewDefinition viewDef = viewDefMap.get(viewDefName);
			
			VarDefinition varDefs = viewDef.getVarDefinition();
			
			for (Quad quadPattern : viewDef.getTemplate()) {
				
				// build a canonical key for the patterns hash map:
				// - variables are replaced by their term constructor
				//   serialization
				// - constants string representations are taken as is 
				String key = "";
				
				
				/* get key part of the quad pattern's subject */
				Node subject = quadPattern.getSubject();
				if (subject.isVariable()) {
					Collection<RestrictedExpr> varRestrs =
							varDefs.getDefinitions(Var.alloc((Node_Variable) subject));
					
					String varTermConstructor = "";
					// dummy loop since there should only one restriction
					for (RestrictedExpr varRestr : varRestrs) {
						varTermConstructor = varRestr.getExpr().toString() + " ";
					}
					key += varTermConstructor;
				}
				else key += subject.toString() + " ";
				
				/* get key part of the quad pattern's predicate */
				Node predicate = quadPattern.getPredicate();
				if (predicate.isVariable()) {
					Collection<RestrictedExpr> varRestrs =
							varDefs.getDefinitions(Var.alloc((Node_Variable) predicate));
					
					String varTermConstructor = "";
					// dummy loop since there should only one restriction
					for (RestrictedExpr varRestr : varRestrs) {
						varTermConstructor = varRestr.getExpr().toString() + " ";
					}
					key += varTermConstructor;
				}
				else key += predicate.toString() + " ";
				
				/* get key part of the quad pattern's object */
				Node object = quadPattern.getObject();
				if (object.isVariable()) {
					Collection<RestrictedExpr> varRestrs =
							varDefs.getDefinitions(Var.alloc((Node_Variable) object));
					
					String varTermConstructor = "";
					// dummy loop since there should only one restriction
					for (RestrictedExpr varRestr : varRestrs) {
						varTermConstructor = varRestr.getExpr().toString() + " ";
					}
					key += varTermConstructor;
					
				} else key += object.toString();
				
				// add key and value (quad-view definition pair) to patterns
				// hash map
				if (patterns.containsKey(key)) {
					// key already exists
					patterns.get(key).add(new Pair<Quad, ViewDefinition>(quadPattern, viewDef));
					
				} else {
					// new key entry must be created
					ArrayList<Pair<Quad, ViewDefinition>> pairList =
							new ArrayList<Pair<Quad, ViewDefinition>>();
					pairList.add(new Pair<Quad, ViewDefinition>(quadPattern, viewDef));
					patterns.put(key, pairList);
				}
			}
		}
		
		// get quads and view defs that need a closer look (i.e. these quad
		// patterns that have multiple values, i.e. that are used more than
		// once)
		for (List<Pair<Quad, ViewDefinition>> quadViewDefs : patterns.values()) {
			if (quadViewDefs.size() >= 1) {
				assessQuads(quadViewDefs);
			}
			
		}
	}


	/**
	 * This methods checks if there are duplicates for a given list of
	 * candidates. These candidates are quad view definition pairs sharing the
	 * same quad signature, i.e. the quads were build based on the same variable
	 * and constant structure (e.g. "variable rdf:type ex:Department") and all
	 * variables are built with the same term constructor (same term constructor
	 * functions involved and same column variables used -- no restriction to
	 * use the same (logical) database table)
	 * 
	 * So here mainly one SQL query is built that looks sth. like this
	 * - SELECT count(*) AS count
	 *   FROM (
	 *       SELECT DISTINCT <involved variables> FROM (
	 *           (
	 *               // first quad
	 *               SELECT DISTINCT <involved variables>
	 *               FROM (<(logical) table>) AS inner_query
	 *               WHERE <all involved variables are not NULL>
	 *           ) UNION (
	 *               // second quad
	 *               SELECT DISTINCT <involved variables>
	 *               FROM (<(logical) table>) AS inner_query
	 *               WHERE <all involved variables are not NULL>
	 *           ) UNION (
	 *               // third quad
	 *               ...
	 *           )
	 *       ) foo
	 *   ) outer_foo
	 *   
	 * This query gives the summed count of
	 *   all distinct values of
	 *     the union of
	 *       all distinct values of every involved quad pattern
	 *  
	 * This count is then compared to the summed count of
	 *   all distinct values of
	 *     every involved quad pattern
	 * 
	 * So, if there are for example two patterns with the distinct triple counts
	 * as follows:
	 * 
	 * - patternA: 10 distinct triples
	 * - patternB:  5 distinct triples
	 * 
	 * and the count of the big query above gives a total count of 13 triples,
	 * there must be 2 duplicates.
	 * If the count retrieved from the query is 15, then the sets of triples
	 * created by patternA and patternB respectively would be distinct.
	 * 
	 * After computing these counts possible duplicates are reported to the
	 * sink.
	 * 
	 * @param quadViewDefs
	 * @throws NotImplementedException
	 */
	private void assessQuads(List<Pair<Quad, ViewDefinition>> quadViewDefs) throws NotImplementedException {
		
		int sumCountEach = 0;
		String queryStr = "";
		
		Set<String> colVarStrs = new TreeSet<String>();
		
		for (Pair<Quad, ViewDefinition> quadViewDef : quadViewDefs) {
			String subQueryStr = "(";
			
			Quad quad = quadViewDef.first;
			ViewDefinition viewDef = quadViewDef.second;
			
			VarDefinition varDefs = viewDef.getVarDefinition();
			
			// get the per-pattern distinct triples count from the hash map
			String key = viewDef.getName() + separator + quad.toString();
			sumCountEach += distinctCounts.get(key);
			
			/*
			 * this is for the first time running into this loop:
			 * Since all quads of quadViewDefs share the same signature, the
			 * set of column variables involved is also the same for all quads.
			 * So the colVars set of the column variable names of this concrete
			 * case do not have to be computed every time, but can rather be
			 * re-used form the first comutation
			 */
			if (colVarStrs.isEmpty()) {
			
				/* subject columns (if subject is variable) */
				Node subject = quad.getSubject();
				if (subject.isVariable()) {
					Collection<RestrictedExpr> termConstructors = varDefs
							.getDefinitions(Var.alloc((Node_Variable) subject));
					
					for (RestrictedExpr tc : termConstructors) {
						Set<Var> vars = tc.getExpr().getVarsMentioned();
						colVarStrs = vars2Strs(vars);
					}
				}
			
				/* predicate columns (if predicate is variable) */
				Node predicate = quad.getPredicate();
				if (predicate.isVariable()) {
					Collection<RestrictedExpr> termConstructors = varDefs
							.getDefinitions(Var.alloc((Node_Variable) predicate));
				
					for (RestrictedExpr tc : termConstructors) {
						Set<Var> vars = tc.getExpr().getVarsMentioned();
						colVarStrs.addAll(vars2Strs(vars));
					}
				}
			
				/* object columns (if object is variable) */
				Node object = quad.getPredicate();
				if (object.isVariable()) {
					Collection<RestrictedExpr> termConstructors = varDefs
							.getDefinitions(Var.alloc((Node_Variable) object));
				
					for (RestrictedExpr tc : termConstructors) {
						Set<Var> colVars = tc.getExpr().getVarsMentioned();
					
						colVarStrs.addAll(vars2Strs(colVars));
					}
				}
			}
			
			/*
			 * subQueryStr then looks sth like this:
			 * SELECT DISTINCT <involved column variables>
			 * FROM (<(logical) table>) AS inner_query
			 * WHERE <all involved column variables are not NULL>
			 */
			subQueryStr += buildDistinctQuery(readRelation(viewDef), colVarStrs);
			subQueryStr += ")";
			
			queryStr += subQueryStr + " UNION ";
		}
		
		// cut off last trailing "UNION"
		int strLength = queryStr.length();
		int cutOffLength = " UNION ".length();
		queryStr = "SELECT DISTINCT " + colsString(colVarStrs) + " FROM (" +
				queryStr.substring(0, strLength - cutOffLength) + ") foo";
		
		// wrap in another SELECT count(*)
		String countQueryStr = "SELECT count(*) AS count FROM (" + queryStr + ") outer_foo";
		
		int unionCount = 0;

		// fire the generated query
		try {
			ResultSet res = conn.createStatement().executeQuery(countQueryStr);
			res.next();
			unionCount = res.getInt("count");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// evaluate the result
		int diff = sumCountEach - unionCount;
		if (diff > 0 ) {	
			float metricValue = 1 - (diff / (float) sumCountEach);
			if (metricValue < threshold || threshold == 0) {
					
				writeMappingQuadMeasureToSink(metricValue, quadViewDefs);
			}
		}
	}


	private Set<String> vars2Strs(Set<Var> vars) {
		Set<String> strs = new TreeSet<String>();
		
		for (Var var : vars) {
			strs.add(var.getVarName());
		}
		
		return strs;
	}


	/**
	 * This method reads the names of all columns referenced by a quad pattern
	 */
	private Set<String> readReferencedColumns(Quad quad, VarDefinition varDefs) {
		Set<String> referencedColumns = new TreeSet<String>();
		
		for (Node node : Arrays.asList(quad.getSubject(), quad.getPredicate(), quad.getObject())) {
			if (node.isVariable())
				addNodeReferencedColumns((Node_Variable) node,
						referencedColumns, varDefs);
		}
		
		return referencedColumns;
	}


	private void addNodeReferencedColumns(Node_Variable node,
			Set<String> referencedColumns, VarDefinition varDefs) {
		Collection<RestrictedExpr> tcs =
				varDefs.getDefinitions(Var.alloc(node));

		Set<Var> colVars = null;
		for (RestrictedExpr termConstructor : tcs) {
			colVars = termConstructor.getExpr().getVarsMentioned();
		}
		
		for (Var colVar : colVars) {
			referencedColumns.add(colVar.getVarName());
		}
	}


	private String buildDistinctQuery(String relation, Set<String> referencedColumns) {
		String query =
			"SELECT DISTINCT " + colsString(referencedColumns) + " " +
			"FROM (" + relation + ") AS inner_query " +
			"WHERE " + colsNotNullString(referencedColumns);
		
		return query;
	}


	private String buildCountDistinctQuery(String relation, Set<String> referencedColumns) {
		String query =
			"SELECT count(*) AS count FROM (" +
				"SELECT DISTINCT " + colsString(referencedColumns) + " " +
				"FROM (" + relation + ") AS inner_query " +
				"WHERE " + colsNotNullString(referencedColumns) +
			") AS outer_query";
		
		return query;
	}


	private String buildCountAllQuery(String relation, Set<String> referencedColumns) {
		String query =
			"SELECT count(*) AS count FROM (" +
				"SELECT " + colsString(referencedColumns) + " " +
				"FROM (" + relation + ") AS inner_query " +
				"WHERE " + colsNotNullString(referencedColumns) +
			") AS outer_query";
		
		return query;
	}


	private String colsString(Set<String> colVarStrs) {
		String colsStr = "";
		for (String colVarStr : colVarStrs) {
			colsStr += colVarStr + ", ";
		}

		int strLen = colsStr.length();
		// assuming that columns will never be empty
		return colsStr.substring(0, strLen - 2);
	}


	private String colsNotNullString(Set<String> colVarStrs) {
		String colsNotNullStr = "";
		
		for (String colVarStr : colVarStrs) {
			colsNotNullStr += colVarStr + " is not NULL AND ";
		}
		
		int strLen = colsNotNullStr.length();
		// assuming that columns will never be empty
		return colsNotNullStr.substring(0, strLen - 4) ;
	}


	private String readRelation(ViewDefinition viewDef) {
		if (relationNames.containsKey(viewDef.getName()))
			return relationNames.get(viewDef.getName());
		
		SqlOp relation = viewDef.getMapping().getSqlOp();
		if (relation instanceof SqlOpTable) {
			String relName = "SELECT * FROM " + ((SqlOpTable) relation).getTableName();
			relationNames.put(viewDef.getName(), relName);
			return relName;
			
		} else if (relation instanceof SqlOpQuery) {
			String relName = ((SqlOpQuery) relation).getQueryString();
			relationNames.put(viewDef.getName(), relName);
			return relName;
		
		} else {
			// should never happen
			return "";
		}
	}
}
