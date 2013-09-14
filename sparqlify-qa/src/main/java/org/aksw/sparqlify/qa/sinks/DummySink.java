package org.aksw.sparqlify.qa.sinks;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.Pair;
import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.metrics.MetricImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;

public class DummySink implements MeasureDataSink {
	
	private Logger logger;

	
	public DummySink() {
		logger = LoggerFactory.getLogger("Dummy sink");
	}
	
	
	@Override
	public void initMeasure(String name,Class<? extends MetricImpl> cls, String parentDimension) {
		// pass
	}

	
	@Override
	public void write(MeasureDatum datum) {
		String logLine = datum.getDimension() + "'s measure " +
				datum.getMetric() + " wrote value " + datum.getValue() + "\n" +
						"\tfor: ";
		
		if (datum instanceof NodeTripleMeasureDatum) {
			logLine += ((NodeTripleMeasureDatum) datum).getTriplePosition().name() +
					" position in " + ((NodeTripleMeasureDatum) datum).getTriple();
			
		} else if (datum instanceof TripleMeasureDatum) {
			logLine += ((TripleMeasureDatum) datum).getTriple();
			
		} else if (datum instanceof MappingQuadMeasureDatum) {
			List<Pair<Quad, ViewDefinition>> quadViewDefs =
					((MappingQuadMeasureDatum) datum).getQuadViewDefs();
			
			for (Pair<Quad, ViewDefinition> quadViewDef : quadViewDefs) {
				logLine += quadViewDef.first + ", ";
			}
			int logLineLength = logLine.length(); 
			logLine = logLine.substring(0, logLineLength-2);
		
		} else if(datum instanceof MappingVarMeasureDatum) {
			
			List<Pair<Node_Variable, ViewDefinition>> nodeViewDefs =
					((MappingVarMeasureDatum) datum).getNodeViewDefs();
			
			for (Pair<Node_Variable, ViewDefinition> nodeViewDef : nodeViewDefs) {
				logLine += nodeViewDef.first + ", ";
			}
			int logLineLength = logLine.length(); 
			logLine = logLine.substring(0, logLineLength-2);
			
			Collection<RestrictedExpr> termConstructors = nodeViewDefs.get(0).second
					.getMapping().getVarDefinition()
					.getDefinitions(Var.alloc(nodeViewDefs.get(0).first));
			
			logLine += " --> ";
			
			for (RestrictedExpr termConstructor : termConstructors) {
				logLine += termConstructor.getExpr().getVarsMentioned() + " ";
			}
		
		} else if (datum instanceof TriplesMeasureDatum) {
			List<Pair<Triple, Set<ViewQuad<ViewDefinition>>>> ppInfos =
					((TriplesMeasureDatum) datum).getPinpoinInfos();
			
			for ( Pair<Triple, Set<ViewQuad<ViewDefinition>>> ppInfo : ppInfos) {
				logLine += ppInfo.first + ", ";
			}
			int logLineLength = logLine.length(); 
			logLine = logLine.substring(0, logLineLength-2);

		} else if (datum instanceof MappingMeasureDatum) {
			Set<ViewQuad<ViewDefinition>> viewQuads = ((MappingMeasureDatum) datum)
					.getViewDefs();
			
			for (ViewQuad<ViewDefinition> viewQuad : viewQuads) {
				logLine += viewQuad.getQuad() + " vs. ";
			}
			int logLineLength = logLine.length(); 
			logLine = logLine.substring(0, logLineLength-5);
			
		} else if (datum instanceof NodeMeasureDatum) {
			Node node = ((NodeMeasureDatum) datum).getNode();
			
			logLine += " " + node.toString();
//		} else if (datum.getClass().getName().equals(DatasetMeasureDatum.class.getName())) {
//			logLine += ???
		}
		
		logger.info(logLine);
	}
}
