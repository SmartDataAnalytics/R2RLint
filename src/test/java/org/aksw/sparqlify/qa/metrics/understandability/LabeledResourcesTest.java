package org.aksw.sparqlify.qa.metrics.understandability;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.dataset.SparqlifyDataset;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.pinpointing.Pinpointer;
import org.aksw.sparqlify.qa.sinks.BooleanTestingSink;
import org.aksw.sparqlify.qa.sinks.TriplePosition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test_bool_beans.xml"})
public class LabeledResourcesTest {
    
    @Autowired
    private Pinpointer pinpointer;
    @Autowired
    private BooleanTestingSink sink;
    @Autowired
    private LabeledResources metric;
    
    private final List<String> prefixes = Arrays.asList("http://ex.org/");
    
    
    @Before
    public void setUp() throws Exception {
        Collection<ViewDefinition> viewDefs = new ArrayList<ViewDefinition>();
        pinpointer.registerViewDefs(viewDefs);
    }
    
    
    /*
     * no violations: everything's labeled
     */
    public SparqlifyDataset dataset01() {
        String content = 
            "<http://ex.org/res/01> <" + RDF.type.getURI() + "> <http://ex.org/Class01> ." +
            "<http://ex.org/res/02> <" + RDF.type.getURI() + "> <http://ex.org/Class02> ." +
            "<http://ex.org/prop01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> ." +
            "<http://ex.org/Class01> <" + RDFS.label.getURI() + "> \"Class 01\" . " +
            "<http://ex.org/Class02> <" + RDFS.label.getURI() + "> \"Class 02\" . " +
            "<http://ex.org/res/01> <" + RDFS.label.getURI() + "> \"Resource 01\" . " +
            "<http://ex.org/res/02> <" + RDFS.label.getURI() + "> \"Resource 02\" . " +
            "<http://ex.org/prop01> <" + RDFS.label.getURI() + "> \"Property 01\" . " +
            "<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
            "_:23 <http://ex.org/prop01> \"42\" . ";
        
        SparqlifyDataset dataset = new SparqlifyDataset();
        dataset.read(new StringReader(content), null, "TTL");
        dataset.registerDump(new StringReader(content));
        dataset.setPrefixes(prefixes);
        
        return dataset;
    }
    
    @Test
    public synchronized void test01() throws NotImplementedException, SQLException {
        String metricName = "test01";
        metric.setName(metricName);
        metric.setParentDimension("parent");
        metric.initMeasureDataSink();
        metric.clearCaches();
        
        SparqlifyDataset dataset = dataset01();
        metric.assessDataset(dataset);
        
        assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
        assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
        assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
    }
    
    
    /*
     * not labeled subject
     */
    public SparqlifyDataset dataset02() {
        String content = 
            "<http://ex.org/res/01> <" + RDF.type.getURI() + "> <http://ex.org/Class01> ." +
            "<http://ex.org/res/02> <" + RDF.type.getURI() + "> <http://ex.org/Class02> ." +
            "<http://ex.org/prop01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> ." +
            "<http://ex.org/Class01> <" + RDFS.label.getURI() + "> \"Class 01\" . " +
            "<http://ex.org/Class02> <" + RDFS.label.getURI() + "> \"Class 02\" . " +
            "<http://ex.org/res/02> <" + RDFS.label.getURI() + "> \"Resource 02\" . " +
            "<http://ex.org/prop01> <" + RDFS.label.getURI() + "> \"Property 01\" . " +
            "<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
            "_:23 <http://ex.org/prop01> \"42\" . ";
        
        SparqlifyDataset dataset = new SparqlifyDataset();
        dataset.read(new StringReader(content), null, "TTL");
        dataset.registerDump(new StringReader(content));
        dataset.setPrefixes(prefixes);
        
        return dataset;
    }
    
    @Test
    public synchronized void test02() throws NotImplementedException, SQLException {
        String metricName = "test02";
        metric.setName(metricName);
        metric.setParentDimension("parent");
        metric.initMeasureDataSink();
        metric.clearCaches();
        
        SparqlifyDataset dataset = dataset02();
        metric.assessDataset(dataset);
        
        assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
        assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
        assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
    }
    
    
    /*
     * not labeled predicate
     */
    public SparqlifyDataset dataset03() {
        String content = 
            "<http://ex.org/res/01> <" + RDF.type.getURI() + "> <http://ex.org/Class01> ." +
            "<http://ex.org/res/02> <" + RDF.type.getURI() + "> <http://ex.org/Class02> ." +
            "<http://ex.org/Class01> <" + RDFS.label.getURI() + "> \"Class 01\" . " +
            "<http://ex.org/Class02> <" + RDFS.label.getURI() + "> \"Class 02\" . " +
            "<http://ex.org/res/01> <" + RDFS.label.getURI() + "> \"Resource 01\" . " +
            "<http://ex.org/res/02> <" + RDFS.label.getURI() + "> \"Resource 02\" . " +
            "<http://ex.org/res/01> <http://ex.org/prop01> \"23\" ." +
            "_:23 <http://ex.org/prop01> \"42\" . ";
        
        SparqlifyDataset dataset = new SparqlifyDataset();
        dataset.read(new StringReader(content), null, "TTL");
        dataset.registerDump(new StringReader(content));
        dataset.setPrefixes(prefixes);
        
        return dataset;
    }
    
    @Test
    public synchronized void test03() throws NotImplementedException, SQLException {
        String metricName = "test03";
        metric.setName(metricName);
        metric.setParentDimension("parent");
        metric.initMeasureDataSink();
        metric.clearCaches();
        
        SparqlifyDataset dataset = dataset03();
        metric.assessDataset(dataset);
        
        assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
        assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
        assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
    }
    
    
    /*
     * not labeled object
     */
    public SparqlifyDataset dataset04() {
        String content = 
            "<http://ex.org/res/01> <" + RDF.type.getURI() + "> <http://ex.org/Class01> ." +
            "<http://ex.org/res/02> <" + RDF.type.getURI() + "> <http://ex.org/Class02> ." +
            "<http://ex.org/prop01> <" + RDF.type.getURI() + "> <" + RDF.Property.getURI() + "> ." +
            "<http://ex.org/Class01> <" + RDFS.label.getURI() + "> \"Class 01\" . " +
            "<http://ex.org/Class02> <" + RDFS.label.getURI() + "> \"Class 02\" . " +
            "<http://ex.org/res/01> <" + RDFS.label.getURI() + "> \"Resource 01\" . " +
            "<http://ex.org/res/02> <" + RDFS.label.getURI() + "> \"Resource 02\" . " +
            "<http://ex.org/prop01> <" + RDFS.label.getURI() + "> \"Property 01\" . " +
            "<http://ex.org/res/01> <http://ex.org/prop01> <http://ex.org/res/03> ." +
            "_:23 <http://ex.org/prop01> \"42\" . ";
        
        SparqlifyDataset dataset = new SparqlifyDataset();
        dataset.read(new StringReader(content), null, "TTL");
        dataset.registerDump(new StringReader(content));
        dataset.setPrefixes(prefixes);
        
        return dataset;
    }
    
    @Test
    public synchronized void test04() throws NotImplementedException, SQLException {
        String metricName = "test04";
        metric.setName(metricName);
        metric.setParentDimension("parent");
        metric.initMeasureDataSink();
        metric.clearCaches();
        
        SparqlifyDataset dataset = dataset04();
        metric.assessDataset(dataset);
        
        assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.SUBJECT));
        assertFalse(sink.nodeMeasureWritten(metricName, TriplePosition.PREDICATE));
        assertTrue(sink.nodeMeasureWritten(metricName, TriplePosition.OBJECT));
    }
}
