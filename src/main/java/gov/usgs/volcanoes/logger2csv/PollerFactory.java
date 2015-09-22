/*
 * I waive copyright and related rights in the this work worldwide
 * through the CC0 1.0 Universal public domain dedication.
 * https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv;

import gov.usgs.volcanoes.core.configfile.ConfigFile;
import gov.usgs.volcanoes.logger2csv.campbell.CampbellDataLogger;
import gov.usgs.volcanoes.logger2csv.campbell.CampbellPoller;

import java.io.IOException;
import java.text.ParseException;

/**
 * Types of data loggers I can talk to.
 * 
 * @author Tom Parker
 *
 */
public final class PollerFactory {
  
  private static enum LoggerType {
  /** Designed to work with CampbellScientific CR850 and CR1000 */
	CAMPBELL, 
	
	/** Designed to work with MetOne E-BAM */
	EBAM
	
  }
  
  // non-instantiatable
  private PollerFactory() {}
  
  public static Poller getPoller(ConfigFile config) throws IOException, ParseException {
    Poller poller = null;
    LoggerType type;
    try {
      type = LoggerType.valueOf(config.getString("type").toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new ParseException("Cannot find logger type.", -1);
    }
    
    switch (type) {
      case CAMPBELL:
        CampbellDataLogger logger = new CampbellDataLogger(config);
        poller = new CampbellPoller(logger);
        break;
      default:
        throw new UnsupportedOperationException("Unknown type: " + type);
    }
    
    return poller;
  }
}
