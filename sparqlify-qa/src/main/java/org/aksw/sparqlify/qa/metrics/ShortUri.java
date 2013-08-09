package org.aksw.sparqlify.qa.metrics;

import java.sql.Connection;
import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class ShortUri implements NodeMetric {

	private String parentDimension = null;
	private String name;
	private float threshold;
	private MeasureDataSink sink;
	private Pinpointer pinpointer;
	private Connection conn;
	
	
	@Override
	public void assessNodes(Triple triple) {
		Node subj = triple.getSubject();
		if (subj.isURI() && resourceTooLong(subj)) {
			Set<ViewQuad<ViewDefinition>> viewQuads =
						pinpointer.getViewCandidates(triple);
			writeBack("subject", triple, viewQuads);
		}
		
		Node pred = triple.getPredicate();
		if (resourceTooLong(pred)) {
			Set<ViewQuad<ViewDefinition>> viewQuads =
					pinpointer.getViewCandidates(triple);
			
			writeBack("subject", triple, viewQuads);
		}
		
		Node obj = triple.getObject();
		if (obj.isURI() && resourceTooLong(obj)) {
			Set<ViewQuad<ViewDefinition>> viewQuads =
					pinpointer.getViewCandidates(triple);
			
			writeBack("subject", triple, viewQuads);
		}
	}

	private boolean resourceTooLong(Node res) {
		if (res.getURI().length() > 50) return true;
		else return false;
	}
	
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
	public void registerMeasureDataSink(MeasureDataSink sink) {
		this.sink = sink;
		
	}


	@Override
	public void registerDbConnection(Connection conn) {
		this.conn = conn;
	}


	@Override
	public void registerPinpointer(Pinpointer pinpointer) {
		this.pinpointer = pinpointer;
	}
	
	
	private void writeBack(String position, Triple triple, Set<ViewQuad<ViewDefinition>> viewQuads){
		// FIXME: just a debug dummy
		String reason = "";
		for (ViewQuad<ViewDefinition> viewQuad : viewQuads) {
			reason += viewQuad.getQuad() + "\n" +
					"of the following view definition:\n" +
					viewQuad.getView() + "\n";
		}
		
		reason += "\n\n";
		MeasureDatum datum = new MeasureDatum(
				parentDimension, name, 0, position + " position of " + triple.toString(), ""); //reason);
		sink.write(datum);
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
}