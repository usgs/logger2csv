package gov.usgs.volcanoes.logger2csv;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.volcanoes.util.configFile.ConfigFile;

/**
 * A class to hold configuration and utility methods for a single data logger
 * 
 * @author Tom Parker
 * 
 *         I waive copyright and related rights in the this work worldwide
 *         through the CC0 1.0 Universal public domain dedication.
 *         https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */
public class DataLogger {
	private static final Logger LOGGER = LoggerFactory.getLogger(Logger2csv.class);

	public static final String DEFAULT_FILE_PATH_FORMAT = "yyyy/MM";
	public static final String DEFAULT_FILE_SUFFIX_FORMAT = "-yyyyMMdd";
	public static final String DEFAULT_PATH_ROOT = "data";
	public static final int DEFAULT_BACKFILL = 2;
	public static final boolean DEFAULT_QUOTE_FIELDS = false;
	public static final String TOA5_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final int HEADER_COUNT = 4;

	public final String name;
	public final int backfill;
	public final String pathRoot;
	public final String address;
	public final boolean quoteFields;
	
	private final SimpleDateFormat filePathFormat;
	private final SimpleDateFormat fileSuffixFormat;
	private final List<String> tables;
	private final SimpleDateFormat dateFormat;

	public DataLogger(ConfigFile config) throws IOException {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		name = config.getString("name");
		if (name == null)
			throw new IOException("Station name must be specified in Config.");
		LOGGER.debug("creating logger {}", name);

		address = config.getString("address");
		if (address == null)
			throw new IOException("Station address must be specified in Config.");

		backfill = config.getInt("backfill", DEFAULT_BACKFILL);
		pathRoot = config.getString("pathRoot", DEFAULT_PATH_ROOT);
		quoteFields = config.getBoolean("quoteFields", DEFAULT_QUOTE_FIELDS);
		tables = config.getList("table");
		if (tables == null || tables.isEmpty())
			throw new IOException("No tables found for " + name);

		String path = config.getString("filePathFormat", DEFAULT_FILE_PATH_FORMAT);
		filePathFormat = new SimpleDateFormat(path);

		String suffix = config.getString("fileSuffixFormat", DEFAULT_FILE_SUFFIX_FORMAT);
		fileSuffixFormat = new SimpleDateFormat(suffix);

		dateFormat = new SimpleDateFormat(TOA5_DATE_FORMAT);
	}

	public Iterator<String> getTableIterator() {
		return tables.iterator();
	}

	public Date parseDate(String date) throws ParseException {
		return dateFormat.parse(date);
	}

	public String getFileName(String table, long timeMs) {
		StringBuilder sb = new StringBuilder();
		sb.append(pathRoot);
		sb.append("/");
		sb.append(name);
		sb.append("/");
		sb.append(filePathFormat.format(timeMs));
		sb.append("/");
		sb.append(name);
		sb.append("-");
		sb.append(table);
		sb.append(fileSuffixFormat.format(timeMs));

		String filename = sb.toString().replace('/', File.separatorChar);
		return filename;
	}
}