package gov.usgs.volcanoes.logger2csv.ebam;

import gov.usgs.volcanoes.logger2csv.FileDataWriter;
import gov.usgs.volcanoes.logger2csv.campbell.CampbellWriter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
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

  private EbamDataLogger logger;
  private DataFile dataFile;
  private List<CSVRecord> records;
  private CSVParser parser;
  private PipedWriter writer;

  public EbamHandler(EbamDataLogger logger, DataFile dataFile) throws IOException {
    this.logger = logger;
    this.dataFile = dataFile;
    writer = new PipedWriter();
    parser = new CSVParser(new PipedReader(writer), CSVFormat.RFC4180);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, String msg) throws IOException {
    writer.write(msg);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    LOGGER.debug("That's everything I'm going to get from {}.", logger.name);
  }
}

