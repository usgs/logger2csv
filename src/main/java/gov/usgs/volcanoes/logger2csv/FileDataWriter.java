/*
 * I waive copyright and related rights in the this work worldwide through the CC0 1.0 Universal
 * public domain dedication. https://creativecommons.org/publicdomain/zero/1.0/legalcode
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

  private final CSVFormat csvFormat;
  private final List<CSVRecord> headers;

  /**
   * Constructor.
   * @param csvFormat CSVFormat of file on disk
   *
   */
  public FileDataWriter(CSVFormat csvFormat) {
    headers = new ArrayList<CSVRecord>();
    this.csvFormat = csvFormat;
  }

  /**
   * Find a Date in a CSV record.
   *
   * @param record Record to search
   * @return the Date
   * @throws ParseException when format cannot be parsed
   */
  abstract protected Date getDate(CSVRecord record) throws ParseException;

  /**
   * Create a filename to hold a CSV record CSV record.
   *
   * @param record Record to be stored
   * @return the name of the file
   * @throws ParseException when format cannot be parsed
   */
  abstract protected File getFile(CSVRecord record) throws ParseException;


  /**
   * Add line to the headers.
   *
   * @param header header row
   */
  public final void addHeader(CSVRecord header) {
    headers.add(header);
  }

  /**
   * Add a list of rows to the header.
   *
   * @param headerList List of header rows to add
   */
  public final void addHeaders(List<CSVRecord> headerList) {
    headers.addAll(headerList);
  }

  /**
   * Write records to daily CSV files
   *
   * @param records records to write
   * @throws ParseException
   * @throws IOException
   */
  public final void write(Iterator<CSVRecord> records) throws ParseException, IOException {
    File workingFile = null;
    CSVPrinter printer = null;

    while (records.hasNext()) {
      final CSVRecord record = records.next();
      File thisFile = null;
      try {
        thisFile = getFile(record);
        LOGGER.debug("working file: {}", thisFile);
      } catch (final ParseException e) {
        if (printer != null) {
          close(printer);
          throw e;
        }
      }

      // new file?
      if (!thisFile.equals(workingFile)) {
        workingFile = thisFile;
        if (printer != null) {
          close(printer);
        }

        try {
          printer = getPrinter(workingFile);
        } catch (final IOException e) {
          close(printer);
          throw e;
        }
      }
      
      printer.printRecord(record);
    }
  }
  
  private void close(Closeable open) {
    try {
      open.close();
    } catch (final IOException ignore) {
    }
  }

  
  private CSVPrinter getPrinter(File workingFile) throws IOException {
    CSVPrinter printer;
    if (workingFile.exists()) {
      final FileWriter writer = new FileWriter(workingFile);
      printer = new CSVPrinter(writer, csvFormat);
    } else {
      workingFile.getParentFile().mkdirs();
      final FileWriter writer = new FileWriter(workingFile, true);
      printer = new CSVPrinter(writer, csvFormat);
      printer.printRecords(headers);
    }

    return printer;
  }


}
