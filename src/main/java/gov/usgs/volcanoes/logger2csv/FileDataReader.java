package gov.usgs.volcanoes.logger2csv;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

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

	public static final String FILE_EXTENSION = ".csv";

	private static final long DAY_TO_MS = 24 * 60 * 60 * 1000;
	
	private final DataLogger logger;
	private final String table;

	public FileDataReader(DataLogger logger, String table) {
		this.logger = logger;
		this.table = table;
	}

	public int findLastRecord() throws IOException {
		LOGGER.debug("Finding last record for {}", logger.name);
		CSVReader reader = null;

		int lr = -1;
		long timeMs = System.currentTimeMillis();
		long ancientMs = timeMs - logger.backfill * DAY_TO_MS;
		while (lr == -1 && timeMs > ancientMs) {
			String fileName = logger.getFileName(table, timeMs) + FILE_EXTENSION;
			LOGGER.debug("looking for {}", fileName);
			try {
			    Reader fr = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
				reader = new CSVReader(fr);
				lr = -1;
				String[] nextLine;

				// skip header lines at the top
				for (int i = 0; i < logger.headerCount; i++)
					nextLine = reader.readNext();

				while ((nextLine = reader.readNext()) != null) {
					lr = Integer.parseInt(nextLine[1]);
				}
			} catch (FileNotFoundException dontCare) {
				LOGGER.debug("This is okay: {}", dontCare.getLocalizedMessage());
			} finally {
				if (reader != null)
					reader.close();
			}
			timeMs -= DAY_TO_MS;
		}

		LOGGER.debug("found record {}.", lr);
		return lr;
	}
}
