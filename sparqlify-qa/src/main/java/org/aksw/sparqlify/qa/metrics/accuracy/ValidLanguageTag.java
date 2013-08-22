package org.aksw.sparqlify.qa.metrics.accuracy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.metrics.NodeMetric;
import org.aksw.sparqlify.qa.metrics.PinpointMetric;
import org.aksw.sparqlify.qa.sinks.TriplePosition;
import org.openjena.riot.LangTag;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Triple;

public class ValidLanguageTag extends PinpointMetric implements NodeMetric {
	
	private String langTagFilePath = "src/main/resources/iana_lang_tags.txt";
	private List<String> langTags;
	
	public ValidLanguageTag() {
		langTags = new ArrayList<String>();
		
		File langTagsFile = new File(langTagFilePath);
		FileReader fileIn = null;
		try {
			fileIn = new FileReader(langTagsFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedReader in = new BufferedReader(fileIn);
		
		String line;
		try {
			while ((line = in.readLine()) != null) {
				langTags.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void assessNodes(Triple triple) throws NotImplementedException {
		// TODO Auto-generated method stub
		Node object = triple.getObject();
		if (object.isLiteral()) {
			
			Node_Literal litObj = (Node_Literal) object;
			if (litObj.getLiteralLanguage() != null && !litObj.getLiteralLanguage().equals("")) {
				
				String[] res = LangTag.parse(litObj.getLiteralLanguage());
				if (res == null || !langTags.contains(res[0])) {
					
					Set<ViewQuad<ViewDefinition>> viewQuads =
							pinpointer.getViewCandidates(triple);
					
					writeNodeMeasureToSink(0, TriplePosition.OBJECT, triple, viewQuads);
				}
			}
			
		}
	}

}
