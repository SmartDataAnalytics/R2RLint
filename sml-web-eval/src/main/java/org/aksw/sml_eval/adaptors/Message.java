package org.aksw.sml_eval.adaptors;


public class Message {
	private Position position;
	private int level;
	private String text;
	
	public Message(Position position, int level, String text) {
		this.position = position;
		this.level = level;
		this.text = text;
	}

	public Position getPosition() {
		return position;
	}

	public int getLevel() {
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