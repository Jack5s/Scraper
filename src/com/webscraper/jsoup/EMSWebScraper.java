package com.webscraper.jsoup;

import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.converter.TimeConverter;
import com.disaster.DisasterType;
import com.disaster.EMSDisaster;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import Common.ParaSetting;

public class EMSWebScraper {
	private String EMSUrl = "https://emergency.copernicus.eu";
	private boolean findFlag = false;
	private int bufferCount = 10;

	public EMSWebScraper() throws Exception {
		Document doc = Jsoup.connect("https://emergency.copernicus.eu/mapping/list-of-activations-rapid").maxBodySize(0)
				.get();
		Element tableEle = doc.getElementsByAttributeValue("class", "views-table cols-7").first();
		Elements trElements = tableEle.getElementsByTag("tbody").first().getElementsByTag("tr");
		for (Element rowElement : trElements) {
			EMSDisaster disaster = new EMSDisaster();
			disaster.title = rowElement.child(2).text().trim();
			String disasterLink = EMSUrl + rowElement.child(2).child(0).attributes().get("href");
			disaster.url = disasterLink;
			String diasaterId = rowElement.child(1).text().trim();
			disaster.id = diasaterId;
			if (ParaSetting.filterFlag == false) {
				boolean checkResult = disaster.checkCanInsert();
				if (checkResult == false) {
					continue;
				}
			}
			boolean checkResult = getAttributes(disasterLink, disaster);
			System.out.println(disaster.url + ": " + checkResult);
			if (checkResult == true) {
				findFlag = true;
				if (ParaSetting.filterFlag == false) {
					disaster.insertIntoDataBase();
				} else {
					disaster.wirteToFile(ParaSetting.outputEMSFilePath);
				}
			} else {
				if (findFlag == true)
					bufferCount++;
			}
			if (bufferCount > 10) {
				break;
			}
		}
	}

	private boolean getAttributes(String disasterLink, EMSDisaster disaster) throws Exception {
		Document disasterDoc = Jsoup.connect(disasterLink).maxBodySize(0).get();
		Element contentElement = disasterDoc
				.getElementsByAttributeValue("class", "obsolete-components-header--field-obsolete clearfix").first();
		Element rowElement = contentElement.child(2);
		// The first data row. The value can be event time or disaster type
		String checkStr = rowElement.child(0).text();
		String disasterTypeStr = "";
		if (checkStr.compareTo("Event Time (UTC):") == 0) {// The first row is event time
			String startTimeStr = rowElement.child(1).text();
			disaster.startCalendar = TimeConverter.getCalendarFromString(startTimeStr);
			// get disaster type
			rowElement = contentElement.child(4);
			disasterTypeStr = rowElement.child(1).text();
			disaster.disasterType = DisasterType.getDisasterTypeFromString(disasterTypeStr);
			if (ParaSetting.filterFlag == true && ParaSetting.filterDisasterType.compareTo("All") != 0) {
				if (disaster.disasterType.toString().compareTo(ParaSetting.filterDisasterType) != 0) {
					return false;
				}
			}
			// get Activation Time
			rowElement = contentElement.child(5);
			String activationTimeStr = rowElement.child(1).text();
			disaster.activationCalendar = TimeConverter.getCalendarFromString(activationTimeStr);
		} else if (checkStr.compareTo("Event Type:") == 0) {// The first row is disaster type
			rowElement = contentElement.child(3);
			String timeStr = rowElement.child(1).text();
			int spaceIndex = timeStr.indexOf(' ');
			disaster.activationCalendar = TimeConverter.getCalendarFromString(timeStr);

			rowElement = contentElement.child(2);// get disaster type
			disasterTypeStr = rowElement.child(1).text();
			disaster.disasterType = DisasterType.getDisasterTypeFromString(disasterTypeStr);
		}

		if (ParaSetting.filterFlag == true) {
			if ((disaster.startCalendar.compareTo(ParaSetting.startCalendar) < 0
					|| disaster.startCalendar.compareTo(ParaSetting.endCalendar) > 0)
					&& (disaster.activationCalendar.compareTo(ParaSetting.startCalendar) < 0
							|| disaster.activationCalendar.compareTo(ParaSetting.endCalendar) > 0)) {
				return false;
			}
		}

		if (disaster.disasterType == DisasterType.SpecialEvent) {
			Element titleElement = contentElement.child(0);
			String titleStr = titleElement.text().trim();
			disaster.disasterType = DisasterType.getDisasterTypeFromString(titleStr);
		}
		//

		// get Position, the position is based on WebMercator, so need to project it to
		// WGS84
		Element headElement = disasterDoc.getElementsByTag("head").first();
		Elements scriptElements = headElement.getElementsByTag("script");
		for (Element element : scriptElements) {
			String scriptStr = element.html();
			// sample: ["wkt":"GEOMETRYCOLLECTION(POINT(1525997.63605671 5994608.04079255))]
			int pointStartIndex = scriptStr.indexOf("POINT(");
			int pointEndIndex = scriptStr.indexOf(')', pointStartIndex);
			if (pointStartIndex == -1 || pointEndIndex == -1) {
				continue;
			}
			String pointPositionStr = scriptStr.substring(pointStartIndex + 6, pointEndIndex);
			String[] positionArray = pointPositionStr.split(" ");
			double x = Double.valueOf(positionArray[0]);
			double y = Double.valueOf(positionArray[1]);
			GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
			Coordinate coordinate = new Coordinate(x, y);
			Point point = geometryFactory.createPoint(coordinate);
			CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:3857");
			CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");
			MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
			Point resultPoint = (Point) JTS.transform(point, transform);
			disaster.latitude = resultPoint.getY();
			disaster.longitude = resultPoint.getX();
		}
		return true;
	}
}
