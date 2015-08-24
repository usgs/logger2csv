package gov.usgs.volcanoes.logger2csv;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martiansoftware.jsap.JSAPException;

import gov.usgs.volcanoes.util.configFile.ConfigFile;



/**
 * An application to write CSV files from a collection of remote data loggers.
 * 
 * @author Tom Parker
 * 
 *         I waive copyright and related rights in the this work worldwide
 *         through the CC0 1.0 Universal public domain dedication.
 *         https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */
public class Logger2csv {
	private static final String EXAMPLE_CONFIG_FILENAME = "logger2csv-example.config";
	public static final String DEFAULT_CONFIG_FILENAME = "logger2csv.config";
	public static final int DEFAULT_INTERVAL_M = 60 * 60;
	private static final Logger LOGGER = LoggerFactory.getLogger(Logger2csv.class);
	private static final int DAY_TO_S = 24 * 60 * 60;
	private static final int M_TO_MS = 60 * 1000;

	private ConfigFile configFile;
	private List<DataLogger> loggers;
	private int interval;
	
	/**
	 * Class constructor
	 * 
	 * @param configFile
	 *            my config file
	 * @throws FileNotFoundException
	 * @throws JSONException
	 */
	public Logger2csv(ConfigFile configFile) throws JSONException {
		LOGGER.info("Launching Logger2csv ({})", Logger2csvVersion.VERSION_STRING);

		this.configFile = configFile;
		this.interval = configFile.getInt("interval", DEFAULT_INTERVAL_M);
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
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			}
		}
		return loggers;
	}

	public static void createConfig() throws IOException {
		InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(EXAMPLE_CONFIG_FILENAME);
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
			int lastRecord = fileReader.findLastRecord();
			Iterator<String[]> results;
			if (lastRecord > 0)
				results = webReader.since_record(lastRecord);
			else
				results = webReader.backFill(logger.backfill * DAY_TO_S);
			try {
				fileWriter.write(results, lastRecord);
			} catch (ParseException e) {
				LOGGER.error("Cannot parse logger response. Skipping {}", logger.name);
				return;
			}

	}

	public void pollAllOnce() {
		for (DataLogger l : loggers) {
			Iterator<String> it = l.getTableIterator();
			while (it.hasNext()) {
				String table = it.next();
				try {
					poll(l, table);
				} catch (IOException e) {
					LOGGER.error(
							"Can't find most recent record for logger {}. Is the file corrutp? I'll skip it this time.",
							l.name);
				}
			}
		}
	}

	private void pollAllCountinous() {
		while(true) {
			pollAllOnce();
			try {
				Thread.sleep(interval * M_TO_MS);
			} catch (InterruptedException ignore) {
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

		Logger2csv logger2csv = new Logger2csv(cf);
		if (config.persistent)
			logger2csv.pollAllCountinous();
		else
		logger2csv.pollAllOnce();
	}
}