package org.aksw.sml_eval.adaptors;

import java.util.List;

public interface MessageParser {
	List<Message> parse(String str);
}
