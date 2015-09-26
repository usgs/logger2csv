package gov.usgs.volcanoes.logger2csv.logger;

import org.apache.commons.csv.CSVRecord;

public class LoggerRecord {

  public final long date;
  public final CSVRecord record;
  
  public LoggerRecord(long date, CSVRecord record) {
    this.date = date;
    this.record = record;
  }
}
