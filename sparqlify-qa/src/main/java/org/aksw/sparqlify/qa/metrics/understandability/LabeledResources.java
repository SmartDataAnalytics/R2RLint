package org.aksw.sparqlify.qa.metrics.understandability;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.DatasetMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.aksw.sparqlify.qa.sinks.TriplePosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * This metric should find individuals, classes and properties that are not
 * labeled and report them.
 * 
 * For performance reasons this metric currently reports only the first
 * occurrence of a not labeled resource. First this avoids additional scans on
 * the dataset (to find all occurrences) and also prevents reporting the same
 * resource appearing on different positions.
 * The drawback of this approach is, that not all quad candidates (in the view
 * definitions) are found but only these that match the considered triple.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class LabeledResources extends MetricImpl implements DatasetMetric {
    private static Logger logger = LoggerFactory.getLogger(LabeledResources.class);
    @Autowired
    private Pinpointer pinpointer;
    
    List<Node> seenResources;
    
    
    protected void clearCaches() {
        seenResources = new ArrayList<Node>();
    }
    
    public LabeledResources() {
        super();
        seenResources = new ArrayList<Node>();
    }
    
    
    @Override
    public void assessDataset(SparqlifyDataset dataset)
            throws NotImplementedException, SQLException {
        
        int dbgCount = 0;
        for (Triple triple : dataset) {
            if (dbgCount++ % 10000 == 0) logger.debug("assessed " + dbgCount + " triples");
            
            Node subject = triple.getSubject();
            Node predicate = triple.getPredicate();
            Node object = triple.getObject();
            
            /* subject */
            if (!seenResources.contains(subject)) {
                // subject is a local URI resource 
                boolean isLocal = false;
                for (String prefix : dataset.getPrefixes()) {
                    if (subject.isURI() && subject.getURI().startsWith(prefix)) {
                        isLocal = true;
                        break;
                    }
                }
                
                if (isLocal) {
                    
                    boolean hasLabel = false;
                    String lblQueryStr = "ASK { <" + subject.getURI() + "> <" + RDFS.label.getURI() + "> ?l }";
                    Query lblQuery = QueryFactory.create(lblQueryStr);
                    
                    QueryExecution lblQe;
                    if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
                        lblQe = QueryExecutionFactory.createServiceRequest(
                                dataset.getSparqlServiceUri(), lblQuery);
                    } else {
                        lblQe = QueryExecutionFactory.create(lblQuery, dataset);
                    }
                    
                    hasLabel = lblQe.execAsk();
                    lblQe.close();
                    
                    if (!hasLabel) {
                        Set<ViewQuad<ViewDefinition>> viewQuads = pinpointer
                                .getViewCandidates(triple);
                        writeNodeTripleMeasureToSink(0, TriplePosition.SUBJECT,
                                triple, viewQuads);
                    }
                }
                seenResources.add(subject);
            }
            
            
            /* predicate */
            
            if (!seenResources.contains(predicate)) {
                // predicate is a local URI resource
                boolean isLocal = false;
                for (String prefix : dataset.getPrefixes()) {
                    if (predicate.getURI().startsWith(prefix)) {
                        isLocal = true;
                        break;
                    }
                }
                
                if(isLocal){
                    
                    boolean hasLabel = false;
                    String lblQueryStr = "ASK { <" + predicate.getURI() + "> <" + RDFS.label.getURI() + "> ?l }";
                    Query lblQuery = QueryFactory.create(lblQueryStr);
                    
                    QueryExecution lblQe;
                    if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
                        lblQe = QueryExecutionFactory.createServiceRequest(
                                dataset.getSparqlServiceUri(), lblQuery);
                    } else {
                        lblQe = QueryExecutionFactory.create(lblQuery, dataset);
                    }
                    
                    hasLabel = lblQe.execAsk();
                    lblQe.close();
                    
                    if (!hasLabel) {
                        Set<ViewQuad<ViewDefinition>> viewQuads = pinpointer
                                .getViewCandidates(triple);
                        writeNodeTripleMeasureToSink(0, TriplePosition.PREDICATE,
                                triple, viewQuads);
                    }
                }
                seenResources.add(predicate);
            }
            
            
            /* object */
            
            // object is a URI resource, not seen yet...
            if (object.isURI() &&!seenResources.contains(object)) {
                
                // object is a local resource...
                boolean isLocal = false;
                for (String prefix : dataset.getPrefixes()) {
                    if (object.getURI().startsWith(prefix)) {
                        isLocal = true;
                        break;
                    }
                }
                if(isLocal) {
                    
                    boolean hasLabel = false;
                    String lblQueryStr = "ASK { <" + object.getURI() + "> <" + RDFS.label.getURI() + "> ?l }";
                    Query lblQuery = QueryFactory.create(lblQueryStr);
                    
                    QueryExecution lblQe;
                    if (dataset.isSparqlService() && dataset.getSparqlServiceUri()!=null) {
                        lblQe = QueryExecutionFactory.createServiceRequest(
                                dataset.getSparqlServiceUri(), lblQuery);
                    } else {
                        lblQe = QueryExecutionFactory.create(lblQuery, dataset);
                    }
                    
                    hasLabel = lblQe.execAsk();
                    lblQe.close();
                    
                    if (!hasLabel) {
                        Set<ViewQuad<ViewDefinition>> viewQuads = pinpointer.getViewCandidates(triple);
                        writeNodeTripleMeasureToSink(0, TriplePosition.OBJECT,
                                triple, viewQuads);
                    }
                }
                seenResources.add(object);
            }
        }
    }
}
