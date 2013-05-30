package org.aksw.sml_eval.mappers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageParserLogFormat
	implements MessageParser
{
	private Pattern pattern;
	private int level;
	private int rest;
	
	public MessageParserLogFormat(Pattern pattern, int level, int rest) {
		this.pattern = pattern;
		this.level = level;
		this.rest = rest;
	}
	
	@Override
	public List<Message> parse(String str) {
		try {
			List<Message> result = _parse(str);
			return result;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<Message> _parse(String str) throws IOException {
		List<Message> result = new ArrayList<Message>();
		
		InputStream in = new ByteArrayInputStream(str.getBytes());
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
		String currentLevel = "INFO";
		String currentMsg = "";
		
		String line;
		while((line = reader.readLine()) != null) {
			Matcher matcher = pattern.matcher(line);
			
			boolean find = matcher.find(); 
			
			if(find) {
				
				if(!currentMsg.isEmpty()) {
					Message msg = new Message(null, currentLevel, currentMsg);					
					result.add(msg);
					currentMsg = "";					
				}
				
				// Start a new message
				currentMsg = matcher.group(rest);
				currentLevel = matcher.group(level);
				
				
			} else {
				// Count the line to the current message
				currentMsg += "\n" + line;
			}
		}

		if(!currentMsg.isEmpty()) {
			currentMsg = "";			
			Message msg = new Message(null, currentLevel, currentMsg);
			result.add(msg);
		}
		

		
		return result;
	}

	//2013-05-30 02:57:22,528 ERROR org.aksw.sparqlify.web.HttpSparqlEndpoint
	public static final Pattern patternSparqlify = Pattern.compile("\\S+ \\S+ (TRACE|DEBUG|INFO|WARN|ERROR|FATAL) \\S+ (.*)");
	
	public static MessageParserLogFormat createForSparqlify() {
		MessageParserLogFormat result = new MessageParserLogFormat(patternSparqlify, 1, 2);
		
		return result;
	}
}
