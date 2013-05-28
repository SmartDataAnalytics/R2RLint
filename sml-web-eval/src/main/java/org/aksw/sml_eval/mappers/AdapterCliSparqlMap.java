package org.aksw.sml_eval.mappers;

import java.io.File;

import org.aksw.commons.util.Files;
import org.aksw.commons.util.jdbc.DataSourceConfig;
import org.aksw.commons.util.jdbc.DataSourceConfigDefault;

import com.google.common.base.Joiner;

public class AdapterCliSparqlMap
	extends AdapterCliBase
{
	public AdapterCliSparqlMap(File exec, DataSourceConfig dsConfig) {
		super(exec, dsConfig, new MessageParserDummy());
	}
	
	@Override
	public MapResult _map(String mappingStr) throws Exception {
		MapResult result = map(exec, dsConfig, messageParser, mappingStr);
		return result;
	}

	public static MapResult map(File exec, DataSourceConfig dsConfig, MessageParser messageParser, String mappingStr)
		throws Exception
	{
		
		MapResult result = null;
		
		File mappingFile = File.createTempFile("sparqlify-", ".sml");
		try {
			Files.writeToFile(mappingFile, mappingStr, false);

			String[] args = new String[] {
					//"-r", esc(dsConfig.getDriverClassName()),
					"-dburi", esc(dsConfig.getJdbcUrl()),
					"-dbuser", esc(dsConfig.getUsername()),
					"-dbpass", esc(dsConfig.getPassword()),
					"-r2rmlfile", esc(mappingFile.getAbsolutePath()),
			};
			
			String argStr = Joiner.on(' ').join(args);
			String command = exec.getAbsolutePath() + " " + argStr;
			
			//Runtime.getRuntime().ex
			result = AdapterCliBase.process(command, messageParser);
		} finally {
			mappingFile.delete();
		}
		
		return result;
	}

}
