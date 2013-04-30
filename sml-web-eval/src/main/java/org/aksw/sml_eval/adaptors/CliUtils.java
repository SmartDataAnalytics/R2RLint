package org.aksw.sml_eval.adaptors;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class CliUtils {

	public static MapResult process(String command, MessageParser messageParser) throws Exception {
		ExecResult execResult = CliUtils.execHelper(command);
		
		Model model = ModelFactory.createDefaultModel();
		ByteArrayInputStream bais = new ByteArrayInputStream(execResult.getStdout().getBytes());
		model.read(bais, "http://example.org/", "N-TRIPLE");

		List<Message> messages = messageParser.parse(execResult.getStderr());
		
		MapResult result = new MapResult(model, messages);
		return result;
	}
	
	public static ExecResult execHelper(String command)
		throws Exception
	{
		ExecResult result = null;
		Process p = Runtime.getRuntime().exec(command);
		
		ExecutorService executors = Executors.newFixedThreadPool(2);
		try {
			Future<String> stdout = executors.submit(new StringReader(p.getInputStream()));
			Future<String> stderr = executors.submit(new StringReader(p.getErrorStream()));
		
			String so = stdout.get(10, TimeUnit.SECONDS);
			String se = stderr.get(10, TimeUnit.SECONDS);
			
			p.waitFor();
			
			result = new ExecResult(so, se);
			
		} finally {
			executors.shutdown();
		}			

		
		return result;
	}
}
