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

import org.aksw.commons.collections.Pair;
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

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node_Variable;
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

        // 6) check view_definition__quad table
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
    public void testMappingQuadMeasureDatumWrite() throws SQLException {
        sink.emptyDb();

        // set up test data
        String expctdDim = "testDimension03";
        String expctdMetric = "testMetric03";
        float expctdVal = (float) 0.03;
        String expctdViewDefName = "view_definition_03";
        String expctdViewDefTbl = "table_03";
        String expctdGraphUri = "http://ex.org/graph03";
        String expctdSubjUri = "http://ex.org/res03";
        String expctdPredUri = "http://ex.org/pred03";
        String expctdObjLit = "literal 03";
        ViewDefinition viewDef = viewDef01(expctdViewDefName, expctdViewDefTbl);

        Quad quad = new Quad(
                NodeFactory.createURI(expctdGraphUri),
                new Triple(
                        NodeFactory.createURI(expctdSubjUri),
                        NodeFactory.createURI(expctdPredUri),
                        NodeFactory.createLiteral(expctdObjLit)));

        Pair<Quad, ViewDefinition> quadViewDef = new Pair<Quad, ViewDefinition>(quad, viewDef);
        List<Pair<Quad, ViewDefinition>> quadViewDefs = new ArrayList<Pair<Quad, ViewDefinition>>();
        quadViewDefs.add(quadViewDef);

        MeasureDatum measDatum = new MappingQuadMeasureDatum(expctdDim,
                expctdMetric, expctdVal, quadViewDefs);

        // write to sink
        sink.write(measDatum);
        // this should result in the following insertions:
        // 1) measure_datum: (#id#, <expctdDim>, <expctdMetric>, <expctdVal>, #assmnt_id#, #timestamp#)
        // 2) view_definition: (#id#, <expctdViewDefName>, <expctdViewDefTable>, #mapping_defs#)
        // 3) measure_datum__view_definition: (#meas_id#, #view_def_id#, #assmnt_id#)
        // 4) quad: (#id#, <expctdGraphUri>, <expctdSubjUri>, <expctdPredUri>, <expctdObjLit>)
        // 5) measure_datum__quad: (#meas_id#, #quad_id#, #assmnt_id#)
        // 6) view_definition__quad: (#view_def_id#, #quad_id#, #assmnt_id#)

        List<String> affectedTblsNames = Arrays.asList(sink.measureDatumTbl,
                sink.quadTbl, sink.viewDefTbl, sink.md2quadTbl,
                sink.md2viewDefTbl, sink.vd2quadTbl);

        Long measureDatumId = null;
        Long assessmentId = null;
        Long viewDefId = null;
        Long quadId = null;
        // check if things are written correctly

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

        // 2) check view_definition table
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

        // 3) check measure_datum_view_definition table
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

        // 4) check quad table
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

        // 6) check measure_datum_quad table
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

        // 5) check view_definition__quad
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
    public void testMappingVarMeasureDatumWrite() throws SQLException {
        sink.emptyDb();

        // set up test data
        String expctdDim = "testDimension04";
        String expctdMetric = "testMetric04";
        float expctdVal = (float) 0.04;
        String expctdViewDefName = "view_def_04";
        String expctdViewDefTbl = "tbl04";
        String expctdVarName = "var04";

        ViewDefinition viewDef = viewDef01(expctdViewDefName, expctdViewDefTbl);
        List<Pair<Node_Variable, ViewDefinition>> nodeViewDefs = new ArrayList<Pair<Node_Variable, ViewDefinition>>();
        Node_Variable nodeVar = (Node_Variable) NodeFactory.createVariable(expctdVarName);
        Pair<Node_Variable, ViewDefinition> varViewDef = new Pair<Node_Variable, ViewDefinition>(nodeVar , viewDef);
        nodeViewDefs.add(varViewDef);

        MeasureDatum measDatum = new MappingVarMeasureDatum(expctdDim, expctdMetric, expctdVal, nodeViewDefs);

        // write to sink
        sink.write(measDatum);
        // this should result in the following insertions:
        // 1) measure_datum: (#id#, <expctdDim>, <expctdMetric>, <expctdVal>, #assmnt_id#, #timestamp#)
        // 2) variable: (#id#, <expctdName>)
        // 3) view_definition: (#id#, <expctdViewDefName>, <expctdViewDefTbl>, #mapping_defs#)
        // 4) measure_datum__view_definition: (#meas_id#, #view_def_id#, #assmnt_id#)
        // 5) measure_datum__variable: (#meas_id#, #var_id#, #assmnt_id#)
        // 6) view_definition__variable: (#view_def_id#, #var_id#, #assmnt_id#)

        List<String> affectedTblsNames = Arrays.asList(sink.measureDatumTbl,
                sink.varTbl, sink.viewDefTbl, sink.md2viewDefTbl,
                sink.md2varTbl, sink.vd2varTbl);

        Long measureDatumId = null;
        Long assessmentId = null;
        Long varId = null;
        Long viewDefId = null;

        // check if things are written correctly

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

        // 2) check variable table
        ResultSet vRes = sink.conn.createStatement().executeQuery(
                "SELECT * FROM " + sink.varTbl);

        int vResCounter = 0;
        while (vRes.next()) {
            vResCounter++;
            // id
            varId = vRes.getLong("id");
            assertNotNull(varId);

            // name
            String vName = vRes.getString("name");
            assertEquals("?" + expctdVarName, vName);
        }
        assertEquals(1, vResCounter);

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

        // 4) check measure_datum__view_definition table
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

        // 5) check measure_datum__variable table
        ResultSet md2vRes = sink.conn.createStatement().executeQuery(
                "SELECT * FROM " + sink.md2varTbl);

        int md2vResCounter = 0;
        while(md2vRes.next()) {
            md2vResCounter++;

            // measure_datum_id
            long measDtmId = md2vRes.getLong("measure_datum_id");
            assertEquals((long) measureDatumId, measDtmId);

            // variable_id
            long vId = md2vRes.getLong("variable_id");
            assertEquals((long) varId, vId);

            // assessment_id
            long assmntId = md2vRes.getLong("assessment_id");
            assertEquals((long) assessmentId, assmntId);
        }
        assertEquals(1, md2vResCounter);

        // 6) check view_definition__variable table
        ResultSet vd2vRes = sink.conn.createStatement().executeQuery(
                "SELECT * FROM " + sink.vd2varTbl);

        int vd2vResCounter = 0;
        while(vd2vRes.next()) {
            vd2vResCounter++;

            // view_definition_id
            long vdId = vd2vRes.getLong("view_definition_id");
            assertEquals((long) viewDefId, vdId);

            // variable_id
            long vId = vd2vRes.getLong("variable_id");
            assertEquals((long) varId, vId);

            // assessment_id
            long assmntId = vd2vRes.getLong("assessment_id");
            assertEquals((long) assessmentId, assmntId);
        }
        assertEquals(1, vd2vResCounter);

        // not affected tables empty?
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
    public void testNodeMeasureDatumWrite() throws SQLException {
        sink.emptyDb();

        // set up test data
        String expctdDim = "testDim05";
        String expctdMetric= "testMetric05";
        float expctdVal = (float) 0.05;
        String expctdNodeUri = "http://ex.org/node05";
        Node node = NodeFactory.createURI(expctdNodeUri );
        MeasureDatum measDatum = new NodeMeasureDatum(expctdDim, expctdMetric, expctdVal, node);

        // write to sink
        sink.write(measDatum);
        // this should result in the following insertions:
        // 1) measure_datum: (#meas_id#, <expctdDim>, <expctdMetric>, <expctdVal> #assmnt_id#, #timestamp#)
        // 2) node: (#id#, <expctdNodeUri>)
        // 3) measure_datum__node: (#meas_id#, #node_id#, #assmnt_id#)
        List<String> affectedTblsNames = Arrays.asList(sink.measureDatumTbl,
                sink.nodeTbl, sink.md2nodeTbl);

        Long measureDatumId = null;
        Long assessmentId = null;
        Long nodeId = null;

        // check if things are written correctly

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

        // 2) check node table
        ResultSet nRes = sink.conn.createStatement().executeQuery(
                "SELECT * FROM " + sink.nodeTbl);

        int nResCounter = 0;
        while (nRes.next()) {
            nResCounter++;

            // id
            nodeId = nRes.getLong("id");
            assertNotNull(nodeId);

            // name
            String nName = nRes.getString("name");
            assertEquals(expctdNodeUri, nName);
        }
        assertEquals(1, nResCounter);

        // 3) check measure_datum__node table
        ResultSet md2nRes = sink.conn.createStatement().executeQuery(
                "SELECT * FROM " + sink.md2nodeTbl);

        int md2nResCounter = 0;
        while (md2nRes.next()) {
            md2nResCounter++;

            // measure_datum_id
            long measId = md2nRes.getLong("measure_datum_id");
            assertEquals((long) measureDatumId, measId);

            // node_id
            long nId = md2nRes.getLong("node_id");
            assertEquals((long) nodeId, nId);

            // assessment_id
            long assmntId = md2nRes.getLong("assessment_id");
            assertEquals((long) assessmentId, assmntId);
        }
        assertEquals(1, md2nResCounter);

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
    public void testNodeTripleMeasureDatumWrite() throws SQLException {
        sink.emptyDb();

        // set up test data
        String expctdDim = "testDim06";
        String expctdMetric = "testMetric06";
        float expctdVal = (float) 0.06;
        TriplePosition expctdPos = TriplePosition.PREDICATE;
        String expctdSubjUri = "http://ex.org/res06";
        String expctdPredUri = "hhtp://ex.org/pred06";
        String expctdObjLit = "literal 06";
        String expctdViewDefName = "view_def_06";
        String expctdViewDefTbl = "table06";
        String expctdQuadGraphUri = "http://ex.org/graph06";
        String expctdQuadSubjUri = "http://ex.org/resq06";
        String expctdQuadPredUri = "http://ex.org/predq06";
        String expctdQuadObjUri = "http://ex.org/resqo06";

        Node subj = NodeFactory.createURI(expctdSubjUri);
        Node pred = NodeFactory.createURI(expctdPredUri);
        Node obj = NodeFactory.createLiteral(expctdObjLit);
        Triple triple = new Triple(subj, pred, obj);

        ViewDefinition view = viewDef01(expctdViewDefName, expctdViewDefTbl);
        Node graph = NodeFactory.createURI(expctdQuadGraphUri);
        Node qSubj = NodeFactory.createURI(expctdQuadSubjUri);
        Node qPred = NodeFactory.createURI(expctdQuadPredUri);
        Node qObj = NodeFactory.createURI(expctdQuadObjUri);
        Triple qTriple = new Triple(qSubj, qPred, qObj);
        Quad quad = new Quad(graph, qTriple);
        ViewQuad<ViewDefinition> viewQuad = new ViewQuad<ViewDefinition>(view, quad);
        Set<ViewQuad<ViewDefinition>> viewQuads = new HashSet<ViewQuad<ViewDefinition>>();
        viewQuads.add(viewQuad);

        MeasureDatum measDatum = new NodeTripleMeasureDatum(expctdDim, expctdMetric, expctdVal, expctdPos, triple, viewQuads);

        // write to sink
        sink.write(measDatum);
        // this should result in the following insertions:
        // 1) measure_datum: (#meas_id#, <expctdDim>, <expctdMetric>, <expctdVal> #assmnt_id#, #timestamp#)
        // 2) node_triple: (#id#, <expctdPos>, <expctdSubjUri>, <expctdPredUri>, <"expctdObjLit">)
        // 3) measure_datum__node_triple: (#meas_id#, #ntr_id#, #assmnt_id#)
        List<String> affectedTblsNames = Arrays.asList(sink.measureDatumTbl,
                sink.nodeTripleTbl, sink.md2nodeTripleTbl);

        Long measureDatumId = null;
        Long assessmentId = null;
        Long nodeTripleId = null;

        // check if things are written correctly

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

        // 2) check node_triple table
        ResultSet ntRes = sink.conn.createStatement().executeQuery(
                "SELECT * FROM " + sink.nodeTripleTbl);

        int ntResCounter = 0;
        while (ntRes.next()) {
            ntResCounter++;

            // id
            nodeTripleId = ntRes.getLong("id");
            assertNotNull(nodeTripleId);

            // position
            String pos = ntRes.getString("position");
            assertEquals(expctdPos.toString(), pos);

            // subject
            String subjUri = ntRes.getString("subject");
            assertEquals(expctdSubjUri, subjUri);

            // predicate
            String predUri = ntRes.getString("predicate");
            assertEquals(expctdPredUri, predUri);

            // object
            String objLit = ntRes.getString("object");
            assertEquals("\"" + expctdObjLit + "\"", objLit);
        }
        assertEquals(1, ntResCounter);

        // 3) check measure_datum__node_triple table
        ResultSet md2ntRes = sink.conn.createStatement().executeQuery(
                "SELECT * FROM " + sink.md2nodeTripleTbl);

        int md2ntResCounter = 0;
        while (md2ntRes.next()) {
            md2ntResCounter++;

            // measure_datum_id
            long measDtmId = md2ntRes.getLong("measure_datum_id");
            assertEquals((long) measureDatumId, measDtmId);

            // node_triple_id
            long ntId = md2ntRes.getLong("node_triple_id");
            assertEquals((long) nodeTripleId, ntId);

            // assessment_id
            long assmntId = md2ntRes.getLong("assessment_id");
            assertEquals((long) assessmentId, assmntId);
        }
        assertEquals(1, md2ntResCounter);

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
    public void testTripleMeasureDatumWrite() throws SQLException {
        sink.emptyDb();

        // set up test data
        String expctdDim = "testDim07";
        String expctdMetric = "testMetric07";
        float expctdVal = (float) 0.07;
        String expctdSubjUri = "http://ex.org/res07";
        String expctdPredUri = "http://ex.org/pred07";
        String expctdObjUri = "http://ex.org/reso07";
        String expctdViewDefName = "view_def_07";
        String expctdViewDefTbl = "table07";
        String expctdQuadGraphUri = "http://ex.org/graph07";
        String expctdQuadSubjUri = "http://ex.org/resq07";
        String expctdQuadPredUri = "http://ex.org/predq07";
        String expctdQuadObjVarName = "objvar";

        Node subj = NodeFactory.createURI(expctdSubjUri);
        Node pred = NodeFactory.createURI(expctdPredUri);
        Node obj = NodeFactory.createURI(expctdObjUri);
        Triple triple = new Triple(subj, pred, obj);
        ViewDefinition view = viewDef01(expctdViewDefName, expctdViewDefTbl);
        Node graph = NodeFactory.createURI(expctdQuadGraphUri);
        Node qSubj = NodeFactory.createURI(expctdQuadSubjUri);
        Node qPred = NodeFactory.createURI(expctdQuadPredUri);
        Node qObj = NodeFactory.createVariable(expctdQuadObjVarName);
        Quad quad = new Quad(graph, qSubj, qPred, qObj);
        ViewQuad<ViewDefinition> viewQuad = new ViewQuad<ViewDefinition>(view, quad);
        Set<ViewQuad<ViewDefinition>> viewQuads = new HashSet<ViewQuad<ViewDefinition>>();
        viewQuads.add(viewQuad);

        MeasureDatum measDatum = new TripleMeasureDatum(expctdDim, expctdMetric, expctdVal, triple, viewQuads);

        // write to sink
        sink.write(measDatum);
        // this should result in the following insertions:
        // 1) measure_datum: (#meas_id#, <expctdDim>, <expctdMetric>, <expctdVal> #assmnt_id#, #timestamp#)
        // 2) triple: (#id#, <expctSubjUri>. <expctdPreduri>, <expctdObjUri>)
        // 3) measure_datum__triple: (#meas_id#, #trpl_id#, #assmnt_id#)
        // 4) quad: (#id#, <expctdQuadGraphUri>, <expctdQuadSubjUri>, <expctdQuadPreduri>, <expctdQuadObjVarName>)
        // 5) view_definition: (#id#, <expctdViewDefName>, <expctdViewDefTbl>, #mapping_defs#)
        // 6) measure_datum__quad: (#meas_id#, #quad_id#, #assmnt_id#)
        // 7) measure_datum__view_definition: (#meas_id#, #view_def_id#, #assmnt_id#)
        // 8) view_definition__quad: (#view_def_id#, #quad_id#, #assmnt_id#)
        List<String> affectedTblsNames = Arrays.asList(sink.measureDatumTbl,
                sink.tripleTbl, sink.md2tripleTbl, sink.quadTbl,
                sink.viewDefTbl, sink.md2quadTbl, sink.md2viewDefTbl,
                sink.vd2quadTbl);

        Long measureDatumId = null;
        Long assessmentId = null;
        Long tripleId = null;
        Long quadId = null;
        Long viewDefId = null;


        // check if things are written correctly

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

        // 2) check triple table
        ResultSet tRes = sink.conn.createStatement().executeQuery(
                "SELECT * FROM " + sink.tripleTbl);

        int tResCounter = 0;
        while (tRes.next()) {
            tResCounter++;
            // id
            tripleId = tRes.getLong("id");
            assertNotNull(tripleId);

            // subject
            String subjUri = tRes.getString("subject");
            assertEquals(expctdSubjUri, subjUri);

            // predicate
            String predUri = tRes.getString("predicate");
            assertEquals(expctdPredUri, predUri);

            // object
            String objUri = tRes.getString("object");
            assertEquals(expctdObjUri, objUri);
        }
        assertEquals(1, tResCounter);

        // 3) check measure_datum__triple table
        ResultSet md2tRes = sink.conn.createStatement().executeQuery(
                "SELECT * FROM " + sink.md2tripleTbl);

        int md2tResCounter = 0;
        while (md2tRes.next()) {
            md2tResCounter++;

            // measure_datum_id
            long mdId = md2tRes.getLong("measure_datum_id");
            assertEquals((long) measureDatumId, mdId);

            // triple_id
            long tId = md2tRes.getLong("triple_id");
            assertEquals((long) tripleId, tId);

            // assessment_id
            long assmntId = md2tRes.getLong("assessment_id");
            assertEquals((long) assessmentId, assmntId);
        }
        assertEquals(1, md2tResCounter);

        // 4) check quad table
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
            assertEquals(expctdQuadGraphUri, graphUri);

            // subject
            String subjectUri = qRes.getString("subject");
            assertEquals(expctdQuadSubjUri, subjectUri);

            // predicate
            String predicateUri = qRes.getString("predicate");
            assertEquals(expctdQuadPredUri, predicateUri);

            // object
            String objLit = qRes.getString("object");
            assertEquals("?" + expctdQuadObjVarName, objLit);
        }
        assertEquals(1, qResCounter);

        // 5) check view_definition table
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

        // 6) check measure_datum__quad table
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

        // 7) measure_datum__view_definition table
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

        // 8) view_definition__quad table
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
    public void testTriplesMeasureDatumWrite() throws SQLException {
        sink.emptyDb();

        // set up test data
        String expctdDim = "testDim08";
        String expctdMetric = "testMetric08";
        float expctdVal = (float) 0.08;
        String expctdSubjUri = "http://ex.org/res08";
        String expctdPredUri = "http://ex.org/pred08";
        String expctdObjLit = "lit 08";
        String expctdViewDefName = "view_definition_08";
        String expctdViewDefTbl = "table08";
        String expctdQuadGraphUri = "http://ex.org/graph08";
        String expctdQuadSubjUri = "http://ex.org/resq08";
        String expctdQuadPredUri = "http://ex.org/predq08";
        String expctdQuadObjUri = "http://ex,org/resqo08";

        Node subj = NodeFactory.createURI(expctdSubjUri);
        Node pred = NodeFactory.createURI(expctdPredUri);
        Node obj = NodeFactory.createLiteral(expctdObjLit);
        Triple triple = new Triple(subj, pred, obj);


        ViewDefinition view = viewDef01(expctdViewDefName, expctdViewDefTbl);
        Node graph = NodeFactory.createURI(expctdQuadGraphUri);
        Node subject = NodeFactory.createURI(expctdQuadSubjUri);
        Node predicate = NodeFactory.createURI(expctdQuadPredUri);
        Node object = NodeFactory.createURI(expctdQuadObjUri);
        Quad quad = new Quad(graph, subject, predicate, object);
        ViewQuad<ViewDefinition> viewQuad = new ViewQuad<ViewDefinition>(view, quad);

        Set<ViewQuad<ViewDefinition>> viewQuads = new HashSet<ViewQuad<ViewDefinition>>();
        viewQuads.add(viewQuad);

        Pair<Triple, Set<ViewQuad<ViewDefinition>>> tripleViewQuad =
                new Pair<Triple, Set<ViewQuad<ViewDefinition>>>(triple, viewQuads);

        List<Pair<Triple, Set<ViewQuad<ViewDefinition>>>> tripleViewQuads =
                new ArrayList<Pair<Triple, Set<ViewQuad<ViewDefinition>>>>();
        tripleViewQuads.add(tripleViewQuad);

        MeasureDatum measDatum = new TriplesMeasureDatum(expctdDim, expctdMetric, expctdVal, tripleViewQuads);

        // write to sink
        sink.write(measDatum);
        // this should result in the following insertions:
        // 1) measure_datum: (#meas_id#, <expctdDim>, <expctdMetric>, <expctdVal> #assmnt_id#, #timestamp#)
        // 2) triple: (#id#, <expctSubjUri>, <expctdPredUri>, <expctdObjUri>)
        // 3) quad: (#id#, <expctdQuadGraphUri>, <expctdQuadSubjUri>, <expctdQuadPredUri>, <expectdQuadObjUri>)
        // 4) view_definition: (#id#, <expctdViewDefName>, <expctdViewDefTbl>, #mapping_defs#)
        // 5) triple__quad: (#trpl_id#, #quad_id#, #assmnt_id#)
        // 6) triple__view_definition: (#trpl_id#, #viewdef_id#, #assmnt_id#)
        // 7) measure_datum__triple: (#meas_id#, #trpl_id#, #assmnt_id#)
        List<String> affectedTblsNames = Arrays.asList(sink.measureDatumTbl,
                sink.tripleTbl, sink.quadTbl, sink.viewDefTbl,
                sink.trpl2quadTbl, sink.trpl2viewDefTbl, sink.md2tripleTbl);

        Long measureDatumId = null;
        Long assessmentId = null;
        Long tripleId = null;
        Long quadId = null;
        Long viewDefId = null;

        // check if things are written correctly

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

        // 2) check triple table
        ResultSet tRes = sink.conn.createStatement().executeQuery(
                "SELECT * FROM " + sink.tripleTbl);

        int tResCounter = 0;
        while (tRes.next()) {
            tResCounter++;
            // id
            tripleId = tRes.getLong("id");
            assertNotNull(tripleId);

            // subject
            String subjUri = tRes.getString("subject");
            assertEquals(expctdSubjUri, subjUri);

            // predicate
            String predUri = tRes.getString("predicate");
            assertEquals(expctdPredUri, predUri);

            // object
            String objLit = tRes.getString("object");
            assertEquals("\"" + expctdObjLit + "\"", objLit);
        }
        assertEquals(1, tResCounter);

        // 3) check quad table
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
            assertEquals(expctdQuadGraphUri, graphUri);

            // subject
            String subjectUri = qRes.getString("subject");
            assertEquals(expctdQuadSubjUri, subjectUri);

            // predicate
            String predicateUri = qRes.getString("predicate");
            assertEquals(expctdQuadPredUri, predicateUri);

            // object
            String objUri = qRes.getString("object");
            assertEquals(expctdQuadObjUri, objUri);
        }
        assertEquals(1, qResCounter);

        // 4) check view_definition table
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

        // 5) check triple__quad table
        ResultSet t2qRes = sink.conn.createStatement().executeQuery(
                "SELECT * FROM " + sink.trpl2quadTbl);

        int t2qResCounter = 0;
        while (t2qRes.next()) {
            t2qResCounter++;

            // triple_id
            long tId = t2qRes.getLong("triple_id");
            assertEquals((long) tripleId, tId);

            // quad_id
            long qId = t2qRes.getLong("quad_id");
            assertEquals((long) quadId, qId);

            // assessment_id
            long assmntId = t2qRes.getLong("assessment_id");
            assertEquals((long) assessmentId, assmntId);
        }
        assertEquals(1, t2qResCounter);

        // 6) check triple__view_definition table
        ResultSet t2vdRes = sink.conn.createStatement().executeQuery(
                "SELECT * FROM " + sink.trpl2viewDefTbl);

        int t2vdResCounter = 0;
        while (t2vdRes.next()) {
            t2vdResCounter++;

            // triple_id
            long trId = t2vdRes.getLong("triple_id");
            assertEquals((long) tripleId, trId);

            // view_definition_id
            long vdId = t2vdRes.getLong("view_definition_id");
            assertEquals((long) viewDefId, vdId);

            // assessment_id
            long assmntId = t2vdRes.getLong("assessment_id");
            assertEquals((long) assessmentId, assmntId);
        }
        assertEquals(1, t2vdResCounter);

        // 7) check measure_datum__triple table
        ResultSet md2tRes = sink.conn.createStatement().executeQuery(
                "SELECT * FROM " + sink.md2tripleTbl);

        int md2tResCounter = 0;
        while (md2tRes.next()) {
            md2tResCounter++;

            // measure_datum_id
            long mdId = md2tRes.getLong("measure_datum_id");
            assertEquals((long) measureDatumId, mdId);

            // triple_id
            long tId = md2tRes.getLong("triple_id");
            assertEquals((long) tripleId, tId);

            // assessment_id
            long assmntId = md2tRes.getLong("assessment_id");
            assertEquals((long) assessmentId, assmntId);
        }
        assertEquals(1, md2tResCounter);

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
}
