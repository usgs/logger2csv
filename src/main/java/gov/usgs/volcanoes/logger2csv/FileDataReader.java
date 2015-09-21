package gov.usgs.volcanoes.logger2csv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;

/**
 * A class to read CSV data from a file.
 * 
 * @author Tom Parker
 * 
 *         I waive copyright and related rights in the this work worldwide
 *         through the CC0 1.0 Universal public domain dedication.
 *         https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */
public class FileDataReader {
  private static final Logger LOGGER = LoggerFactory.getLogger(Logger2csv.class);

  private static final String FILE_EXTENSION = ".csv";

  private static final long DAY_TO_MS = 24 * 60 * 60 * 1000;

  private final DataLogger logger;

  public FileDataReader(DataLogger logger) {
    this.logger = logger;
  }

  public String[] findLastRecord(String fileNamePattern) throws IOException {
    LOGGER.debug("Finding last record for {}", logger.name);
    SimpleDateFormat dateFormat = new SimpleDateFormat(fileNamePattern);
    String[] lastLine = null;
    CSVReader reader = null;

    long timeMs = System.currentTimeMillis();
    long ancientMs = timeMs - logger.backfill * DAY_TO_MS;
    while (lastLine == null && timeMs > ancientMs) {
      String fileName = dateFormat.format(timeMs);
      LOGGER.debug("looking for {}", fileName);
      try {
        Reader fr = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
        reader = new CSVReader(fr);

        String[] nextLine;
        // skip header lines at the top
        for (int i = 0; i < logger.headerCount; i++)
          nextLine = reader.readNext();

        while ((nextLine = reader.readNext()) != null) {
          lastLine = nextLine;
        }
      } catch (FileNotFoundException dontCare) {
        LOGGER.debug("This is okay: {}", dontCare.getLocalizedMessage());
      } finally {
        if (reader != null)
          reader.close();
      }
      timeMs -= DAY_TO_MS;
    }

    return lastLine;
  }
}
