/*
 * I waive copyright and related rights in the this work worldwide
 * through the CC0 1.0 Universal public domain dedication.
 * https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv.campbell;

import gov.usgs.volcanoes.logger2csv.FileDataWriter;

import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A class to write data from a single data logger.
 * 
 * @author Tom Parker
 */
public class CampbellWriter extends FileDataWriter {

  private final SimpleDateFormat loggerDateFormat;
  private final SimpleDateFormat fileDateFormat;
  private final CampbellDataLogger logger;

  /**
   * Constructor.
   * 
   * @param filePattern Patter used to create filenames
   */
  public CampbellWriter(CampbellDataLogger logger, String table) {
    super(logger.csvFormat);
    this.logger = logger;
    loggerDateFormat = new SimpleDateFormat(CampbellDataLogger.DATE_FORMAT_STRING);
    fileDateFormat = new SimpleDateFormat(logger.getFilePattern(table));
  }

  @Override
  protected Date getDate(CSVRecord record) throws ParseException {
    return loggerDateFormat.parse(record.get(CampbellDataLogger.DATE_COLUMN));
  }

  @Override
  protected File getFile(CSVRecord record) throws ParseException {
    Date date = logger.parseDate(record);
    return new File(fileDateFormat.format(date));
  }

}
