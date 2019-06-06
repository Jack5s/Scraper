package com.webscraper.jsoup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.disaster.DisasterType;
import com.disaster.GDELTDisaster;

import Common.ParaSetting;

public class GDELTScraper {
	private String url = "http://data.gdeltproject.org/gdeltv2/masterfilelist.txt";
	private String gkgZIPDownloadPath = "C:/Temp";
	private String gkgCSVDownloadPath = "C:/Temp";
	private String gkgTotalCSVPath = "C:/Temp";

	public GDELTScraper() throws Exception {
		scrpeFromWeb();
	}

	private void scrpeFromWeb() throws Exception {

		Document doc = Jsoup.connect(url).maxBodySize(0).timeout(0).get();
		StringReader stringReader = new StringReader(doc.wholeText());
		BufferedReader bufferedReader = new BufferedReader(stringReader);
		Stack<String> dataLineStack = new Stack<>();
		String dataLine = bufferedReader.readLine();
		while (dataLine != null) {
			dataLineStack.push(dataLine);
			dataLine = bufferedReader.readLine();
		}
		bufferedReader.close();
		stringReader.close();
		System.out.println("Stack Finished");

		dataLine = dataLineStack.pop();
		while (dataLine != null) {
			String[] dataArray = dataLine.split(" ");
			if (dataArray.length != 3) {
				if (dataLineStack.isEmpty() == true) {
					break;
				}
				dataLine = dataLineStack.pop();
				continue;
			}

			GDELTDisaster disaster = new GDELTDisaster();
			disaster.id = dataArray[0];
			String downloadUrl = dataArray[2];
			String fileName = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1, downloadUrl.lastIndexOf('.'));
			String[] strArray = fileName.split("\\.");
			String fileType = strArray[1].toLowerCase();
			String downloadZIPFileName = "";
			String csvFileName = "";

			// filter with time
			Calendar fileCalendar = Calendar.getInstance();
			String timeStr = fileName.substring(0, 14);
			fileCalendar = getCalendarFromString(timeStr);
			if (ParaSetting.filterFlag == true) {
				if (fileCalendar.compareTo(ParaSetting.startCalendar) < 0
						|| fileCalendar.compareTo(ParaSetting.endCalendar) > 0) {
					if (dataLineStack.isEmpty() == true) {
						break;
					}
					dataLine = dataLineStack.pop();
					continue;
				}
			}
			switch (fileType) {
			case "export":

				break;
			case "mentions":

				break;
			case "gkg":
				downloadZIPFileName = gkgZIPDownloadPath + "/" + fileName + ".zip";
				csvFileName = gkgCSVDownloadPath + "/" + fileName + ".csv";
				boolean checkFileCanInsert = true;

				if (ParaSetting.filterFlag == false) {
					checkFileCanInsert = checkFileList(fileName);
				} else {
					checkFileCanInsert = true;
				}
				if (checkFileCanInsert == true) {
					boolean checkDownloadFile = downloadFile(downloadUrl, downloadZIPFileName);
					if (checkDownloadFile == true) {
						unzipFile(downloadZIPFileName, csvFileName);
						exportGKGFile(fileName);
						if (ParaSetting.filterFlag == false) {
							insertFileName(fileName, "GDELTFileName");
						}
						// delete file
						File zipFile = new File(downloadZIPFileName);
						boolean checkZIPFile = zipFile.delete();
						if (checkZIPFile == false) {
							System.err.println("Fail to delete " + downloadZIPFileName);
						}
						File csvFile = new File(gkgCSVDownloadPath + "/" + fileName + ".csv");
						boolean checkcsvFile = csvFile.delete();
						if (checkcsvFile == false) {
							System.err.println("Fail to delete " + gkgCSVDownloadPath + "/" + fileName + ".csv");
						}
					}
				}
				break;
			default:
				break;
			}
			if (dataLineStack.isEmpty() == true) {
				break;
			}
			dataLine = dataLineStack.pop();
		}
	}

	private boolean downloadFile(String downloadUrl, String downloadZIPFileName) throws IOException {
		InputStream in = new URL(downloadUrl).openStream();
		try {
			// InputStream in = new URL(downloadUrl).openStream();
			Files.copy(in, Paths.get(downloadZIPFileName), StandardCopyOption.REPLACE_EXISTING);

			return true;
		} catch (IOException e) {
			in.close();
			e.printStackTrace();
			return false;
		}
	}

	private void unzipFile(String downloadZIPFileName, String dataFileName) throws IOException {
		byte[] buffer = new byte[1024];
		ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(downloadZIPFileName));
		try {
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			while (zipEntry != null) {
				File newFile = new File(dataFileName);
				FileOutputStream fileOutputStream = new FileOutputStream(newFile);
				int len = zipInputStream.read(buffer);
				while (len > 0) {
					fileOutputStream.write(buffer, 0, len);
					len = zipInputStream.read(buffer);
				}
				fileOutputStream.close();
				zipEntry = zipInputStream.getNextEntry();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			zipInputStream.closeEntry();
			zipInputStream.close();
		}
	}

	private void exportGKGFile(String gkgFileName) throws Exception {
		BufferedReader bufferedReader = new BufferedReader(
				new FileReader(gkgCSVDownloadPath + "/" + gkgFileName + ".csv"));
		String dataLine = bufferedReader.readLine();
		String totalFileName = gkgTotalCSVPath + "/" + gkgFileName + ".txt";
		PrintWriter clearWriter = new PrintWriter(totalFileName);
		clearWriter.close();
		while (dataLine != null) {
			String[] dataArray = dataLine.split("\t");
			String V1THEMES = null;
			try {
				V1THEMES = dataArray[7].toLowerCase();
			} catch (Exception e) {
				dataLine = bufferedReader.readLine();
			}
			if (V1THEMES != null) {
				// gdelt data has two field for storing the themes, V1THEMES and
				// V2ENHANCEDTHEMES
				// V2GCAM field is the keyword of the content (it is not very accurate)
				// indexArray1 and indexArray2 is the indexes of the keyword "natural_disaster"
				// index3 is the indexes of the keyword "c18.156"(means natural_disaster) in
				// V2GCAM
				int[] indexArray1 = getAllIndex(V1THEMES, "natural_disaster");
				String V2ENHANCEDTHEMES = dataArray[8].toLowerCase();
				int[] indexArray2 = getAllIndex(V2ENHANCEDTHEMES, "natural_disaster");
				String V2GCAM = dataArray[17].toLowerCase();
				int index3 = V2GCAM.indexOf("c18.156");

				if (indexArray1.length > 0 && indexArray2.length > 0 && index3 >= 0) {
					BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(totalFileName, true));
					bufferedWriter.write(dataLine + "\r\n");
					bufferedWriter.close();

					GDELTDisaster disaster = new GDELTDisaster();
					disaster.id = dataArray[0];
					String dateTimeStr = dataArray[1];
					disaster.startCalendar = getCalendarFromString(dateTimeStr);
					// if (ParaSetting.filterFlag == true) {
					// if (disaster.startCalendar.compareTo(ParaSetting.startCalendar) < 0
					// || disaster.startCalendar.compareTo(ParaSetting.endCalendar) > 0) {
					// dataLine = bufferedReader.readLine();
					// continue;
					// }
					// }
					disaster.url = dataArray[4];

					String locationStr = dataArray[9];
					if (locationStr.length() > 0) {
						String[] locationArray = locationStr.split("#");
						String latitudeStr = locationArray[4];
						String longitudeStr = locationArray[5];
						if (latitudeStr.length() > 0 && longitudeStr.length() > 0) {
							disaster.latitude = Double.valueOf(latitudeStr);
							disaster.longitude = Double.valueOf(longitudeStr);
						} else {
						}
					}
					String[] disasterTypeStrArray = getDisasterTypeString(V1THEMES, indexArray1, "natural_disaster");
					for (int i = 0; i < disasterTypeStrArray.length; i++) {
						String disasterTypeStr = disasterTypeStrArray[i];
						disaster.disasterType = DisasterType.getDisasterTypeFromString(disasterTypeStr);
						if (disaster.disasterType != DisasterType.SpecialEvent) {
							break;
						}
					}
					// filter with Disaster Type
					if (ParaSetting.filterFlag == true && ParaSetting.filterDisasterType.compareTo("All") != 0) {
						if (disaster.disasterType.toString().compareTo(ParaSetting.filterDisasterType) != 0) {
							dataLine = bufferedReader.readLine();
							continue;
						}
					}

					try {
						Document doc = Jsoup.connect(disaster.url).maxBodySize(0).timeout(50000).get();
						disaster.title = doc.title();
					} catch (Exception e) {
						disaster.title = "";
					} finally {
						System.out.println(disaster.url);
						if (ParaSetting.filterFlag == false) {
							disaster.insertIntoDataBase();
						} else {

							disaster.wirteToFile(ParaSetting.outputGDELTFilePath);
						}
						dataLine = bufferedReader.readLine();
					}
				} else {
					dataLine = bufferedReader.readLine();
				}
			}
		}
		bufferedReader.close();
	}

	private int[] getAllIndex(String str, String fitStr) {
		ArrayList<Integer> indexList = new ArrayList<>();
		int index = str.indexOf(fitStr);
		while (index >= 0) {
			indexList.add(Integer.valueOf(index));
			index = str.indexOf(fitStr, index + fitStr.length());
		}
		int[] indexArray = new int[indexList.size()];
		for (int i = 0; i < indexArray.length; i++) {
			indexArray[i] = indexList.get(i).intValue();
		}
		return indexArray;
	}

	private String[] getDisasterTypeString(String valueStr, int[] indexArray, String fitStr) {
		ArrayList<String> disasterTypeStrList = new ArrayList<>();
		String disasterTypeStr = "";
		for (int i = 0; i < indexArray.length; i++) {
			String str = valueStr.substring(indexArray[i] + fitStr.length(), valueStr.indexOf(';', indexArray[i]));
			if (str.length() > 0) {
				disasterTypeStr = str.substring(1, str.length());
				disasterTypeStrList.add(disasterTypeStr);
			}
		}
		String[] disasterTypeStrArray = new String[disasterTypeStrList.size()];
		disasterTypeStrList.toArray(disasterTypeStrArray);
		return disasterTypeStrArray;
	}

	// insert the whole text into database, only for gdelt
	public static void insertFileName(String fileName, String tableName) throws SQLException {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		PreparedStatement preparedStatement = ParaSetting.connection
				.prepareStatement("insert into disaster_event.\"" + tableName + "\" values (?,?);");
		preparedStatement.setString(1, fileName);
		preparedStatement.setTimestamp(2, timestamp);
		preparedStatement.executeUpdate();
		preparedStatement.close();
	}

	// insert the whole text into database, only for gdelt, not use now
	public static void insertTextContent(String id, String content, String tableName) throws SQLException {
		PreparedStatement preparedStatement = ParaSetting.connection
				.prepareStatement("insert into disaster_event.\"" + tableName + "\" values (?,?);");
		preparedStatement.setString(1, id);
		preparedStatement.setString(2, content);
		preparedStatement.executeUpdate();
		preparedStatement.close();
	}

	// check the whether the file can be insert into database, only for GDELT
	public static boolean checkFileList(String fileName) throws SQLException {
		String select = "select * from disaster_event.\"GDELTFileName\" where \"fileName\"='" + fileName + "';";
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

	// String Format: 20190203120000
	private Calendar getCalendarFromString(String timeStr) {
		int fileYear = Integer.valueOf(timeStr.substring(0, 4));
		int fileMonth = Integer.valueOf(timeStr.substring(4, 6));
		int fileDay = Integer.valueOf(timeStr.substring(6, 8));
		int fileHour = Integer.valueOf(timeStr.substring(8, 10));
		int fileMinute = Integer.valueOf(timeStr.substring(10, 12));
		Calendar calendar = Calendar.getInstance();
		calendar.set(fileYear, fileMonth - 1, fileDay, fileHour, fileMinute, 0);
		calendar.clear(Calendar.MILLISECOND);
		return calendar;
	}
}
