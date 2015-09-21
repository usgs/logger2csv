/*
 * I waive copyright and related rights in the this work worldwide
 * through the CC0 1.0 Universal public domain dedication.
 * https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */
package gov.usgs.volcanoes.logger2csv;

import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
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
 * A class to write CSV data to a file.
 * 
 * @author Tom Parker
 * 
 */
public abstract class FileDataWriter {
	private static final Logger LOGGER = LoggerFactory.getLogger(Logger2csv.class);
	public static final String FILE_EXTENSION = ".csv";

	private final SimpleDateFormat fileFormat;

	private final List<CSVRecord> headers;

	abstract protected Date getDate(CSVRecord record) throws ParseException;
	
	abstract protected File getFile(CSVRecord record) throws ParseException;
	
	/**
	 * Constructor.
	 * @param filePattern filename pattern
	 */
	public FileDataWriter(String filePattern) {
		headers = new ArrayList<CSVRecord>();
		fileFormat = new SimpleDateFormat(filePattern);
	}

	public void write(Iterator<CSVRecord> results) throws ParseException, IOException {
		File workingFile;
		
		while (results.hasNext()) {
			CSVRecord record = results.next();
			File thisFile = getFile(record);
			
			if (!thisFile.equals(workingFile)) {
				continue here!!!
			}
		}
	}

	public void addHeader(CSVRecord header) {
		headers.add(header);
	}

	public void setHeader(List<CSVRecord> headerList) {
		headers.addAll(headerList);
	}

}
