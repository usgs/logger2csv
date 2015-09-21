/*
 * I waive copyright and related rights in the this work worldwide
 * through the CC0 1.0 Universal public domain dedication.
 * https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv.campbell;

import gov.usgs.volcanoes.logger2csv.FileDataReader;
import gov.usgs.volcanoes.logger2csv.FileDataWriter;
import gov.usgs.volcanoes.logger2csv.Poller;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A class to poll a single data logger.
 * 
 * @author Tom Parker
 */
public class CampbellPoller extends Poller {
  private static final Logger LOGGER = LoggerFactory.getLogger(CampbellPoller.class);

  private final CampbellDataLogger logger;

  /**
   * A Poller class for collecting data from a CompbellScientific data logger.
   * 
   * @param logger
   *          The DataLogger to poll
   */
  public CampbellPoller(CampbellDataLogger logger) {
    this.logger = logger;
  }

  @Override
  public void updateFiles() {
    LOGGER.debug("Polling {}", logger.name);
    Iterator<String> tableIt = logger.getTableIterator();
    while (tableIt.hasNext()) {
      String table = tableIt.next();
      try {
        updateTable(table);
      } catch (IOException e) {
        LOGGER.error("Unable to update {}.{}, I'll try again next time. ({})", logger.name, table,
            e.getLocalizedMessage());
      }
    }
  }

  private void updateTable(String table) throws IOException {
    LOGGER.debug("Polling {}.{}", logger.name, table);

    int lastRecordNum = findLastRecordNum(table);

    ListIterator<CSVRecord> results;
    try {
      results = retrieveNewData(table, lastRecordNum);
    } catch (IOException e) {
      LOGGER.error("Cannot retrieve data from {}.{}, I'll try again next time. ({})", logger.name,
          table, e.getLocalizedMessage());
      return;
    }

    if (!results.hasNext()) {
      LOGGER.info("No new data found in table {}.{}", logger.name, table);
      return;
    }

    List<CSVRecord> headers = extractHeaders(results);

    CSVRecord record = results.next();
    if (recordMatches(record, lastRecordNum)) {
      // do nothing and let the old value fall on the floor
    } else {
      // different record. Quick, put it back!
      results.previous();
    }

    // write new data
    FileDataWriter fileWriter = new CampbellWriter(logger, table);
    fileWriter.setHeader(headers);
    fileWriter.write(results);
  }

  private ListIterator<CSVRecord> retrieveNewData(String table, int lastRecordNum)
      throws IOException {
    ListIterator<CSVRecord> results;
    if (lastRecordNum > 0)
      results = since_record(lastRecordNum, table);
    else
      results = backFill((int) (logger.backfill * DAY_TO_S), table);

    return results;
  }

  private boolean recordMatches(CSVRecord recordString, int recordNum) {
    if (Integer.parseInt(recordString.get(0)) == recordNum)
      return true;
    else
      return false;
  }

  private int findLastRecordNum(String table) throws IOException {
    FileDataReader fileReader = new FileDataReader(logger);
    CSVRecord lastRecord = fileReader.findLastRecord(logger.getFilePattern(table));
    return Integer.parseInt(lastRecord.get(1));
  }

  public ListIterator<CSVRecord> since_record(int record, String table) throws IOException {
    LOGGER.info("Downloading new records from {}.{}.", logger.name, table);
    return getResults("since-record", record, table);
  }

  public ListIterator<CSVRecord> backFill(int backfillS, String table) throws IOException {
    LOGGER.info("Downloading all recent records from {}.{}.", logger.name, table);
    return getResults("backfill", backfillS, table);
  }

  private ListIterator<CSVRecord> getResults(final String mode, final int p1, String table)
      throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("http://");
    sb.append(logger.address);
    sb.append("/?command=DataQuery&uri=dl:");
    sb.append(table);
    sb.append("&mode=");
    sb.append(mode);
    sb.append("&format=TOA5");
    sb.append("&p1=");
    sb.append(p1);

    // String url = sb.toString();
    URL url = new URL(sb.toString());
    CSVParser parser = CSVParser.parse(url, StandardCharsets.UTF_8, logger.getCsvFormat());
    ListIterator<CSVRecord> iterator = parser.getRecords().listIterator();

    return iterator;
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
}
