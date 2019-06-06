package com.webscraper.jsoup;

import java.util.Calendar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.converter.TimeConverter;
import com.disaster.DisasterType;
import com.disaster.ReliefDisaster;
import com.google.gson.Gson;

import Common.ParaSetting;

public class ReliefWebScraper {
	public ReliefWebScraper() throws Exception {
		Document disasterDoc = Jsoup.connect("https://reliefweb.int/disasters").maxBodySize(0).get();
		Element headElement = disasterDoc.getElementsByTag("head").first();
		Elements scriptElements = headElement.getElementsByTag("script");
		for (Element element : scriptElements) {
			String scriptStr = element.html();
			int beginIndex = scriptStr.indexOf("\"data\":");
			int endIndex = scriptStr.lastIndexOf(';') - 4;
			if (beginIndex == -1 || endIndex == -1) {
				continue;
			}
			scriptStr = scriptStr.substring(beginIndex, endIndex);
			scriptStr = "{" + scriptStr + "}";
			System.out.println(scriptStr);
			Gson gson = new Gson();
			ReliefGeoJson reliefGeoJson = gson.fromJson(scriptStr, ReliefGeoJson.class);
			for (int i = 0; i < reliefGeoJson.data.features.length; i++) {
				ReliefDisaster disaster = new ReliefDisaster();
				String iconStr = reliefGeoJson.data.features[i].properties.icon;
				disaster.disasterType = DisasterType.getDisasterTypeFromString(iconStr);
				if (ParaSetting.filterFlag == true && ParaSetting.filterDisasterType.compareTo("All") != 0) {
					if (disaster.disasterType.toString().compareTo(ParaSetting.filterDisasterType) != 0) {
						continue;
					}
				}
				disaster.longitude = reliefGeoJson.data.features[i].features[0].geometry.coordinates[0];
				disaster.latitude = reliefGeoJson.data.features[i].features[0].geometry.coordinates[1];
				String disasterLink = reliefGeoJson.data.features[i].properties.url;
				disaster.url = disasterLink;
				boolean checkResult = getAttributes(disasterLink, disaster);
				System.out.println(disaster.url + ": " + checkResult);
				if (checkResult == true) {
					if (ParaSetting.filterFlag == false) {
						disaster.insertIntoDataBase();
					} else {
						disaster.wirteToFile(ParaSetting.outputReliefFilePath);
					}
				}
			}
			break;
		}
	}

	private boolean getAttributes(String disasterLink, ReliefDisaster disaster) throws Exception {
		Document doc = Jsoup.connect(disasterLink).maxBodySize(0).get();
		Element idElement = doc.getElementsByAttributeValue("class", "glide").first();
		String idStr = idElement.text();
		idStr = idStr.substring(7);
		disaster.id = idStr;
		if (ParaSetting.filterFlag == false) {
			boolean checkResult = disaster.checkCanInsert();
			if (checkResult == false) {
				return false;
			}
		}

		Element divElement = doc.getElementsByAttributeValue("class", "profile-sections-description").first();
		Elements aElements = divElement.getElementsByTag("a");
		for (Element element : aElements) {
			String content = element.text();
			// System.out.println(content);
			int beginIndex = content.lastIndexOf('(');
			int endIndex = content.lastIndexOf(')');
			String dateStr = "";
			if (beginIndex == -1 || endIndex == -1) {
				dateStr = content;
			} else {
				dateStr = content.substring(beginIndex, endIndex);
			}
			int commaIndex = dateStr.indexOf(',');
			if (commaIndex == -1) {
				continue;
			}
			dateStr = dateStr.substring(commaIndex + 2);
			// System.out.println(dateStr);
			String[] dateArray = dateStr.split(" ");
			if (dateArray.length != 3) {
				continue;
			}
			int startDay = Integer.valueOf(dateArray[0]);
			int startMonth = TimeConverter.getMonthFromStr(dateArray[1]);
			int startYear = Integer.valueOf(dateArray[2]);
			disaster.startCalendar.set(startYear, startMonth, startDay, 0, 0, 0);
			disaster.startCalendar.clear(Calendar.MILLISECOND);
			if (ParaSetting.filterFlag == true) {
				if (disaster.startCalendar.compareTo(ParaSetting.startCalendar) < 0
						|| disaster.startCalendar.compareTo(ParaSetting.endCalendar) > 0) {
					return false;
				} else {
					break;
				}
			}
		}
		disaster.title = doc.getElementsByTag("h1").first().text();
		return true;
	}
}
