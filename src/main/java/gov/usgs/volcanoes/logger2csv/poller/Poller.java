/*
 * I waive copyright and related rights in the this work worldwide
 * through the CC0 1.0 Universal public domain dedication.
 * https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv.poller;

/**
 * A poller of a data logger.
 * 
 * @author Tom Parker
 *
 */
public interface Poller {
  
  /** One minute in seconds. */
  public static final long M_TO_S = 60;
  
  /** One day in seconds. */
  public static final long DAY_TO_S = 24 * 60 * M_TO_S;
  
  /** One minute in milliseconds */
  public static final long M_TO_MS = 60 * 1000;

  /**
   * Update all files I control once.
   */
  abstract public void updateFiles();
}
