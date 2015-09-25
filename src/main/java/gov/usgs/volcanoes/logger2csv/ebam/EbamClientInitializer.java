package gov.usgs.volcanoes.logger2csv.ebam;

import java.io.IOException;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.CharsetUtil;

public class EbamClientInitializer extends ChannelInitializer<SocketChannel> {


  private final EbamDataLogger logger;
  private final DataFile dataFile;

  public EbamClientInitializer(EbamDataLogger logger, DataFile dataFile) {
    this.logger = logger;
    this.dataFile = dataFile;
  }

  @Override
  public void initChannel(SocketChannel ch) throws IOException {
    ChannelPipeline pipeline = ch.pipeline();

    // Decoders
    pipeline.addLast(new LineBasedFrameDecoder(8192));
    pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
    pipeline.addLast("readTimeoutHandler", new ReadTimeoutHandler(10));
    pipeline.addLast(new EbamHandler(logger, dataFile));

    // Encoders
    pipeline.addLast(new StringEncoder());
  }
}
