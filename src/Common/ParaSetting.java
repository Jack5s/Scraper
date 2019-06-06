package Common;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Calendar;
import java.util.Properties;

import com.converter.TimeConverter;

public class ParaSetting {
	// if filterFlag is true, the filter will be used and the selected disasters
	// event will be export to a file
	// if filterFlag is false, the filter will not be used and the disasters will be
	// inserted into database
	public static boolean filterFlag;
	public static String filterDisasterType;
	public static Calendar startCalendar;
	public static Calendar endCalendar;
	public static Connection connection;

	public static String outputEMSFilePath;
	public static String outputGDACSFilePath;
	public static String outputGDELTFilePath;
	public static String outputReliefFilePath;
	static {
		InputStream inputStream;
		try {
			inputStream = new FileInputStream("config.properties");
			Properties properties = new Properties();
			// load a properties file
			properties.load(inputStream);
			String databasePath = properties.getProperty("db.localPath");
			System.out.println(databasePath);
//			databasePath ="C:\\Users\\s1061395\\Project\\Java\\t.txt";
			BufferedReader bufferedReader = new BufferedReader(new FileReader(databasePath));
			String url= bufferedReader.readLine();
			String userName = bufferedReader.readLine();
			String password = bufferedReader.readLine();
			bufferedReader.close();
			connection = DriverManager.getConnection(url, userName, password);

			String limitStr = properties.getProperty("filter.flag");
			if (limitStr.compareTo("True") == 0) {
				filterFlag = true;
			} else if (limitStr.compareTo("False") == 0) {
				filterFlag = false;
			} else {
				throw new Exception("filter.flag cvalue is wrong!");
			}
			filterDisasterType = properties.getProperty("filter.disasterType");
			startCalendar = TimeConverter.getCalendarFromString(properties.getProperty("filter.startTime"));
			endCalendar = TimeConverter.getCalendarFromString(properties.getProperty("filter.endTime"));
			if (startCalendar.compareTo(endCalendar) > 0) {
				throw new Exception("filter.startTime must be lower than filter.endTime!");
			}
			outputEMSFilePath = properties.getProperty("output.ems");
			outputGDACSFilePath = properties.getProperty("output.gdacs");
			outputGDELTFilePath = properties.getProperty("output.gdelt");
			outputReliefFilePath = properties.getProperty("output.relief");
			System.out.println("Parameters Setting is finished");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
