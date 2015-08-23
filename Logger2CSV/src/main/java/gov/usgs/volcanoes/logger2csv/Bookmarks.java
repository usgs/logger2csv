package gov.usgs.volcanoes.logger2csv;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONWriter;

public class Bookmarks {
    public static final String DEFAULT_BOOKMARK_FILENAME = "logger2csv.bookmarks";

    private JSONObject bookmarks;
    private String bookmarksFile;

    public Bookmarks() {
        bookmarks = new JSONObject();
        bookmarksFile = DEFAULT_BOOKMARK_FILENAME;
    }

    public Bookmarks(String bookmarksFile) throws JSONException, FileNotFoundException {
        this.bookmarksFile = bookmarksFile;
        bookmarks = new JSONObject(new JSONTokener(new FileInputStream(bookmarksFile)));
    }

    public void write() throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(bookmarksFile);
        writer.println(bookmarks);
        writer.close();
    }

    public int getBookmark(String station) {
        return bookmarks.getInt(station);
    }
}
