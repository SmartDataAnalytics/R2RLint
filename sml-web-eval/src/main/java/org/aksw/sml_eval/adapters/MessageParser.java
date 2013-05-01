package org.aksw.sml_eval.adapters;

import java.util.List;

public interface MessageParser {
	List<Message> parse(String str);
}
