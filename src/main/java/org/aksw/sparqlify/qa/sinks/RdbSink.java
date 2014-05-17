package org.aksw.sparqlify.qa.sinks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.aksw.commons.collections.Pair;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * TODO: check if the prepared statements make sense performance-wise
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
@Component
public class RdbSink implements MeasureDataSink {

    @Autowired
    @Qualifier("resDb")
    private DataSource sinkDb;
    protected Connection conn;
    private long nextId;
    private long assessmentId;
    // db table names
    protected List<String> tableNames;
    protected final String measureDatumTbl = "measure_datum";
    
    private final String nodeTripleTbl = "node_triple";
    private final String nodeTbl = "node";
    protected final String quadTbl = "quad";
    private final String tripleTbl = "triple";
    private final String varTbl = "variable";
    protected final String viewDefTbl = "view_definition";
    
    private final String md2nodeTbl = "measure_datum__node";
    private final String md2nodeTripleTbl = "measure_datum__node_triple";
    protected final String md2quadTbl = "measure_datum__quad";
    private final String md2tripleTbl = "measure_datum__triple";
    private final String md2varTbl  = "measure_datum__variable";
    protected final String md2viewDefTbl = "measure_datum__view_definition";
    private final String trpl2quadTbl = "triple__quad";
    private final String trpl2viewDefTbl = "triple__view_definition";
    protected final String vd2quadTbl = "view_definition__quad";
    private final String vd2varTbl = "view_definition__variable";
    
    @PostConstruct
    private void init() throws SQLException {
        conn = sinkDb.getConnection();
        initDb();
        assessmentId = RandomUtils.nextLong();
        tableNames = Arrays.asList(measureDatumTbl, nodeTripleTbl,
                nodeTbl, quadTbl, tripleTbl, varTbl, viewDefTbl, md2nodeTbl,
                md2nodeTripleTbl, md2quadTbl, md2tripleTbl, md2varTbl,
                md2viewDefTbl, trpl2quadTbl, trpl2viewDefTbl, vd2quadTbl,
                vd2varTbl);
    }
    @PreDestroy
    private void cleanUp() throws SQLException {
        conn.close();
    }
    
    // for testing purposes
    protected void emptyDb() throws SQLException {
        for (String tblName : tableNames) {
            conn.createStatement().executeUpdate(
                    "DELETE FROM " + tblName + ";");
        }
    }
    
    
    private void initDb() throws SQLException {
        // get id counter
        conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS next_id (id bigint);");
        ResultSet res = conn.createStatement().executeQuery("SELECT id FROM next_id LIMIT 1");
        if (res.next()) {
            nextId = res.getLong("id");
        } else {
            conn.createStatement().executeUpdate("INSERT INTO next_id VALUES (0);");
            nextId = 0;
        }
        
        /*
         * -------------------------- measure data --------------------------
         */
        // generic measure datum table
        conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + measureDatumTbl + " (" +
                    "id bigint PRIMARY KEY, " +
                    "dimension varchar(400), " +
                    "metric varchar(400), " +
                    "value real NOT NULL, " +
                    "assessment_id bigint NOT NULL, " +
                    "timestamp timestamp default current_timestamp " +
                ");");
        
        /*
         * -------------------- measure datum artifacts --------------------
         *              (view definitions, quads, variables, ...)
         */
        
        // node table
        conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + nodeTbl + " (" +
                    "id bigint PRIMARY KEY, " +
                    "name varchar(300) NOT NULL" +
                ");");
        
        // node triple table
        conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + nodeTripleTbl + " (" +
                    "id bigint PRIMARY KEY, " +
                    "position varchar(20), " +
                    "subject varchar(300) , " +
                    "predicate varchar(300), " +
                    "object varchar(3000) " +
                ");");
        
        // quad table
        conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + quadTbl + " (" +
                    "id bigint PRIMARY KEY, " +
                    "graph varchar(300), " +
                    "subject varchar(300), " +
                    "predicate varchar(300), " +
                    "object varchar(3000)" +
                ");");
        
        // triple table
        conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + tripleTbl + " (" +
                    "id bigint PRIMARY KEY, " +
                    "subject varchar(500), " +
                    "predicate varchar(500), " +
                    "object varchar(3000) " +
                ");");
        
        // variable table
        conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + varTbl + " (" +
                    "id bigint PRIMARY KEY, " +
                    "name varchar(50) " +
                ");");
        
        // view definition table
        conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + viewDefTbl + " (" +
                    "id bigint PRIMARY KEY, " +
                    "name varchar(200), " +
                    "mapping_sql_op varchar(3000), " +
                    "mapping_definitions varchar(3000) " +
                ");");
        
        /*
         * --------------------------- n:m tables ---------------------------
         */
        
        // measure datum  --  node
        conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + md2nodeTbl + " (" +
                    "measure_datum_id bigint REFERENCES " + measureDatumTbl + "(id), " +
                    "node_id bigint REFERENCES " + nodeTbl + "(id), " +
                    "assessment_id bigint, " +
                    "PRIMARY KEY(measure_datum_id, node_id, assessment_id)" +
                ");");
        
        // measure datum  --  node triple
        conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + md2nodeTripleTbl + " (" +
                    "measure_datum_id bigint REFERENCES " + measureDatumTbl + "(id), " +
                    "node_triple_id bigint REFERENCES " + nodeTripleTbl + "(id), " +
                    "assessment_id bigint, " +
                    "PRIMARY KEY (measure_datum_id, node_triple_id, assessment_id)" +
                ");");
        
        // measure datum  --  quad
        conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + md2quadTbl + " (" +
                    "measure_datum_id bigint REFERENCES " + measureDatumTbl + "(id), " +
                    "quad_id bigint REFERENCES " + quadTbl + "(id), " +
                    "assessment_id bigint, " +
                    "PRIMARY KEY(measure_datum_id, quad_id, assessment_id)" +
                ");");
        
        // measure datum  --  triple
        conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + md2tripleTbl + " (" +
                    "measure_datum_id bigint REFERENCES " + measureDatumTbl + "(id), " +
                    "triple_id bigint REFERENCES " + tripleTbl + "(id), " +
                    "assessment_id bigint, " +
                    "PRIMARY KEY (measure_datum_id, triple_id, assessment_id)" +
                ");");
        
        // measure datum  --  variable
        conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + md2varTbl + " (" +
                    "measure_datum_id bigint REFERENCES " + measureDatumTbl + "(id), " +
                    "variable_id bigint REFERENCES " + varTbl + "(id), " +
                    "assessment_id bigint, " +
                    "PRIMARY KEY (measure_datum_id, variable_id, assessment_id)" +
                ");");
        
        // measure datum  --  view definition 
        conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + md2viewDefTbl + " (" +
                    "measure_datum_id bigint REFERENCES " + measureDatumTbl + "(id), " +
                    "view_definition_id bigint REFERENCES " + viewDefTbl + "(id), " +
                    "assessment_id bigint, " +
                    "PRIMARY KEY(measure_datum_id, view_definition_id, assessment_id)" +
                ");");
        
        // triple  --  quad
        conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + trpl2quadTbl + " (" +
                    "triple_id bigint REFERENCES " + tripleTbl + "(id), " +
                    "quad_id bigint REFERENCES " + quadTbl + "(id), " +
                    "assessment_id bigint, " +
                    "PRIMARY KEY (triple_id, quad_id, assessment_id)" +
                ");");
        
        // triple  --  view definition
        conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + trpl2viewDefTbl + " (" +
                    "triple_id bigint REFERENCES " + tripleTbl + "(id), " +
                    "view_definition_id bigint REFERENCES " + viewDefTbl + "(id), " +
                    "assessment_id bigint, " +
                    "PRIMARY KEY (triple_id, view_definition_id, assessment_id)" +
                ");");
        
        // view definition  --  quad
        conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + vd2quadTbl + " (" +
                    "view_definition_id bigint REFERENCES view_definition(id), " +
                    "quad_id bigint REFERENCES quad(id), " +
                    "assessment_id bigint, " +
                    "PRIMARY KEY(view_definition_id, quad_id, assessment_id)" +
                ");");
        
        // view definition  --  variable
        conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + vd2varTbl + " (" +
                    "view_definition_id bigint REFERENCES " + viewDefTbl + "(id), " +
                    "variable_id bigint REFERENCES " + varTbl + "(id), " +
                    "assessment_id bigint, " +
                    "PRIMARY KEY (variable_id, view_definition_id, assessment_id)" +
                ");");
        
        conn.commit();
    }
    
    @Override
    public void initMeasure(String name, Class<? extends MetricImpl> class1,
            String parentDimension) throws NotImplementedException {
        // nothing to do here..
    }

    @Override
    public void write(MeasureDatum datum) throws SQLException {
        if (datum instanceof DatasetMeasureDatum) {
            /* a DatasetMeasureDatum has the structure of a generic measure
             * datum, i.e. dimension name, metric name and value
             */
            String dimensionName = datum.getDimension();
            String metricName = datum.getMetric();
            float value = datum.getValue();
            long measureDatumId = getNextId();
            writeMeasureDatum(measureDatumId, dimensionName, metricName, value);
            
        } else if (datum instanceof MappingMeasureDatum) {
            /* a MappingMeasureDatum has the generic structure + a set of view
             * quad candidates being a Set<ViewQuad<ViewDefinition>>
             */
            writeMappingMeasureDatum((MappingMeasureDatum) datum);
            
        } else if (datum instanceof MappingQuadMeasureDatum) {
            /* a MappingQuadMeasureDatum has the generic structure + a list of
             * quad - view definition pair
             * List<Pair<Quad, ViewDefinition>> quadViewDefs
             */
            writeMappingQuadMeasureDatum((MappingQuadMeasureDatum) datum);
            
        } else if (datum instanceof MappingVarMeasureDatum) {
            /* a MappingVarMeasureDatum has the generic structure + a list of
             * variables List<Pair<Node_Variable, ViewDefinition>>
             */
            writeMappingVarMeasureDatum((MappingVarMeasureDatum) datum);
            
        } else if (datum instanceof NodeMeasureDatum) {
            /* a NodeMeasureDatum has the generic structure + a node (Node) */
            writeNodeMeasureDatum((NodeMeasureDatum) datum);
            
        } else if (datum instanceof NodeTripleMeasureDatum) {
            /* a NodeTripleMeasureDatum has the generic structure + a triple
             * position (TriplePosition), a triple (Triple) and a set of view
             * quads (Set<ViewQuad<ViewDefinition>>)
             */
            writeNodeTripleMeasureDatum((NodeTripleMeasureDatum) datum);
            
        } else if(datum instanceof TripleMeasureDatum) {
            /* a TripleMeasureDatum has the generic structure + a triple
             * (Triple) and a set of view quads (Set<ViewQuad<ViewDefinition>>)
             */
            writeTripleMeasureDatum((TripleMeasureDatum) datum);
            
        } else if (datum instanceof TriplesMeasureDatum) {
            /* a TriplesMeasureDatum has the generic structure + a list of
             * triple-view quad pairs
             * (List<Pair<Triple, Set<ViewQuad<ViewDefinition>>>>)
             */
            writeTriplesMeasureDatum((TriplesMeasureDatum) datum);
        }
    }
    
    // ------------------- measure datum write methods -------------------
    
    /**
     * Method to write the generic part of a measure datum to the database
     */
    private void writeMeasureDatum(long measureDatumId, String dimension,
            String metric, float value) throws SQLException {
        
        PreparedStatement insStmnt = conn.prepareStatement(
                "INSERT INTO " + measureDatumTbl + " VALUES (?, ?, ?, ?, ?, ?);");
        insStmnt.setLong(1, measureDatumId);
        insStmnt.setString(2, dimension);
        insStmnt.setString(3, metric);
        insStmnt.setFloat(4, value);
        insStmnt.setLong(5, assessmentId);
        insStmnt.setNull(6, Types.TIMESTAMP);
        insStmnt.executeUpdate();
        conn.commit();
    }
    
    
    /**
     * @param datum: a MappingMeasureDatum comprising the following attributes:
     * - String dimension
     * - String metric
     * - float value
     * - Set<ViewQuad<ViewDefinition>> candidates
     * @throws SQLException 
     */
    private void writeMappingMeasureDatum(MappingMeasureDatum datum) throws SQLException {
        
        // write generic part
        long measureDatumId = getNextId();
        String dimensionName = datum.getDimension();
        String metricName = datum.getMetric();
        float value = datum.getValue();
        writeMeasureDatum(measureDatumId, dimensionName, metricName, value);
        
        // write candidates
        if (datum.getViewDefs() != null) {
            for (ViewQuad<ViewDefinition> viewQuad : datum.getViewDefs()) {
                Quad quad = viewQuad.getQuad();
                ViewDefinition viewDef = (ViewDefinition) viewQuad.getView();
                long quadId = writeQuad(quad);
                long viewDefId = writeViewDef(viewDef);
                writeMeasureDatum2quad(measureDatumId, quadId);
                writeMeasureDatum2viewDef(measureDatumId, viewDefId);
                writeViewDef2quad(viewDefId, quadId);
            }
        }
    }
    
    
    /**
     * @param datum: a MappingQuadMeasureDatum comprising the following
     * attributes:
     * - String dimension
     * - String metric
     * - float value
     * - ArrayList<Pair<Quad, ViewDefinition>> quadViewDefs
     * @throws SQLException 
     */
    private void writeMappingQuadMeasureDatum(MappingQuadMeasureDatum datum)
            throws SQLException {
        
        // write generic metric part
        String dimensionName = datum.getDimension();
        String metricName = datum.getMetric();
        float value = datum.getValue();
        long measureDatumId = getNextId();
        writeMeasureDatum(measureDatumId, dimensionName, metricName, value);
        
        // write viewQuad part
        for (Pair<Quad, ViewDefinition> viewQuad : datum.getQuadViewDefs()) {
            
            ViewDefinition viewDef = viewQuad.second;
            long viewDefId = writeViewDef(viewDef);
            // write n:m entries
            writeMeasureDatum2viewDef(measureDatumId, viewDefId);
            
            // quad may be null in case of the property completeness metric
            Quad quad = viewQuad.first;
            if (quad != null) {
                long quadId = writeQuad(quad);
                // write n:m entries
                writeMeasureDatum2quad(measureDatumId, quadId);
                writeViewDef2quad(viewDefId, quadId);
            }
        }
    }
    
    
    /**
     * @param datum: a MappingVarMeasureDatum comprising the following
     * attributes:
     * - String dimension
     * - String metric
     * - float value
     * - ArrayList<Pair<Var, ViewDefinition>> nodeViewDefs
     * @throws SQLException
     */
    private void writeMappingVarMeasureDatum(MappingVarMeasureDatum datum) throws SQLException {
        
        // write generic measure datum part
        long measureDatumId = getNextId();
        String dimensionName = datum.getDimension();
        String metricName = datum.getMetric();
        float value = datum.getValue();
        writeMeasureDatum(measureDatumId, dimensionName, metricName, value);

        // write node-view defintion part
        for (Pair<Node_Variable, ViewDefinition> varViewDef : datum.getNodeViewDefs()) {
            // write var
            Node_Variable var = varViewDef.first;
            long varId = writeVariable(var);
            
            // write view definition
            ViewDefinition viewDef = varViewDef.second;
            long viewDefId = writeViewDef(viewDef);
            
            // write measure datum view definition m:n
            writeMeasureDatum2viewDef(measureDatumId, viewDefId);
            
            // write measure datum variable n:m
            writeMeasureDatum2var(measureDatumId, varId);
            
            // write view definition variable n:m
            writeViewDef2var(viewDefId, varId);
        }
    }
    
    
    /**
     * @param datum: a NodeMeasureDatum comprises the following attributes:
     * - String dimension
     * - String metric
     * - float value
     * - Node node
     * @throws SQLException 
     */
    private void writeNodeMeasureDatum(NodeMeasureDatum datum) throws SQLException {
        String dimensionName = datum.getDimension();
        String metricName = datum.getMetric();
        float value = datum.getValue();
        long measureDatumId = getNextId();
        writeMeasureDatum(measureDatumId, dimensionName, metricName, value);

        long nodeId = writeNode(datum.getNode());

        // write measure datum node n:m
        writeMeasureDatum2node(measureDatumId, nodeId);
    }
    
    
    /**
     * @param datum: a NodeTripleMeasureDatum comprises the following attributes:
     * - String dimension
     * - String metric
     * - float value
     * - TriplePosition pos
     * - Triple triple
     * - Set<ViewQuad<ViewDefinition>> viewQuads
     * @throws SQLException 
     */
    private void writeNodeTripleMeasureDatum(NodeTripleMeasureDatum datum)
            throws SQLException {
        
        // write generic measure datum part
        String dimensionName = datum.getDimension();
        String metricName = datum.getMetric();
        float value = datum.getValue();
        long measureDatumId = getNextId();
        writeMeasureDatum(measureDatumId, dimensionName, metricName, value);
        
        long nodeTripleId = writeNodeTriple(datum);
        
        // write measure datum node triple n:m
        writeMeasureDatum2nodeTriple(measureDatumId, nodeTripleId);
    }
    
    
    /**
     * @param datum: a TripleMeasureDatum comprises the following attributes:
     * - String dimension
     * - String metric
     * - float value
     * - Triple triple
     * - Set<ViewQuad<ViewDefinition>> viewQuads
     * @throws SQLException 
     */
    private void writeTripleMeasureDatum(TripleMeasureDatum datum)
            throws SQLException {
        
        // write generic measure datum part
        String dimensionName = datum.getDimension();
        String metricName = datum.getMetric();
        float value = datum.getValue();
        long measureDatumId = getNextId();
        writeMeasureDatum(measureDatumId, dimensionName, metricName, value);
        
        Triple triple = datum.getTriple();
        long tripleId = writeTriple(triple);
        writeMeasureDatum2triple(measureDatumId, tripleId);
        
        
        if (datum.getViewQuads() != null) {
            for (ViewQuad<ViewDefinition> viewQuad : datum.getViewQuads()) {
                // write quad
                Quad quad = viewQuad.getQuad();
                long quadId = writeQuad(quad);
                
                // write view definition
                ViewDefinition viewDef = (ViewDefinition) viewQuad.getView();
                long viewDefId = writeViewDef(viewDef);
                
                // write measure datum quad n:m
                writeMeasureDatum2quad(measureDatumId, quadId);
                
                // write measure datum view definition n:m
                writeMeasureDatum2viewDef(measureDatumId, viewDefId);
                
                // write view definitoin quad n_m
                writeViewDef2quad(viewDefId, quadId);
            }
        }
    }
    
    
    /**
     * @param datum: a TriplesMeasureDatum comprises the following attributes:
     * - String dimension
     * - String metric
     * - float value
     * - List<Pair<Triple, Set<ViewQuad<ViewDefinition>>>> tripleViewQuads
     * @throws SQLException 
     */
    private void writeTriplesMeasureDatum(TriplesMeasureDatum datum)
            throws SQLException {
        
        // write generic measure datum part
        String dimensionName = datum.getDimension();
        String metricName = datum.getMetric();
        float value = datum.getValue();
        long measureDatumId = getNextId();
        writeMeasureDatum(measureDatumId, dimensionName, metricName, value);
        
        for (Pair<Triple, Set<ViewQuad<ViewDefinition>>> tripleViewQuads : datum.getPinpoinInfos()) {
            
            // write triple
            Triple triple = tripleViewQuads.first;
            long tripleId = writeTriple(triple);
            
            Set<ViewQuad<ViewDefinition>> viewQuads = tripleViewQuads.second;
            for (ViewQuad<ViewDefinition> viewQuad : viewQuads) {
                // write quad
                Quad quad = viewQuad.getQuad();
                long quadId = writeQuad(quad);
                
                // write view
                ViewDefinition viewDef = viewQuad.getView();
                long viewDefId = writeViewDef(viewDef);
                
                // write triple quad n:m
                writeTriple2quad(tripleId, quadId);
                
                // write triple view n:m
                writeTriple2viewDef(tripleId, viewDefId);
            }
            
            // write measure datum triple n:m
            writeMeasureDatum2triple(measureDatumId, tripleId);
        }
    }
    
    
    // --------------- measure datum artifact write methods ---------------
    
    private long writeQuad(Quad quad)
            throws SQLException {
        
        long quadId;
        
        String quadGraph = quad.getGraph().getURI();

        Node subject = quad.getSubject();
        String quadSubject = getNodeStrRepr(subject);
        
        Node predicate = quad.getPredicate();
        String quadPredicate = getNodeStrRepr(predicate);
        
        // get object string representation
        Node object = quad.getObject();
        String quadObject = getNodeStrRepr(object);
        
        
        // check if already exists
        PreparedStatement qpQuery = conn.prepareStatement(
                "SELECT id FROM " + quadTbl + " " +
                "WHERE graph=? AND subject=? AND predicate=? AND object=?;");
        qpQuery.setString(1, quadGraph);
        qpQuery.setString(2, quadSubject);
        qpQuery.setString(3, quadPredicate);
        qpQuery.setString(4, quadObject);
        ResultSet qpRes = qpQuery.executeQuery();
        
        if (!qpRes.next()) {
            // quad does not exist in the database, yet
            quadId = getNextId();
            PreparedStatement qpStmnt = conn.prepareStatement(
                    "INSERT INTO " + quadTbl + " VALUES (?, ?, ?, ?, ?);");
            qpStmnt.setLong(1, quadId);
            qpStmnt.setString(2, quadGraph);
            qpStmnt.setString(3, quadSubject);
            qpStmnt.setString(4, quadPredicate);
            qpStmnt.setString(5, quadObject);
            qpStmnt.executeUpdate();
            
        } else {
            // quad does exist
            quadId = qpRes.getLong("id");
        }
        
        return quadId;
    }
    
    
    private long writeTriple(Triple triple) throws SQLException {
        
        Node subject = triple.getSubject();
        String subjectStr = getNodeStrRepr(subject);

        Node predicate = triple.getPredicate();
        String predicateStr = getNodeStrRepr(predicate);
        
        Node object = triple.getObject();
        String objectStr = getNodeStrRepr(object);
        
        long tripleId;
        
        // check if already exists
        PreparedStatement tripleQStmnt = conn.prepareStatement(
                "SELECT id FROM " + tripleTbl + " " +
                "WHERE subject=? AND predicate=? AND object=?;");
        tripleQStmnt.setString(1, subjectStr);
        tripleQStmnt.setString(2, predicateStr);
        tripleQStmnt.setString(3, objectStr);
        ResultSet res = tripleQStmnt.executeQuery();
        
        if (!res.next()) {
            tripleId = getNextId();
            // triple does not exist in the database, yet
            PreparedStatement tripleIStmnt = conn.prepareStatement(
                    "INSERT INTO " + tripleTbl + " VALUES(?, ?, ?, ?);");
            tripleIStmnt.setLong(1, tripleId);
            tripleIStmnt.setString(2, subjectStr);
            tripleIStmnt.setString(3, predicateStr);
            tripleIStmnt.setString(4, objectStr);
            tripleIStmnt.executeUpdate();
            conn.commit();
            
        } else {
            // triple alreasy exists in the database
            tripleId = res.getLong("id");
        }
        
        return tripleId;
    }
    
    
    private long writeViewDef(ViewDefinition viewDef) throws SQLException {
        
        String viewDefName = viewDef.getName();
        long viewDefId;
        
        // check if exists
        PreparedStatement vdQStmnt = conn.prepareStatement(
                "SELECT id FROM " + viewDefTbl + " WHERE name=?;");
        vdQStmnt.setString(1, viewDefName);
        ResultSet res = vdQStmnt.executeQuery();
                
        if (!res.next()) {
            // view does not exist in DB
            viewDefId = getNextId();
            String viewDefMappSqlOp = null;
            SqlOp sqlOp = viewDef.getMapping().getSqlOp();
            if (sqlOp instanceof SqlOpQuery) {
                viewDefMappSqlOp = ((SqlOpQuery) sqlOp).getQueryString();
            } else if (sqlOp instanceof SqlOpTable) {
                viewDefMappSqlOp = ((SqlOpTable) sqlOp).getTableName();
            }
            String viewDefMappDefs = viewDef.getMapping().getVarDefinition()
                    .toPrettyString();
            
            PreparedStatement vdIStmnt = conn.prepareStatement(
                    "INSERT INTO " + viewDefTbl + " VALUES (?, ?, ?, ?);");
            vdIStmnt.setLong(1, viewDefId);
            vdIStmnt.setString(2, viewDefName);
            vdIStmnt.setString(3, viewDefMappSqlOp);
            vdIStmnt.setString(4, viewDefMappDefs);
            vdIStmnt.executeUpdate();
            conn.commit();
                    
        } else {
            // view definition already exists in DB
            viewDefId = res.getLong("id");
        }
        
        return viewDefId;
    }
    
    
    private long writeVariable(Node_Variable var) throws SQLException {
        
        String varName = var.toString();
        long varId;
        
        // check if already exists
        PreparedStatement varQStmnt = conn.prepareStatement(
                "SELECT id FROM " + varTbl + " WHERE name=?;");
        varQStmnt.setString(1, varName);
        ResultSet res = varQStmnt.executeQuery();
        
        if (!res.next()) {
            // variable does not exist in the database, yet
            varId = getNextId();
            
            PreparedStatement varIStmnt = conn.prepareStatement(
                    "INSERT INTO " + varTbl + " VALUES (?, ?);");
            varIStmnt.setLong(1, varId);
            varIStmnt.setString(2, varName);
            varIStmnt.executeUpdate();
            conn.commit();
            
        } else {
            // variable already exists
            varId = res.getLong("id");
        }
        
        return varId;
    }
    
    
    private long writeNode(Node node) throws SQLException {
        
        String nodeName = getNodeStrRepr(node);
        long nodeId;
        
        // check if already exists
        PreparedStatement nodeQStmnt = conn.prepareStatement(
                "SELECT id FROM " + nodeTbl + " WHERE name=?;");
        nodeQStmnt.setString(1, nodeName);
        ResultSet res = nodeQStmnt.executeQuery();
        
        if (!res.next()) {
            // node does not exist in the database, yet
            nodeId = getNextId();
            
            PreparedStatement nodeIStmnt = conn.prepareStatement(
                    "INSERT INTO " + nodeTbl + " VALUES (?, ?);");
            nodeIStmnt.setLong(1, nodeId);
            nodeIStmnt.setString(2, nodeName);
            nodeIStmnt.executeUpdate();
            conn.commit();
        } else {
            // node already exists in the database
            nodeId = res.getLong("id");
        }
        
        return nodeId;
    }
    
    
    private long writeNodeTriple(NodeTripleMeasureDatum datum) throws SQLException {

        String nodeTriplePosition = datum.getTriplePosition().name();
        
        Node subject = datum.getTriple().getSubject();
        String nodeTripleSubject = getNodeStrRepr(subject);
        
        Node predicate = datum.getTriple().getPredicate();
        String nodeTriplePredicate = getNodeStrRepr(predicate);
        
        Node object = datum.getTriple().getObject();
        String nodeTripleObject = getNodeStrRepr(object);
        
        long nodeTripleId;
        
        // check if already exists
        PreparedStatement ntQStmnt = conn.prepareStatement(
                "SELECT id FROM " + nodeTripleTbl + " " +
                "WHERE position=? AND subject=? AND predicate=? AND object=?;");
        ntQStmnt.setString(1, nodeTriplePosition);
        ntQStmnt.setString(2, nodeTripleSubject);
        ntQStmnt.setString(3, nodeTriplePredicate);
        ntQStmnt.setString(4, nodeTripleObject);
        ResultSet res = ntQStmnt.executeQuery();
        
        if (!res.next()) {
            // node triple does not exist in the database, yet
            nodeTripleId = getNextId();
            
            // schema:
            // id bigint, position varchar(20), subject varchar(300),
            // predicate varchar(300), object varchar(300)
            PreparedStatement ntIStmnt = conn.prepareStatement(
                    "INSERT INTO " + nodeTripleTbl + " VALUES (?, ? , ?, ?, ?)");
            ntIStmnt.setLong(1, nodeTripleId);
            ntIStmnt.setString(2, nodeTriplePosition);
            ntIStmnt.setString(3, nodeTripleSubject);
            ntIStmnt.setString(4, nodeTriplePredicate);
            ntIStmnt.setString(5, nodeTripleObject);
            ntIStmnt.executeUpdate();
            conn.commit();
        } else {
            nodeTripleId = res.getLong("id");
        }
        
        return nodeTripleId;
    }
    
    
    
    // --------------------------- n:m methods ---------------------------
    
    private void writeMeasureDatum2node(long measureDatumId, long nodeId) throws SQLException {

        // check if entry already exists
        PreparedStatement md2nodeQStmnt = conn.prepareStatement(
                "SELECT measure_datum_id FROM " + md2nodeTbl + " " +
                "WHERE measure_datum_id=? AND node_id=? AND assessment_id=?;");
        md2nodeQStmnt.setLong(1, measureDatumId);
        md2nodeQStmnt.setLong(2, nodeId);
        md2nodeQStmnt.setLong(3, assessmentId);
        ResultSet res = md2nodeQStmnt.executeQuery();
        
        if (!res.next()) {
            // extry does not exist in the database, yet
            PreparedStatement md2nodeIStmnt = conn.prepareStatement(
                    "INSERT INTO " + md2nodeTbl + " VALUES(?, ?, ?);");
            md2nodeIStmnt.setLong(1, measureDatumId);
            md2nodeIStmnt.setLong(2, nodeId);
            md2nodeIStmnt.setLong(3, assessmentId);
            md2nodeIStmnt.executeUpdate();
            conn.commit();
            
        }
    }
    
    
    private void writeMeasureDatum2nodeTriple(long measureDatumId,
            long nodeTripleId) throws SQLException {
        
        // check if already exists
        PreparedStatement md2ntQStmnt = conn.prepareStatement(
                "SELECT measure_datum_id FROM " + md2nodeTripleTbl + " " +
                "WHERE measure_datum_id=? AND node_triple_id=? " +
                    "AND assessment_id=?");
        md2ntQStmnt.setLong(1, measureDatumId);
        md2ntQStmnt.setLong(2, nodeTripleId);
        md2ntQStmnt.setLong(3, assessmentId);
        ResultSet res = md2ntQStmnt.executeQuery();
        
        if (!res.next()) {
            // entry does not exist in the database, yet
            PreparedStatement md2ntIStmnt = conn.prepareStatement(
                    "INSERT INTO " + md2nodeTripleTbl + " VALUES (?, ?, ?);");
            md2ntIStmnt.setLong(1, measureDatumId);
            md2ntIStmnt.setLong(2, nodeTripleId);
            md2ntIStmnt.setLong(3, assessmentId);
            md2ntIStmnt.executeUpdate();
            conn.commit();
        }
    }
    
    
    private void writeMeasureDatum2quad(long measureDatumId,
            long quadId) throws SQLException {
        
        // check if entry already exists
        PreparedStatement md2qpQStmnt = conn.prepareStatement(
                "SELECT measure_datum_id FROM " + md2quadTbl + " " +
                "WHERE measure_datum_id=? AND quad_id=? AND assessment_id=?;");
        md2qpQStmnt.setLong(1, measureDatumId);
        md2qpQStmnt.setLong(2, quadId);
        md2qpQStmnt.setLong(3, assessmentId);
        ResultSet res = md2qpQStmnt.executeQuery();
        
        if (!res.next()) {
            // entry does not exist in the database, yet
            PreparedStatement md2qpIStmnt = conn.prepareStatement(
                    "INSERT INTO " + md2quadTbl + " VALUES (?, ?, ?);");
            md2qpIStmnt.setLong(1, measureDatumId);
            md2qpIStmnt.setLong(2, quadId);
            md2qpIStmnt.setLong(3, assessmentId);
            md2qpIStmnt.executeUpdate();
            conn.commit();
        }
    }
    
    
    private void writeMeasureDatum2triple(long measureDatumid, long tripleid)
            throws SQLException {
        
        // check if entry already exists
        PreparedStatement md2trplQStmnt = conn.prepareStatement(
                "SELECT measure_datum_id FROM " + md2tripleTbl + " " +
                "WHERE measure_datum_id=? AND triple_id=? AND assessment_id=?;");
        md2trplQStmnt.setLong(1, measureDatumid);
        md2trplQStmnt.setLong(2, tripleid);
        md2trplQStmnt.setLong(3, assessmentId);
        ResultSet res = md2trplQStmnt.executeQuery();
        
        if (!res.next()) {
            // entry does not exist in the database, yet
            PreparedStatement md2trplIStmnt = conn.prepareStatement(
                    "INSERT INTO " + md2tripleTbl + " VALUES (?, ?, ?);");
            md2trplIStmnt.setLong(1, measureDatumid);
            md2trplIStmnt.setLong(2, tripleid);
            md2trplIStmnt.setLong(3, assessmentId);
            md2trplIStmnt.executeUpdate();
            conn.commit();
        }
    }
    
    
    private void writeMeasureDatum2var(long measureDatumId, long varId)
            throws SQLException {
        
        // check if entry already exits
        PreparedStatement md2varQStmnt = conn.prepareStatement(
                "SELECT measure_datum_id FROM " + md2varTbl + " " +
                "WHERE measure_datum_id=? AND variable_id=? AND assessment_id=?;");
        md2varQStmnt.setLong(1, measureDatumId);
        md2varQStmnt.setLong(2, varId);
        md2varQStmnt.setLong(3, assessmentId);
        ResultSet res = md2varQStmnt.executeQuery();
        
        if (!res.next()) {
            // entry does not exist in the database, yet
            PreparedStatement md2varIStmnt = conn.prepareStatement(
                    "INSERT INTO " + md2varTbl + " VALUES (?, ?, ?);");
            md2varIStmnt.setLong(1, measureDatumId);
            md2varIStmnt.setLong(2, varId);
            md2varIStmnt.setLong(3, assessmentId);
            md2varIStmnt.executeUpdate();
            conn.commit();
        }
    }
    
    
    private void writeMeasureDatum2viewDef(long measureDatumId, long viewDefId)
            throws SQLException {
        
        // check if entry already exists
        PreparedStatement md2vdQStmnt = conn.prepareStatement(
                "SELECT measure_datum_id FROM " + md2viewDefTbl + " " +
                "WHERE measure_datum_id=? AND view_definition_id=? " +
                    "AND assessment_id=?;");
        md2vdQStmnt.setLong(1, measureDatumId);
        md2vdQStmnt.setLong(2, viewDefId);
        md2vdQStmnt.setLong(3, assessmentId);
        ResultSet res = md2vdQStmnt.executeQuery();
        
        if (!res.next()) {
            // entry does not exist in the database, yet
            PreparedStatement md2vdIStmnt = conn.prepareStatement(
                    "INSERT INTO " + md2viewDefTbl + " VALUES (?, ?, ?);");
            md2vdIStmnt.setLong(1, measureDatumId);
            md2vdIStmnt.setLong(2, viewDefId);
            md2vdIStmnt.setLong(3, assessmentId);
            md2vdIStmnt.executeUpdate();
            conn.commit();
        }
    }
    
    
    private void writeTriple2quad(long tripleId, long quadId) throws SQLException {
        
        // check if entry already exists
        PreparedStatement trpl2qudQStmnt = conn.prepareStatement(
                "SELECT triple_id FROM " + trpl2quadTbl + " " +
                "WHERE triple_id=? AND quad_id=? AND assessment_id=?;");
        trpl2qudQStmnt.setLong(1, tripleId);
        trpl2qudQStmnt.setLong(2, quadId);
        trpl2qudQStmnt.setLong(3, assessmentId);
        ResultSet res = trpl2qudQStmnt.executeQuery();
        
        if (!res.next()) {
            // entry does not exist in the database, yet
            PreparedStatement trpl2qudIStmnt = conn.prepareStatement(
                    "INSERT INTO " + trpl2quadTbl + " VALUES (?, ?, ?);");
            trpl2qudIStmnt.setLong(1, tripleId);
            trpl2qudIStmnt.setLong(2, quadId);
            trpl2qudIStmnt.setLong(3, assessmentId);
            trpl2qudIStmnt.executeUpdate();
            conn.commit();
        }
    }
    
    
    private void writeTriple2viewDef(long tripleId, long viewDefId) throws SQLException {
        // check if entry exists
        PreparedStatement trpl2vdQStmnt = conn.prepareStatement(
                "SELECT triple_id FROM " + trpl2viewDefTbl + " " +
                "WHERE triple_id=? AND view_definition_id=? AND assessment_id=?;");
        trpl2vdQStmnt.setLong(1, tripleId);
        trpl2vdQStmnt.setLong(2, viewDefId);
        trpl2vdQStmnt.setLong(3, assessmentId);
        ResultSet res = trpl2vdQStmnt.executeQuery();
        
        if (!res.next()) {
            // entry does not exist in the database, yet
            PreparedStatement trpl2vdIStmnt = conn.prepareStatement(
                    "INSERT INTO " + trpl2viewDefTbl + " VALUES (?,?, ?);");
            trpl2vdIStmnt.setLong(1, tripleId);
            trpl2vdIStmnt.setLong(2, viewDefId);
            trpl2vdIStmnt.setLong(3, assessmentId);
            trpl2vdIStmnt.executeUpdate();
            conn.commit();
        }
    }
    
    
    private void writeViewDef2quad(long viewDefId, long quadId)
            throws SQLException {
        
        // check if entry already exists
        PreparedStatement vd2qpQStmnt = conn.prepareStatement(
                "SELECT view_definition_id FROM " + vd2quadTbl + " " +
                "WHERE view_definition_id=? AND quad_id=? " +
                    "AND assessment_id=?");
        vd2qpQStmnt.setLong(1, viewDefId);
        vd2qpQStmnt.setLong(2, quadId);
        vd2qpQStmnt.setLong(3, assessmentId);
        ResultSet res = vd2qpQStmnt.executeQuery();
        
        if (!res.next()) {
            // entry does not exist in the database, yet
            PreparedStatement vd2qpIStmnt = conn.prepareStatement(
                    "INSERT INTO " + vd2quadTbl + " VALUES (?, ?, ?);");
            vd2qpIStmnt.setLong(1, viewDefId);
            vd2qpIStmnt.setLong(2, quadId);
            vd2qpIStmnt.setLong(3, assessmentId);
            vd2qpIStmnt.executeUpdate();
            conn.commit();
        }
    }
    
    
    private void writeViewDef2var(long viewDefId, long varId) throws SQLException {
        
        // check if entry already exists
        PreparedStatement vd2varQStmnt = conn.prepareStatement(
                "SELECT view_definition_id FROM " + vd2varTbl + " " +
                "WHERE view_definition_id=? AND variable_id=? AND assessment_id=?;");
        vd2varQStmnt.setLong(1, viewDefId);
        vd2varQStmnt.setLong(2, varId);
        vd2varQStmnt.setLong(3, assessmentId);
        ResultSet res = vd2varQStmnt.executeQuery();
        
        if (!res.next()) {
            // entry does not exist in the database, yet
            PreparedStatement vd2varIStmnt = conn.prepareStatement(
                    "INSERT INTO " + vd2varTbl + " VALUES (?, ?, ?);");
            vd2varIStmnt.setLong(1, viewDefId);
            vd2varIStmnt.setLong(2, varId);
            vd2varIStmnt.setLong(3, assessmentId);
            vd2varIStmnt.executeUpdate();
            conn.commit();
        }
    }
    
    
    // -------------------------------- utils --------------------------------
    
    private long getNextId() throws SQLException {
        conn.createStatement().executeUpdate("UPDATE next_id SET id=" + (++nextId) + ";");
        return nextId;
    }
    
    
    private String getNodeStrRepr(Node node) {
        String repr;
        if (node.isBlank()) {
            repr = ((Node_Blank) node).getBlankNodeLabel();
            
        } else if (node.isLiteral()) {
            repr = ((Node_Literal) node).toString();
            
        } else if (node.isURI()) {
            repr = ((Node_URI) node).getURI();
            
        } else if (node.isVariable()) {
            repr = ((Node_Variable) node).toString();
            
        } else {
            repr = node.toString();
        }
        return repr;
    }
}
