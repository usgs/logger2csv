/*
 * I waive copyright and related rights in the this work worldwide through the CC0 1.0 Universal
 * public domain dedication. https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv;

import gov.usgs.volcanoes.logger2csv.logger.DataLogger;
import gov.usgs.volcanoes.logger2csv.poller.PollerException;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;

/**
 * A class to read CSV data from a file.
 * 
 * @author Tom Parker
 */
public class FileDataReader {
  private static final Logger LOGGER = LoggerFactory.getLogger(Logger2csv.class);

  private static final long DAY_TO_MS = 24 * 60 * 60 * 1000;

  private final DataLogger logger;

  /**
   * Constructor.
   * 
   * @param logger my data logger
   */
  public FileDataReader(final DataLogger logger) {
    this.logger = logger;
  }

  /**
   * Retrieve the most recent record I have for this logger.
   * 
   * @param fileNamePattern Pattern used to create file names
   * @return the most recent record
   * @throws PollerException when things go wrong
   */
  public CSVRecord findLastRecord(final String fileNamePattern) throws PollerException {
    LOGGER.debug("Finding last record for {}", logger.name);

    final File recentFile = findRecentFile(fileNamePattern);
    if (recentFile == null || !recentFile.exists())
      return null;

    CSVParser parser;
    try {
      parser = CSVParser.parse(recentFile, StandardCharsets.UTF_8, logger.csvFormat);
    } catch (IOException e) {
      throw new PollerException(e);
    }
    final Iterator<CSVRecord> iterator = parser.iterator();

    // demand files have at least one record if they exist.
    if (!iterator.hasNext()) {
      final String message = String.format(
          "The most recent data file has no records, remove it before proceeding. (%s)",
          recentFile);
      throw new PollerException(message);
    }

    CSVRecord record = iterator.next();

    while (iterator.hasNext()) {
      record = iterator.next();
    }
    return record;
  }

  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private File findRecentFile(final String fileNamePattern) {
    final SimpleDateFormat dateFormat = new SimpleDateFormat(fileNamePattern, Locale.ENGLISH);

    long timeMs = System.currentTimeMillis();
    final long ancientMs = timeMs - logger.backfill * DAY_TO_MS;

    while (timeMs > ancientMs) {
      final String fileName = dateFormat.format(timeMs);
      final File file = new File(fileName);
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
