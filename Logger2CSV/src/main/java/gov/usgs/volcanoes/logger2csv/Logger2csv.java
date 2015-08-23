package gov.usgs.volcanoes.logger2csv;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
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

    private List<DataLogger> loggers;
    private Bookmarks bookmarks;

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
        try {
            bookmarks = new Bookmarks(Bookmarks.DEFAULT_BOOKMARK_FILENAME);
        } catch (FileNotFoundException e) {
            LOGGER.warn("Bookmarks file not found. I'll create a new one.");
            bookmarks = new Bookmarks();
        }
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

    public void startPolling() {
    
//        while (true) {
//            
//        }
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
        logger2csv.startPolling();
    }

}
