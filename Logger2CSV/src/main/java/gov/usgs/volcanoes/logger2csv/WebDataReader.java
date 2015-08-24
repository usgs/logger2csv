package gov.usgs.volcanoes.logger2csv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVParser;

/**
 * A class to read tables from a remote data logger, presenting the data in a
 * form familiar to the OpenCSV library.
 * 
 * @author Tom Parker
 * 
 *         I waive copyright and related rights in the this work worldwide
 *         through the CC0 1.0 Universal public domain dedication.
 *         https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */
public class WebDataReader {
	private static final Logger LOGGER = LoggerFactory.getLogger(Logger2csv.class);

	private final DataLogger dataLogger;
	private final String table;

	public WebDataReader(DataLogger logger, String table) {
		this.dataLogger = logger;
		this.table = table;
	}

	public Iterator<String[]> since_record(int record) throws IOException {
		return getResults("since-record", record);
	}

	public Iterator<String[]> backFill(int backfillS) throws IOException {
		return getResults("backfill", backfillS);
	}

	private Iterator<String[]> getResults(final String mode, final int p1) throws IOException {
		List<String[]> records = new ArrayList<String[]>();

		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("http://");
			sb.append(dataLogger.address);
			sb.append("/?command=DataQuery&uri=dl:");
			sb.append(table);
			sb.append("&mode=");
			sb.append(mode);
			sb.append("&format=TOA5");
			sb.append("&p1=");
			sb.append(p1);

			String url = sb.toString();
			HttpGet httpget = new HttpGet(url);

			System.out.println("Executing request " + httpget.getRequestLine());

			// Create a custom response handler
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

				public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}

			};
			String responseBody = httpclient.execute(httpget, responseHandler);
			CSVParser parser = new CSVParser();
			for (String line : responseBody.split("\\r?\\n"))
				records.add(parser.parseLineMulti(line));
		} finally {
			httpclient.close();
		}

		return records.iterator();
	}
}
