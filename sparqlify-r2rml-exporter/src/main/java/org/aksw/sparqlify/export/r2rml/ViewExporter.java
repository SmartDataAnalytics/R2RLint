package org.aksw.sparqlify.export.r2rml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.aksw.sparqlify.algebra.sql.nodes.SqlOpBase;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;

class ResultNumberMismatchError extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
}

public class ViewExporter {
	final String subj = "subject";
	final String pred = "predicate";
	final String obj = "object";
	
	int idCounter;
	
	public ViewExporter() {
		idCounter = 1;
	}

	
	/**
	 * Builds up the RDF model "r2rml" representing the R2RML Triples Maps
	 * generated from the given SML view.
	 * @param viewDef
	 * 			the SML view definition
	 * @param r2rml
	 * 			the target com.hp.hpl.jena.rdf.model.Model
	 * @throws ResultNumberMismatchError 
	 */
	public void export(ViewDefinition viewDef, Model r2rml) throws ResultNumberMismatchError {

		// Create View ... As
		// Construct
		List<Quad> patterns = (List<Quad>) viewDef.getTemplate().getList();
		// With
		VarDefinition varDefs = viewDef.getMapping().getVarDefinition();
		// From
		SqlOpBase relation = (SqlOpBase) viewDef.getMapping().getSqlOp();
		
		/* get logical table information */
		TermMap logicalTableMap;
		if (relation instanceof SqlOpTable) {
			String tblName = ((SqlOpTable) relation).getTableName();
			TermMapValue tblValue = new TermMapValue(Constants.rrTableName, tblName);
			logicalTableMap = new TermMap(Constants.rrlogicalTable);
			logicalTableMap.setTermMapValue(tblValue);
		} else if (relation instanceof SqlOpQuery) {
			String queryStr = ((SqlOpQuery) relation).getQueryString();
			TermMapValue sqlValue = new TermMapValue(Constants.rrSqlQuery, queryStr);
			logicalTableMap = new TermMap(Constants.rrlogicalTable);
			logicalTableMap.setTermMapValue(sqlValue);
		} else {
			// TODO: throw exception
			TermMapValue logicalTblValue = new TermMapValue(Constants.rrSqlQuery, "1=1");
			logicalTableMap = new TermMap(Constants.rrlogicalTable);
			logicalTableMap.setTermMapValue(logicalTblValue);
		}
		
		/* get term map information */
		Vector<TermMaps> triplesMapCandidates = new Vector<TermMaps>();
		
		for (Quad pattern : patterns) {
			TermMaps triplesMapCandidate = new TermMaps();
			// logical table
			triplesMapCandidate.setLogicalTable(logicalTableMap);
			
			// subject
			Node smlSubject = pattern.getSubject();
			TermMap subjectMap = buildSubjectMap(smlSubject, varDefs);
			if (smlSubject.isVariable()) {
				triplesMapCandidate.setSubjectMap(subjectMap, (Var) smlSubject);
			} else triplesMapCandidate.setSubjectMap(subjectMap);
			
			// predicate
			Node smlPredicate = pattern.getPredicate();
			TermMap predicateMap = buildPredicateMap(smlPredicate, varDefs);
			if (smlPredicate.isVariable()) {
				triplesMapCandidate.setPredMap(predicateMap, (Var) smlPredicate);
			} else triplesMapCandidate.setPredMap(predicateMap);
			
			// object
			Node smlObject = pattern.getObject();
			TermMap objectMap = buildObjectMap(smlObject, varDefs);
			if (smlObject.isVariable()) {
				triplesMapCandidate.setObjectMap(objectMap, (Var) smlObject);
			} else triplesMapCandidate.setObjectMap(objectMap);

			
			triplesMapCandidates.add(triplesMapCandidate);
		}
		
		buildTriplesMaps(triplesMapCandidates, r2rml);
	}
	
	
	/**
	 * Builds a term map representing the R2RML subject map. A term map simply
	 * contains a predicate (like rr:subjectMap) and a TermMapValue instance.
	 *  
	 * @param smlSubject
	 * 		The Node instance in the subject position of the considered quad
	 * 		pattern from the SML view definition, e.g. the ?p in "?p a ex:Person"
	 * @param varDefs
	 * 		The variable definitions of the With part of the considered SML view
	 * 		definition
	 * @return
	 * 		A TermMap instance containing all the necessary content to build up
	 * 		an R2RML term map like:
	 * 		rr:subjectMap [ rr:template "http://ex.org/emp/{emp_id}" ]; 
	 * @throws ResultNumberMismatchError
	 */
	private TermMap buildSubjectMap(Node smlSubject, VarDefinition varDefs)
			throws ResultNumberMismatchError {

		TermMapValue termMapValue = buildTermMapValue(smlSubject, varDefs);
		TermMap subjectMap = new TermMap(Constants.rrSubjectMap);
		subjectMap.setTermMapValue(termMapValue);

		return subjectMap;
	}

	
	/**
	 * Builds a term map representing the R2RML predicate map. A term map simply
	 * contains a predicate (like rr:predicateMap) and a TermMapValue instance.
	 * 
	 * @param smlPredicate
	 * 		The Node instance in the predicate position of the considered quad
	 * 		pattern from the SML view definition, e.g. the ex:worksIn in
	 * 		"?p ex:worksIn ?d"
	 * @param varDefs
	 * 		The variable definitions of the With part of the considered SML view
	 * 		definition
	 * @return
	 * 		A TermMap instance containing all the necessary content to build up
	 * 		an R2RML term map like:
	 * 		rr:predicateMap [ rr:constant ex:worksIn ]; 
	 * @throws ResultNumberMismatchError
	 */
	private TermMap buildPredicateMap(Node smlPredicate, VarDefinition varDefs)
			throws ResultNumberMismatchError {

		TermMapValue termMapValue = buildTermMapValue(smlPredicate, varDefs);
		TermMap predicateMap = new TermMap(Constants.rrPredicateMap);
		predicateMap.setTermMapValue(termMapValue);

		return predicateMap;
	}
	
	
	/**
	 * Builds a term map representing the R2RML object map. A term map simply
	 * contains a predicate (like rr:objectMap) and a TermMapValue instance.
	 * 
	 * @param smlObject
	 * 		The Node instance in the object position of the considered quad
	 * 		pattern from the SML view definition, e.g. the ?d in "?p ex:worksIn ?d"
	 * @param varDefs
	 * 		The variable definitions of the With part of the considered SML view
	 * 		definition
	 * @return
	 * 		A TermMap instance containing all the necessary content to build up
	 * 		an R2RML term map like:
	 * 		rr:objectMap [ rr:template "http://ex.org/dpt/{dpt_id}" ]; 
	 * @throws ResultNumberMismatchError
	 */
	private TermMap buildObjectMap(Node smlObject, VarDefinition varDefs)
			throws ResultNumberMismatchError {

		TermMapValue termMapValue = buildTermMapValue(smlObject, varDefs);

		TermMap objectMap = new TermMap(Constants.rrObjectMap);
		objectMap.setTermMapValue(termMapValue);

		return objectMap;
	}
	
	
	/**
	 * Does the actual generic term map building. Depending on the type of the
	 * considered Node instance of the quad pattern from the SML view definition
	 * different actions are performed. Such a Node can be the subject, predicate
	 * or object of the quad pattern and can be a variable, IRI, blank node or
	 * literal.
	 * 
	 * @param smlNode
	 * @param varDefs
	 * @return
	 * @throws ResultNumberMismatchError
	 */
	private TermMapValue buildTermMapValue(Node smlNode, VarDefinition varDefs)
			throws ResultNumberMismatchError {
		
		TermMapValue termMapValue;
		
		/* node is a variable */
		if (smlNode.isVariable()) {
			
			Collection<RestrictedExpr> varRestrictions = varDefs
					.getDefinitions((Var) smlNode);
			
			List<TermMapValue> termMapVals = TermConstructorExporter
					.varDef2TermMapValues(varRestrictions);

			// more than one or no result at all
			// TODO: how to handle ResultNumberMismatchError
			if (termMapVals.size() != 1) {
				throw new ResultNumberMismatchError();
			}

			// TODO: can there be more than one restriction expression resulting
			// in more than one TermMapValue instance?
			termMapValue = termMapVals.get(0);
			
		/* node is a URI */
		} else if (smlNode.isURI()) {
			termMapValue = new TermMapValue(Constants.rrConstant, smlNode);
			termMapValue.setTermType(Constants.rrIRI);
			
		/* node is a blank node */
		} else if (smlNode.isBlank()) {
			// TODO: since the syntax of the SML is quite strict, I assume that
			// blank node ids are not reused and so I don't have to store it
	
			// the actual blank node is created automatically
			termMapValue = new TermMapValue(Constants.rrConstant);
			termMapValue.setTermType(Constants.rrBlankNode);
			
		/* node is a literal */
		} else if (smlNode.isLiteral()) {
			String literal = smlNode.getLiteralLexicalForm();
			termMapValue = new TermMapValue(Constants.rrConstant, literal);

			String lang = smlNode.getLiteralLanguage();
			// strip off leading "@"
			if (lang.startsWith("@")) lang = lang.substring(1);
			if (!lang.isEmpty()) termMapValue.setLanguage(lang);
			
		/* fallback */
		} else {
			// not possible
			// TODO: raise reasonable error
			termMapValue = new TermMapValue(Constants.rrConstant);
			termMapValue.setTermType(Constants.rrBlankNode);
		}
		
		return termMapValue;
	}
	
	
	/**
	 * A wrapper function iterating over a TermMaps Vector holding the Triples
	 * Maps candidates and calling buildTriplesMap. The term maps here are only
	 * Triples Maps candidates since two or more of them can be put into one
	 * Triples Map.
	 * Finally all the built Triples Maps are added to the RDF graph r2rml.
	 * 
	 * @param triplesMapCandidates
	 * 		A TermMaps Vector holding data that can possibly result in one
	 * 		new Triples Map or be added to an already existing one. 
	 * @param r2rml
	 * 		An RDF model that should hold all the Triples Maps according to the
	 * 		R2RML standard after having built these Triples Maps
	 */
	private void buildTriplesMaps(Vector<TermMaps> triplesMapCandidates, Model r2rml) {
		List<TriplesMap> triplesMaps = new ArrayList<TriplesMap>();

		TermMaps firstTriplesMapCandidate = triplesMapCandidates.remove(0);
		buildTriplesMap(firstTriplesMapCandidate, triplesMapCandidates,
				triplesMaps, null, null);

		for (TriplesMap tm : triplesMaps) {
			Model rdf = tm.toRDF();
			r2rml.add(rdf);
		}
	}
	
	
	/**
	 * Builds up Triples Maps by considering one candidate of the candidate
	 * list, checking if there are any other candidates referencing the current
	 * one or having the same subject. (All candidates with the same subject
	 * can be put into one Triples Map.) All candidates processed thereby are
	 * removed from the candidate list.
	 * After this, the method calls itself recursively on the remaining
	 * candidate list until it is empty.
	 *  
	 * @param triplesMapCandidate
	 * 		The currently considered candidate of the candidate list
	 * @param triplesMapCandidates
	 * 		The whole list of candidates still to be processed
	 * @param triplesMaps
	 * 		A List containing all the Triples Maps created so far. 
	 * @param refId
	 * 		In case the current candidate references another Triples Maps
	 * 		subject, refId holds the id of the referenced Triples Map
	 * @param refColName
	 * 		In case the current candidate references another Triples Maps
	 * 		subject, refColName holds the column name the referenced subject
	 * 		gets its data from 
	 */
	private void buildTriplesMap(TermMaps triplesMapCandidate,
			Vector<TermMaps> triplesMapCandidates,
			List<TriplesMap> triplesMaps, String refId, String refColName) {
		
		/* initialize Triples Map */
		TriplesMap triplesMap;
		
		// current candidate is a referencing object map 
		if (refId != null) {
			triplesMap = new TriplesMap(Integer.toString(idCounter++),
					triplesMapCandidate, refId, refColName);
			
		// 'normal' case
		} else {
			triplesMap = new TriplesMap(Integer.toString(idCounter++),
					triplesMapCandidate);
		}
		
		// find other candidates that have a referencing object map as well as
		// candidates with additional predicateObjectMaps of the current subject 
		if (triplesMapCandidate.hasSubjectVar()) {
			Var subjVar = triplesMapCandidate.getSubjectVar();
			
			// candidates having the same subject
			// go on here with clean up
			List<TermMaps> sameSubjVarTMCands =
					findMatchingSubjVarTMCands(subjVar, triplesMapCandidates);
			
			for (TermMaps sameSubjVarTMCand : sameSubjVarTMCands) {
				triplesMap.addPredicateObjectMap(sameSubjVarTMCand);
				// since the referencing object case requires a Triples Map
				// candidate having a different subject, it is OK to remove
				// the candidates having the same subject here
				triplesMapCandidates.remove(sameSubjVarTMCand);
			}
			
			// candidates referencing the current Triples Map
			List<TermMaps> referencingObjectTMCands =
					findMatchingObjVarTMCands(subjVar, triplesMapCandidates);
			
			for (TermMaps refObjTMCand : referencingObjectTMCands) {
				triplesMapCandidates.remove(refObjTMCand);
				String referencingTMId = triplesMap.getId();
				// FIXME: can be null
				String referencingTMColName =
						triplesMap.getSubjectMapColumnReference();
				
				buildTriplesMap(refObjTMCand, triplesMapCandidates,
						triplesMaps, referencingTMId, referencingTMColName);
			}
		}
		
		// add Triples Map to the Triples Maps list
		triplesMaps.add(triplesMap);
		
		/* build Triples Maps of the remaining candidates */
		if (triplesMapCandidates.size()!=0) {
			TermMaps first = triplesMapCandidates.remove(0);
			buildTriplesMap(first, triplesMapCandidates, triplesMaps, null, null);
		}
	}
	

	/**
	 * Looks for TermMaps having the same object variable as the subjVar param
	 * and returns all matching TermMaps instances in a list.
	 * 
	 * @param subjVar
	 * 		the SML variable to match
	 * @param triplesMapCandidates
	 * 		Triples Map candidates Vector to be searched
	 * @return
	 * 		all the Triples Maps candidates that have the same SML variable on
	 * 		the object position
	 */
	private List<TermMaps> findMatchingObjVarTMCands(Var subjVar,
			Vector<TermMaps> triplesMapCandidates) {
		
		List<TermMaps> matchingObjCandidates = new ArrayList<TermMaps>();
		
		for (TermMaps triplesMapCandidate : triplesMapCandidates) {
			if (triplesMapCandidate.hasObjectVar()) {
				if (triplesMapCandidate.getObjectVar().matches(subjVar)) {
					matchingObjCandidates.add(triplesMapCandidate);
				}
			}
		}
		
		return matchingObjCandidates;
	}
	
	
	/**
	 * Looks for TermMaps having the same subject variable as the subjVar param
	 * and returns all matching TermMaps instances in a list.
	 * 
	 * @param subjVar
	 * 		the SML variable to match
	 * @param candidates
	 * 		Triples Map candidates Vector to be searched
	 * @return
	 * 		all the Triples Maps candidates that have the same SML variable on
	 * 		the subject position
	 */
	private List<TermMaps> findMatchingSubjVarTMCands(Var subjVar,
			List<TermMaps> candidates) {
		
		List<TermMaps> matchingSubjCandidates = new ArrayList<TermMaps>();
		
		for (TermMaps triplesMapCandidate : candidates) {
			if (triplesMapCandidate.hasSubjectVar()) {
				if (triplesMapCandidate.getSubjectVar().matches(subjVar)) {
					matchingSubjCandidates.add(triplesMapCandidate);
				}
			}
		}
		
		return matchingSubjCandidates;
	}
}