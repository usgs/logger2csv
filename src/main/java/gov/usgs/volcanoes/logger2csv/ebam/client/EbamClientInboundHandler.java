package gov.usgs.volcanoes.logger2csv.ebam.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 
 * @author Tom Parker
 *
 */
@Sharable
public class EbamClientInboundHandler extends SimpleChannelInboundHandler<String> {
  private static final char ESC = 0x1b;
  private static final Logger LOGGER = LoggerFactory.getLogger(EbamClientInboundHandler.class);


  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    LOGGER.info("connected.");
  }

  protected void channelRead0(final ChannelHandlerContext ctx, final String msgIn)
      throws IOException {
    System.out.println(msgIn.replace(ESC, '~'));
  }
}
