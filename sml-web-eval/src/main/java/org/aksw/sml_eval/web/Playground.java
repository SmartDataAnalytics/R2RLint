package org.aksw.sml_eval.web;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.aksw.commons.util.jdbc.JdbcUtils;
import org.aksw.commons.util.jdbc.Relation;
import org.aksw.sml_eval.core.TaskRepo;
import org.aksw.sml_eval.core.TaskRepoReader;
import org.h2.jdbcx.JdbcDataSource;

public class Playground {
	public static void main(String[] args) throws Exception {
		
		TaskRepoReader taskRepoReader = new TaskRepoReader();
		
		TaskRepo repo = taskRepoReader.getTaskRepo();
		System.out.println(repo.toJson());
		
		//System.out.println(tables.keySet());

		

	}
	

}
