package org.aksw.sparqlify.qa.main;

import java.io.IOException;
import java.sql.SQLException;

import org.aksw.sparqlify.qa.exceptions.DimensionUnknownException;
import org.aksw.sparqlify.qa.exceptions.NotImplementedException;
import org.aksw.sparqlify.qa.exceptions.TripleParseException;
import org.antlr.runtime.RecognitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Run {
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws DimensionUnknownException 
	 * @throws SQLException 
	 * @throws RecognitionException 
	 * @throws TripleParseException 
	 * @throws NotImplementedException 
	 */
	public static void main(String[] args) throws IOException,
			DimensionUnknownException, SQLException, RecognitionException,
			TripleParseException, NotImplementedException {
		
		@SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
		
		QualityAssessment app = (QualityAssessment) context.getBean("app");
		app.exec();
	}

}
