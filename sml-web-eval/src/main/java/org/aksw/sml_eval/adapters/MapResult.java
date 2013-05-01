package org.aksw.sml_eval.adapters;

import java.util.List;


import com.hp.hpl.jena.rdf.model.Model;

public class MapResult {
	private Model model;
	private List<Message> messages;
	
	public MapResult(Model model, List<Message> messages) {
		super();
		this.model = model;
		this.messages = messages;
	}

	public Model getModel() {
		return model;
	}

	public List<Message> getMessages() {
		return messages;
	}
}