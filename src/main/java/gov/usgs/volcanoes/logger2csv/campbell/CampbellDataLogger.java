/*
 * I waive copyright and related rights in the this work worldwide
 * through the CC0 1.0 Universal public domain dedication.
 * https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */
package gov.usgs.volcanoes.logger2csv.campbell;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.volcanoes.core.configfile.ConfigFile;
import gov.usgs.volcanoes.logger2csv.DataLogger;
import gov.usgs.volcanoes.logger2csv.Logger2csv;

/**
 * A class to hold configuration and utility methods for a single data logger
 * 
 * @author Tom Parker
 * 
 *         I waive copyright and related rights in the this work worldwide
 *         through the CC0 1.0 Universal public domain dedication.
 *         https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */
public class CampbellDataLogger extends DataLogger {
  private static final Logger LOGGER = LoggerFactory.getLogger(CampbellDataLogger.class);

  public static final int HEADER_COUNT = 4;

  private final List<String> tables;

  public CampbellDataLogger(ConfigFile config) throws IOException {
    super(config, HEADER_COUNT);

    tables = config.getList("table");
    if (tables == null || tables.isEmpty())
      throw new IOException("No tables found for " + name);
  }

  public Iterator<String> getTableIterator() {
    return tables.iterator();
  }

  public Date parseDate(String date) throws ParseException {
    return dateFormat.parse(date);
  }

  public String getFileName(String table, long timeMs) {
    StringBuilder sb = new StringBuilder();
    sb.append(pathRoot);
    sb.append("/");
    sb.append(name);
    sb.append("/");
    sb.append(filePathFormat.format(timeMs));
    sb.append("/");
    sb.append(name);
    sb.append("-");
    sb.append(table);
    sb.append(fileSuffixFormat.format(timeMs));

    String filename = sb.toString().replace('/', File.separatorChar);
    return filename;
  }
  
  public String getFilePattern(String table) {
    StringBuilder sb = new StringBuilder();
    sb.append("'" + pathRoot + "/" + name + "/'");
    sb.append(filePathFormat);
    sb.append("'/" + name + "-" + table + "'");
    sb.append(fileSuffixFormat);

    String filename = sb.toString().replace('/', File.separatorChar);
    return filename;
  }
}
