package gov.usgs.volcanoes.logger2csv.ebam.client;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class EbamClientOutboundHandler extends ChannelHandlerAdapter {
  private static final char ESC = 0x1b;

  private ChannelHandlerContext ctx;

  /**
   * Do nothing by default, sub-classes may override this method.
   */
  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    this.ctx = ctx;
  }

  public void send(String msg) {
    ctx.writeAndFlush(ESC + msg);
  }
}
