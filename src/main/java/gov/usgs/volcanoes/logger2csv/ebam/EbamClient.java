/*
 * I waive copyright and related rights in the this work worldwide through the CC0 1.0 Universal
 * public domain dedication. https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv.ebam;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class EbamClient {

  private static final char ESC = 0x1b;
  
  public static void main(String[] args) throws Exception {
//
//    EventLoopGroup group = new NioEventLoopGroup();
//    try {
//      Bootstrap b = new Bootstrap();
//      b.group(group).channel(NioSocketChannel.class).handler(new EbamClientInitializer());
//     
//
//      Channel ch = b.connect(args[0], Integer.valueOf(args[1])).sync().channel();
//
//      System.err.println("connected");
//      
//      ChannelFuture lastWriteFuture = null;
//
//      
////      lastWriteFuture = ch.writeAndFlush(ESC + "RF2 R\r\n");
//      lastWriteFuture = ch.writeAndFlush(ESC + "PF2 413\r\n");
//      lastWriteFuture.sync();
//
//      Thread.sleep(10000);
//      // If user typed the 'bye' command, wait until the server closes
//      // the connection.
//      ch.closeFuture().sync();
//
//    } finally
//    {
//      group.shutdownGracefully();
//    }
  }
}
