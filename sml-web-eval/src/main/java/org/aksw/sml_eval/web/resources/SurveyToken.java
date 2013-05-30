package org.aksw.sml_eval.web.resources;

public class SurveyToken {
	private String token;
	private String lang;
	
	public SurveyToken() {
		super();
	}

	public SurveyToken(String token, String lang) {
		super();
		this.token = token;
		this.lang = lang;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}	
}