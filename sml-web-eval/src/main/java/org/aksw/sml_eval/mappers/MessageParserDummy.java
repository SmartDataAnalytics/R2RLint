package org.aksw.sml_eval.mappers;

import java.util.ArrayList;
import java.util.List;


public class MessageParserDummy
	implements MessageParser
{
	@Override
	public List<Message> parse(String str) {
		Message msg = new Message(null, "INFO", str);
		
		List<Message> result = new ArrayList<Message>();
		result.add(msg);
		
		return result;
	}
}
