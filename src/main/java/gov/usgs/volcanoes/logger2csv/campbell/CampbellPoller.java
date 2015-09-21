package gov.usgs.volcanoes.logger2csv.campbell;

import gov.usgs.volcanoes.logger2csv.FileDataReader;
import gov.usgs.volcanoes.logger2csv.FileDataWriter;
import gov.usgs.volcanoes.logger2csv.Poller;

import org.apache.commons.csv.CSVParser;
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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class CampbellPoller extends Poller {
	private static final Logger LOGGER = LoggerFactory.getLogger(CampbellPoller.class);

	private final CampbellDataLogger logger;

	public CampbellPoller(CampbellDataLogger logger) {
		this.logger = logger;
	}

	@Override
	public void updateFiles() {
		LOGGER.debug("Polling {}", logger.name);
		Iterator<String> tableIt = logger.getTableIterator();
		while (tableIt.hasNext()) {
			String table = tableIt.next();
			updateTable(table);
		}
	}

	private void updateTable(String table) {
		LOGGER.debug("Polling {}.{}", logger.name, table);

		int lastRecordNum = findLastRecordNum(table);

		// retrieve new data
		ListIterator<String[]> results;
		if (lastRecordNum > 0)
			results = since_record(lastRecordNum, table);
		else
			results = backFill((int) (logger.backfill * DAY_TO_S), table);

		if (!results.hasNext()) {
			LOGGER.info("No new data found in table {}.{}", logger.name, table);
			return;
		}
		
		if (recordMatches(results.next(), lastRecordNum)) {
			// do nothing and let the old value fall on the floor
		} else {
			// different record. Quick, put it back!
			results.previous();
		}
		
		
		// write new data
		FileDataWriter fileWriter = new FileDataWriter(logger.getFilePattern(table));
		try {
			fileWriter.write(results);
		} catch (ParseException e) {
			LOGGER.error("Cannot parse logger response. Skipping {}", logger.name);
			return;
		} catch (IOException e) {
			LOGGER.error("Cannot write to datafile for {}.{}.", logger.name, table);
			return;
		}

	}

	private boolean recordMatches(String[] recordString, int recordNum) {
		if (Integer.parseInt(recordString[0]) == recordNum)
			return true;
		else
			return false;
	}

	private int findLastRecordNum(String table) throws IOException {
		FileDataReader fileReader = new FileDataReader(logger);
		String[] lastRecord = fileReader.findLastRecord(logger.getFilePattern(table));
		return Integer.parseInt(lastRecord[1]);
	}

	public ListIterator<String[]> since_record(int record, String table) throws IOException {
		LOGGER.info("Downloading new records from {}.{}.", logger.name, table);
		return getResults("since-record", record, table);
	}

	public ListIterator<String[]> backFill(int backfillS, String table) throws IOException {
		LOGGER.info("Downloading all recent records from {}.{}.", logger.name, table);
		return getResults("backfill", backfillS, table);
	}

	private ListIterator<String[]> getResults(final String mode, final int p1, String table) throws IOException {
		List<String[]> records = new ArrayList<String[]>();

		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("http://");
			sb.append(logger.address);
			sb.append("/?command=DataQuery&uri=dl:");
			sb.append(table);
			sb.append("&mode=");
			sb.append(mode);
			sb.append("&format=TOA5");
			sb.append("&p1=");
			sb.append(p1);

			String url = sb.toString();
			HttpGet httpget = new HttpGet(url);

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
