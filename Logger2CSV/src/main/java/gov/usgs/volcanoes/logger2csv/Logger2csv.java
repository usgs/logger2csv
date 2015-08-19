package gov.usgs.volcanoes.logger2csv;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martiansoftware.jsap.JSAPException;

import gov.usgs.util.ConfigFile;

/**
 * Hello world!
 *
 */
public class Logger2csv {
	public static final String DEFAULT_CONFIG_FILENAME = "logger2csv.config";

	/** my logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(Logger2csv.class);

	/** my configuration file */
	private ConfigFile configFile;

	/**
	 * Class constructor
	 * 
	 * @param configFile
	 *            my config file
	 */
	public Logger2csv(ConfigFile configFile) {

		this.configFile = configFile;
		long now = System.currentTimeMillis();
		configFile.put("applicationLaunch", "" + now);
		LOGGER.info("Launching Logger2csv ({})", Logger2csvVersion.VERSION_STRING);
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

	}

}
