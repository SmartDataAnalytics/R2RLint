package org.linkedgeodata.usertags.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.aksw.commons.util.jdbc.SqlUtils;

public class UserTagsStore {
	private DataSource dataSource;
	
	public UserTagsStore(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	/**
	 * Simple transaction wrapper helper
	 * 
	 * @param txWrapper
	 * @return
	 * @throws SQLException
	 */
	public <T> T wrap(TxWrapper<T> txWrapper)
		throws SQLException
	{	
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			T result = txWrapper.tx(conn);
			
			conn.commit();
			
			return result;
		} catch(Exception e) {
			if(conn != null) {
				conn.rollback();
			}
			
			throw new RuntimeException(e);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}		
	}
	
	public static void updateTags(Connection conn, UserId userId, OsmEntityType osmEntityType, Long osmEntityId, List<Tag> tags)
			throws SQLException
	{
		deleteTags(conn, userId, osmEntityType, osmEntityId);
		writeTags(conn, userId, osmEntityType, osmEntityId, tags);
	}
	
	public static void writeTags(Connection conn, UserId userId, OsmEntityType osmEntityType, Long osmEntityId, List<Tag> tags)
			throws SQLException 
	{
		String tableName = osmEntityType + "_user_tags";
		String idName = osmEntityType + "_id";
		String qs = "INSERT INTO " + tableName + "(user_id, " + idName + ", k, v) VALUES (?, ?, ?, ?)";

		long uid = ((UserIdOsm)userId).getUserId();
		
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(qs);
			
			for(Tag tag : tags) {
				SqlUtils.execute(stmt, Void.class, uid, osmEntityId, tag.getK(), tag.getV());
			}
		} finally {
			if(stmt != null) {
				stmt.close();
			}
		}
	}
		
	public static void deleteTags(Connection conn, UserId userId, OsmEntityType osmEntityType, Long osmEntityId)
				throws SQLException
	{	
		String tableName = osmEntityType + "_user_tags";
		String idName = osmEntityType + "_id";
		String qs = "DELETE FROM " + tableName + " WHERE user_id = ? AND " + idName + " = ?";
	
		long uid = ((UserIdOsm)userId).getUserId();
		
		SqlUtils.execute(conn, qs, Void.class, uid, osmEntityId);	
	}
}