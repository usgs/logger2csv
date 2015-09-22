/*
 * I waive copyright and related rights in the this work worldwide through the CC0 1.0 Universal
 * public domain dedication. https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv.poller;

/**
 * Signals that a single poller has encountered an error that prevents it from completing.
 * 
 * @author Tom Parker
 *
 */
public class PollerException extends Exception {

  /** Maybe I'll be serialized one day? */
  private static final long serialVersionUID = -2778645971529842119L;

  /**
   * Constructor.
   * 
   * @param message Helpful information on cause of trouble
   */
  public PollerException(String message) {
    super(message);
  }

  /**
   * Exception-wrapping constructor.
   * 
   * @param e exception to wrap
   */
  public PollerException(Exception e) {
    super(e);
  }
}
