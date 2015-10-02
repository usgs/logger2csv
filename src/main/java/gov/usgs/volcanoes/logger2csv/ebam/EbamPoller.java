/*
 * I waive copyright and related rights in the this work worldwide through the CC0 1.0 Universal
 * public domain dedication. https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv.ebam;

import gov.usgs.volcanoes.logger2csv.poller.Poller;
import gov.usgs.volcanoes.logger2csv.poller.PollerException;

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
  private static final char ESC = 0x1b;

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
//       updateFile(DataFile.ERROR_LOG);
      updateFile(DataFile.DATA_LOG);
    } catch (PollerException e) {
      LOGGER.error("Unable to retrieve data from {}. ({})", logger.name, e);
    }
  }

  private void updateFile(DataFile dataFile) throws PollerException {
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap();
      ChannelHandler handler = new EbamClientInitializer(logger, dataFile);
      b.group(group).channel(NioSocketChannel.class).handler(handler);

      Channel ch;
      try {
        ch = b.connect(logger.address, logger.port).sync().channel();
        // ch.pipeline().addLast(new EbamHandler(logger, dataFile));
      } catch (InterruptedException e) {
        throw new PollerException(e);
      }

      LOGGER.debug("connected");

      try {
        ch.writeAndFlush(ESC + "RF" + dataFile.value + " R\r\n");
        // Thread.sleep(2000);
        // ChannelFuture lastWriteFuture = ch.writeAndFlush(ESC + "PF" + dataFile + " 31\r\n");
        // Thread.sleep(4000);
        ChannelFuture lastWriteFuture = ch.writeAndFlush(ESC + "PF" + dataFile.value + " -15\r\n");
        // Thread.sleep(20000);
        // lastWriteFuture = ch.writeAndFlush(ESC + "PF" + dataFile + " 0\r\n");
        // Thread.sleep(2000);
        while (!lastWriteFuture.isDone()) {
          try {
            lastWriteFuture.sync();
          } catch (InterruptedException keepWaiting) {
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      // If user typed the 'bye' command, wait until the server closes
      // the connection.
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
