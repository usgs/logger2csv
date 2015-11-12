package gov.usgs.volcanoes.logger2csv.ebam.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

/**
 * A simple ebam "escape" command client.
 * 
 * Adapted from the Netty is Action echo client.
 *
 * @author Tom Parker
 */
public final class EbamClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(EbamClient.class);
  private static final char ESC = 0x1b;

  // uninstantiable
  private EbamClient() {}

  /**
   * E-BAM "escape command" client.
   * 
   * @param args <host> <port>
   * @throws InterruptedException when connection cannot be established
   * @throws IOException when commands cannot be read
   */
  public static void main(final String... args) throws InterruptedException, IOException  {
    if (args.length != 2) {
      LOGGER.error("Usage: " + EbamClient.class.getSimpleName() + " <host> <port>");
      return;
    }

    final String host = args[0];
    final int port = Integer.parseInt(args[1]);

    final EventLoopGroup group = new NioEventLoopGroup();
    try {
      final Bootstrap bootS = new Bootstrap();
      bootS.group(group).channel(NioSocketChannel.class)
          .remoteAddress(new InetSocketAddress(host, port))
          .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(final SocketChannel ch) {
              ch.pipeline().addLast(new LineBasedFrameDecoder(1024, true, true));
              ch.pipeline().addLast(new StringDecoder(CharsetUtil.US_ASCII));
              ch.pipeline().addLast(new EbamClientInboundHandler());
              ch.pipeline().addLast(new StringEncoder());
            }
          });

      final ChannelFuture connectF = bootS.connect().sync();
      final ChannelPipeline chanP = connectF.channel().pipeline();
      
      final BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
      for (;;) {
        final String line = input.readLine();
        if (line == null) {
          break;
        }

        final ChannelFuture commandF = chanP.writeAndFlush(ESC + line + "\r\n");
        commandF.awaitUninterruptibly();
      }
      
      connectF.channel().closeFuture().sync();
    } finally {
      group.shutdownGracefully().sync();
    }
  }
}
