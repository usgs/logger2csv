/*
 * I waive copyright and related rights in the this work worldwide through the CC0 1.0 Universal
 * public domain dedication. https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv;

import gov.usgs.volcanoes.core.args.ArgumentException;
import gov.usgs.volcanoes.core.configfile.ConfigFile;
import gov.usgs.volcanoes.logger2csv.poller.Poller;
import gov.usgs.volcanoes.logger2csv.poller.PollerException;
import gov.usgs.volcanoes.logger2csv.poller.PollerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * An application to write CSV files from a collection of remote data loggers.
 *
 * @author Tom Parker
 *
 */
public class Logger2csv {

  /** Default config file name */
  public static final String DEFAULT_CONFIG_FILENAME = "logger2csv.config";

  /** Default polling interval */
  public static final int DEFAULT_INTERVAL_M = 60;

  private static final Logger LOGGER = LoggerFactory.getLogger(Logger2csv.class);
  private static final long M_TO_MS = 60 * 1000;

  private final ConfigFile configFile;
  private final int interval;
  private final List<Poller> pollers;

  /**
   * Constructor.
   *
   * @param configFile app configuration
   */
  public Logger2csv(final ConfigFile configFile) {
    LOGGER.info("Launching Logger2csv ({})", Version.VERSION_STRING);

    this.configFile = configFile;
    interval = configFile.getInt("interval", DEFAULT_INTERVAL_M);
    pollers = getPollers();
  }

  private List<Poller> getPollers() {
    final List<Poller> pollers = new ArrayList<Poller>();

    for (final String station : configFile.getList("station")) {
      final ConfigFile config = configFile.getSubConfig(station, true);
      config.put("name", station);

      try {
        pollers.add(PollerFactory.getPoller(config));
      } catch (final PollerException e) {
        LOGGER.error(e.getMessage());
      }
    }
    return pollers;
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
      } catch (final InterruptedException ignore) {
      }
    }
  }

  /**
   * Poll each configured logger once
   */
  public void pollAllOnce() {
    LOGGER.debug("Polling all loggers");
    for (final Poller p : pollers) {
      p.updateFiles();
    }
  }

  private static Logger2csvArgs getArgs(final String... argsArray) {
    Logger2csvArgs args = null;
    // Parse the command line
    try {
      args = new Logger2csvArgs(argsArray);
    } catch (final ArgumentException e) {
      LOGGER.error("Cannot parse command line. (" + e + ")");
    }

    return args;
  }

  private static ConfigFile getConfigFile(final String configFileName) {
    ConfigFile conf = new ConfigFile(configFileName);
    if (!conf.wasSuccessfullyRead()) {
      LOGGER.error("Can't parse config file " + configFileName + ". Try using the --help flag.");
      System.exit(1);
    }
    return conf;
  }

  /**
   * Retrieve data from a list of data loggers.
   *
   * @param args Command line arguments
   */
  public static void main(final String... args) {
    // Parse command line
    final Logger2csvArgs cmdLineArgs = getArgs(args);
    if (cmdLineArgs == null || !cmdLineArgs.runnable) {
      System.exit(1);
    }

    // Parse config file
    final ConfigFile conf = getConfigFile(cmdLineArgs.configFileName);
    if (conf == null) {
      System.exit(1);
    }

    // Get data
    final Logger2csv logger2csv = new Logger2csv(conf);
    if (cmdLineArgs.persistent) {
      logger2csv.pollAllCountinous();
    } else {
      logger2csv.pollAllOnce();
    }
  }
}
