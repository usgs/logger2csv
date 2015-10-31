/*
 * I waive copyright and related rights in the this work worldwide through the CC0 1.0 Universal
 * public domain dedication. https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv.campbell;

import gov.usgs.volcanoes.logger2csv.FileDataReader;
import gov.usgs.volcanoes.logger2csv.FileDataWriter;
import gov.usgs.volcanoes.logger2csv.logger.LoggerRecord;
import gov.usgs.volcanoes.logger2csv.poller.Poller;
import gov.usgs.volcanoes.logger2csv.poller.PollerException;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

/**
 * A class to poll a single data logger.
 *
 * @author Tom Parker
 */
public final class CampbellPoller implements Poller {
  private static final Logger LOGGER = LoggerFactory.getLogger(CampbellPoller.class);

  private final CampbellDataLogger logger;

  /**
   * A Poller class for collecting data from a CompbellScientific data logger.
   *
   * @param logger The DataLogger to poll
   */
  public CampbellPoller(final CampbellDataLogger logger) {
    this.logger = logger;
  }

  private ListIterator<CSVRecord> backFill(final int backfillS, final String table)
      throws PollerException {
    LOGGER.info("Downloading all recent records from {}.{}.", logger.name, table);
    return getResults("backfill", backfillS, table);
  }

  private List<CSVRecord> extractHeaders(final Iterator<CSVRecord> iterator) {
    final List<CSVRecord> headers = new ArrayList<CSVRecord>();
    for (int i = 0; i < logger.headerCount; i++) {
      if (iterator.hasNext()) {
        headers.add(iterator.next());
      }
    }

    return headers;
  }

  private int findLastRecordNum(final String table) throws PollerException {
    final FileDataReader fileReader = new FileDataReader(logger);
    final CSVRecord lastRecord = fileReader.findLastRecord(logger.getFilePattern(table));
    if (lastRecord == null) {
      LOGGER.debug("No recent data file was found.");
      return -1;
    } else {
      LOGGER.debug("Most recent record num is {}", lastRecord.get(1));
      return Integer.parseInt(lastRecord.get(1));
    }
  }

  private ListIterator<CSVRecord> getResults(final String mode, final int param1,
      final String table) throws PollerException {

    List<CSVRecord> records;
    try {
      // final URL url = new URL(urlBuf.toString());
      final String urlString =
          String.format("http://%s:%d/?command=DataQuery&uri=dl:%s&mode=%s&format=TOA5&p1=%d",
              logger.address, logger.port, table, mode, param1);
      final URL url = new URL(urlString);

      LOGGER.debug("Polling from {}", url);
      final CSVParser parser = CSVParser.parse(url, StandardCharsets.UTF_8, logger.csvFormat);
      records = parser.getRecords();
    } catch (final IOException e) {
      throw new PollerException(e);
    }

    return records.listIterator();
  }

  private boolean recordMatches(final CSVRecord record, final int recordNum) {
    return logger.parseRecordNum(record) == recordNum;
  }

  private ListIterator<CSVRecord> retrieveNewData(final String table, final int lastRecordNum)
      throws PollerException {
    LOGGER.debug("Polling {}.{}", logger.name, table);

    ListIterator<CSVRecord> results;
    if (lastRecordNum > 0) {
      results = sinceRecord(lastRecordNum, table);
    } else {
      results = backFill((int) (logger.backfill * DAY_TO_S), table);
    }

    return results;
  }

  private ListIterator<CSVRecord> sinceRecord(final int record, final String table)
      throws PollerException {
    LOGGER.info("Downloading new records from {}.{}.", logger.name, table);
    return getResults("since-record", record, table);
  }

  public void updateFiles() {
    LOGGER.debug("Polling {}", logger.name);
    final Iterator<String> tableIt = logger.getTableIterator();
    while (tableIt.hasNext()) {
      final String table = tableIt.next();
      try {
        updateTable(table);
      } catch (final PollerException e) {
        LOGGER.error("Unable to update {}.{}, I'll try again next time. ({})", logger.name, table,
            e.getLocalizedMessage());
      }
    }
  }

  // TODO: use of iterator introduces side effects. Find a better way.
  private void updateTable(final String table) throws PollerException {
    final int lastRecordNum = findLastRecordNum(table);
    final ListIterator<CSVRecord> results = retrieveNewData(table, lastRecordNum);

    if (!results.hasNext()) {
      LOGGER.info("No response when polling {}.{}", logger.name, table);
      return;
    }

    // 
    final List<CSVRecord> headers = extractHeaders(results);

    if (!results.hasNext()) {
      LOGGER.debug("No new data in table {}.{}", logger.name, table);
      return;
    }

    final CSVRecord record = results.next();
    if (!recordMatches(record, lastRecordNum)) {
      // different record. Quick, put it back!
      results.previous();
    }

    if (!results.hasNext()) {
      LOGGER.debug("no data found");
      return;
    }

    List<LoggerRecord> records;
    final SimpleDateFormat dateFormat =
        new SimpleDateFormat(CampbellDataLogger.DATE_FORMAT, Locale.US);
    try {
      records = LoggerRecord.fromCSVList(results, dateFormat, CampbellDataLogger.DATE_COLUMN);
    } catch (final ParseException e1) {
      throw new PollerException(e1);
    }

    // write new data
    final FileDataWriter fileWriter =
        new FileDataWriter(logger.csvFormat, logger.getFilePattern(table));
    fileWriter.addHeaders(headers);
    try {
      fileWriter.write(records.iterator());
      // } catch (ParseException e) {
      // LOGGER.error("Cannot parse logger response. Skipping {}. ({})", logger.name, e);
      // return;
    } catch (final IOException e) {
      LOGGER.error("Cannot write to datafile for {}.{}.", logger.name, table);
      return;
    }

  }
}
