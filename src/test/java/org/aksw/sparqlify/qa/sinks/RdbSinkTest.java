package org.aksw.sparqlify.qa.sinks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.util.MapReader;
import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test_rdbsink_beans.xml"})
public class RdbSinkTest {

    @Autowired
    private RdbSink sink;
    private ViewDefinitionFactory vdf;

    @Before
    public void setUp() throws Exception {
        Map<String, String> typeAlias = MapReader.read(
                new File("src/test/resources/type-map.h2.tsv"));
        vdf = SparqlifyUtils.createDummyViewDefinitionFactory(typeAlias);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testInitDb() throws SQLException {
        List<String> expectedTblNames = new ArrayList<String>(sink.tableNames);
        
        ResultSet res = sink.conn.getMetaData().getTables(null, null, null,
                new String[] { "TABLE" });
        while (res.next()) {
            String tblName = res.getString("TABLE_NAME").toLowerCase();
            expectedTblNames.remove(tblName);
        }
        assertEquals(0, expectedTblNames.size());
    }

    @Test
    public void testDatasetMeasureDatumWrite() throws SQLException {
        sink.emptyDb();

        // set up test data
        String expctdDim = "testDim01";
        String expctdMetric = "testMetric01";
        float expctdVal = (float) 0.01;

        MeasureDatum testDatum = new DatasetMeasureDatum(expctdDim, expctdMetric,
                expctdVal);

        // write to sink
        sink.write(testDatum);
        // this should have caused the following inserts:
        // measure_datum: (#id#, 'testDim01', 'testMetric01', 0.01, #assmnt_id#, #timestamp#)
        
        
        // check if things are written correctly
        ResultSet res = sink.conn.createStatement().executeQuery(
                "SELECT * FROM " + sink.measureDatumTbl);
        
        int resCounter = 0;
        while (res.next()) {
            resCounter++;
            
            // id
            long assessmentId = res.getInt("id");
            assertNotNull(assessmentId);
            
            // dimension
            String dim = res.getString("dimension");
            assertEquals(expctdDim, dim);
            
            // metric
            String metr = res.getString("metric");
            assertEquals(expctdMetric, metr);
            
            // value
            float val = res.getFloat("value");
            assertEquals(expctdVal, val, 0);
            
            // assessment_id
            long assmntId = res.getLong("assessment_id");
            assertNotNull(assmntId);
            
            // timestamp
            long timestamp = res.getLong("timestamp");
            assertNotNull(timestamp);
        }
        
        for (String tblName : sink.tableNames) {
            if (tblName.equals(sink.measureDatumTbl)) continue;
            
            String queryStr = "SELECT count(*) AS count FROM " + tblName;
            res = sink.conn.createStatement().executeQuery(queryStr);
            
            while (res.next()) {
                int num_rows = res.getInt("count");
                assertEquals(0, num_rows);
            }
        }
        assertEquals(1, resCounter);
    }

    private ViewDefinition viewDef01(String name, String tbl) {
        String viewDefStr = 
               "Create View " + name + " As " +
                   "Construct { " +
                       "?a a <http://ex.org/Sth> . " +
                       "?a <http://ex.org/prop> \"some val\"@pl . " +
                   "} " +
                   "With " +
                       "?a = uri(?uri) " +
                   "FROM " +
                       tbl;
        return vdf.create(viewDefStr);
    }
    @Test
    public void testMappingMeasureDatumWrite() throws SQLException {
        sink.emptyDb();

        // set up test data
        String expctdDim = "testDim02";
        String expctdMetric = "testMetric02";
        String expctdGraphUri = "http://ex.org/graph01/";
        String expctdSubjUri = "http://ex.org/res91";
        String expctdPredUri = "http://ex.org/prop91";
        String expctdObjLit = "something";
        String expctdViewDefName = "view_def_01";
        String expctdViewDefTbl = "urls";
        float expctdVal = (float) 0.02;
        Set<ViewQuad<ViewDefinition>> candidates = new HashSet<ViewQuad<ViewDefinition>>();
        ViewDefinition viewDef = viewDef01(expctdViewDefName, expctdViewDefTbl);

        Quad quad = new Quad(
                NodeFactory.createURI(expctdGraphUri),
                new Triple(
                        NodeFactory.createURI(expctdSubjUri),
                        NodeFactory.createURI(expctdPredUri),
                        NodeFactory.createLiteral(expctdObjLit)));

        ViewQuad<ViewDefinition> viewQuad = new ViewQuad<ViewDefinition>(viewDef, quad);
        candidates.add(viewQuad);

        MeasureDatum testDatum = new MappingMeasureDatum(expctdDim,
                expctdMetric, expctdVal, candidates);

        // write to sink
        sink.write(testDatum);
        // this should result in the following insertions:
        // 1) measure_datum: (#id#, <expctdDim>, <expctdMetric>, <expctdVal>, #assmnt_id#, #timestamp#)
        // 2) quad: (#id#, <expctdGraphUri>, <expectedSubjUri>, <expctdPredUri>, <expctdObjLit>)
        // 3) view_definition: (#id#, <expctdViewDefName>, <expctdViewDefTbl>, #mapping_definitions#)
        // 4) measure_datum__quad: (#meas_id#, #quad_id#, #assmnt_id#)
        // 5) measure_datum__view_definition: (#meas_id#, #viewdef_id#, #assmnt_id#)
        // 6) view_definition__quad: (#viewdef_id#, #quad_id#, #assmnt_id#)
        List<String> affectedTblsNames = Arrays.asList(sink.measureDatumTbl,
                sink.quadTbl, sink.viewDefTbl, sink.md2quadTbl,
                sink.md2viewDefTbl, sink.vd2quadTbl);

        // check if things are written correctly
        // varibales to cross check
        Long measureDatumId = null;
        Long assessmentId = null;
        Long quadId = null;
        Long viewDefId = null;
        
        // 1) check measure_datum table
        ResultSet mdRes = sink.conn.createStatement().executeQuery(
                "SELECT * FROM " + sink.measureDatumTbl);
        
        int mdResCounter = 0;
        while (mdRes.next()) {
            mdResCounter++;
            
            // id
            measureDatumId = mdRes.getLong("id");
            assertNotNull(measureDatumId);
            
            // dimension
            String dim = mdRes.getString("dimension");
            assertEquals(expctdDim, dim);
            
            // metric
            String metric = mdRes.getString("metric");
            assertNotNull(metric);
            
            // value
            float val = mdRes.getFloat("value");
            assertEquals(expctdVal, val, 0);
            
            // assessment_id
            assessmentId = mdRes.getLong("assessment_id");
            assertNotNull(assessmentId);
            
            // timestamp
            long timestamp = mdRes.getLong("timestamp");
            assertNotNull(timestamp);
        }
        assertEquals(1, mdResCounter);

        // 2) check quad table
        ResultSet qRes = sink.conn.createStatement().executeQuery(
                "SELECT * FROM " + sink.quadTbl);
        
        int qResCounter = 0;
        while (qRes.next()) {
            qResCounter++;
            
            // id
            quadId = qRes.getLong("id");
            assertNotNull(quadId);
            
            // graph
            String graphUri = qRes.getString("graph");
            assertEquals(expctdGraphUri, graphUri);
            
            // subject
            String subjectUri = qRes.getString("subject");
            assertEquals(expctdSubjUri, subjectUri);
            
            // predicate
            String predicateUri = qRes.getString("predicate");
            assertEquals(expctdPredUri, predicateUri);
            
            // object
            String objLit = qRes.getString("object");
            assertEquals("\"" + expctdObjLit + "\"", objLit);
        }
        assertEquals(1, qResCounter);
        
        // 3) check view_definition table
        ResultSet vdRes = sink.conn.createStatement().executeQuery(
                "SELECT * FROM " + sink.viewDefTbl);
        
        int vdResCounter = 0;
        while (vdRes.next()) {
            vdResCounter++;
            
            // id
            viewDefId = vdRes.getLong("id");
            assertNotNull(viewDefId);
            
            // name
            String name = vdRes.getString("name");
            assertEquals(expctdViewDefName, name);
            
            // mapping_sql_op
            String sqlMapOp = vdRes.getString("mapping_sql_op");
            assertEquals(expctdViewDefTbl, sqlMapOp);
            
            // mapping_definitions
            String mappingDefs = vdRes.getString("mapping_definitions");
            assertNotNull(mappingDefs);
        }
        assertEquals(1, vdResCounter);
        
        // 4) check measure_datum_quad table
        ResultSet md2qRes = sink.conn.createStatement().executeQuery(
                "SELECT * FROM " + sink.md2quadTbl);
        
        int md2qResCounter = 0;
        while (md2qRes.next()) {
            md2qResCounter++;
            
            // measure_datum_id
            long measDtmId = md2qRes.getLong("measure_datum_id");
            assertEquals((long) measureDatumId, measDtmId);
            
            // quad_id
            long qId = md2qRes.getLong("quad_id");
            assertEquals((long) quadId, qId);
            
            // assessment_id
            long assmntId = md2qRes.getLong("assessment_id");
            assertEquals((long) assessmentId, assmntId);
        }
        assertEquals(1, md2qResCounter);
        
        // 5) check measure_datum_view_definition table
        ResultSet md2vdRes = sink.conn.createStatement().executeQuery(
                "SELECT * FROM " + sink.md2viewDefTbl);
        
        int md2vdResCounter = 0;
        while (md2vdRes.next()) {
            md2vdResCounter++;
            
            // measure_datum_id
            long measDtmId = md2vdRes.getLong("measure_datum_id");
            assertEquals((long) measureDatumId, measDtmId);
            
            // view_definition_id
            long vdId = md2vdRes.getLong("view_definition_id");
            assertEquals((long) viewDefId, vdId);
            
            // assessment_id
            long assmntId = md2vdRes.getLong("assessment_id");
            assertEquals((long) assessmentId, assmntId);
        }
        assertEquals(1, md2vdResCounter);
        
        // 6) check view_definition__quad
        ResultSet vd2qRes = sink.conn.createStatement().executeQuery(
                "SELECT * FROM " + sink.vd2quadTbl);
        
        int vd2qResCounter = 0;
        while (vd2qRes.next()) {
            vd2qResCounter++;
            
            // view_definition_id
            long vdId = vd2qRes.getLong("view_definition_id");
            assertEquals((long) viewDefId, vdId);
            
            // quad_id
            long qId = vd2qRes.getLong("quad_id");
            assertEquals((long) quadId, qId);
            
            // assessment_id
            long assmntId = vd2qRes.getLong("assessment_id");
            assertEquals((long) assessmentId, assmntId);
        }
        assertEquals(1, vd2qResCounter);
        
        // check whether the remaining tables are empty
        for (String tblName : sink.tableNames) {
            if (affectedTblsNames.contains(tblName)) continue;
            
            String queryStr = "SELECT count(*) AS count FROM " + tblName;
            ResultSet res = sink.conn.createStatement().executeQuery(queryStr);
            
            while (res.next()) {
                int num_rows = res.getInt("count");
                assertEquals(0, num_rows);
            }
        }
    }
    
    @Test
    public void testXYWrite() {
        // set up test data
        // write to sink
        // check if things are written correctly
    }

}
