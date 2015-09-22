/*
 * I waive copyright and related rights in the this work worldwide
 * through the CC0 1.0 Universal public domain dedication.
 * https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv;

import gov.usgs.volcanoes.core.configfile.ConfigFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.FileAlreadyExistsException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * An application to write CSV files from a collection of remote data loggers.
 * 
 * @author Tom Parker
 * 
 */
public class Logger2csv {

  private static final long M_TO_MS = 60 * 1000;

  /** Default config file name */
  public static final String DEFAULT_CONFIG_FILENAME = "logger2csv.config";

  /** Default polling interval */
  public static final int DEFAULT_INTERVAL_M = 60;

  private static final Logger LOGGER = LoggerFactory.getLogger(Logger2csv.class);

  private ConfigFile configFile;
  private List<Poller> pollers;
  private int interval;

  /**
   * Constructor.
   * 
   * @param configFile app connfiguration
   */
  public Logger2csv(ConfigFile configFile) {
    LOGGER.info("Launching Logger2csv ({})", Version.VERSION_STRING);

    this.configFile = configFile;
    this.interval = configFile.getInt("interval", DEFAULT_INTERVAL_M);
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
      } catch (ParseException e) {
        LOGGER.error("Unable to parse station stanza. ({}: {})", station, e.getLocalizedMessage());
      }
    }
    return pollers;
  }

  /**
   * Poll each configured logger once
   */
  public void pollAllOnce() {
    LOGGER.debug("Polling all loggers");
    for (Poller p : pollers) {
      p.updateFiles();
    }
  }

  /**
   * Poll each configured logger with a fixed rest interval. Poll times will slip since rest
   * interval is fixed.
   */
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
    } catch (FileAlreadyExistsException e) {
      LOGGER.error("I will not overwrite an exissting config file. Please stash logger2csv.config somewhere safe before creating an example config. (" + e + ")");
    } catch (Exception e) {
      LOGGER.error("Cannot parse command line. (" + e + ")");
    }

    if (cmdLineArgs == null || !cmdLineArgs.runnable) {
      System.exit(1);
    }

    // Parse the config file
    ConfigFile cf = null;
    try {
      cf = new ConfigFile(cmdLineArgs.configFileName);
    } catch (FileNotFoundException e) {
      LOGGER.error(
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