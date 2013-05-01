package org.aksw.sml_eval.mappers;

import java.io.File;

import org.aksw.commons.util.jdbc.DataSourceConfig;



public abstract class AdapterCliBase
	implements Adapter
{
	protected File exec;
	protected DataSourceConfig dsConfig;
	protected MessageParser messageParser;
	
	public AdapterCliBase(File exec, DataSourceConfig dsConfig, MessageParser messageParser) {
		this.exec = exec;
		this.dsConfig = dsConfig;
		this.messageParser = messageParser;
	}
	
	public static String esc(String str) {
		return "\"" + str.replace("\"", "\\\"") + "\"";
	}
	
	public static MapResult process(String command, MessageParser messageParser) throws Exception {
		MapResult result = CliUtils.process(command, messageParser);
		return result;
	}

	public MapResult map(String mappingStr) {
		MapResult result = null;
		try {
			result = _map(mappingStr);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		return result;
	}
	
	public abstract MapResult _map(String mappingStr) throws Exception;
}
