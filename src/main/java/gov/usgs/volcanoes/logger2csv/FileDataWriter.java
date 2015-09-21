/*
 * I waive copyright and related rights in the this work worldwide
 * through the CC0 1.0 Universal public domain dedication.
 * https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * A class to write CSV data to a file.
 * 
 * @author Tom Parker
 * 
 */
public abstract class FileDataWriter {
  private static final Logger LOGGER = LoggerFactory.getLogger(Logger2csv.class);
  public static final String FILE_EXTENSION = ".csv";


  private final List<CSVRecord> headers;
  private final CSVFormat csvFormat;
  
  abstract protected Date getDate(CSVRecord record) throws ParseException;
  abstract protected File getFile(CSVRecord record) throws ParseException;

  /**
   * Constructor.
   * 
   * @param filePattern filename pattern
   */
  public FileDataWriter(CSVFormat csvFormat) {
    headers = new ArrayList<CSVRecord>();
    this.csvFormat = csvFormat;
  }

  public void write(Iterator<CSVRecord> results) {
    File workingFile = null;
    CSVPrinter printer = null;

    while (results.hasNext()) {
      CSVRecord record = results.next();
      File thisFile = null;
      try {
        thisFile = getFile(record);
      } catch (ParseException e) {
        if (printer != null) {
          LOGGER.error("Unable to parse record. ({})", e.getLocalizedMessage());
          close(printer);
          return;
        }
      }

      if (!thisFile.equals(workingFile)) {
        workingFile = thisFile;
        if (printer != null) {
          close(printer);
        }
        try {
          printer = getPrinter(workingFile);
        } catch (IOException e) {
          LOGGER.error("Unable to open file. ({})", e.getLocalizedMessage());
          close(printer);
          return;
        }
      }
      
      try {
        printer.printRecord(record);
      } catch (IOException e) {
        LOGGER.error("Unable to write record. ({})", e.getLocalizedMessage());
        close(printer);
        return;
      }
    }
  }

  private void close(Closeable open) {
    try {
      open.close();
    } catch (IOException ignore) {
    }
  }
  
  private CSVPrinter getPrinter(File workingFile) throws IOException {
    CSVPrinter printer;
    if (workingFile.exists()) {
      FileWriter writer = new FileWriter(workingFile);
      printer = new CSVPrinter(writer, csvFormat);
    } else {
      FileWriter writer = new FileWriter(workingFile, true);
      printer = new CSVPrinter(writer, csvFormat);
      printer.printRecords(headers);
    }
    
    return printer;
  }

  public void addHeader(CSVRecord header) {
    headers.add(header);
  }

  public void addHeaders(List<CSVRecord> headerList) {
    headers.addAll(headerList);
  }

}
