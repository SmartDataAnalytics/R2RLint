package org.linkedgeodata.usertags.core;

import java.sql.Connection;

public interface TxWrapper<T> {
	T tx(Connection conn) throws Exception;
}