package org.aksw.sparqlify.qa.metrics.interpretability;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.commons.collections.Pair;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.MappingMetric;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;

/*
 * This metric reports occurrences of blank nodes used in statements of a given
 * dataset.
 * Since occurrences of blank nodes in the context of the Sparqlify RDB2RDF
 * tool can only be caused by an explicit mapping definition (using the
 * bNode(...) term constructor) only the view definitions have to be considered.
 * 
 */
@Component
public class AvoidBlankNodes extends MetricImpl implements MappingMetric{


	@Override
	public void assessMappings(Collection<ViewDefinition> viewDefs)
			throws NotImplementedException, SQLException {
		
		for (ViewDefinition viewDef : viewDefs) {
			
			VarDefinition varDefs = viewDef.getVarDefinition();
			
			for (Quad pattern : viewDef.getTemplate()) {
				
				Node subject = pattern.getSubject();
				Node predicate = pattern.getPredicate();
				Node object = pattern.getObject();
				
				/* subject */
				if (subject.isVariable()) {
					
					Collection<RestrictedExpr> termConstructor =
							varDefs.getDefinitions(Var.alloc(subject));
					
					if (constructsBNode(termConstructor))
						write((Node_Variable) subject, viewDef);
				}
				
				/* predicate */
				// event though blank nodes are not allowed on predicate
				// position one may create mapping definitions that do so.
				// That's why this is checked here
				if (predicate.isVariable()) {
					
					Collection<RestrictedExpr> termConstructor =
							varDefs.getDefinitions(Var.alloc(predicate));
					
					if (constructsBNode(termConstructor))
						write((Node_Variable) predicate, viewDef);
				}
				
				/* object */
				if (object.isVariable()) {
					
					Collection<RestrictedExpr> termConstructor =
							varDefs.getDefinitions(Var.alloc(object));
					
					if (constructsBNode(termConstructor))
						write((Node_Variable) object, viewDef);
				}
			}
			
		}
		
	}


	private boolean constructsBNode(Collection<RestrictedExpr> termConstructor) {
		double funcVal = -1;
		
		// dummy loop because in this context the collection can only contain
		// one RestrictedExpr
		for (RestrictedExpr restExpr : termConstructor) {
			Expr expr = restExpr.getExpr();

			// -1 = var, 0 = bNode, 1 = uri, 2 = plainLiteral, 3 = typedLiteral
			funcVal = expr.getFunction().getArg(1).getConstant().getDouble();
			
		}
		
		if (funcVal == 0) return true;
		else return false;
	}


	private void write(Node_Variable node, ViewDefinition viewDef)
			throws NotImplementedException, SQLException {
		
		List<Pair<Node_Variable, ViewDefinition>> nodeViewDefs =
				new ArrayList<Pair<Node_Variable, ViewDefinition>>();
		
		nodeViewDefs.add(new Pair<Node_Variable, ViewDefinition>(node, viewDef));
		
		writeMappingVarMeasureToSink(0, nodeViewDefs);
	}
}
