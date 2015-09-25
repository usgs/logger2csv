package gov.usgs.volcanoes.logger2csv.ebam;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;

/**
   * Handles a client-side channel.
   */
  @Sharable
  public class EbamHandler extends SimpleChannelInboundHandler<String> {
  
      @Override
      protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
          System.err.println(msg.replace((char)0x1b, ' '));
      }
  
      @Override
      public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof ReadTimeoutException) {
          System.out.println("Guess that's all.");
        }
          cause.printStackTrace();
          ctx.close();
      }
  }

