/*
 * I waive copyright and related rights in the this work worldwide
 * through the CC0 1.0 Universal public domain dedication.
 * https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv.ebam;

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
public final class EbamWriter extends FileDataWriter {

  private final SimpleDateFormat loggerDateFormat;
  private final SimpleDateFormat fileDateFormat;
  private final EbamDataLogger logger;

  /**
   * Constructor.
   * 
   * @param logger Data logger model
   * @param table Table being polled
   */
  public EbamWriter(EbamDataLogger logger) {
    super(logger.csvFormat);
    this.logger = logger;
    loggerDateFormat = new SimpleDateFormat(EbamDataLogger.DATE_FORMAT_STRING);
    fileDateFormat = new SimpleDateFormat(logger.getFilePattern());
  }

  @Override
  protected Date getDate(CSVRecord record) throws ParseException {
    return loggerDateFormat.parse(record.get(EbamDataLogger.DATE_COLUMN));
  }

  @Override
  protected File getFile(CSVRecord record) throws ParseException {
    Date date = logger.parseDate(record);
    return new File(fileDateFormat.format(date));
  }
}