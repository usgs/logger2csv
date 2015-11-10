package gov.usgs.volcanoes.logger2csv.ebam.client;

import java.io.BufferedReader;
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
public class EbamClient {
  private static final char ESC = 0x1b;

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Usage: " + EbamClient.class.getSimpleName() + " <host> <port>");
      return;
    }

    final String host = args[0];
    final int port = Integer.parseInt(args[1]);

    EventLoopGroup group = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap();
      b.group(group).channel(NioSocketChannel.class)
          .remoteAddress(new InetSocketAddress(host, port))
          .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
              ch.pipeline().addLast(new LineBasedFrameDecoder(1024, true, true));
              ch.pipeline().addLast(new StringDecoder(CharsetUtil.US_ASCII));
              ch.pipeline().addLast(new EbamClientInboundHandler());
              ch.pipeline().addLast(new StringEncoder());
            }
          });

      ChannelFuture f = b.connect().sync();


      ChannelPipeline cp = f.channel().pipeline();
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      for (;;) {
        String line = in.readLine();
        if (line == null) {
          break;
        }

        ChannelFuture cf = cp.writeAndFlush(ESC + line + "\r\n");
        cf.awaitUninterruptibly();
      }
      f.channel().closeFuture().sync();
    } finally {
      group.shutdownGracefully().sync();
    }
  }
}
