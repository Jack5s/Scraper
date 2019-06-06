package com.disaster;

import java.sql.SQLException;

public interface IDataBaseOperation {
	void insertIntoDataBase() throws Exception;
	boolean checkCanInsert() throws SQLException;
}
