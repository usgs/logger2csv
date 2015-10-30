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
  public CampbellPoller(CampbellDataLogger logger) {
    this.logger = logger;
  }

  public void updateFiles() {
    LOGGER.debug("Polling {}", logger.name);
    Iterator<String> tableIt = logger.getTableIterator();
    while (tableIt.hasNext()) {
      String table = tableIt.next();
      try {
        updateTable(table);
      } catch (PollerException e) {
        LOGGER.error("Unable to update {}.{}, I'll try again next time. ({})", logger.name, table,
            e.getLocalizedMessage());
      }
    }
  }

  private void updateTable(String table) throws PollerException {
    int lastRecordNum = findLastRecordNum(table);
    ListIterator<CSVRecord> results = retrieveNewData(table, lastRecordNum);

    if (!results.hasNext()) {
      LOGGER.info("No response when polling {}.{}", logger.name, table);
      return;
    }

    List<CSVRecord> headers = extractHeaders(results);

    if (!results.hasNext()) {
      LOGGER.debug("No new data in table {}.{}", logger.name, table);
      return;
    }

    CSVRecord record = results.next();
    if (recordMatches(record, lastRecordNum)) {
      // do nothing and let the old value fall on the floor
    } else {
      // different record. Quick, put it back!
      results.previous();
    }

    if (!results.hasNext()) {
      LOGGER.debug("no data found");
      return;
    }

    List<LoggerRecord> records;
    SimpleDateFormat dateFormat = new SimpleDateFormat(CampbellDataLogger.DATE_FORMAT_STRING);
    try {
      records = LoggerRecord.fromCSVList(results, dateFormat, CampbellDataLogger.DATE_COLUMN);
    } catch (ParseException e1) {
      throw new PollerException("Cannot parse results. (" + e1.getLocalizedMessage() + ")");
    }

    // write new data
    FileDataWriter fileWriter = new FileDataWriter(logger.csvFormat, logger.getFilePattern(table));
    fileWriter.addHeaders(headers);
    try {
      fileWriter.write(records.iterator());
//    } catch (ParseException e) {
//      LOGGER.error("Cannot parse logger response. Skipping {}. ({})", logger.name, e);
//      return;
    } catch (IOException e) {
      LOGGER.error("Cannot write to datafile for {}.{}.", logger.name, table);
      return;
    }

  }

  private ListIterator<CSVRecord> retrieveNewData(String table, int lastRecordNum)
      throws PollerException {
    LOGGER.debug("Polling {}.{}", logger.name, table);

    ListIterator<CSVRecord> results;
    if (lastRecordNum > 0)
      results = since_record(lastRecordNum, table);
    else
      results = backFill((int) (logger.backfill * DAY_TO_S), table);

    return results;
  }

  private boolean recordMatches(CSVRecord record, int recordNum) {
    if (logger.parseRecordNum(record) == recordNum)
      return true;
    else
      return false;
  }

  private int findLastRecordNum(String table) throws PollerException {
    FileDataReader fileReader = new FileDataReader(logger);
    CSVRecord lastRecord = fileReader.findLastRecord(logger.getFilePattern(table));
    if (lastRecord == null) {
      LOGGER.debug("No recent data file was found.");
      return -1;
    } else {
      LOGGER.debug("Most recent record num is {}", lastRecord.get(1));
      return Integer.parseInt(lastRecord.get(1));
    }
  }

  private ListIterator<CSVRecord> since_record(int record, String table) throws PollerException {
    LOGGER.info("Downloading new records from {}.{}.", logger.name, table);
    return getResults("since-record", record, table);
  }

  private ListIterator<CSVRecord> backFill(int backfillS, String table) throws PollerException {
    LOGGER.info("Downloading all recent records from {}.{}.", logger.name, table);
    return getResults("backfill", backfillS, table);
  }

  private ListIterator<CSVRecord> getResults(final String mode, final int p1, String table)
      throws PollerException {
    StringBuilder sb = new StringBuilder();
    sb.append("http://");
    sb.append(logger.address);
    sb.append(":" + logger.port);
    sb.append("/?command=DataQuery&uri=dl:");
    sb.append(table);
    sb.append("&mode=");
    sb.append(mode);
    sb.append("&format=TOA5");
    sb.append("&p1=");
    sb.append(p1);

    List<CSVRecord> records = null;
    try {
      // String url = sb.toString();
      URL url = new URL(sb.toString());

      LOGGER.debug("Polling from {}", url);
      CSVParser parser = CSVParser.parse(url, StandardCharsets.UTF_8, logger.csvFormat);
      records = parser.getRecords();
    } catch (IOException e) {
      throw new PollerException(e);
    }

    return records.listIterator();
  }

  private List<CSVRecord> extractHeaders(Iterator<CSVRecord> iterator) {
    List<CSVRecord> headers = new ArrayList<CSVRecord>();
    for (int i = 0; i < logger.headerCount; i++) {
      if (iterator.hasNext()) {
        headers.add(iterator.next());
      }
    }

    return headers;
  }

//  private List<LoggerRecord> extractRecords(Iterator<CSVRecord> iterator) {
//    List<LoggerRecord> records = new ArrayList<LoggerRecord>();
//    while (iterator.hasNext()) {
//      CSVRecord record = iterator.next();
//      try {
//        records.add(new LoggerRecord(logger.parseDate(record).getTime(), record));
//      } catch (ParseException e) {
//        LOGGER.info("Discarding unparsable record. ({})", record);
//      }
//    }
//    return records;
//
//  }
}
