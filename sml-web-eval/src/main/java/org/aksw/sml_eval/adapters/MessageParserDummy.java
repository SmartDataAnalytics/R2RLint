package org.aksw.sml_eval.adapters;

import java.util.ArrayList;
import java.util.List;

public class MessageParserDummy
	implements MessageParser
{
	@Override
	public List<Message> parse(String str) {
		Message msg = new Message(null, 0, str);
		
		List<Message> result = new ArrayList<Message>();
		result.add(msg);
		
		return result;
	}
}
