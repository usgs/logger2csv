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
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * A class to poll a single eBAM data logger over the network.
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
  public EbamPoller(final EbamDataLogger logger) {
    this.logger = logger;
  }

  /**
   * Update all files
   */
  public void updateFiles() {
    LOGGER.debug("Polling {}", logger.name);
    try {
      updateFile(DataFile.DATA_LOG);
      updateFile(DataFile.ERROR_LOG);
    } catch (PollerException e) {
      LOGGER.error("Unable to retrieve data from {}. ({})", logger.name, e);
    }
  }

  private int findLastRecordNum(final DataFile dataFile) throws PollerException {
    final FileDataReader fileReader = new FileDataReader(logger);
    final CSVRecord lastRecord = fileReader.findLastRecord(logger.getFilePattern(dataFile));
    if (lastRecord == null) {
      LOGGER.debug("No recent data file was found.");
      return -1;
    } else {
      final int recordNumIdx = lastRecord.size() - 1;
      LOGGER.debug("Most recent record num is {}", lastRecord.get(recordNumIdx));
      return Integer.parseInt(lastRecord.get(recordNumIdx));
    }
  }

  private void updateFile(final DataFile dataFile) throws PollerException {
    final EventLoopGroup group = new NioEventLoopGroup();
    try {
      final Bootstrap bStrap = new Bootstrap();
      final int recordIndex = findLastRecordNum(dataFile);
      final ChannelHandler handler = new EbamClientInitializer(logger, dataFile, recordIndex);
      bStrap.group(group).channel(NioSocketChannel.class).handler(handler);

      Channel chan;
      try {
        chan = bStrap.connect(logger.address, logger.port).sync().channel();
      } catch (InterruptedException e) {
        throw new PollerException(e);
      }

      LOGGER.debug("connected to {}.", logger.name);
      try {
        chan.closeFuture().sync();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

    } finally {
      group.shutdownGracefully();
    }

  }
}
