package com.disaster;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Calendar;

//UTC time
public abstract class Disaster {
	public String id;
	public double longitude;
	public double latitude;
	public Calendar startCalendar;
	public String url;
	public DisasterType disasterType;
	public String title;

	public Disaster() {
		this.longitude = -999;
		this.latitude = -999;
		this.startCalendar = Calendar.getInstance();
		this.title = "";
	}

	@Override
	public String toString() {
		Timestamp timestamp = new Timestamp(startCalendar.getTimeInMillis());
		String dataLine = this.id + "\t" + this.longitude + "\t" + this.latitude + "\t" + timestamp.toString() + "\t"
				+ this.url + "\t" + this.disasterType + "\t" + this.title + "\t";
		return dataLine;
	}
}