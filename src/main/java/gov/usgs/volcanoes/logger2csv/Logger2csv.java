package gov.usgs.volcanoes.logger2csv;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.volcanoes.core.configfile.ConfigFile;

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

  private static final long M_TO_MS = 60 * 1000;

  public static final String DEFAULT_CONFIG_FILENAME = "logger2csv.config";
  public static final long DEFAULT_INTERVAL_M = 60;

  private static final Logger LOGGER = LoggerFactory.getLogger(Logger2csv.class);

  private ConfigFile configFile;
  private List<Poller> pollers;
  private int interval;

  public Logger2csv(ConfigFile configFile) {
    LOGGER.info("Launching Logger2csv ({})", Logger2csvVersion.VERSION_STRING);

    this.configFile = configFile;
    this.interval = configFile.getInt("interval", (int) DEFAULT_INTERVAL_M);
    pollers = getPollers();
  }

  private List<Poller> getPollers() {
    List<Poller> pollers = new ArrayList<Poller>();

    for (String station : configFile.getList("station")) {
      ConfigFile config = configFile.getSubConfig(station, true);
      config.put("name", station);

      try {
        pollers.add(PollerFactory.getPoller(config));
      } catch (UnknownHostException e) {
        LOGGER.error("Cannot find host \"{}\". I'll skip it this time.",
            config.getString("address"));
      } catch (IOException e) {
        LOGGER.error(e.getMessage());
      }
    }
    return pollers;
  }

  public void pollAllOnce() {
    for (Poller p : pollers) {
      p.updateFiles();
    }
  }

  private void pollAllCountinous() {
    while (true) {
      pollAllOnce();
      try {
        Thread.sleep(interval * M_TO_MS);
      } catch (InterruptedException ignore) {
      }
    }
  }

  /**
   * Retrieve data from a list of data loggers.
   * 
   * @param args Command line arguments
   */
  public static void main(String[] args) {
    Logger2csvArgs cmdLineArgs = null;

    // Parse the command line
    try {
      cmdLineArgs = new Logger2csvArgs(args);
    } catch (Exception e) {
      LOGGER.error("Cannot parse command line. (" + e.getLocalizedMessage() + ")");
    }

    if (cmdLineArgs == null) {
      System.exit(1);
    }

    // Parse the config file
    ConfigFile cf = null;
    try {
      cf = new ConfigFile(cmdLineArgs.configFileName);
    } catch (FileNotFoundException e) {
      LOGGER.warn(
          "Can't parse config file " + cmdLineArgs.configFileName + ". Try using the --help flag.");
    }

    if (cf == null) {
      System.exit(1);
    }


    // Get the data
    Logger2csv logger2csv = new Logger2csv(cf);
    if (cmdLineArgs.persistent)
      logger2csv.pollAllCountinous();
    else
      logger2csv.pollAllOnce();
  }
}
