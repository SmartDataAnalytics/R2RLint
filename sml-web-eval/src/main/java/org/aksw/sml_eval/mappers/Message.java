package org.aksw.sml_eval.mappers;


public class Message {
	private Position position;
	private String level;
	private String text;
	
	public Message(Position position, String level, String text) {
		this.position = position;
		this.level = level;
		this.text = text;
	}

	public Position getPosition() {
		return position;
	}

	public String getLevel() {
		return level;
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return "Message [position=" + position + ", level=" + level + ", text="
				+ text + "]";
	}
}