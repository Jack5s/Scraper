package com.webscraper.jsoup;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import com.converter.TimeConverter;
import com.disaster.DisasterType;
import com.disaster.GDACSDisaster;

import Common.ParaSetting;

public class GDACSWebScraper {
	public GDACSWebScraper() throws Exception {
		Document doc = Jsoup.connect("http://www.gdacs.org/datareport/resources/").maxBodySize(0).get();
		// http://www.gdacs.org/report.aspx?eventid=1013150&eventtype=DR
		Element preElement = doc.getElementsByTag("pre").first();
		Elements aElements = preElement.getElementsByTag("a");
		for (Element element : aElements) {
			try {
				String disasterTypeStr = element.text();
				String disasterArrayLinkStr = "http://www.gdacs.org" + element.attr("href");

				if (disasterTypeStr.compareTo("DR") == 0 || disasterTypeStr.compareTo("EQ") == 0
						|| disasterTypeStr.compareTo("FL") == 0 || disasterTypeStr.compareTo("TC") == 0
						|| disasterTypeStr.compareTo("VO") == 0 || disasterTypeStr.compareTo("VW") == 0) {
					Document disasterDoc = Jsoup.connect(disasterArrayLinkStr).maxBodySize(0).get();
					Element disasterPreElement = disasterDoc.getElementsByTag("pre").first();
					Elements disasteraElements = disasterPreElement.getElementsByTag("a");
					for (Element disasterElement : disasteraElements) {
						if (disasterElement.text().compareTo("[To Parent Directory]") == 0) {
							continue;
						}
						String id = disasterElement.text();

						GDACSDisaster disaster = new GDACSDisaster();
						disaster.id = disasterTypeStr + " " + id;
						if (ParaSetting.filterFlag == false) {
							boolean checkResult = disaster.checkCanInsert();
							if (checkResult == false) {
								continue;
							}
						}
						disaster.url = "http://www.gdacs.org/datareport/resources/" + disasterTypeStr + "/" + id + "/";
						switch (disasterTypeStr) {
						case "DR":
							disaster.disasterType = DisasterType.Drought;
							break;
						case "EQ":
							disaster.disasterType = DisasterType.Earthquake;
							break;
						case "FL":
							disaster.disasterType = DisasterType.Flood;
							break;
						case "TC":
							disaster.disasterType = DisasterType.Storm;
							break;
						case "VO":
							disaster.disasterType = DisasterType.Volcano;
							break;
						case "VW":
							disaster.disasterType = DisasterType.Storm;
							break;
						default:
							disaster = null;
							break;
						}
						if (ParaSetting.filterFlag == true && ParaSetting.filterDisasterType.compareTo("All") != 0) {
							if (disaster.disasterType.toString().compareTo(ParaSetting.filterDisasterType) != 0) {
								disaster = null;
								continue;
							}
						}
						String xmlLink = "http://www.gdacs.org/datareport/resources/" + disasterTypeStr + "/" + id
								+ "/rss_" + id + ".xml";
						boolean checkResult = getDisaster(xmlLink, disaster);
						System.out.println(disaster.url + ": " + checkResult);
						if (checkResult == true) {
							if (ParaSetting.filterFlag == false) {
								disaster.insertIntoDataBase();
							} else {
								disaster.wirteToFile(ParaSetting.outputGDACSFilePath);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		}
	}

	@SuppressWarnings("finally")
	private boolean getDisaster(String xmlLink, GDACSDisaster disaster) throws Exception {
		Document xmlDocument = null;
		// The links of some record is special
		try {
			InputStream inputStream = new URL(xmlLink).openStream();
			xmlDocument = Jsoup.parse(inputStream, null, "", Parser.xmlParser());
		} catch (IOException e) {
			String parentLinkStr = xmlLink.substring(0, xmlLink.lastIndexOf('/'));
			Document doc = Jsoup.connect(parentLinkStr).maxBodySize(0).get();
			Elements aElements = doc.getElementsByTag("a");
			for (Element element : aElements) {
				String text = element.text();
				if (text.substring(0, 3).compareTo("rss") == 0
						&& text.substring(text.length() - 3, text.length()).compareTo("xml") == 0) {
					xmlLink = "http://www.gdacs.org" + element.attr("href");
					break;
				}
			}
			xmlDocument = Jsoup.parse(new URL(xmlLink).openStream(), null, "", Parser.xmlParser());
		} finally {
			if (xmlDocument == null) {
				System.err.println(xmlLink);
				return false;
			}
			xmlDocument = Jsoup.parse(new URL(xmlLink).openStream(), null, "", Parser.xmlParser());
			disaster.longitude = Double.valueOf(xmlDocument.getElementsByTag("geo:long").first().text());
			disaster.latitude = Double.valueOf(xmlDocument.getElementsByTag("geo:lat").first().text());
			disaster.startCalendar = getCalendarFromString(
					xmlDocument.getElementsByTag("gdacs:fromdate").first().text());
			disaster.endCalendar = getCalendarFromString(xmlDocument.getElementsByTag("gdacs:todate").first().text());
			if (ParaSetting.filterFlag == true) {
				if ((disaster.startCalendar.compareTo(ParaSetting.startCalendar) < 0
						|| disaster.startCalendar.compareTo(ParaSetting.endCalendar) > 0)
						&& (disaster.endCalendar.compareTo(ParaSetting.startCalendar) < 0
								|| disaster.endCalendar.compareTo(ParaSetting.endCalendar) > 0)) {
					return false;
				}
			}

			disaster.title = xmlDocument.getElementsByTag("item").first().children().select("title").first().text();
			if (disaster.title.compareTo("No title for this event") == 0) {
				disaster.title = "";
			}
			// some record don't have score
			try {
				disaster.score = Float.valueOf(xmlDocument.getElementsByTag("gdacs:alertscore").first().text());
			} catch (Exception e) {
				disaster.score = -999;
			}
			return true;
		}
	}

	// String Format: Sun, 21 May 2017 10:57:08 GMT
	private Calendar getCalendarFromString(String timeStr) {
		Calendar calendar = Calendar.getInstance();
		String[] dateStrArray = timeStr.split(" ");
		int day = Integer.valueOf(dateStrArray[1]);
		int month = TimeConverter.getMonthFromStr(dateStrArray[2]);
		int year = Integer.valueOf(dateStrArray[3]);

		String[] timeStrArr = dateStrArray[4].split(":");
		int hour = Integer.valueOf(timeStrArr[0]);
		int minute = Integer.valueOf(timeStrArr[1]);
		int second = Integer.valueOf(timeStrArr[2]);
		calendar.set(year, month - 1, day, hour, minute, second);
		return calendar;
	}
}
