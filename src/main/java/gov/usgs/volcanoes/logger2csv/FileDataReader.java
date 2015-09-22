/*
 * I waive copyright and related rights in the this work worldwide
 * through the CC0 1.0 Universal public domain dedication.
 * https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv;

import gov.usgs.volcanoes.logger2csv.logger.DataLogger;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Iterator;

/**
 * A class to read CSV data from a file.
 * 
 * @author Tom Parker
 */
public class FileDataReader {
  private static final Logger LOGGER = LoggerFactory.getLogger(Logger2csv.class);

  private static final long DAY_TO_MS = 24 * 60 * 60 * 1000;

  private final DataLogger logger;

  public FileDataReader(DataLogger logger) {
    this.logger = logger;
  }

  public CSVRecord findLastRecord(String fileNamePattern) throws IOException {
    LOGGER.debug("Finding last record for {}", logger.name);

    File recentFile = findRecentFile(fileNamePattern);
    if (recentFile == null || !recentFile.exists())
      return null;

    CSVParser parser = CSVParser.parse(recentFile, StandardCharsets.UTF_8, logger.csvFormat);
    Iterator<CSVRecord> iterator = parser.iterator();

    // demand files have at least one record if they exist.
    CSVRecord record = iterator.next();

    while (iterator.hasNext()) {
      record = iterator.next();
    }
    return record;
  }

  private File findRecentFile(String fileNamePattern) {
    LOGGER.error("Pattern '{}'",fileNamePattern);
    SimpleDateFormat dateFormat = new SimpleDateFormat(fileNamePattern);

    long timeMs = System.currentTimeMillis();
    long ancientMs = timeMs - logger.backfill * DAY_TO_MS;

    while (timeMs > ancientMs) {
      String fileName = dateFormat.format(timeMs);
      File file = new File(fileName);
      if (file.exists()) {
        return file;
      } else {
        LOGGER.debug("didn't find: {}", fileName);
      }
      timeMs -= DAY_TO_MS;
    }
    return null;
  }
}
