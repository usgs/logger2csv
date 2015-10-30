/*
 * I waive copyright and related rights in the this work worldwide through the CC0 1.0 Universal
 * public domain dedication. https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv.ebam;

import gov.usgs.volcanoes.logger2csv.FileDataReader;
import gov.usgs.volcanoes.logger2csv.poller.Poller;
import gov.usgs.volcanoes.logger2csv.poller.PollerException;

import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * A class to poll a single data logger.
 * 
 * @author Tom Parker
 */
public final class EbamPoller implements Poller {
  private static final Logger LOGGER = LoggerFactory.getLogger(EbamPoller.class);

  private final EbamDataLogger logger;

  /**
   * A Poller class for collecting data from a CompbellScientific data logger.
   * 
   * @param logger The DataLogger to poll
   */
  public EbamPoller(EbamDataLogger logger) {
    this.logger = logger;
  }

  public void updateFiles() {
    LOGGER.debug("Polling {}", logger.name);
    try {
      updateFile(DataFile.DATA_LOG);
    } catch (PollerException e) {
      LOGGER.error("Unable to retrieve data from {}. ({})", logger.name, e);
    }
  }

  private int findLastRecordNum(DataFile dataFile) throws PollerException {
    FileDataReader fileReader = new FileDataReader(logger);
    CSVRecord lastRecord = fileReader.findLastRecord(logger.getFilePattern(dataFile));
    if (lastRecord == null) {
      LOGGER.debug("No recent data file was found.");
      return -1;
    } else {
      LOGGER.debug("Most recent record num is {}", lastRecord.get(13));
      return Integer.parseInt(lastRecord.get(13));
    }
  }

  private void updateFile(DataFile dataFile) throws PollerException {
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap();
      int recordIndex = findLastRecordNum(dataFile);
      ChannelHandler handler = new EbamClientInitializer(logger, dataFile, recordIndex);
      b.group(group).channel(NioSocketChannel.class).handler(handler);

      Channel ch;
      try {
        ch = b.connect(logger.address, logger.port).sync().channel();
      } catch (InterruptedException e) {
        throw new PollerException(e);
      }

      LOGGER.debug("connected");
      try {
        ch.closeFuture().sync();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    } finally {
      group.shutdownGracefully();
    }

  }
}
