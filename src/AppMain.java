import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import com.disaster.Disaster;
import com.disaster.EMSDisaster;
import com.disaster.GDACSDisaster;
import com.disaster.GDELTDisaster;
import com.disaster.IDataBaseOperation;
import com.disaster.ReliefDisaster;
import com.webscraper.jsoup.EMSWebScraper;
import com.webscraper.jsoup.GDACSWebScraper;
import com.webscraper.jsoup.GDELTScraper;
import com.webscraper.jsoup.ReliefWebScraper;

import Common.ParaSetting;

public class AppMain {

	public static void main(String[] args) {
		try {
			// to show whether use filter and initialize the paraSetting class, don't remove
			System.out.println("Filter status: " + ParaSetting.filterFlag);
//			 scraper("EMS");
//			 scraper("GDACS");
			 scraper("GDELT");
//			 scraper("Relief");

			 
//			insertFileintoDatabase(ParaSetting.outputEMSFilePath, "EMS");
//			 insertFileintoDatabase(ParaSetting.outputGDACSFilePath, "GDACS");
//			 insertFileintoDatabase(ParaSetting.outputGDELTFilePath, "GDELT");
//			 insertFileintoDatabase(ParaSetting.outputReliefFilePath, "Relief");
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("ok!!");
	}

	private static void scraper(String webStr) throws Exception {
		if (ParaSetting.filterFlag == true) {
			PrintWriter printWriter;
			printWriter = new PrintWriter(new FileWriter(ParaSetting.outputEMSFilePath));
			printWriter.print("");
			printWriter.close();
			printWriter = new PrintWriter(new FileWriter(ParaSetting.outputGDACSFilePath));
			printWriter.print("");
			printWriter.close();
			printWriter = new PrintWriter(new FileWriter(ParaSetting.outputGDELTFilePath));
			printWriter.print("");
			printWriter.close();
			printWriter = new PrintWriter(new FileWriter(ParaSetting.outputReliefFilePath));
			printWriter.print("");
			printWriter.close();
		}
		System.out.println(webStr);
		try {
			switch (webStr) {
			case "EMS":
				EMSWebScraper emsWebScraper = new EMSWebScraper();
				break;
			case "GDACS":
				GDACSWebScraper gdacsWebScraper = new GDACSWebScraper();
				break;
			case "GDELT":
				GDELTScraper gdeltScraper = new GDELTScraper();
				break;
			case "Relief":
				ReliefWebScraper reliefWebScraper = new ReliefWebScraper();
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// read disaster array from files and insert these disasters into dataBase
	private static void insertFileintoDatabase(String filePath, String webSource) throws Exception {
		BufferedReader bufferedReader;
		bufferedReader = new BufferedReader(new FileReader(filePath));
		String dataLine = bufferedReader.readLine();
		while (dataLine != null) {
			IDataBaseOperation iDataBaseOperation = null;
			switch (webSource) {
			case "EMS":
				iDataBaseOperation = EMSDisaster.readFromString(dataLine);
				break;
			case "GDACS":
				iDataBaseOperation = GDACSDisaster.readFromString(dataLine);
				break;
			case "GDELT":
				iDataBaseOperation = GDELTDisaster.readFromString(dataLine);
				break;
			case "Relief":
				iDataBaseOperation = ReliefDisaster.readFromString(dataLine);
				break;
			default:
				break;
			}
			boolean checkResult = iDataBaseOperation.checkCanInsert();
			if (checkResult == true) {
				Disaster disaster = (Disaster)iDataBaseOperation;
				System.out.println(disaster.url + ": " + checkResult);
				iDataBaseOperation.insertIntoDataBase();
			}
			dataLine = bufferedReader.readLine();
		}
		bufferedReader.close();
	}
}
