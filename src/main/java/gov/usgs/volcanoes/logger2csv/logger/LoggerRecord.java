/*
 * I waive copyright and related rights in the this work worldwide through the CC0 1.0 Universal
 * public domain dedication. https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv.logger;

import org.apache.commons.csv.CSVRecord;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Holder class for  single CSV record.
 * 
 * @author Tom Parker
 *
 */
public class LoggerRecord {

  /**  time in ms as returned by Date.getTime()  */
  public final long date;
  
  /** My CSVRecord */
  public final CSVRecord record;

  /**
   * Constructor.
   * 
   * @param date Time in ms as returned by Date.getTime()
   * @param record My CSVRecord
   */
  public LoggerRecord(long date, CSVRecord record) {
    this.date = date;
    this.record = record;
  }

  /**
   * Utility method to create a new LoggerRecord.
   * 
   * @param record My CSVRecord
   * @param dateFormat Format passed to SimpleDateFormat to parse the date field
   * @param dateIndex Index of date filed
   * @return A newly constructed LoggerRecord
   * @throws ParseException when date cannot be parsed
   */
  public static LoggerRecord fromCSVRecord(CSVRecord record, SimpleDateFormat dateFormat,
      int dateIndex) throws ParseException {
    String dateString = record.get(dateIndex);
    long date = dateFormat.parse(dateString).getTime();

    return new LoggerRecord(date, record);
  }

  /**
   * Utility method to create a list of LoggerRecords.
   * 
   * @param recordList List of CSVRecords
   * @param dateFormat Format passed to SimpleDateFormat to parse the date field
   * @param dateIndex Index of date filed
   * @return A newly constructed list of loggerRecords
   * @throws ParseException when any of the CSVRecords contain an unparsable date field
   */
  public static List<LoggerRecord> fromCSVList(ListIterator<CSVRecord> recordList,
      SimpleDateFormat dateFormat, int dateIndex) throws ParseException {
    List<LoggerRecord> loggerRecords = new ArrayList<LoggerRecord>();

    while (recordList.hasNext()) {
      CSVRecord record = recordList.next();
      String dateString = record.get(dateIndex);
      long date = dateFormat.parse(dateString).getTime();
      loggerRecords.add(new LoggerRecord(date, record));
    }

    return loggerRecords;
  }
}
