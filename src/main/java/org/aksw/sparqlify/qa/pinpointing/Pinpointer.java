package org.aksw.sparqlify.qa.pinpointing;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.aksw.sparqlify.core.algorithms.CandidateViewSelectorImpl;
import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.database.Clause;
import org.aksw.sparqlify.database.NestedNormalForm;
import org.aksw.sparqlify.restriction.RestrictionManagerImpl;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;

@Component
public class Pinpointer {

//	private Collection<ViewDefinition> viewDefs;
	CandidateViewSelectorImpl candidateSelector;
	
	/*
	 * TODO:
	 * - add query method
	 * - add caching
	 */
	
	public void registerViewDefs(Collection<ViewDefinition> viewDefs) {
//		this.viewDefs = viewDefs;
		candidateSelector = new CandidateViewSelectorImpl();
		for (ViewDefinition viewDef : viewDefs) {
			candidateSelector.addView(viewDef);
		}
	}
	
	
	public Set<ViewQuad<ViewDefinition>> getViewCandidates(Triple triple) {
		Var g = Var.alloc("g");
		Var s = Var.alloc("s");
		Var p = Var.alloc("p");
		Var o = Var.alloc("o");

		Node gv = Quad.defaultGraphNodeGenerated;
		Node sv = triple.getSubject();
		Node pv = triple.getPredicate();
		Node ov = triple.getObject();
		
		Quad tmpQuad = new Quad(g, s, p, o);
		
		RestrictionManagerImpl r = new RestrictionManagerImpl();
		
		Set<Clause> clauses = new HashSet<Clause>();
		clauses.add(new Clause(new E_Equals(new ExprVar(g), NodeValue.makeNode(gv))));
		clauses.add(new Clause(new E_Equals(new ExprVar(s), NodeValue.makeNode(sv))));
		clauses.add(new Clause(new E_Equals(new ExprVar(p), NodeValue.makeNode(pv))));
		clauses.add(new Clause(new E_Equals(new ExprVar(o), NodeValue.makeNode(ov))));

		NestedNormalForm nnf = new NestedNormalForm(clauses);
		
		r.stateCnf(nnf);
		
		Set<ViewQuad<ViewDefinition>> result = candidateSelector.findCandidates(tmpQuad, r);
		
		return result;
	}
}
