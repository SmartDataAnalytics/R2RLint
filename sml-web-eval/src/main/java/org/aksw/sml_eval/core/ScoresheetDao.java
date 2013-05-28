package org.aksw.sml_eval.core;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;


class Option {
	private int id;
	private String text;
	
}

class Item {
	private int id;
	public String text;
	
	private List<Option> options;
	
	public Item() {
	}
	
	public Item(int id, String text) {
		super();
		this.id = id;
		this.text = text;
	}

	public int getId() {
		return id;
	}

	public String getText() {
		return text;
	}
}

class Scoresheet
{
	private List<Item> items = new ArrayList<Item>();
	
}


/*
public class ScoresheetDao {

	public getScoreSheet(Connection conn, Integer userId, String itemBank) {
		
		"SELECT b.item_id FROM item_bank a JOIN item_bank_item b ON (b.item_bank_id = a.id)WHERE a.name = ?"
	}
	
}
*/