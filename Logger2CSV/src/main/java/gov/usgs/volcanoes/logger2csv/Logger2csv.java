package gov.usgs.volcanoes.logger2csv;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martiansoftware.jsap.JSAPException;
import com.opencsv.CSVReader;

import gov.usgs.util.ConfigFile;

/**
 * Hello world!
 *
 */
public class Logger2csv {
	public static final String DEFAULT_CONFIG_FILENAME = "logger2csv.config";

	private static final int DAY_TO_S = 24 * 60 * 60;
	
	/** my logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(Logger2csv.class);

	/** my configuration file */
	private ConfigFile configFile;

	private List<DataLogger> loggers;

	/**
	 * Class constructor
	 * 
	 * @param configFile
	 *            my config file
	 * @throws FileNotFoundException
	 * @throws JSONException
	 */
	public Logger2csv(ConfigFile configFile) throws JSONException {

		this.configFile = configFile;
		long now = System.currentTimeMillis();
		configFile.put("applicationLaunch", "" + now);
		LOGGER.info("Launching Logger2csv ({})", Logger2csvVersion.VERSION_STRING);

		loggers = getLoggers();
	}

	private List<DataLogger> getLoggers() {
		List<DataLogger> loggers = new ArrayList<DataLogger>();

		for (String station : configFile.getList("station")) {
			ConfigFile config = configFile.getSubConfig(station, true);
			config.put("name", station);
			try {
				loggers.add(new DataLogger(config));
			} catch (UnknownHostException e) {
				LOGGER.error("Cannot find host \"{}\". I'll skip it this time", config.getString("address"));
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
			}
		}
		return loggers;
	}

	public static void createConfig() throws IOException {

		InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("logger2csv-example.config");
		FileOutputStream os = new FileOutputStream(DEFAULT_CONFIG_FILENAME);
		try {
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			is.close();
			os.close();
		}
	}

	public void poll(DataLogger logger, String table) throws IOException {
		FileDataReader fileReader = new FileDataReader(logger, table);
		WebDataReader webReader = new WebDataReader(logger, table);
		FileDataWriter fileWriter = new FileDataWriter(logger, table);
		LOGGER.debug("Polling {}.{}", logger.name, table);
		try {
			int lastRecord = fileReader.findLastRecord(table);
			Iterator<String[]> results;
			if (lastRecord > 0)
				results = webReader.since_record(lastRecord);
			else
				results = webReader.backFill(logger.maxAge * DAY_TO_S);
			fileWriter.write(results, lastRecord);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void pollAll() {
		for (DataLogger l : loggers) {
			Iterator<String> it = l.getTableIterator();
			while (it.hasNext()) {
				String table = it.next();
				try {
					poll(l, table);
				} catch (IOException e) {
					LOGGER.error("Can't find most recent record for logger {}. Is the file corrutp? I'll skip it this time.", l.name);
				}
			}
		}
	}

	public static void main(String[] args) {
		Args config = null;
		try {
			config = new Args(args);
		} catch (JSAPException e1) {
			System.err.println("Couldn't parse command line. Try using the --help flag.");
			System.exit(1);
		}

		if (config.createConfig) {
			try {
				LOGGER.warn("Creating example config " + DEFAULT_CONFIG_FILENAME);
				Logger2csv.createConfig();
			} catch (IOException e) {
				LOGGER.warn("Cannot write example config. " + e.getLocalizedMessage());
			}
			System.exit(0);
		}

		ConfigFile cf = new ConfigFile(config.configFileName);
		if (!cf.wasSuccessfullyRead()) {
			LOGGER.warn("Can't parse config file " + config.configFileName + ". Try using the --help flag.");
			System.exit(1);
		}

		Logger2csv logger2csv = null;
		try {
			logger2csv = new Logger2csv(cf);
		} catch (JSONException e) {
			LOGGER.error("Cannot parse bookmarks file.");
			System.exit(1);
		}
		logger2csv.pollAll();
	}

}
