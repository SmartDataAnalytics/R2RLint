package org.aksw.sparqlify.export.r2rml;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.Var;


class PredicateObjectMap {
	// rr:predicateMap
	private final Property rrPredicateMap =
			ResourceFactory.createProperty(Constants.rrPredicateMap);
	private Property rrPredicateMapPredicate;
	private RDFNode rrPredicateMapObject;
	private Var rrPredicateMapColRef;
	// rr:objectMap
	private final Property rrObjectMap =
			ResourceFactory.createProperty(Constants.rrObjectMap);
	private Property rrObjectMapPredicate;
	private RDFNode rrObjectMapObject;
	// optional features
	private Resource rrObjectMapTermType;
	private Resource rrObjectMapDatatype;
	private Literal rrObjectMapLang;
	private String rrObjectMapColRef;
	// referencing object features
	private Literal rrJoinConditionChild;
	private Literal rrJoinConditionParent;
	private boolean isReferencingObjectMap = false;
	
	/*
	 * 'normal' case (no referencing object)
	 */
	PredicateObjectMap(TermMap predicateMap, TermMap objectMap) {
		
		/* predicate map value */
		TermMapValue predVal = predicateMap.getTermMapValue();
		
		// e.g. http://www.w3.org/ns/r2rml#constant
		rrPredicateMapPredicate = predVal.getMappingProperty();
		
		// e.g. http://www.w3.org/1999/02/22-rdf-syntax-ns#type
		rrPredicateMapObject = predVal.getValue();
		
		if (predVal.hasColumnReference()) {
			rrPredicateMapColRef = predVal.getColumnReference();
		}
		
		/* object map value */
		TermMapValue objVal = objectMap.getTermMapValue();
		
		rrObjectMapPredicate = objVal.getMappingProperty();
		rrObjectMapObject = objVal.getValue();
		
		if (objVal.hasDatatype()) rrObjectMapDatatype = objVal.getDatatype();
		if (objVal.hasLanguage()) rrObjectMapLang = objVal.getLanguage();
		if (objVal.hastTermType()) rrObjectMapTermType = objVal.getTermType();

		if (objVal.hasColumnReference()) {
			// strip off the leading '?' of the object's referenced column
			// and assign it
			rrObjectMapColRef =
					objVal.getColumnReference().toString().substring(1);
		}
	}
	

	/**
	 * In the referencing object case the object map consists of
	 * - a link to the resource its subject is referenced (rr:parentTriplesMap)
	 * - a join condition statement (rr:joinCondition) consisting of
	 * 		- the column name used for the referencing object map (rr:child)   
	 * 		- the column name used for the subject map referenced (rr:parent)
	 * as shown below:
	 * 
	 * 	...
	 * 		rr:objectMap [
	 * 			rr:parentTriplesMap <#TriplesMap1>;
	 * 			rr:joinCondition [
	 * 				rr:child "dept_id";
	 * 				rr:parent "dept_id";
	 * 			];
	 * 		];
	 * 
	 * The predicate map looks as usual.
	 * 
	 * @param predicateMap
	 * 		a TermMap holding the predicate map information
	 * @param refTMId
	 * 		the id of the Triples Map its subject is referenced
	 * @param childColRef
	 * 		the column name used for the referencing object map
	 * @param parColRef
	 * 		the column name used for the subject map referenced
	 */
	PredicateObjectMap(TermMap predicateMap, String refTMId,
			String childColRef, String parColRef) {
		
		/* predicate map */
		TermMapValue predVal = predicateMap.getTermMapValue();
		// e.g. http://www.w3.org/ns/r2rml#constant
		rrPredicateMapPredicate = predVal.getMappingProperty();
		// e.g. http://www.w3.org/1999/02/22-rdf-syntax-ns#type
		rrPredicateMapObject = predVal.getValue();
		
		if (predVal.hasColumnReference()){
			rrPredicateMapColRef = predVal.getColumnReference();
		}
		
		/* object map */
		rrObjectMapPredicate =
				ResourceFactory.createProperty(Constants.rrParentTriplesMap);
		
		rrObjectMapObject = ResourceFactory.createResource(refTMId);
		rrJoinConditionChild = ResourceFactory.createPlainLiteral(childColRef);
		rrJoinConditionParent = ResourceFactory.createPlainLiteral(parColRef);
		isReferencingObjectMap = true;
	}
	
	
	boolean isReferencingObjectMap() {
		return isReferencingObjectMap;
	}
	
	
	boolean hasObjectMapTermType() {
		if (rrObjectMapTermType != null) return true;
		else return false;
	}
	
	
	Resource getObjectMapTermType() {
		return rrObjectMapTermType;
	}
	
	
	boolean hasObjectMapDatatype() {
		if (rrObjectMapDatatype != null) return true;
		else return false;
	}
	
	
	Resource getObjectMapDatatype() {
		return rrObjectMapDatatype;
	}
	
	
	boolean hasObjectMapLanguage() {
		if (rrObjectMapLang != null) return true;
		else return false;
	}
	
	
	Literal getObjectMapLanguage() {
		return rrObjectMapLang;
	}
	
	
	boolean hasPredicateMapColumnReference() {
		if (rrPredicateMapColRef != null) return true;
		else return false;
	}
	
	
	Var getPredicateMapColumnRef() {
		return rrPredicateMapColRef;
	}
	
	
	boolean hasObjectMapColumnReference() {
		if (rrObjectMapColRef != null) return true;
		else return false;
	}
	
	
	String getObjectMapcolumnRef() {
		return rrObjectMapColRef; 
	}


	public Model toRdf(Resource triplesMap) {
		/*
		 *	...
		 *		rr:predicateObjectMap [
		 *			rr:predicate ex:name;
		 *			rr:objectMap [ rr:column "DNAME" ];
		 *		];
		 * 
		 * or
		 * 
		 * 	...
		 * 		rr:predicateObjectMap [
		 * 			rr:predicateMap [
		 * 				rr:constant ex:worksIn;
		 * 			];
		 * 			rr:objectMap [
		 * 				rr:parentTriplesMap <#TriplesMap1>;
		 * 				rr:joinCondition [
		 * 					rr:child "dept_id";
		 * 					rr:parent "dept_id";
		 * 				];
		 * 			];
		 * 		].
		 */
		Model predObjMapRdf = ModelFactory.createDefaultModel();
		Resource predObjMapBlankNode = ResourceFactory.createResource();
		
		/* predicate map */
		Resource predMapBlankNode = ResourceFactory.createResource();

		Statement predicateMapStatement = ResourceFactory.createStatement(
			predMapBlankNode, rrPredicateMapPredicate, rrPredicateMapObject);
		
		predObjMapRdf.add(predicateMapStatement);
		
		
		/* object map */
		Resource objMapBlankNode = ResourceFactory.createResource();
		
		// the actual mapping value
		Statement innerValStatement = ResourceFactory.createStatement(
					objMapBlankNode, rrObjectMapPredicate, rrObjectMapObject);
		predObjMapRdf.add(innerValStatement);

		// additional informations if this is a referencing object map
		if (isReferencingObjectMap) {
			// parent triples map...
			// ... is handled generically, i.e. it is contained in the
			// rrPredicateMapPredicate and rrPredicateMapObject attributes
			
			// join condition
			Resource joinCondBlankNode = ResourceFactory.createResource();
			
			Property rrJoinCondition =
					ResourceFactory.createProperty(Constants.rrJoincondition);
			
			Property rrChild =
					ResourceFactory.createProperty(Constants.rrChild);
			Property rrParent =
					ResourceFactory.createProperty(Constants.rrParent);
			
			Statement childStatement = ResourceFactory.createStatement(
					joinCondBlankNode, rrChild, rrJoinConditionChild);
			
			Statement parentStatement = ResourceFactory.createStatement(
					joinCondBlankNode, rrParent, rrJoinConditionParent);
			
			Statement refObjMapStatement = ResourceFactory.createStatement(
					objMapBlankNode, rrJoinCondition, joinCondBlankNode);
			
			predObjMapRdf.add(childStatement);
			predObjMapRdf.add(parentStatement);
			predObjMapRdf.add(refObjMapStatement);
		}
		
		/* possible additional informations like datatype, language and
		 * term type */
		if (hasObjectMapDatatype()) {
			Property rrDatatype =
					ResourceFactory.createProperty(Constants.rrDatatype);
			
			Statement dataTypeStatement = ResourceFactory.createStatement(
					objMapBlankNode, rrDatatype, rrObjectMapDatatype);
			predObjMapRdf.add(dataTypeStatement);
		}
		
		if (hasObjectMapLanguage()) {
			Property rrLanguage =
					ResourceFactory.createProperty(Constants.rrLanguage);
			
			Statement langStatement = ResourceFactory.createStatement(
					objMapBlankNode, rrLanguage, rrObjectMapLang);
			predObjMapRdf.add(langStatement);
		}
		
		if (hasObjectMapTermType()) {
			Property rrTermType =
					ResourceFactory.createProperty(Constants.rrTermType);
			
			Statement termTypeStatement = ResourceFactory.createStatement(
					objMapBlankNode, rrTermType, rrObjectMapTermType);
			predObjMapRdf.add(termTypeStatement);
		}
		
		/* predicate object map */
		Statement predicateMapSatement = ResourceFactory.createStatement(
				predObjMapBlankNode, rrPredicateMap, predMapBlankNode);
		predObjMapRdf.add(predicateMapSatement);
		
		Statement objectMapStatement = ResourceFactory.createStatement(
				predObjMapBlankNode, rrObjectMap, objMapBlankNode);
		predObjMapRdf.add(objectMapStatement);
		
		Property rrPredicateObjectMap = ResourceFactory.createProperty(
				Constants.rrPredicateObjectMap);
		
		Statement predicateObjectMapStatement = ResourceFactory.createStatement(
				triplesMap, rrPredicateObjectMap, predObjMapBlankNode);
		predObjMapRdf.add(predicateObjectMapStatement);
		
		
		return predObjMapRdf;
	}
}


/**
 * A Triples Map (as definded in http://www.w3.org/TR/2012/REC-r2rml-20120927/)
 * determins a mapping from a (logical) relational database table (i.e. an SQL
 * query or a table) to RDF templates.
 * The main parts are:
 * - a resource identifying the Triples Map
 * - a logical table map (rr:logicalTable) defining the relational data source
 * - a subject map  (rr:subjectMap)
 * - several predicate object maps (rr:predicateObjectMap) modeled as objects of
 *   the PredicateObjectMap class
 *   
 * An example serialized in Turtle may look like (taken from 
 * http://www.w3.org/TR/r2rml/):
 * 
 * 	<#TriplesMap2>
 * 		rr:logicalTable [ rr:tableName "DEPT" ];;
 * 		rr:subjectMap [
 * 			rr:template "http://data.example.com/department/{DEPTNO}";
 * 			rr:class ex:Department;
 * 		];
 * 		rr:predicateObjectMap [
 * 			rr:predicate ex:name;
 * 			rr:objectMap [ rr:column "DNAME" ];
 * 		];
 * 		rr:predicateObjectMap [
 * 			rr:predicate ex:location;
 * 			rr:objectMap [ rr:column "LOC" ];
 * 		];
 * 		rr:predicateObjectMap [
 * 			rr:predicate ex:staff;
 * 			rr:objectMap [ rr:column "STAFF" ];
 * 		].
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
public class TriplesMap {	
	
	// Triples Map id
	private final String idTemplate = "#TriplesMap%s"; 
	private String id;
	
	// Triples Map resource
	private Resource rrTriplesMap;
	
	// rr:logicalTable
	private final Property rrLogicalTable =
					ResourceFactory.createProperty(Constants.rrlogicalTable);
	private Property rrLogicalTablePredicate;
	private RDFNode rrLogicalTableObject;
	
	// rr:subjectMap
	private final Property rrSubjectMap =
					ResourceFactory.createProperty(Constants.rrSubjectMap);
	private Property rrSubjectMapPredicate;
	private RDFNode rrSubjectMapObject;
	private Resource rrSubjectMapTermType;
	
	// rr:predicateObjectMap
	private final Property rrPredicateObjectMap =
				ResourceFactory.createProperty(Constants.rrPredicateObjectMap);
	private List<PredicateObjectMap> predicateObjectMaps;
	private String rrSubjectMapColRef;
	
	
	public TriplesMap(String id, TermMaps triplesMapCandidate) {
		/* set Triples Map id */
		this.id = String.format(idTemplate, id);
		rrTriplesMap = ResourceFactory.createResource(this.id);
		
		/* set logical table */
		TermMapValue logTblVal =
				triplesMapCandidate.getLogicalTableMap().getTermMapValue();
		
		rrLogicalTablePredicate = logTblVal.getMappingProperty();
		
		// has to be a plain literal, so looking for datatype, term type etc
		// makes no sense here
		rrLogicalTableObject = logTblVal.getValue();
		
		/* set subject map */
		TermMapValue subjMapVal =
				triplesMapCandidate.getSubjectMap().getTermMapValue();
		
		rrSubjectMapPredicate = subjMapVal.getMappingProperty();
		rrSubjectMapObject = subjMapVal.getValue();
		
		// must be a URI or blank node so there is no need to check for a
		// datatype or a language tag here
		if (subjMapVal.hastTermType()) rrSubjectMapTermType = subjMapVal.getTermType();
		
		// get column reference (needed for possible referencing object
		// situations)
		if (subjMapVal.hasColumnReference()) {
			// get column name and strip off the leading "?"
			String colName =
					subjMapVal.getColumnReference().toString().substring(1);
			rrSubjectMapColRef = colName; 
		}
		
		/* set predicate object map */
		predicateObjectMaps = new ArrayList<PredicateObjectMap>();
		
		PredicateObjectMap predObjMap = new PredicateObjectMap(
				triplesMapCandidate.getPredictateMap(),
				triplesMapCandidate.getObjectMap());
		predicateObjectMaps.add(predObjMap);
	}
	
	
	/**
	 * Constructor for the referencing object case, e.g.:
	 *	<#TriplesMap1>
	 *		rr:logicalTable [ rr:tableName "employees" ];
	 *		rr:subjectMap [
	 *			rr:template "http://ex.org/dept/{dept_id}"
	 *		];
	 *		rr:predicateObjectMap [
	 *			rr:predicate rdf:type;
	 *			rr:object ex:Department;
	 *		].
	 *
	 *	<#TriplesMap2>
	 *		rr:logicalTable [ rr:tableName "employees" ];
	 *		rr:subjectMap [
	 *			rr:template "http://ex.org/emp/{emp_id}"
	 *		];
	 *		rr:predicateObjectMap [
	 *			rr:predicate ex:worksIn;
	 *			rr:objectMap [
	 *				rr:parentTriplesMap <#TriplesMap1>;
	 *				rr:joinCondition [
	 *					rr:child "dept_id";
	 *					rr:parent "dept_id";
	 *				];
	 *			];
	 *		].
	 *
	 * @param id
	 * 		the Triples Map id 
	 * @param triplesMapCandidate
	 * 		the TermMaps instance representing a Triples Map candidate
	 * @param refId
	 * 		the id of the Triples Map its subject is referenced by the current
	 * 		Triples Map	candidate's object map
	 * @param refColName
	 * 		the logical table column (used for the retrieval of the relational
	 * 		data) of the subject references by the current Triples Map
	 * 		candidate's object map  
	 */
	public TriplesMap(String id, TermMaps triplesMapCandidate, String refId,
			String refColName) {
		
		/* set Triples Map id */
		this.id = String.format(idTemplate, id);
		rrTriplesMap = ResourceFactory.createResource(this.id);
		
		/* set logical table */
		TermMapValue logTblVal =
				triplesMapCandidate.getLogicalTableMap().getTermMapValue();
		
		rrLogicalTablePredicate = logTblVal.getMappingProperty();
		
		// has to be a plain literal, so looking for a datatype or term type
		// makes no sense here
		rrLogicalTableObject = logTblVal.getValue();
		
		/* set subject map */
		TermMapValue subjMapVal =
				triplesMapCandidate.getSubjectMap().getTermMapValue();
		
		rrSubjectMapPredicate = subjMapVal.getMappingProperty();
		rrSubjectMapObject = subjMapVal.getValue();
		
		// must be a URI or blank node so there is no need to check for a
		// datatype or language here
		if (subjMapVal.hastTermType()) rrSubjectMapTermType = subjMapVal.getTermType();

		// get column reference 
		if (subjMapVal.hasColumnReference()) {
			// get column name and strip off the leading "?"
			String colName =
					subjMapVal.getColumnReference().toString().substring(1);
			rrSubjectMapColRef = colName; 
		}
		
		/* set predicate object map */
		predicateObjectMaps = new ArrayList<PredicateObjectMap>();
		
		// get the logical table column used by the Triples Map candidate's
		// object and remove the leading '?'
		String objectsColumnReference =
				triplesMapCandidate.getObjectMap().getTermMapValue().getColumnReference().toString().substring(1);
		
		PredicateObjectMap predObjMap = new PredicateObjectMap(
				triplesMapCandidate.getPredictateMap(),
				refId,
				objectsColumnReference,
				refColName);
		
		predicateObjectMaps.add(predObjMap);
		
	}

	
	public String getId() {
		return id;
	}
	
	
	public boolean hasSubjectMapColumnReference() {
		if (rrSubjectMapColRef != null) return true;
		else return false;	
	}
	
	
	public String getSubjectMapColumnReference() {
		return rrSubjectMapColRef;
	}
	
	
	public void addPredicateObjectMap(TermMaps predObjMapTerms) {
		PredicateObjectMap predObjMap = new PredicateObjectMap(
				predObjMapTerms.getPredictateMap(),
				predObjMapTerms.getObjectMap());
		
		predicateObjectMaps.add(predObjMap);
	}
	

	/*
	 * Triples Map example (http://www.w3.org/TR/r2rml/):
	 * 
	 * 	<#TriplesMap2>
	 * 		rr:logicalTable [ rr:tableName "DEPT" ];
	 * 		rr:subjectMap [
	 * 			rr:template "http://data.example.com/department/{DEPTNO}";
	 * 			rr:class ex:Department;
	 * 		];
	 * 		rr:predicateObjectMap [
	 * 			rr:predicate ex:name;
	 * 			rr:objectMap [ rr:column "DNAME" ];
	 * 		];
	 * 		rr:predicateObjectMap [
	 * 			rr:predicate ex:location;
	 * 			rr:objectMap [ rr:column "LOC" ];
	 * 		];
	 * 		rr:predicateObjectMap [
	 * 			rr:predicate ex:staff;
	 * 			rr:objectMap [ rr:column "STAFF" ];
	 * 		].
	 */
	public Model toRDF() {
		Model rdf = ModelFactory.createDefaultModel();
		
		rdf.add(logicalTableToRDF());
		rdf.add(subjectMapToRdf());
		rdf.add(predicateObjectMapsToRdf());

		//rdf.write(System.out, "TURTLE", "<http://foo.org/resources>");
		return rdf;
	}
	
	
	private Model predicateObjectMapsToRdf() {
		Model predObjMaps = ModelFactory.createDefaultModel();
		
		for (PredicateObjectMap predObjMap : predicateObjectMaps) {
			predObjMaps.add(predObjMap.toRdf(rrTriplesMap));
		}
		
		
		return predObjMaps;
	}

	
	private Model subjectMapToRdf() {
		/* e.g.
		 * 	...
		 * 		rr:subjectMap [
		 * 			rr:template "http://data.example.com/department/{DEPTNO}";
		 * 			rr:class ex:Department;
		 * 		];
		 */
		Model subjMapRdf = ModelFactory.createDefaultModel();
		Resource blankNode = ResourceFactory.createResource();
		
		// inner statement assigning the actual value
		Statement innerValStatement = ResourceFactory.createStatement(
				blankNode, rrSubjectMapPredicate, rrSubjectMapObject);
		subjMapRdf.add(innerValStatement);
		
		// possible additional inner term type statement
		if (rrSubjectMapTermType != null) {
			Property rrTermType = ResourceFactory
					.createProperty(Constants.rrTermType);
			Statement termTypeStatement = ResourceFactory.createStatement(
					blankNode, rrTermType, rrSubjectMapTermType);
			subjMapRdf.add(termTypeStatement);
		}
		
		// outer statement
		Statement outerSubjMapStatement = ResourceFactory.createStatement(
						rrTriplesMap, rrSubjectMap, blankNode);
		
		subjMapRdf.add(outerSubjMapStatement);
		
		return subjMapRdf;
	}
	
	
	private Model logicalTableToRDF() {
		/* e.g. ... rr:logicalTable [ rr:tableName "DEPT" ]; */
		
		Model logTblRdf = ModelFactory.createDefaultModel();
		Resource blankNode = ResourceFactory.createResource();
		
		Statement innerStatement = ResourceFactory.createStatement(
					blankNode, rrLogicalTablePredicate, rrLogicalTableObject);
		logTblRdf.add(innerStatement);
		
		Statement outerStatement = ResourceFactory.createStatement(
					rrTriplesMap, rrLogicalTable, blankNode);
		logTblRdf.add(outerStatement);
		
		return logTblRdf;
	}
}
