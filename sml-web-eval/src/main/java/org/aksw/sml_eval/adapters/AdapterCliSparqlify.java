package org.aksw.sml_eval.adapters;

import java.io.File;

import org.aksw.commons.util.Files;
import org.aksw.commons.util.jdbc.DataSourceConfig;
import org.aksw.commons.util.jdbc.DataSourceConfigDefault;

import com.google.common.base.Joiner;

public class AdapterCliSparqlify
	extends AdapterCliBase
{
	public AdapterCliSparqlify(File exec, DataSourceConfig dsConfig) {
		super(exec, dsConfig, new MessageParserDummy());
	}	
	
	public MapResult _map(String mappingStr)
		throws Exception
	{
		MapResult result = null;
		
		File mappingFile = File.createTempFile("sparqlify-", ".sml");
		try {
			Files.writeToFile(mappingFile, mappingStr, false);

			String[] args = new String[] {
					//"-r", esc(dsConfig.getDriverClassName()),
					"-j", esc(dsConfig.getJdbcUrl()),
					"-u", esc(dsConfig.getUsername()),
					"-p", esc(dsConfig.getPassword()),
					"-m", esc(mappingFile.getAbsolutePath()),
					"-D"
			};
			
			String argStr = Joiner.on(' ').join(args);
			String command = exec.getAbsolutePath() + " " + argStr;
			
			//Runtime.getRuntime().ex
			result = process(command);
		} finally {
			mappingFile.delete();
		}
		
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		File exec = new File("/home/raven/Projects/Eclipse/sparqlify-parent/sparqlify-core/sparqlify.sh");
		DataSourceConfigDefault dsc = new DataSourceConfigDefault();
		dsc.setJdbcUrl("jdbc:postgresql://localhost/smleval");
		dsc.setUsername("postgres");
		dsc.setPassword("postgres");

		AdapterCliSparqlify wrapper = new AdapterCliSparqlify(exec, dsc);
		MapResult r = wrapper._map("Prefix ex: <http://example.org/> Create View test As Construct { ?s a ex:Thing } With ?s = uri(ex:, ?id) From users");
		r.getModel().write(System.out, "TTL");
		System.err.println(r.getMessages());
	}
}
