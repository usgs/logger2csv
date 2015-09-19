/*
 * I waive copyright and related rights in the this work worldwide
 * through the CC0 1.0 Universal public domain dedication.
 * https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */
package gov.usgs.volcanoes.logger2csv;

import com.opencsv.CSVWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import cern.colt.Arrays;

/**
 * A class to write CSV data to a file
 * 
 * @author Tom Parker
 * 
 */
public class FileDataWriter {
  private static final Logger LOGGER = LoggerFactory.getLogger(Logger2csv.class);
  public static final String FILE_EXTENSION = ".csv";

  private final SimpleDateFormat fileFormat;

  public FileDataWriter(String filePattern) {
    fileFormat = new SimpleDateFormat(filePattern);
  }

  public void write(Iterator<String[]> results, int lastRecord) throws ParseException, IOException {
    CSVWriter csvWriter = null;
    String workingFile = null;
    List<String[]> headers = new ArrayList<String[]>();

    for (int i = 0; i < logger.headerCount; i++)
      headers.add(results.next());

    while (results.hasNext()) {
      String[] line = results.next();
      LOGGER.debug("line: " + Arrays.toString(line));

      Date date = logger.parseDate(line[0]);
      String recordFile = logger.getFileName(table, date.getTime()) + FILE_EXTENSION;

      if (!recordFile.equals(workingFile)) {
        if (csvWriter != null)
          csvWriter.close();

        workingFile = recordFile;
        boolean newFile = false;
        File file = new File(workingFile);
        if (!file.exists()) {
          LOGGER.debug("Creating new file.");
          newFile = true;
          File parent = file.getParentFile();
          if (!parent.exists()) {
            LOGGER.debug("Creating new directory.");
            boolean result = file.getParentFile().mkdirs();
            if (!result)
              throw new IOException("Couldn't create directory: " + parent);
          }
        }

        Writer w = new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8");
        csvWriter = new CSVWriter(w);
        if (newFile)
          csvWriter.writeAll(headers, false);
        else if (Integer.parseInt(line[1]) == lastRecord)
          continue;
      }

      csvWriter.writeNext(line, logger.quoteFields);
    }

    if (csvWriter != null)
      csvWriter.close();
  }

}
