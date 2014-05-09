package org.aksw.sparqlify.qa.metrics.semanticaccuracy;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.aksw.commons.collections.Pair;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.aksw.sparqlify.qa.metrics.ViewMetric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

@Component
public class PreservedFunctionalAttributes extends MetricImpl implements
        ViewMetric {
    
    @Autowired
    DataSource rdb;
    Connection conn;
    
    @PostConstruct
    private void init() throws SQLException {
        conn = rdb.getConnection();
    }
    
    @PreDestroy
    private void cleanUp() throws SQLException {
        conn.close();
    }
    
    
    @Override
    public void assessViews(Collection<ViewDefinition> viewDefs)
            throws NotImplementedException, SQLException {
        
        for (ViewDefinition viewDef : viewDefs) {
        
            Multimap<Var, RestrictedExpr> tcMap = viewDef.getVarDefinition().getMap();
            
            SqlOp tbl = viewDef.getMapping().getSqlOp();
            String tblName = null;
            if (tbl instanceof SqlOpTable) {
                tblName = ((SqlOpTable) tbl).getTableName();
            } else {
                return;
            }
            
            List<String> pKeyCols = getPKeys(tblName);
            
            
            // iterate over quads of viewDef
            QuadPattern quadPattern = viewDef.getTemplate();
            for (Quad quad : quadPattern) {
                Node subject = quad.getSubject();
                Node object = quad.getObject();
                // subject and object are variables
                if (subject.isVariable() && object.isVariable()) {
                    // Is subject built referring to the primary key of the
                    // underlying table?
                    Collection<RestrictedExpr> tcs = tcMap.get((Var) subject);
                    
                    // dummy loop since there is usually just one term constructor
                    for (RestrictedExpr tc : tcs) {
                        Set<Var> colVars = tc.getExpr().getVarsMentioned();
                        
                        boolean nothingButPKeyCols = false;
                        // loop over col vars to check if they are the pkey cols
                        for (Var colVar : colVars) {
                            String colName = colVar.getVarName();
                            if (pKeyCols.contains(colName)) {
                                nothingButPKeyCols = true;
                            } else {
                                break;
                            }
                        }
                        
                        if (nothingButPKeyCols) {
                            Node prop = quad.getPredicate();
                            // look for a quad statement
                            // prop a owl:FunctionalProperty
                            
                            boolean funcPropStmntFound = false;
                            for (ViewDefinition searchViewDef : viewDefs) {
                                QuadPattern searchQuadPattern = searchViewDef.getTemplate();
                                for (Quad pquad : searchQuadPattern) {
                                    if (pquad.getSubject().equals(prop)
                                            && pquad.getPredicate().equals(RDF.type.asNode())
                                            && pquad.getObject().equals(OWL.FunctionalProperty.asNode())) {
                                        funcPropStmntFound = true;
                                        break;
                                    }
                                }
                                if (funcPropStmntFound) break;
                            }
                            if (!funcPropStmntFound) {
                                List<Pair<Quad, ViewDefinition>> quadViewDefs =
                                        new ArrayList<Pair<Quad,ViewDefinition>>();
                                
                                Pair<Quad, ViewDefinition> entry =
                                        new Pair<Quad, ViewDefinition>(quad, viewDef);
                                
                                quadViewDefs.add(entry);
                                
                                writeMappingQuadMeasureToSink(0, quadViewDefs);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private List<String> getPKeys(String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        List<String> pkeys = new ArrayList<String>();
        
        ResultSet pkRes = meta.getPrimaryKeys(conn.getCatalog(), null, tableName);
        
        while (pkRes.next()) {
            pkeys.add(pkRes.getString(4));
        }
        
        return pkeys;
    }
}
