package org.aksw.sparqlify.qa.metrics.interpretability;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.aksw.sparqlify.qa.sinks.ValueTestingSink;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test_val_beans.xml"})
public class CorrectCollectionUseTest {
    
    private float score_nilHasRest = (float) 0.1;
    private float score_restStatementHasLiteralObject = (float) 0.2;
    private float score_noneOrMultipleFirstStatements = (float) 0.3;
    private float score_firstStatementHasLiteralObject = (float) 0.4;
    private float score_collectionNotTerminatedWithNil = (float) 0.5;
    private float score_restStatementHasMultipleSuccessors = (float) 0.6;
    private float score_restStatementHasMultiplePredecessors = (float) 0.7;
    private float noViolation = -1;
    
    @Autowired
    private ValueTestingSink sink;
    @Autowired
    private Pinpointer pinpointer;
    @Autowired
    private CorrectCollectionUse metric;
    
    @Before
    public void setUp() throws Exception {
        // set up dummy pinpointer
        Collection<ViewDefinition> viewDefs = new ArrayList<ViewDefinition>();
        pinpointer.registerViewDefs(viewDefs);
        
        // init values that are written in different error cases
        metric.setScore_nilHasRest(score_nilHasRest);
        metric.setScore_restStatementHasLiteralObject(score_restStatementHasLiteralObject);
        metric.setScore_noneOrMultipleFirstStatements(score_noneOrMultipleFirstStatements);
        metric.setScore_firstStatementHasLiteralObject(score_firstStatementHasLiteralObject);
        metric.setScore_collectionNotTerminatedWithNil(score_collectionNotTerminatedWithNil);
        metric.setScore_restStatementHasMultipleSuccessors(score_restStatementHasMultipleSuccessors);
        metric.setScore_restStatementHasMultiplePredecessors(score_restStatementHasMultiplePredecessors);
    }
    
    
    /*
     * no collection used --> no violations
     */
    public SparqlifyDataset dataset01() {
        String content = 
            "<http://ex.org/Class01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> ." +
            "<http://ex.org/res/01> <" + RDF.type.getURI() + "> <http://ex.org/Class01> ." +
            "<http://ex.org/prop01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> ." +
            "<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/02> ." +
            "_:23 <http://ex.org/prop01> <http://ex.org/res/02> . ";
        
        SparqlifyDataset dataset = new SparqlifyDataset();
        dataset.read(new StringReader(content), null, "TTL");
        dataset.registerDump(new StringReader(content));
        
        return dataset;
    }
    
    @Test
    public synchronized void test01() throws NotImplementedException, SQLException {
        String metricName = "test01";
        metric.setName(metricName);
        metric.setParentDimension("parent");
        metric.initMeasureDataSink();
        
        SparqlifyDataset dataset = dataset01();
        metric.assessDataset(dataset);
        
        assertEquals(noViolation, sink.writtenValue(metricName), 0);
    }


    /*
     * list with no violations
     */
    public SparqlifyDataset dataset02() {
        String content = 
            "<http://ex.org/Class01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> ." +
            "<http://ex.org/res/l01> <" + RDF.type.getURI() + "> <http://ex.org/Class01> ." +
            "<http://ex.org/prop01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> ." +
            "_:l05 <" + RDF.rest.getURI() + "> <" + RDF.nil + "> . " +
            "_:l05 <" + RDF.first.getURI() + "> <http://ex.org/res/m05> . " +
            "_:l04 <" + RDF.rest.getURI() + "> _:l05 . " +
            "_:l04 <" + RDF.first.getURI() + "> <http://ex.org/res/m04> . " +
            "_:l03 <" + RDF.rest.getURI() + "> _:l04 . " +
            "_:l03 <" + RDF.first.getURI() + "> <http://ex.org/res/m03> . " +
            "_:l02 <" + RDF.rest.getURI() + "> _:l03 . " +
            "_:l02 <" + RDF.first.getURI() + "> <http://ex.org/res/m02> . " +
            "_:l01 <" + RDF.rest.getURI() + "> _:l02 . " +
            "_:l01 <" + RDF.first.getURI() + "> <http://ex.org/res/m01> . " +
            "<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . ";
        
        SparqlifyDataset dataset = new SparqlifyDataset();
        dataset.read(new StringReader(content), null, "TTL");
        dataset.registerDump(new StringReader(content));
        
        return dataset;
    }
    
    @Test
    public synchronized void test02() throws NotImplementedException, SQLException {
        String metricName = "test02";
        metric.setName(metricName);
        metric.setParentDimension("parent");
        metric.initMeasureDataSink();
        
        SparqlifyDataset dataset = dataset02();
        metric.assessDataset(dataset);
        
        assertEquals(noViolation, sink.writtenValue(metricName), 0);
    }
    
    
    /*
     * list with multiple predecessors
     */
    public SparqlifyDataset dataset03() {
        String content = 
            "<http://ex.org/Class01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> ." +
            "<http://ex.org/res/l01> <" + RDF.type.getURI() + "> <http://ex.org/Class01> ." +
            "<http://ex.org/prop01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> ." +
            "<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . " +
            "_:l01 <" + RDF.first.getURI() + "> <http://ex.org/res/m01> . " +
            "_:l01 <" + RDF.rest.getURI() + "> _:l02 . " +
            "_:l02 <" + RDF.first.getURI() + "> <http://ex.org/res/m02> . " +
            "_:l02 <" + RDF.rest.getURI() + "> _:l03 . " +
            "_:l71 <" + RDF.first.getURI() + "> <http://ex.org/res/m71> . " +
            "_:l71 <" + RDF.rest.getURI() + "> _:l03 . " +
            "_:l03 <" + RDF.first.getURI() + "> <http://ex.org/res/m03> . " +
            "_:l03 <" + RDF.rest.getURI() + "> _:l04 . " +
            "_:l04 <" + RDF.first.getURI() + "> <http://ex.org/res/m04> . " +
            "_:l04 <" + RDF.rest.getURI() + "> _:l05 . " +
            "_:l05 <" + RDF.first.getURI() + "> <http://ex.org/res/m05> . " +
            "_:l05 <" + RDF.rest.getURI() + "> <" + RDF.nil.getURI() + "> . ";
        
        SparqlifyDataset dataset = new SparqlifyDataset();
        dataset.read(new StringReader(content), null, "TTL");
        dataset.registerDump(new StringReader(content));
        
        return dataset;
    }
    
    @Test
    public void test03() throws NotImplementedException, SQLException {
        String metricName = "test03";
        metric.setName(metricName);
        metric.setParentDimension("parent");
        metric.initMeasureDataSink();
        
        SparqlifyDataset dataset = dataset03();
        metric.assessDataset(dataset);
        
        assertEquals(score_restStatementHasMultiplePredecessors,
                sink.writtenValue(metricName), 0);
    }
    
    
    /*
     * list with multiple successors
     */
    public SparqlifyDataset dataset04() {
        String content = 
            "<http://ex.org/Class01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> ." +
            "<http://ex.org/res/l01> <" + RDF.type.getURI() + "> <http://ex.org/Class01> ." +
            "<http://ex.org/prop01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> ." +
            "<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . " +
            "_:l01 <" + RDF.first.getURI() + "> <http://ex.org/res/m01> . " +
            "_:l01 <" + RDF.rest.getURI() + "> _:l02 . " +
            "_:l02 <" + RDF.first.getURI() + "> <http://ex.org/res/m02> . " +
            "_:l02 <" + RDF.rest.getURI() + "> _:l03 . " +
            "_:l71 <" + RDF.first.getURI() + "> <http://ex.org/res/m71> . " +
            "_:l71 <" + RDF.rest.getURI() + "> <" + RDF.nil.getURI() + "> . " +
            "_:l03 <" + RDF.first.getURI() + "> <http://ex.org/res/m03> . " +
            "_:l03 <" + RDF.rest.getURI() + "> _:l04 . " +
            "_:l03 <" + RDF.rest.getURI() + "> _:l71 . " +
            "_:l04 <" + RDF.first.getURI() + "> <http://ex.org/res/m04> . " +
            "_:l04 <" + RDF.rest.getURI() + "> _:l05 . " +
            "_:l05 <" + RDF.first.getURI() + "> <http://ex.org/res/m05> . " +
            "_:l05 <" + RDF.rest.getURI() + "> <" + RDF.nil.getURI() + "> . ";
        
        SparqlifyDataset dataset = new SparqlifyDataset();
        dataset.read(new StringReader(content), null, "TTL");
        dataset.registerDump(new StringReader(content));
        
        return dataset;
    }
    
    @Test
    public synchronized void test04() throws NotImplementedException, SQLException {
        String metricName = "test04";
        metric.setName(metricName);
        metric.setParentDimension("parent");
        metric.initMeasureDataSink();
        
        SparqlifyDataset dataset = dataset04();
        metric.assessDataset(dataset);
        
        assertEquals(score_restStatementHasMultipleSuccessors, sink.writtenValue(metricName), 0);
    }
    
    
    /*
     * list not terminated with rdf:nil
     */
    public SparqlifyDataset dataset05() {
        String content = 
            "<http://ex.org/Class01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> ." +
            "<http://ex.org/res/l01> <" + RDF.type.getURI() + "> <http://ex.org/Class01> ." +
            "<http://ex.org/prop01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> ." +
            "_:l05 <" + RDF.first.getURI() + "> <http://ex.org/res/m05> . " +
            "_:l04 <" + RDF.rest.getURI() + "> _:l05 . " +
            "_:l04 <" + RDF.first.getURI() + "> <http://ex.org/res/m04> . " +
            "_:l03 <" + RDF.rest.getURI() + "> _:l04 . " +
            "_:l03 <" + RDF.first.getURI() + "> <http://ex.org/res/m03> . " +
            "_:l02 <" + RDF.rest.getURI() + "> _:l03 . " +
            "_:l02 <" + RDF.first.getURI() + "> <http://ex.org/res/m02> . " +
            "_:l01 <" + RDF.rest.getURI() + "> _:l02 . " +
            "_:l01 <" + RDF.first.getURI() + "> <http://ex.org/res/m01> . " +
            "<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . ";
        
        SparqlifyDataset dataset = new SparqlifyDataset();
        dataset.read(new StringReader(content), null, "TTL");
        dataset.registerDump(new StringReader(content));
        
        return dataset;
    }
    
    @Test
    public synchronized void test05() throws NotImplementedException, SQLException {
        String metricName = "test05";
        metric.setName(metricName);
        metric.setParentDimension("parent");
        metric.initMeasureDataSink();
        
        SparqlifyDataset dataset = dataset05();
        metric.assessDataset(dataset);
        
        
        assertEquals(score_collectionNotTerminatedWithNil, sink.writtenValue(metricName), 0);
    }
    
    
    /*
     * rdf:first statement has literal object
     */
    public SparqlifyDataset dataset06() {
        String content = 
            "<http://ex.org/Class01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> ." +
            "<http://ex.org/res/l01> <" + RDF.type.getURI() + "> <http://ex.org/Class01> ." +
            "<http://ex.org/prop01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> ." +
            "_:l05 <" + RDF.rest.getURI() + "> <" + RDF.nil + "> . " +
            "_:l05 <" + RDF.first.getURI() + "> <http://ex.org/res/m05> . " +
            "_:l04 <" + RDF.rest.getURI() + "> _:l05 . " +
            "_:l04 <" + RDF.first.getURI() + "> <http://ex.org/res/m04> . " +
            "_:l03 <" + RDF.rest.getURI() + "> _:l04 . " +
            "_:l03 <" + RDF.first.getURI() + "> <http://ex.org/res/m03> . " +
            "_:l02 <" + RDF.rest.getURI() + "> _:l03 . " +
            "_:l02 <" + RDF.first.getURI() + "> \"m02\" . " +
            "_:l01 <" + RDF.rest.getURI() + "> _:l02 . " +
            "_:l01 <" + RDF.first.getURI() + "> <http://ex.org/res/m01> . " +
            "<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . ";
        
        SparqlifyDataset dataset = new SparqlifyDataset();
        dataset.read(new StringReader(content), null, "TTL");
        dataset.registerDump(new StringReader(content));
        
        return dataset;
    }
    
    @Test
    public synchronized void test06() throws NotImplementedException, SQLException {
        String metricName = "test06";
        metric.setName(metricName);
        metric.setParentDimension("parent");
        metric.initMeasureDataSink();
        
        SparqlifyDataset dataset = dataset06();
        metric.assessDataset(dataset);
        
        assertEquals(score_firstStatementHasLiteralObject, sink.writtenValue(metricName), 0);
    }
    
    
    /*
     * list node with no rdf:first node
     */
    public SparqlifyDataset dataset07() {
        String content = 
            "<http://ex.org/Class01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> ." +
            "<http://ex.org/res/l01> <" + RDF.type.getURI() + "> <http://ex.org/Class01> ." +
            "<http://ex.org/prop01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> ." +
            "<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . " +
            "_:l01 <" + RDF.first.getURI() + "> <http://ex.org/res/m01> . " +
            "_:l01 <" + RDF.rest.getURI()  +"> _:l02 . " +
            "_:l02 <" + RDF.rest.getURI() + "> _:l03 . " +
            "_:l03 <" + RDF.first.getURI() + "> <http://ex.org/res/m03> . " +
            "_:l03 <" + RDF.rest.getURI() + "> _:l04 . " +
            "_:l04 <" + RDF.first.getURI() + "> <http://ex.org/res/m04> . " +
            "_:l04 <" + RDF.rest.getURI() + "> _:l05 . " +
            "_:l05 <" + RDF.first.getURI() + "> <http://ex.org/res/m05> . " +
            "_:l05 <" + RDF.rest.getURI() + "> <" + RDF.nil.getURI() + "> . ";
        
        SparqlifyDataset dataset = new SparqlifyDataset();
        dataset.read(new StringReader(content), null, "TTL");
        dataset.registerDump(new StringReader(content));
        
        return dataset;
    }
    
    @Test
    public synchronized void test07() throws NotImplementedException, SQLException {
        String metricName = "test07";
        metric.setName(metricName);
        metric.setParentDimension("parent");
        metric.initMeasureDataSink();
        
        SparqlifyDataset dataset = dataset07();
        metric.assessDataset(dataset);
        
        assertEquals(score_noneOrMultipleFirstStatements, sink.writtenValue(metricName), 0);
    }
    
    
    
    /*
     * list node with multiple rdf:first nodes
     */
    public SparqlifyDataset dataset08() {
        String content = 
            "<http://ex.org/Class01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> ." +
            "<http://ex.org/res/l01> <" + RDF.type.getURI() + "> <http://ex.org/Class01> ." +
            "<http://ex.org/prop01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> ." +
            "<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . " +
            "_:l01 <" + RDF.first.getURI() + "> <http://ex.org/res/m01> . " +
            "_:l01 <" + RDF.rest.getURI() + "> _:l02 . " +
            "_:l02 <" + RDF.first.getURI() + "> <http://ex.org/res/m02> . " +
            "_:l02 <" + RDF.first.getURI() + "> <http://ex.org/res/m72> . " +
            "_:l02 <" + RDF.rest.getURI() + "> _:l03 . " +
            "_:l03 <" + RDF.first.getURI() + "> <http://ex.org/res/m03> . " +
            "_:l03 <" + RDF.rest.getURI() + "> _:l04 . " +
            "_:l04 <" + RDF.first.getURI() + "> <http://ex.org/res/m04> . " +
            "_:l04 <" + RDF.rest.getURI() + "> _:l05 . " +
            "_:l05 <" + RDF.first.getURI() + "> <http://ex.org/res/m05> . " +
            "_:l05 <" + RDF.rest.getURI() + "> <" + RDF.nil.getURI() + "> . ";
        
        SparqlifyDataset dataset = new SparqlifyDataset();
        dataset.read(new StringReader(content), null, "TTL");
        dataset.registerDump(new StringReader(content));
        
        return dataset;
    }
    
    @Test
    public synchronized void test08() throws NotImplementedException, SQLException {
        String metricName = "test08";
        metric.setName(metricName);
        metric.setParentDimension("parent");
        metric.initMeasureDataSink();
        
        SparqlifyDataset dataset = dataset08();
        metric.assessDataset(dataset);
        
        assertEquals(score_noneOrMultipleFirstStatements, sink.writtenValue(metricName), 0);
    }
    
    
    /*
     * list has literal in rdf:rest
     */
    public SparqlifyDataset dataset09() {
        String content = 
            "<http://ex.org/Class01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> ." +
            "<http://ex.org/res/l01> <" + RDF.type.getURI() + "> <http://ex.org/Class01> ." +
            "<http://ex.org/prop01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> ." +
            "<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . " +
            "_:l01 <" + RDF.first.getURI() + "> <http://ex.org/res/m01> . " +
            "_:l01 <" + RDF.rest.getURI() + "> _:l02 . " +
            "_:l02 <" + RDF.first.getURI() + "> <http://ex.org/res/m02> . " +
            "_:l02 <" + RDF.rest.getURI() + "> _:l03 . " +
            "_:l03 <" + RDF.first.getURI() + "> <http://ex.org/res/m03> . " +
            "_:l03 <" + RDF.rest.getURI() + "> \"l04\" . " +
            "_:l04 <" + RDF.first.getURI() + "> <http://ex.org/res/m04> . " +
            "_:l04 <" + RDF.rest.getURI() + "> _:l05 . " +
            "_:l05 <" + RDF.first.getURI() + "> <http://ex.org/res/m05> . " +
            "_:l05 <" + RDF.rest.getURI() + "> <" + RDF.nil.getURI() + "> . ";
        
        SparqlifyDataset dataset = new SparqlifyDataset();
        dataset.read(new StringReader(content), null, "TTL");
        dataset.registerDump(new StringReader(content));
        
        return dataset;
    }
    
    /*
     * This test only works if the restStatementHasLiteralObject case is checked
     * after the collectionNotTerminatedWithNil case. Otherwise the
     * collectionNotTerminatedWithNil score is reported, since having a literal
     * at rest position means that the collection "chain" ends with the literal
     */
    @Test
    public synchronized void test09() throws NotImplementedException, SQLException {
        String metricName = "test09";
        metric.setName(metricName);
        metric.setParentDimension("parent");
        metric.initMeasureDataSink();
        
        SparqlifyDataset dataset = dataset09();
        metric.assessDataset(dataset);
        
        assertEquals(score_restStatementHasLiteralObject, sink.writtenValue(metricName), 0);
    }
    
    
    /*
     * rdf:nil node has successor
     */
    public SparqlifyDataset dataset10() {
        String content = 
            "<http://ex.org/Class01> <" + RDF.type.getURI() + "> <" + RDFS.Class.getURI() + "> ." +
            "<http://ex.org/res/l01> <" + RDF.type.getURI() + "> <http://ex.org/Class01> ." +
            "<http://ex.org/prop01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> ." +
            "<http://ex.org/res/l01> <http://ex.org/prop01> _:l01 . " +
            "<" + RDF.nil.getURI() + "> <" + RDF.first.getURI() + "> <http://ex.org/res/m03> . " +
            "<" + RDF.nil.getURI() + "> <" + RDF.rest.getURI() + "> _:l04 . " +
            "_:l04 <" + RDF.first.getURI() + "> <http://ex.org/res/m04> . " +
            "_:l04 <" + RDF.rest.getURI() + "> _:l05 . " +
            "_:l05 <" + RDF.first.getURI() + "> <http://ex.org/res/m05> . " +
            "_:l05 <" + RDF.rest.getURI() + "> <" + RDF.nil.getURI() + "> . ";
        
        SparqlifyDataset dataset = new SparqlifyDataset();
        dataset.read(new StringReader(content), null, "TTL");
        dataset.registerDump(new StringReader(content));
        
        return dataset;
    }
    
    @Test
    public synchronized void test10() throws NotImplementedException, SQLException {
        String metricName = "test10";
        metric.setName(metricName);
        metric.setParentDimension("parent");
        metric.initMeasureDataSink();
        
        SparqlifyDataset dataset = dataset10();
        metric.assessDataset(dataset);
        
        assertEquals(score_nilHasRest, sink.writtenValue(metricName), 0);
    }
}
