package gov.usgs.volcanoes.logger2csv.logger;

import org.apache.commons.csv.CSVRecord;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class LoggerRecord {

  public final long date;
  public final CSVRecord record;

  public LoggerRecord(long date, CSVRecord record) {
    this.date = date;
    this.record = record;
  }

  public static LoggerRecord fromCSVRecord(CSVRecord record, SimpleDateFormat dateFormat,
      int dateIndex) throws ParseException {
    String dateString = record.get(dateIndex);
    long date = dateFormat.parse(dateString).getTime();

    return new LoggerRecord(date, record);
  }

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
