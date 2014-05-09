package org.aksw.sparqlify.qa.metrics.interpretability;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.Pair;
import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * This metric checks if there are any collections created and if they are
 * valid as far as the corresponding syntax rules are concerned.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class CorrectCollectionUse extends MetricImpl implements DatasetMetric {
    
    @Autowired
    private Pinpointer pinpointer;
    private float score_nilHasRest = (float) 0.5;  // a)
    private float score_restStatementHasLiteralObject = 0;  // b)
    private float score_noneOrMultipleFirstStatements = (float) 0.5;  // c)
    private float score_firstStatementHasLiteralObject = 0;  // d)
    private float score_collectionNotTerminatedWithNil = (float) 0.5;  // e)
    private float score_restStatementHasMultipleSuccessors = (float) 0.5;  // f)
    private float score_restStatementHasMultiplePredecessors = (float) 0.5; // g)
    
    
    // setter mainly used for testing purposes
    protected void setScore_nilHasRest(float val) {
        score_nilHasRest = val;
    }
    protected void setScore_restStatementHasLiteralObject(float val) {
        score_restStatementHasLiteralObject = val;
    }
    protected void setScore_noneOrMultipleFirstStatements(float val) {
        score_noneOrMultipleFirstStatements = val;
    }
    protected void setScore_firstStatementHasLiteralObject(float val) {
        score_firstStatementHasLiteralObject = val;
    }
    public void setScore_collectionNotTerminatedWithNil(float val) {
        score_collectionNotTerminatedWithNil = val;
    }
    public void setScore_restStatementHasMultipleSuccessors(float val) {
        score_restStatementHasMultipleSuccessors = val;
    }
    public void setScore_restStatementHasMultiplePredecessors(float val) {
        score_restStatementHasMultiplePredecessors = val;
    }
    
    
    @Override
    public void assessDataset(SparqlifyDataset dataset)
            throws NotImplementedException, SQLException {
        
        check_nilHasRest(dataset);
        check_noneOrMultipleFirstStatements(dataset);
        check_firstStatementHasLiteralObject(dataset);
        check_collectionNotTerminatedWithNil(dataset);
        check_restStatementHasLiteralObject(dataset);
        check_restStatementHasMultipleSuccessors(dataset);
        check_restStatementHasMultiplePredecessors(dataset);
    }
    
    
    private void check_nilHasRest(SparqlifyDataset dataset)
            throws NotImplementedException, SQLException {
        
        String queryStr =
                "SELECT * { " +
                    "<" + RDF.nil.getURI() + "> <" + RDF.rest.getURI() + "> ?rest . " +
                "}";
        QueryExecution qe = getQueryExecution(queryStr, dataset);
        ResultSet res = qe.execSelect();
        while (res.hasNext()) {
            QuerySolution sol = res.next();
            Node rest = sol.get("rest").asNode();
            // build triple for reporting
            Triple triple = new Triple(RDF.nil.asNode(), RDF.rest.asNode(), rest);
            Set<ViewQuad<ViewDefinition>> quadViewDefs = pinpointer
                    .getViewCandidates(triple);
            writeTripleMeasureToSink(score_nilHasRest, triple, quadViewDefs);
        }
        
        
    }
    
    
    private void check_restStatementHasLiteralObject(SparqlifyDataset dataset)
            throws NotImplementedException, SQLException {
        
        String queryStr =
                "SELECT * { " +
                    "?lnode <" + RDF.rest.getURI() + "> ?rest . " +
                    "FILTER (isLiteral(?rest) ) " +
                "}";
        QueryExecution qe = getQueryExecution(queryStr, dataset);
        ResultSet res = qe.execSelect();
        while (res.hasNext()) {
            QuerySolution sol = res.next();
            // build tripe to report
            Node lNode = sol.get("lnode").asNode();
            Node rest = sol.get("rest").asNode();
            Triple triple = new Triple(lNode, RDF.rest.asNode(), rest);
            Set<ViewQuad<ViewDefinition>> quadViewDefs = pinpointer
                    .getViewCandidates(triple);
            writeTripleMeasureToSink(score_restStatementHasLiteralObject,
                    triple, quadViewDefs);
        }
    }
    
    
    private void check_noneOrMultipleFirstStatements(SparqlifyDataset dataset)
            throws NotImplementedException, SQLException {
        
        // ----------------- none -----------------
        String noneQueryStr =
                "SELECT * { " +
                    "?lnode <" + RDF.rest.getURI() + "> ?rest . " +
                    "FILTER ( NOT EXISTS {?lnode <" + RDF.first.getURI() + "> [] } ) " +
                "}";
        QueryExecution noneQe = getQueryExecution(noneQueryStr, dataset);
        ResultSet noneRes = noneQe.execSelect();
        while (noneRes.hasNext()) {
            QuerySolution sol = noneRes.next();
            // build triple for reporting (actually the missing triple is
            // reported here)
            Node lNode = sol.get("lnode").asNode();
            Node first = NodeFactory.createURI("http://ex.org/missing");
            Triple triple = new Triple(lNode, RDF.first.asNode(), first);
            
            Set<ViewQuad<ViewDefinition>> quadViewDefs = pinpointer
                    .getViewCandidates(triple);
            writeTripleMeasureToSink(score_noneOrMultipleFirstStatements,
                    triple, quadViewDefs);
        }
        
        // --------------- multiple ---------------
        String mulQueryStr =
                "SELECT * { " +
//                    "?lnode <" + RDF.rest.getURI() + "> ?rest . " +
                    "?lnode <" + RDF.first.getURI() + "> ?first1 . " +
                    "?lnode <" + RDF.first.getURI() + "> ?first2 . " +
                    "FILTER (?first1 != ?first2 ) " +
                "}";
        QueryExecution mulQe = getQueryExecution(mulQueryStr, dataset);
        ResultSet mulRes = mulQe.execSelect();
        while (mulRes.hasNext()) {
            QuerySolution sol = mulRes.next();
            // build triples for reporting
            Node lNode = sol.get("lnode").asNode();
            Node first1 = sol.get("first1").asNode();
            Node first2 = sol.get("first2").asNode();
            Triple triple1 = new Triple(lNode, RDF.first.asNode(), first1);
            Triple triple2 = new Triple(lNode, RDF.first.asNode(), first2);
            
            reportTriples(Arrays.asList(triple1, triple2),
                    score_noneOrMultipleFirstStatements);
        }
    }
    
    
    private void check_firstStatementHasLiteralObject(SparqlifyDataset dataset)
            throws NotImplementedException, SQLException {
        
        String queryStr =
                "SELECT * {" +
                    "?lnode <" + RDF.first.getURI() + "> ?first " +
                    "FILTER (isLiteral(?first)) " +
                "}";
        QueryExecution qe = getQueryExecution(queryStr, dataset);
        ResultSet res = qe.execSelect();
        while (res.hasNext()) {
            QuerySolution sol = res.next();
            Node lNode = sol.get("lnode").asNode();
            Node firstVal = sol.get("first").asNode();
            Triple triple = new Triple(lNode, RDF.first.asNode(), firstVal);
            
            Set<ViewQuad<ViewDefinition>> quadViewDefs = pinpointer
                    .getViewCandidates(triple);
            writeTripleMeasureToSink(score_firstStatementHasLiteralObject,
                    triple, quadViewDefs);
        }
    }
    
    
    private void check_collectionNotTerminatedWithNil(SparqlifyDataset dataset)
            throws NotImplementedException, SQLException {
        
        String queryStr =
                "SELECT * { " +
                    "?lnode <" + RDF.rest.getURI() + "> ?rest . " +
                    "FILTER (NOT EXISTS { ?lnode <" + RDF.rest.getURI() + ">+ <" + RDF.nil.getURI() + "> } ) " +
                "}";
        QueryExecution qe = getQueryExecution(queryStr, dataset);
        ResultSet res = qe.execSelect();
        while (res.hasNext()) {
            QuerySolution sol = res.next();
            // report violation
            Node lNode = sol.get("lnode").asNode();
            Node rest = sol.get("rest").asNode();
            Triple triple = new Triple(lNode, RDF.rest.asNode(), rest);
            
            Set<ViewQuad<ViewDefinition>> quadViewDefs = pinpointer
                    .getViewCandidates(triple);
            writeTripleMeasureToSink(score_collectionNotTerminatedWithNil,
                    triple, quadViewDefs);
        }
        qe.close();
    }
    
    
    private void check_restStatementHasMultipleSuccessors(
            SparqlifyDataset dataset) throws NotImplementedException,
            SQLException {
        
        String queryStr =
                "SELECT *  { " +
                    "?par <" + RDF.rest.getURI() + "> ?rest1 . " +
                    "?par <" + RDF.rest.getURI() + "> ?rest2 . " +
                    "FILTER (?rest1 != ?rest2)" +
                "}";
        QueryExecution qe = getQueryExecution(queryStr, dataset);
        ResultSet res = qe.execSelect();
        while (res.hasNext()) {
            QuerySolution sol = res.next();
            // build statement for reporting
            Node par = sol.get("par").asNode();
            Node pred = RDF.rest.asNode();
            Node obj1 = sol.get("rest1").asNode();
            Node obj2 = sol.get("rest2").asNode();
            
            Triple triple1 = new Triple(par, pred, obj1);
            Triple triple2 = new Triple(par, pred, obj2);
            
            reportTriples(Arrays.asList(triple1, triple2),
                    score_restStatementHasMultipleSuccessors);
        }
        qe.close();
    }
    
    
    private void check_restStatementHasMultiplePredecessors(
            SparqlifyDataset dataset) throws NotImplementedException,
            SQLException {
        
        String queryStr =
                "SELECT * {" +
                    "?child <" + RDF.rest.getURI() + "> ?rest . " +
                    "?par1 <" + RDF.rest.getURI() + "> ?child . " +
                    "?par2 <" + RDF.rest.getURI() + "> ?child . " +
                    "FILTER ( ?par1 != ?par2 ) " +
                "}";
        
        QueryExecution qe = getQueryExecution(queryStr, dataset);
        ResultSet res = qe.execSelect();
        while (res.hasNext()) {
            QuerySolution sol = res.next();
            // build statement for reporting
            Node child = sol.get("child").asNode();
            Node pred = RDF.rest.asNode();
            Node subj1 = sol.get("par1").asNode();
            Node subj2 = sol.get("par2").asNode();
            
            Triple triple1 = new Triple(subj1, pred, child);
            Triple triple2 = new Triple(subj2, pred, child);
            
            reportTriples(Arrays.asList(triple1, triple2),
                    score_restStatementHasMultiplePredecessors);
        }
        qe.close();
    }
    
    
    private QueryExecution getQueryExecution(String queryStr, SparqlifyDataset dataset) {
        Query query = QueryFactory.create(queryStr);
        QueryExecution qe;
        if (dataset.isSparqlService() && dataset.getSparqlServiceUri() != null) {
            qe = QueryExecutionFactory.createServiceRequest(
                    dataset.getSparqlServiceUri(), query);
        } else {
            qe = QueryExecutionFactory.create(query, dataset);
        }
        
        return qe;
    }
    
    
    private void reportTriples(List<Triple> triples, float value)
            throws NotImplementedException, SQLException {
        
        List<Pair<Triple, Set<ViewQuad<ViewDefinition>>>> pinpointResults =
                new ArrayList<Pair<Triple,Set<ViewQuad<ViewDefinition>>>>();
        
        for (Triple triple : triples) {
            
            Set<ViewQuad<ViewDefinition>> quadViewDefs =
                    pinpointer.getViewCandidates(triple);
            
            pinpointResults.add(
                    new Pair<Triple, Set<ViewQuad<ViewDefinition>>>(
                            triple, quadViewDefs));
        }
        writeTriplesMeasureToSink(value, pinpointResults);
    }
}
