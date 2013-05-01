package org.aksw.sml_eval.mappers;

import java.util.List;

public interface MessageParser {
	List<Message> parse(String str);
}
