/*
 * I waive copyright and related rights in the this work worldwide
 * through the CC0 1.0 Universal public domain dedication.
 * https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv;

import gov.usgs.volcanoes.core.configfile.ConfigFile;
import gov.usgs.volcanoes.core.configfile.parser.CsvFormatParser;

import org.apache.commons.csv.CSVFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * A class to hold configuration and utility methods for a single data logger
 * 
 * @author Tom Parker
 * 
 *         I waive copyright and related rights in the this work worldwide
 *         through the CC0 1.0 Universal public domain dedication.
 *         https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */
public abstract class DataLogger {
  private static final Logger LOGGER = LoggerFactory.getLogger(Logger2csv.class);

  public static final String DEFAULT_FILE_PATH_FORMAT = "yyyy/MM";
  public static final String DEFAULT_FILE_SUFFIX_FORMAT = "-yyyyMMdd";
  public static final String DEFAULT_PATH_ROOT = "data";
  public static final int DEFAULT_BACKFILL = 2;
  public static final boolean DEFAULT_QUOTE_FIELDS = false;
  public static final String TOA5_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
  public static final CSVFormat DEFAULT_CSV_FORMAT = CSVFormat.RFC4180;
  public static final String DEFAULT_COMMENT_CHAR = "#";

  public final String name;
  public final int backfill;
  public final String pathRoot;
  public final String address;
  public final boolean quoteFields;
  public final int headerCount;
  public final CSVFormat csvFormat;
  public final String commentChar;

  protected final SimpleDateFormat filePathFormat;
  protected final SimpleDateFormat fileSuffixFormat;
  protected final SimpleDateFormat dateFormat;
  protected final SimpleDateFormat csvDateFormat;

  public DataLogger(ConfigFile config, int headerCount) throws IOException, ParseException {
    this.headerCount = headerCount;
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

    name = config.getString("name");
    if (name == null)
      throw new IOException("Station name must be specified in Config.");
    LOGGER.debug("creating logger {}", name);

    address = config.getString("address");
    if (address == null)
      throw new IOException("Station address must be specified in Config.");

    backfill = config.getInt("backfill", DEFAULT_BACKFILL);
    pathRoot = config.getString("pathRoot", DEFAULT_PATH_ROOT);
    quoteFields = config.getBoolean("quoteFields", DEFAULT_QUOTE_FIELDS);

    String path = config.getString("filePathFormat", DEFAULT_FILE_PATH_FORMAT);
    filePathFormat = new SimpleDateFormat(path);

    String suffix = config.getString("fileSuffixFormat", DEFAULT_FILE_SUFFIX_FORMAT);
    fileSuffixFormat = new SimpleDateFormat(suffix);

    dateFormat = new SimpleDateFormat(TOA5_DATE_FORMAT);

    csvFormat = config.getObject("csvFormat", new CsvFormatParser());

    commentChar = config.getString("commentChar", DEFAULT_COMMENT_CHAR);

    String csvDateFormatString = config.getString("csvDateFormat", dateFormat.toPattern());
    csvDateFormat = new SimpleDateFormat(csvDateFormatString);
  }
}
