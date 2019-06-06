package com.disaster;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import com.converter.TimeConverter;

import Common.ParaSetting;

public class GDELTDisaster extends Disaster implements IFileIO, IDataBaseOperation {

	@Override
	public void wirteToFile(String fileName) throws Exception {
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName, true));
		String dataLine = super.toString();
		bufferedWriter.write(dataLine + "\r\n");
		bufferedWriter.close();
	}

	@Override
	public void insertIntoDataBase() throws Exception {
		String insertStr = "insert into disaster_event.\"GDELT\" VALUES (?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = ParaSetting.connection.prepareStatement(insertStr);
		preparedStatement.setString(1, this.id);
		preparedStatement.setDouble(2, this.longitude);
		preparedStatement.setDouble(3, this.latitude);
		preparedStatement.setString(4, this.disasterType.toString());
		preparedStatement.setString(5, this.url);
		preparedStatement.setTimestamp(6, new Timestamp(this.startCalendar.getTimeInMillis()));
		preparedStatement.setString(7, this.title);
		preparedStatement.executeUpdate();
		preparedStatement.close();
	}

	// because the table of gdelt is too big, it is not a good idea to use id to
	// check the disaster can be insert, so I use fileName to check it
	@Override
	public boolean checkCanInsert() throws SQLException {
		String fileName = this.id.substring(0, 14) + ".gkg.csv";
		String select = "select * from disaster_event.\"GDELTFileName\" where \"fileName\"='" + fileName + "' limit 1;";
		Statement stmt = ParaSetting.connection.createStatement();
		ResultSet rs = stmt.executeQuery(select);
		boolean checkHasRecord = rs.next();
		stmt.close();
		if (checkHasRecord == true) {
			return false;
		} else {
			return true;
		}
	}

	public static GDELTDisaster readFromString(String dataLine) {
		String[] dataArray = dataLine.split("\t");
		GDELTDisaster disaster = new GDELTDisaster();
		disaster.id = dataArray[0];
		disaster.longitude = Double.valueOf(dataArray[1]);
		disaster.latitude = Double.valueOf(dataArray[2]);
		disaster.startCalendar = TimeConverter.getCalendarFromString(dataArray[3]);
		disaster.url = dataArray[4];
		disaster.disasterType = DisasterType.getDisasterTypeFromString(dataArray[5]);
		disaster.title = dataArray[6];
		return disaster;
	}

}
