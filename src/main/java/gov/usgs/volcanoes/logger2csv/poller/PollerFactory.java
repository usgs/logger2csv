/*
 * I waive copyright and related rights in the this work worldwide through the CC0 1.0 Universal
 * public domain dedication. https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv.poller;

import gov.usgs.volcanoes.core.configfile.ConfigFile;
import gov.usgs.volcanoes.logger2csv.campbell.CampbellDataLogger;
import gov.usgs.volcanoes.logger2csv.campbell.CampbellPoller;
import gov.usgs.volcanoes.logger2csv.ebam.EbamDataLogger;
import gov.usgs.volcanoes.logger2csv.ebam.EbamPoller;

/**
 * Provide the appropriate Poller class.
 * 
 * @author Tom Parker
 *
 */
public final class PollerFactory {

  private static enum LoggerType {
    /** Tested with CampbellScientific CR850 and CR1000 */
    CAMPBELL,

    /** Designed to work with MetOne E-BAM */
    EBAM
  }

  // uninstantiatable
  private PollerFactory() {}

  /**
   * Return an appropriate initialized Poller.
   * 
   * @param config config stanza for one logger
   * @return an appropriate initialized Poller
   * @throws PollerException when Poller cannot be created
   */
  public static Poller getPoller(ConfigFile config) throws PollerException {
    Poller poller = null;
    LoggerType type;
    try {
      type = LoggerType.valueOf(config.getString("type").toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new PollerException("Cannot find logger type.");
    }

    switch (type) {
      case CAMPBELL:
        try {
          poller = new CampbellPoller(new CampbellDataLogger(config));
        } catch (Exception e) {
          throw new PollerException(e);
        }

        break;
      case EBAM:
        try {
          poller = new EbamPoller(new EbamDataLogger(config));
        } catch (Exception e) {
          throw new PollerException(e);
        }
        break;
      default:
        throw new UnsupportedOperationException("Unknown type: " + type);
    }

    return poller;
  }
}
