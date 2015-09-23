/*
 * I waive copyright and related rights in the this work worldwide through the CC0 1.0 Universal
 * public domain dedication. https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv.campbell;

import gov.usgs.volcanoes.core.configfile.ConfigFile;
import gov.usgs.volcanoes.logger2csv.logger.DataLogger;
import gov.usgs.volcanoes.logger2csv.logger.LoggerException;

import org.apache.commons.csv.CSVRecord;

import java.io.File;
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

  /** column index of date field */
  public static final int DATE_COLUMN = 0;

  /** column index of record number */
  public static final int RECORD_NUM_COLUMN = 1;

  /** format of date field */
  public static final String DATE_FORMAT_STRING = "yyyy-MM-dd hh:mm:ss";

  /** number of header lines */
  public static final int HEADER_COUNT = 4;

  private final List<String> tables;

  /**
   * A data model for working with a CampbellScientific data logger.
   *
   * @param config logger configuration
   * @throws LoggerException when there are no tables configured
   */
  public CampbellDataLogger(ConfigFile config) throws LoggerException {
    super(config, HEADER_COUNT);

    tables = config.getList("table");
    if (tables == null || tables.isEmpty()) {
      throw new LoggerException("No tables found for " + name);
    }
  }

  /**
   * Return a filename pattern suitable for passing to SimpleDateFormat.
   * 
   * @param table logger table
   * @return String suitable for SimpleDateFormat
   */
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

  /**
   * Find a date in the record.
   * 
   * @param record record to search
   * @return the Date found
   * @throws ParseException when format cannot be parsed
   */
  protected Date parseDate(CSVRecord record) throws ParseException {
    String dateString = record.get(DATE_COLUMN);

    return dateFormat.parse(dateString);
  }

  /**
   * Find a record number if a record.
   * 
   * @param record to search
   * @return int record number
   */
  public int parseRecordNum(CSVRecord record) {
    return Integer.parseInt(record.get(RECORD_NUM_COLUMN));
  }
}
