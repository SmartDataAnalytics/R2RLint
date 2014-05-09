package org.aksw.sparqlify.export.r2rml;

import com.hp.hpl.jena.sparql.core.Var;

/**
 *  * A class being a container for several Term Maps building a Triples Map
 * candidate. So the contained Term Maps are:
 * - logical table map
 * - subject map
 * - predicate map
 * - object map

 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
public class TermMaps {
	private TermMap subjMap;
	private Var subjVar;
	private TermMap predMap;
	private Var predVar;
	private TermMap objMap;
	private Var objVar;
	private TermMap logTblMap;

	
	public void setSubjectMap(TermMap subjMap) {
		this.subjMap = subjMap;
	}
	
	
	public void setSubjectMap(TermMap subjMap, Var subjVar) {
		this.subjMap = subjMap;
		this.subjVar = subjVar;
	}
	
	
	public TermMap getSubjectMap() {
		return subjMap;
	}
	
	
	public boolean hasSubjectVar() {
		if (subjVar != null) return true;
		else return false;
	}
	
	
	public Var getSubjectVar() {
		return subjVar;
	}
	
	
	public void setPredMap(TermMap predMap) {
		this.predMap = predMap;
	}
	
	
	public void setPredMap(TermMap predMap, Var predVar) {
		this.predMap = predMap;
		this.predVar = predVar;
	}
	
	
	public TermMap getPredictateMap() {
		return predMap;
	}
	
	
	public boolean hasPredicateVar() {
		if (predVar != null) return true;
		else return false;
	}
	
	
	public Var getPredicateVar() {
		return predVar;
	}
	
	
	public void setObjectMap(TermMap objMap) {
		this.objMap = objMap;
	}
	
	
	public void setObjectMap(TermMap objMap, Var objVar) {
		this.objMap = objMap;
		this.objVar = objVar;
	}
	
	
	public TermMap getObjectMap() {
		return objMap;
	}
	
	
	public boolean hasObjectVar() {
		if (objVar != null) return true;
		else return false;
	}
	
	
	public Var getObjectVar() {
		return objVar;
	}
	
	
	public void setLogicalTable(TermMap logTblMap) {
		this.logTblMap = logTblMap;
	}
	
	
	public TermMap getLogicalTableMap() {
		return logTblMap;
	}
}
