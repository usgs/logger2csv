/*
 * I waive copyright and related rights in the this work worldwide through the CC0 1.0 Universal
 * public domain dedication. https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv.logger;

import gov.usgs.volcanoes.core.configfile.ConfigFile;
import gov.usgs.volcanoes.core.configfile.parser.CsvFormatParser;
import gov.usgs.volcanoes.logger2csv.Logger2csv;

import org.apache.commons.csv.CSVFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * A class to hold configuration and utility methods for a single data logger
 * 
 * @author Tom Parker
 * 
 *         I waive copyright and related rights in the this work worldwide through the CC0 1.0
 *         Universal public domain dedication.
 *         https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */
public abstract class DataLogger {
  private static final Logger LOGGER = LoggerFactory.getLogger(Logger2csv.class);

  /** Default pattern for file paths. */
  public static final String DEFAULT_FILE_PATH_FORMAT = "yyyy/MM";

  /** Default patther for file suffixes, including file extension. */
  public static final String DEFAULT_FILE_SUFFIX_FORMAT = "-yyyyMMdd'.csv'";

  /** Default root of file paths. */
  public static final String DEFAULT_PATH_ROOT = "data";

  /** Default number of days to backfill. */
  public static final int DEFAULT_BACKFILL = 2;

  /** Default pattern of CSV date columns */
  public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

  /** Default CSV format. */
  public static final CSVFormat DEFAULT_CSV_FORMAT = CSVFormat.RFC4180;

  /** My name. */
  public final String name;

  /** Number of days to backfill. */
  public final int backfill;

  /** Path root. */
  public final String pathRoot;

  /** Address of logger */
  public final String address;

  /** Number of header rows */
  public final int headerCount;

  /** Format of CSV files */
  public final CSVFormat csvFormat;

  protected final SimpleDateFormat filePathFormat;
  protected final SimpleDateFormat fileSuffixFormat;
  protected final SimpleDateFormat dateFormat;
  protected final SimpleDateFormat csvDateFormat;

  /**
   * The super logger.
   * 
   * @param config Logger config stanza
   * @param headerCount Number of header rows
   * @throws LoggerException when logger
   */
  public DataLogger(ConfigFile config, int headerCount) throws LoggerException {
    this.headerCount = headerCount;
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

    name = config.getString("name");
    if (name == null)
      throw new LoggerException("Station name must be specified in Config.");
    LOGGER.debug("creating logger {}", name);

    address = config.getString("address");
    if (address == null)
      throw new LoggerException("Station address must be specified in Config.");

    backfill = config.getInt("backfill", DEFAULT_BACKFILL);
    pathRoot = config.getString("pathRoot", DEFAULT_PATH_ROOT);

    String path = config.getString("filePathFormat", DEFAULT_FILE_PATH_FORMAT);
    filePathFormat = new SimpleDateFormat(path);

    String suffix = config.getString("fileSuffixFormat", DEFAULT_FILE_SUFFIX_FORMAT);
    fileSuffixFormat = new SimpleDateFormat(suffix);

    dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);

    try {
      csvFormat = config.getObject("csvFormat", new CsvFormatParser());
    } catch (ParseException e) {
      throw new LoggerException(e);
    }

    String csvDateFormatString = config.getString("csvDateFormat", dateFormat.toPattern());
    csvDateFormat = new SimpleDateFormat(csvDateFormatString);
  }
}
