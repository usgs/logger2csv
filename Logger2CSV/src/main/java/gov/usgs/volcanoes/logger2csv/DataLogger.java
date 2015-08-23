package gov.usgs.volcanoes.logger2csv;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.util.ConfigFile;

public class DataLogger {
    /** my logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(Logger2csv.class);

    private ConfigFile config;
    private InetAddress address;
    private String name;
    private Map<String, Integer> bookmarks;
    
    public DataLogger(ConfigFile config) throws UnknownHostException {
        this.config = config;
        
        address = InetAddress.getByName(config.getString("address"));
        name = config.getString("name");
        bookmarks = new HashMap<String, Integer>();
        LOGGER.debug("creating logger {}", name);
    }
    
    public void setBookmarks(Map<String, Integer> bookmarks) {
        this.bookmarks = bookmarks;
    }
}
