/*
 * I waive copyright and related rights in the this work worldwide through the CC0 1.0 Universal
 * public domain dedication. https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv.ebam;

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
public final class EbamDataLogger extends DataLogger {

  /** column index of date field */
  public static final int DATE_COLUMN = 0;

  /** format of date field */
  public static final String DATE_FORMAT_STRING = "dd-MMM-yyyy hh:mm:ss";

  /** number of header lines */
  public static final int HEADER_COUNT = 1;

  /**
   * A data model for working with a MetOne E-BAM.
   *
   * @param config logger configuration
   * @throws LoggerException when there are no tables configured
   */
  public EbamDataLogger(ConfigFile config) throws LoggerException {
    super(config, HEADER_COUNT);
  }

  /**
   * Return a filename pattern suitable for passing to SimpleDateFormat.
   * 
   * @param dataFile
   * 
   * @param table logger table
   * @return String suitable for SimpleDateFormat
   */
  public String getFilePattern(DataFile dataFile) {
    final StringBuilder sb = new StringBuilder();
    sb.append("'" + pathRoot + "/" + name + "/'");
    sb.append(filePathFormat.toPattern());
    sb.append("'/" + name + "-" + dataFile.toString() + "'");
    sb.append(fileSuffixFormat.toPattern());

    final String filename = sb.toString().replace('/', File.separatorChar);
    return filename;
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
}
