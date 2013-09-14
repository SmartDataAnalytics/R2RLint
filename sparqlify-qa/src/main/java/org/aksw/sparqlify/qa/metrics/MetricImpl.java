package org.aksw.sparqlify.qa.metrics;

import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.Pair;
import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.sinks.DatasetMeasureDatum;
import org.aksw.sparqlify.qa.sinks.MappingMeasureDatum;
import org.aksw.sparqlify.qa.sinks.MappingQuadMeasureDatum;
import org.aksw.sparqlify.qa.sinks.MappingVarMeasureDatum;
import org.aksw.sparqlify.qa.sinks.MeasureDataSink;
import org.aksw.sparqlify.qa.sinks.MeasureDatum;
import org.aksw.sparqlify.qa.sinks.NodeMeasureDatum;
import org.aksw.sparqlify.qa.sinks.NodeTripleMeasureDatum;
import org.aksw.sparqlify.qa.sinks.TripleMeasureDatum;
import org.aksw.sparqlify.qa.sinks.TriplePosition;
import org.aksw.sparqlify.qa.sinks.TriplesMeasureDatum;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

public abstract class MetricImpl implements Metric {

	protected String name;
	protected String parentDimension;
	protected String prefix = "";
	protected float threshold = 0;
	protected MeasureDataSink sink;


	@Override
	public String getParentDimension() {
		return parentDimension;
	}


	@Override
	public void setParentDimension(String parentDimension) {
		this.parentDimension = parentDimension;
	}


	@Override
	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}


	@Override
	public void registerMeasureDataSink(MeasureDataSink sink) throws NotImplementedException {
		this.sink = sink;
		sink.initMeasure(name, getClass(), parentDimension);
	}


	@Override
	public String getName() {
		return name;
	}


	@Override
	public void setName(String name) {
		this.name = name;
	}


	// TODO: fix this first shot approach method signature
	protected void writeNodeTripleMeasureToSink(float val, TriplePosition pos,
			Triple triple, Set<ViewQuad<ViewDefinition>> viewQuads)
			throws NotImplementedException {
		
		MeasureDatum datum = new NodeTripleMeasureDatum(parentDimension, name, val,
				pos, triple, viewQuads);
		
		sink.write(datum);
	}


	// TODO: fix this first shot approach method signature
	protected void writeNodeMeasureToSink(float value, Node node) {
		MeasureDatum datum = new NodeMeasureDatum(parentDimension, name, value, node);
		
		sink.write(datum);
	}


	protected void writeTripleMeasureToSink(float val, Triple triple,
			Set<ViewQuad<ViewDefinition>> viewQuads)
			throws NotImplementedException {
		
		MeasureDatum datum = new TripleMeasureDatum(parentDimension, name, val,
				triple, viewQuads);
		
		sink.write(datum);
	}


	// TODO: fix this first shot approach method signature
	protected void writeDatasetMeasureToSink(float val) throws NotImplementedException {
		MeasureDatum datum = new DatasetMeasureDatum(parentDimension, name, val);
		sink.write(datum);
	}


	// TODO: fix this first shot approach method signature
	protected void writeTriplesMeasureToSink(float val,
			List<Pair<Triple, Set<ViewQuad<ViewDefinition>>>> pinpointResult)
			throws NotImplementedException {
		
		MeasureDatum datum = new TriplesMeasureDatum(parentDimension, name,
				val, pinpointResult);
		
		sink.write(datum);
	}


	protected void writeTriplesMeasureToSink(String metricName, float val,
			List<Pair<Triple, Set<ViewQuad<ViewDefinition>>>> pinpointResult) {
		
		MeasureDatum datum = new TriplesMeasureDatum(parentDimension,
				metricName, val, pinpointResult);
		
		sink.write(datum);
	}


	// TODO: fix this first shot approach method signature
	protected void writeMappingMeasureToSink(float val, Set<ViewQuad<ViewDefinition>> candidates)
			throws NotImplementedException {
		
		MeasureDatum datum = new MappingMeasureDatum(parentDimension, name, val, candidates);
		sink.write(datum);
	}


	protected void writeMappingVarMeasureToSink(float val,
			List<Pair<Node_Variable, ViewDefinition>> nodeViewDefs)
			throws NotImplementedException {
		
		MeasureDatum datum = new MappingVarMeasureDatum(parentDimension, name,
				val, nodeViewDefs);
		sink.write(datum);
	}


	protected void writeMappingQuadMeasureToSink(float val,
			List<Pair<Quad, ViewDefinition>> quadViewDefs)
			throws NotImplementedException {

		MeasureDatum datum = new MappingQuadMeasureDatum(parentDimension, name,
				val, quadViewDefs);
		sink.write(datum);
	}


	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

}
