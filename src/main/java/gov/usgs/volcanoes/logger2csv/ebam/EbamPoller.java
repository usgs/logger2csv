/*
 * I waive copyright and related rights in the this work worldwide through the CC0 1.0 Universal
 * public domain dedication. https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv.ebam;

import gov.usgs.volcanoes.logger2csv.FileDataReader;
import gov.usgs.volcanoes.logger2csv.FileDataWriter;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A class to poll a single data logger.
 * 
 * @author Tom Parker
 */
public final class EbamPoller implements Poller {
  private static final Logger LOGGER = LoggerFactory.getLogger(EbamPoller.class);

  private final EbamDataLogger logger;

  /**
   * A Poller class for collecting data from a CompbellScientific data logger.
   * 
   * @param logger The DataLogger to poll
   */
  public EbamPoller(EbamDataLogger logger) {
    this.logger = logger;
  }

  public void updateFiles() {
    LOGGER.debug("Polling {}", logger.name);
    try {
      LOGGER.debug("Polling {}.", logger.name);

      ListIterator<CSVRecord> results;
      try {
        results = retrieveNewData();
      } catch (IOException e) {
        LOGGER.error("Cannot retrieve data from {}, I'll try again next time. ({})", logger.name,
            e.getLocalizedMessage());
        return;
      }

      if (!results.hasNext()) {
        LOGGER.info("No data retrieved from {}.", logger.name);
        return;
      }

      List<CSVRecord> headers = extractHeaders(results);

      if (!results.hasNext()) {
        LOGGER.debug("no data found");
        return;
      }

      // write new data
      FileDataWriter fileWriter = new EbamWriter(logger);
      fileWriter.addHeaders(headers);
      try {
        fileWriter.write(results);
      } catch (ParseException e) {
        LOGGER.error("Cannot parse logger response. Skipping {}", logger.name);
        return;
      } catch (IOException e) {
        LOGGER.error("Cannot write to datafile for {}.", logger.name);
        return;
      }
    } catch (PollerException e) {
      LOGGER.error("Unable to update {}, I'll try again next time. ({})", logger.name,
          e.getLocalizedMessage());
    }
  }

  private ListIterator<CSVRecord> retrieveNewData() throws IOException {
    ListIterator<CSVRecord> results;
    
    if (lastRecordNum > 0)
      results = since_record(lastRecordNum, table);
    else
      results = backFill((int) (logger.backfill * DAY_TO_S), table);

    return results;
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

    LOGGER.debug("Polling from {}", url);
    CSVParser parser = CSVParser.parse(url, StandardCharsets.UTF_8, logger.csvFormat);
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
