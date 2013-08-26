package org.aksw.sparqlify.qa.metrics;

import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.sinks.DatasetMeasureDatum;
import org.aksw.sparqlify.qa.sinks.MappingMeasureDatum;
import org.aksw.sparqlify.qa.sinks.MeasureDataSink;
import org.aksw.sparqlify.qa.sinks.MeasureDatum;
import org.aksw.sparqlify.qa.sinks.NodeMeasureDatum;
import org.aksw.sparqlify.qa.sinks.TripleMeasureDatum;
import org.aksw.sparqlify.qa.sinks.TriplePosition;

import com.hp.hpl.jena.graph.Triple;

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
	protected void writeNodeMeasureToSink(float val, TriplePosition pos,
			Triple triple, Set<ViewQuad<ViewDefinition>> viewQuads)
			throws NotImplementedException {
		
		MeasureDatum datum = new NodeMeasureDatum(parentDimension, name, val,
				pos, triple, viewQuads);
		
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
	protected void writeDatasetMeasureToDisk(float val) throws NotImplementedException {
		MeasureDatum datum = new DatasetMeasureDatum(parentDimension, name, val);
		sink.write(datum);
	}


	// TODO: fix this first shot approach method signature
	protected void writeMappingMeasureToDisk(float val, ViewDefinition viewDef)
			throws NotImplementedException {
		
		MeasureDatum datum = new MappingMeasureDatum(parentDimension, name, val, viewDef);
		sink.write(datum);
	}


	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

}
