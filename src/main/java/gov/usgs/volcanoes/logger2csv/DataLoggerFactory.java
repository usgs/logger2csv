package gov.usgs.volcanoes.logger2csv;

import java.io.IOException;

import gov.usgs.volcanoes.core.configfile.ConfigFile;
import gov.usgs.volcanoes.logger2csv.campbell.CampbellDataLogger;

/**
 * Types of data loggers I can talk to.
 * 
 * @author Tom Parker
 *
 */
public final class DataLoggerFactory {
  
  private static enum LoggerType {
  /** Designed to work with CampbellScientific CR850 and CR1000 */
	CAMPBELL, 
	
	/** Designed to work with MetOne E-BAM */
	EBAM
	
  }
  
  // non-instantiatable
  private DataLoggerFactory() {}
  
  public static DataLogger getLogger(ConfigFile config) throws IOException {
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
}
