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
  private final int recordIndex;

  public EbamClientInitializer(EbamDataLogger logger, DataFile dataFile, int recordIndex) {
    this.logger = logger;
    this.dataFile = dataFile;
    this.recordIndex = recordIndex;
  }

  @Override
  public void initChannel(SocketChannel ch) throws IOException {
    ChannelPipeline pipeline = ch.pipeline();

    // Decoders
    pipeline.addLast(new LineBasedFrameDecoder(1024, true, true));
    pipeline.addLast(new StringDecoder(CharsetUtil.US_ASCII));
    pipeline.addLast("readTimeoutHandler", new ReadTimeoutHandler(10));
    pipeline.addLast(new EbamHandler(logger, dataFile, recordIndex));

    // Encoders
    pipeline.addLast(new StringEncoder());
  }
}
