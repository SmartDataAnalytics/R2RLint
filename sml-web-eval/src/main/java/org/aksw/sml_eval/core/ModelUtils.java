package org.aksw.sml_eval.core;

import java.io.ByteArrayOutputStream;

import com.google.gson.Gson;
import com.hp.hpl.jena.rdf.model.Model;
import com.talis.rdfwriters.json.JSONJenaWriter;

public class ModelUtils {
	
	public static Object toJsonObject(Model model) {
		String str = toJsonString(model);
		
		Gson gson = new Gson();
		Object result = gson.fromJson(str, Object.class);
		
		return result;
	}
	
	public static String toJsonString(Model model) {
		JSONJenaWriter writer = new JSONJenaWriter();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writer.write(model, out, "http://example.org/");

		String result = out.toString();
		return result;
	}
}
