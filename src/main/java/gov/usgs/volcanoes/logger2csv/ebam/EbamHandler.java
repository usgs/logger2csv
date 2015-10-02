package gov.usgs.volcanoes.logger2csv.ebam;

import gov.usgs.volcanoes.logger2csv.FileDataWriter;
import gov.usgs.volcanoes.logger2csv.logger.LoggerRecord;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PipedWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ListIterator;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;

/**
 * Handles a client-side channel.
 */
@Sharable
public class EbamHandler extends SimpleChannelInboundHandler<String> {
  private static final Logger LOGGER = LoggerFactory.getLogger(EbamHandler.class);

  private static final int HEADER_COUNT = 7;
  private EbamDataLogger logger;
  private DataFile dataFile;
  private CSVParser parser;
  private PipedWriter writer;
  private int recordIndex = -1;
  private int headersFound = 0;
  StringBuffer records;

  public EbamHandler(EbamDataLogger logger, DataFile dataFile) throws IOException {
    this.logger = logger;
    this.dataFile = dataFile;
    writer = new PipedWriter();
    records = new StringBuffer();
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, String msg) throws IOException {
    LOGGER.debug("Recieved record: {}", msg.replace((char) 0x1b, '`'));


    // Record index "RF3 R 32 L 32 X0750"
    if (recordIndex == -1 && msg.matches(".*RF. R \\d+.*")) {
      recordIndex = Integer.parseInt(msg.split("\\s+")[2]);
      LOGGER.debug("Found record index {}", recordIndex);
    }

    if (headersFound < HEADER_COUNT) {
      headersFound++;
    } else if (headersFound == HEADER_COUNT) {
      records.append(msg + ",Index\n");
      headersFound++;
    } else {
      records.append(msg + "," + recordIndex++ + "\n");
    }

  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    if (cause instanceof ReadTimeoutException) {
      LOGGER.debug("That's everything I'm going to get from {}.", logger.name);
      writeData();
    } else {
      LOGGER.error("Error polling E-BAM. ({})", cause.getLocalizedMessage());
    }
  }

  private void writeData() {
    FileDataWriter fileWriter =
        new FileDataWriter(logger.csvFormat, logger.getFilePattern(dataFile));

    try {
      CSVParser parser = CSVParser.parse(records.toString(), CSVFormat.RFC4180);
      ListIterator<CSVRecord> listIt = parser.getRecords().listIterator();
      fileWriter.addHeader(listIt.next());

      SimpleDateFormat dateFormat = new SimpleDateFormat(EbamDataLogger.DATE_FORMAT_STRING);
      fileWriter.write(
          LoggerRecord.fromCSVList(listIt, dateFormat, EbamDataLogger.DATE_COLUMN).listIterator());
    } catch (ParseException e) {
      LOGGER.error("Cannot parse logger response. Skipping {}", logger.name);
    } catch (IOException e) {
      LOGGER.error("Unable to write records for {}.{}. I'll try again next time. ({})", logger.name,
          dataFile, e.getLocalizedMessage());
    }
  }
}
