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
  
  public static DataLogger getLogger(ConfigFile config) throws IOException, ParseException {
    DataLogger logger = null;
    LoggerType type = LoggerType.valueOf(config.getString("type"));
    switch (type) {
      case CAMPBELL:
        logger = new CampbellDataLogger(config);
        break;
      default:
        throw new UnsupportedOperationException("Unknown type: " + type);
    }
    
    return logger;
  }

  public static Poller getPoller(ConfigFile config) throws IOException, ParseException {
    Poller poller = null;
    LoggerType type = LoggerType.valueOf(config.getString("type"));
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
