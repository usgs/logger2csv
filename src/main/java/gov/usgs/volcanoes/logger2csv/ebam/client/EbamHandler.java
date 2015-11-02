/*
 * I waive copyright and related rights in the this work worldwide through the CC0 1.0 Universal
 * public domain dedication. https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv.ebam.client;

import gov.usgs.volcanoes.logger2csv.FileDataWriter;
import gov.usgs.volcanoes.logger2csv.logger.LoggerRecord;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ListIterator;
import java.util.Locale;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.CharsetUtil;

/**
 * Handles a client-side channel.
 */
@Sharable
public class EbamHandler extends SimpleChannelInboundHandler<String> {
  private static final Logger LOGGER = LoggerFactory.getLogger(EbamHandler.class);
  private static final char ESC = 0x1b;
  private static final long DAY_TO_MS = 24 * 60 * 60 * 1000;
  private static final int PREAMBLE_LENGTH = 5;

  private final EbamDataLogger logger;
  private final DataFile dataFile;
  private final StringBuffer records;

  private int recordIndex;
  private int headersFound = 0;

  /**
   * Constructor.
   * 
   * @param logger The eBAM I'm polling
   * @param dataFile The eBAM data file I'm polling
   * @param recordIndex The most recent record index already retrieved
   * @throws IOException then communication fails.
   */
  public EbamHandler(final EbamDataLogger logger, final DataFile dataFile, final int recordIndex) throws IOException {
    super();
    this.logger = logger;
    this.dataFile = dataFile;
    this.recordIndex = recordIndex;
    records = new StringBuffer();
  }

  @Override
  public void channelActive(final ChannelHandlerContext ctx) {
    if (recordIndex == -1) {
      final String msg = ESC + "RF" + dataFile.value + " R\r\n";
      ctx.writeAndFlush(Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
    } else {
      final String msg = String.format("PF%s %d", dataFile.value, recordIndex + 1);
      ctx.writeAndFlush(Unpooled.copiedBuffer(ESC + msg + "\r\n", CharsetUtil.UTF_8));
    }
  }

  @Override
  protected void channelRead0(final ChannelHandlerContext ctx, final String msgIn) throws IOException {
    LOGGER.debug("Recieved record: {}", msgIn.replace((char) 0x1b, '`'));


    // Record index "RF3 R 32 L 32 X0750"
    if (recordIndex == -1 && msgIn.matches(".*RF. R \\d+.*")) {
      recordIndex = Integer.parseInt(msgIn.split("\\s+")[2]);
//      recordIndex -= 15;
    recordIndex = 1;
      LOGGER.debug("Found record index {}", recordIndex);

//      final String msgOut = String.format("PF%s -15", dataFile.value);
    final String msgOut = String.format("PF%s 1", dataFile.value);
      LOGGER.debug("sending {}", msgOut);
      ctx.writeAndFlush(Unpooled.copiedBuffer(ESC + msgOut + "\r\n", CharsetUtil.UTF_8));
    } else {
      if (headersFound <= PREAMBLE_LENGTH) {
        headersFound++;
      } else if (dataFile.hasHeader && headersFound == PREAMBLE_LENGTH + 1) {
        records.append(msgIn).append(",Index\n");
        headersFound++;
      } else {
        records.append(msgIn + "," + recordIndex++ + "\n");
      }
    }
  }

  @Override
  public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
    if (cause instanceof ReadTimeoutException) {
      LOGGER.debug("That's everything I'm going to get from {}.", logger.name);
      writeData();
    } else {
      LOGGER.error("Error polling E-BAM. ({})", cause.getLocalizedMessage());
    }
  }

  private void writeData() {
    final FileDataWriter fileWriter =
        new FileDataWriter(logger.csvFormat, logger.getFilePattern(dataFile));
    
    final long ancientMs = System.currentTimeMillis() - logger.backfill * DAY_TO_MS;
    fileWriter.setEarliestTime(ancientMs);

    try {
      String recordsString = records.toString();
      recordsString = recordsString.replaceAll(",\\s+", ",");
      
      final CSVParser parser = CSVParser.parse(recordsString, CSVFormat.RFC4180);
      final ListIterator<CSVRecord> listIt = parser.getRecords().listIterator();
      if (dataFile.hasHeader)
        fileWriter.addHeader(listIt.next());

      // first line always repeated. Skip it.
      listIt.next();
      
      final SimpleDateFormat dateFormat = new SimpleDateFormat(EbamDataLogger.DATE_FORMAT, Locale.ENGLISH);
      fileWriter.write(
          LoggerRecord.fromCSVList(listIt, dateFormat, EbamDataLogger.DATE_COLUMN).listIterator());
    } catch (ParseException e) {
      LOGGER.error("Cannot parse logger response. Skipping {}. ({})", logger.name, e);
    } catch (IOException e) {
      LOGGER.error("Unable to write records for {}.{}. I'll try again next time. ({})", logger.name,
          dataFile, e.getLocalizedMessage());
    }
  }
}
