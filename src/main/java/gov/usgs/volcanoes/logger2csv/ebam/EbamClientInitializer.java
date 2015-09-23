package gov.usgs.volcanoes.logger2csv.ebam;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;

public class EbamClientInitializer extends ChannelInitializer<SocketChannel> {
  
      private static final StringDecoder DECODER = new StringDecoder();
      private static final StringEncoder ENCODER = new StringEncoder();
  
      private static final EbamClientHandler CLIENT_HANDLER = new EbamClientHandler();
  
  
      public EbamClientInitializer() {
      }
  
      @Override
      public void initChannel(SocketChannel ch) {
          ChannelPipeline pipeline = ch.pipeline();
  
          // Add the text line codec combination first,
          pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
          pipeline.addLast(DECODER);
          pipeline.addLast(ENCODER);
  
          // and then business logic.
          pipeline.addLast(CLIENT_HANDLER);
      }
  }
