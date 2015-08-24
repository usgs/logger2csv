package gov.usgs.volcanoes.logger2csv;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.util.ConfigFile;

public class DataLogger {
    /** my logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(Logger2csv.class);
    
    private static final String DEFAULT_FILE_PATH_FORMAT = "yyyy/MM";
    private static final String DEFAULT_FILE_SUFFIX_FORMAT = "-yyyyMMdd";
    private static final String DEFAULT_PATH_ROOT = "data";
    private static final String TOA5_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static final int HEADER_COUNT = 4;

    private ConfigFile config;
    public final String name;
    private Map<String, Integer> bookmarks;
    public final int maxAge;
    public final String pathRoot;
    public final String address;
    private final SimpleDateFormat filePathFormat;
    private final SimpleDateFormat fileSuffixFormat;
    private final List<String> tables;
    private final SimpleDateFormat dateFormat;
    
    public DataLogger(ConfigFile config) throws IOException {
        this.config = config;
        
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        
        name = config.getString("name");
        address = config.getString("address");
        maxAge = Integer.parseInt(config.getString("maxAge"));
        bookmarks = new HashMap<String, Integer>();
        pathRoot = config.getString("pathRoot", DEFAULT_PATH_ROOT);
        String path = config.getString("filePathFormat", DEFAULT_FILE_PATH_FORMAT);
        String suffix = config.getString("fileSuffixFormat", DEFAULT_FILE_SUFFIX_FORMAT);
        tables = config.getList("table");
        
        // TODO: find a better exception class
        if (tables == null)
        	throw new IOException("No tables found for " + name);
        
        filePathFormat = new SimpleDateFormat(path);
        fileSuffixFormat = new SimpleDateFormat(suffix);
        dateFormat = new SimpleDateFormat(TOA5_DATE_FORMAT);
        LOGGER.debug("creating logger {}", name);
    }
    
    public void setBookmarks(Map<String, Integer> bookmarks) {
        this.bookmarks = bookmarks;
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
