package org.aksw.sparqlify.qa.metrics.accuracy;

import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.NodeMetric;
import org.aksw.sparqlify.qa.metrics.PinpointMetric;
import org.aksw.sparqlify.qa.sinks.TriplePosition;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Triple;

/**
 * This class tests if the value of a typed literal is valid with regards to
 * the given xsd datatype.
 * This metric is a somewhat lightweight version of a more general datatype
 * check metric also considering non-xsd types, to be coded in the future.
 * 
 * @author Patrick Westphal <patrick.westphal@informatik.uni-leipzig.de>
 *
 */
public class XSDDatatypeCompatibleLiterals extends PinpointMetric implements
		NodeMetric {

	@Override
	public void assessNodes(Triple triple) throws NotImplementedException {
		Node object = triple.getObject();
		
		if (object.isLiteral()
				&& ((Node_Literal) object).getLiteralDatatype() != null
				&& !((Node_Literal) object).getLiteralDatatype().isValid(
						object.getLiteralLexicalForm())) {
			
			Set<ViewQuad<ViewDefinition>> viewQuads =
					pinpointer.getViewCandidates(triple);
			
			writeNodeMeasureToSink(0, TriplePosition.OBJECT, triple, viewQuads);
		}
	}

}
