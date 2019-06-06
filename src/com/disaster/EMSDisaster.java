package com.disaster;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;

import com.converter.TimeConverter;

import Common.ParaSetting;

public class EMSDisaster extends Disaster implements IFileIO, IDataBaseOperation {
	public Calendar activationCalendar;

	public EMSDisaster() {
		this.activationCalendar = Calendar.getInstance();
	}

	@Override
	public void wirteToFile(String fileName) throws Exception {
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName, true));
		Timestamp timestamp = new Timestamp(this.activationCalendar.getTimeInMillis());
		String dataLine = super.toString() + timestamp.toString() + "\t";
		bufferedWriter.write(dataLine + "\r\n");
		bufferedWriter.close();
	}

	@Override
	public void insertIntoDataBase() throws Exception {
		String insertStr = "insert into disaster_event.\"EMS\" VALUES (?,?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = ParaSetting.connection.prepareStatement(insertStr);
		preparedStatement.setString(1, this.id);
		preparedStatement.setDouble(2, this.longitude);
		preparedStatement.setDouble(3, this.latitude);
		preparedStatement.setString(4, this.disasterType.toString());
		preparedStatement.setString(5, this.url);
		preparedStatement.setTimestamp(6, new Timestamp(this.startCalendar.getTimeInMillis()));
		preparedStatement.setTimestamp(7, new Timestamp(this.activationCalendar.getTimeInMillis()));
		preparedStatement.setString(8, this.title);
		preparedStatement.executeUpdate();
		preparedStatement.close();
	}

	@Override
	public boolean checkCanInsert() throws SQLException {
		String selectStr = "select * from disaster_event.\"EMS\" where \"id\"='" + this.id + "' limit 1";
		Statement stmt = ParaSetting.connection.createStatement();
		ResultSet rs = stmt.executeQuery(selectStr);
		boolean checkHasRecord = rs.next();
		stmt.close();
		if (checkHasRecord == true) {
			return false;
		} else {
			return true;
		}
	}

	public static EMSDisaster readFromString(String dataLine) {
		String[] dataArray = dataLine.split("\t");
		EMSDisaster disaster = new EMSDisaster();
		disaster.id = dataArray[0];
		disaster.longitude = Double.valueOf(dataArray[1]);
		disaster.latitude = Double.valueOf(dataArray[2]);
		disaster.startCalendar = TimeConverter.getCalendarFromString(dataArray[3]);
		disaster.url = dataArray[4];
		disaster.disasterType = DisasterType.getDisasterTypeFromString(dataArray[5]);
		disaster.title = dataArray[6];
		disaster.activationCalendar = TimeConverter.getCalendarFromString(dataArray[7]);
		return disaster;
	}
}
