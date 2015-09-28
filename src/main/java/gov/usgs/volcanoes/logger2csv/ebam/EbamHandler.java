package gov.usgs.volcanoes.logger2csv.ebam;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.List;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Handles a client-side channel.
 */
@Sharable
public class EbamHandler extends SimpleChannelInboundHandler<String> {
  private static final Logger LOGGER = LoggerFactory.getLogger(EbamHandler.class);

  private static final int HEADER_COUNT = 6;
  private EbamDataLogger logger;
  private DataFile dataFile;
  private List<CSVRecord> records;
  private CSVParser parser;
  private PipedWriter writer;
  private int recordIndex = -1;
private int headersFound = 0;

  public EbamHandler(EbamDataLogger logger, DataFile dataFile) throws IOException {
    this.logger = logger;
    this.dataFile = dataFile;
    writer = new PipedWriter();
    parser = new CSVParser(new PipedReader(writer), CSVFormat.RFC4180);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, String msg) throws IOException {
    LOGGER.debug("Recieved record: {}", msg.replace((char)0x1b, '`'));
    
    
    // Record index "RF3 R 32 L 32 X0750"
    if (recordIndex == -1 && msg.matches("RF. R \\d+")) {
      recordIndex = Integer.parseInt(msg.split("\\s+")[2]);
      LOGGER.debug("Found record index {}", recordIndex);
    } else if (headersFound < HEADER_COUNT){
      headersFound++;
    } else {
      writer.write(msg + "," + recordIndex++);
    }
      
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    LOGGER.debug("That's everything I'm going to get from {}.", logger.name);
  }
}

