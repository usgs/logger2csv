/*
 * I waive copyright and related rights in the this work worldwide
 * through the CC0 1.0 Universal public domain dedication.
 * https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv;

public interface Poller {
  public static final long M_TO_S = 60;
  public static final long DAY_TO_S = 24 * 60 * M_TO_S;
  public static final long M_TO_MS = 60 * 1000;

  abstract public void updateFiles();
}
