package org.aksw.sml_eval.adaptors;

import java.io.File;

import org.aksw.commons.util.jdbc.DataSourceConfig;


public abstract class CliWrapperBase {
	protected File exec;
	protected DataSourceConfig dsConfig;
	protected MessageParser messageParser;
	
	public CliWrapperBase(File exec, DataSourceConfig dsConfig, MessageParser messageParser) {
		this.exec = exec;
		this.dsConfig = dsConfig;
		this.messageParser = messageParser;
	}
	
	public static String esc(String str) {
		return "\"" + str.replace("\"", "\\\"") + "\"";
	}
	
	public MapResult process(String command) throws Exception {
		MapResult result = CliUtils.process(command, messageParser);
		return result;
	}
}
