/*
 * I waive copyright and related rights in the this work worldwide
 * through the CC0 1.0 Universal public domain dedication.
 * https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv.campbell;

import gov.usgs.volcanoes.logger2csv.FileDataWriter;

import org.apache.commons.csv.CSVRecord;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A class to write data from a single data logger.
 * 
 * @author Tom Parker
 */
public class CampbellWriter extends FileDataWriter {

	private final SimpleDateFormat dateFormat;
	
	/**
	 * Constructor.
	 * 
	 * @param filePattern Patter used to create filenames
	 */
	public CampbellWriter(String filePattern) {
		super(filePattern);
		dateFormat = new SimpleDateFormat(CampbellDataLogger.DATE_FORMAT_STRING);
	}

	public Date getDate(CSVRecord record) throws ParseException {
		return dateFormat.parse(record.get(CampbellDataLogger.DATE_COLUMN));
	}
}
