/*
 * I waive copyright and related rights in the this work worldwide
 * through the CC0 1.0 Universal public domain dedication.
 * https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv.campbell;

import gov.usgs.volcanoes.core.configfile.ConfigFile;
import gov.usgs.volcanoes.logger2csv.DataLogger;

import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * A class to hold configuration and utility methods for a single data logger.
 *
 * @author Tom Parker
 */
public final class CampbellDataLogger extends DataLogger {
  public static final int DATE_COLUMN = 0;

  public static final String DATE_FORMAT_STRING = "yyyy-MM-dd hh:mm:ss";

  public static final int HEADER_COUNT = 4;

  private static final Logger LOGGER = LoggerFactory.getLogger(CampbellDataLogger.class);

  private final List<String> tables;

  /**
   * A data model for working with a CampbellScientific data logger.
   *
   * @param config logger configuration
   * @throws IOException when there are no tables configured
   * @throws ParseException when station stanza cannot be parsed
   */
  public CampbellDataLogger(ConfigFile config) throws IOException, ParseException {
    super(config, HEADER_COUNT);

    tables = config.getList("table");
    if (tables == null || tables.isEmpty()) {
      throw new IOException("No tables found for " + name);
    }
  }

  public String getFilePattern(String table) {
    final StringBuilder sb = new StringBuilder();
    sb.append("'" + pathRoot + "/" + name + "/'");
    sb.append(filePathFormat.toPattern());
    sb.append("'/" + name + "-" + table + "'");
    sb.append(fileSuffixFormat.toPattern());

    final String filename = sb.toString().replace('/', File.separatorChar);
    return filename;
  }

  /**
   * Retrieve the table iterator
   *
   * @return table Iterator
   */
  protected Iterator<String> getTableIterator() {
    return tables.iterator();
  }

  protected Date parseDate(CSVRecord record) throws ParseException {
    return parseDate(record.get(1));
  }

  public Date parseDate(String date) throws ParseException {
    return dateFormat.parse(date);
  }

}
