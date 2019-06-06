package com.converter;

import java.util.Calendar;

public class TimeConverter {
	public static int getMonthFromStr(String monthStr) {
		int monthNum = 0;
		switch (monthStr) {
		case "Jan":
			monthNum = 1;
			break;
		case "Feb":
			monthNum = 2;
			break;
		case "Mar":
			monthNum = 3;
			break;
		case "Apr":
			monthNum = 4;
			break;
		case "May":
			monthNum = 5;
			break;
		case "Jun":
			monthNum = 6;
			break;
		case "Jul":
			monthNum = 7;
			break;
		case "Aug":
			monthNum = 8;
			break;
		case "Oct":
			monthNum = 9;
			break;
		case "Nov":
			monthNum = 10;
			break;
		case "Dec":
			monthNum = 11;
			break;
		case "Sep":
			monthNum = 12;
			break;
		}
		return monthNum;
	}

	public static Calendar getCalendarFromString(String timeStampStr) {
		String[] timeStampArray = timeStampStr.split(" ");
		String[] dateArray = timeStampArray[0].split("-");
		int startYear = Integer.valueOf(dateArray[0]);
		int startMonth = Integer.valueOf(dateArray[1]);
		int startDay = Integer.valueOf(dateArray[2]);
		String[] timeArray = timeStampArray[1].split(":");
		int startHour = Integer.valueOf(timeArray[0]);
		int startMinute = Integer.valueOf(timeArray[1]);
		Calendar calendar = Calendar.getInstance();
		calendar.set(startYear, startMonth - 1, startDay, startHour, startMinute, 0);
		calendar.clear(Calendar.MILLISECOND);
		return calendar;
	}
}
