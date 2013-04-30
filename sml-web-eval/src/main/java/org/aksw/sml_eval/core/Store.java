package org.aksw.sml_eval.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.aksw.commons.sparql.api.cache.extra.SqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class User {
	private int id;
	private String name;
	
	public User(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + "]";
	}
}

public class Store {
	
	private static final Logger logger = LoggerFactory.getLogger(Store.class);
	
	private DataSource ds;
	
	
	public Store(DataSource ds) {
		this.ds = ds;
	}
	
	
	// Returns the user id - null if that fails
	public Integer authenticate(String username, String password) throws SQLException
	{
		Integer result = null;
		Connection conn = null;
		try {
			conn = ds.getConnection();
		
			String sql = "SELECT id FROM users WHERE name = ? AND password = ?";
		
			result = SqlUtils.execute(conn, sql, Integer.class, username, password);

		} finally {
			if(conn != null) {
				conn.close();
			}
		}
		
		return result;
	}
	
	public boolean registerUser(String username, String password, String email) throws SQLException {
		
		// The result indicates success
		boolean result = false;
		
		Connection conn = null;
		try {
			conn = ds.getConnection();
			
			String sql = "INSERT INTO users(name, email, password) VALUES (?, ?, ?)";
			
			SqlUtils.execute(conn, sql, Void.class, username, email, password);
			
			result = true;
		} finally {
			if(conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Error closing connection: ", e);
				}
			}
		}
		
		return result;
	}
}
