package gov.usgs.volcanoes.logger2csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

public class FileDataReader  {
	/** my logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(Logger2csv.class);

	private static final String FILE_EXTENSION = ".csv";
	
	private static final int DAY_TO_MS = 24 * 60 * 60 * 1000;
	private DataLogger logger;

	public FileDataReader(DataLogger logger, String table) {
		this.logger = logger;
	}

	public int findLastRecord(String table) throws NumberFormatException, IOException {

		LOGGER.debug("Finding last record for {}", logger.name);
		CSVReader reader = null;

		int lr = -1;
		long timeMs = System.currentTimeMillis();
		long ancientMs = timeMs - logger.maxAge * DAY_TO_MS;
		while (lr == -1 && timeMs > ancientMs) {
			String fileName = logger.getFileName(table, timeMs) + FILE_EXTENSION;
			LOGGER.debug("looking for {}", fileName);
			try {
				FileReader fr = new FileReader(fileName);
				reader = new CSVReader(fr);
				lr = -1;
				String[] nextLine;

				// skip four header lines at the top
				for (int i = 0; i < 5; i++)
					nextLine = reader.readNext();
				while ((nextLine = reader.readNext()) != null) {
					lr = Integer.parseInt(nextLine[1]);
				}
			} catch (FileNotFoundException dontCare) {
				LOGGER.debug(dontCare.getLocalizedMessage());
			} finally {
				if (reader != null)
					reader.close();
			}
			timeMs -= DAY_TO_MS;
		}

		String fileName = "/Users/tomp/Downloads/AUSS_ChemData_Sec2-20150823.csv";

		LOGGER.debug("found record {}.", lr);
		return lr;
	}

}
