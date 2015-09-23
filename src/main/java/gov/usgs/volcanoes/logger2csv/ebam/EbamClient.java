package gov.usgs.volcanoes.logger2csv.ebam;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class EbamClient {

  static final boolean SSL = System.getProperty("ssl") != null;
  static final String HOST = System.getProperty("host", "127.0.0.1");
  static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8992" : "8023"));

  public static void main(String[] args) throws Exception {

    EventLoopGroup group = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap();
      b.group(group).channel(NioSocketChannel.class).handler(new EbamClientInitializer());

      // Start the connection attempt.
      Channel ch = b.connect(HOST, PORT).sync().channel();

      // Read commands from the stdin.
      ChannelFuture lastWriteFuture = null;

      // Sends the received line to the server.
      lastWriteFuture = ch.writeAndFlush("\r\n\r\n\r\n");

      // If user typed the 'bye' command, wait until the server closes
      // the connection.
      ch.closeFuture().sync();

      // Wait until all messages are flushed before closing the channel.
      if (lastWriteFuture != null) {
        lastWriteFuture.sync();
      }
    } finally
    {
      group.shutdownGracefully();
    }
  }
}
