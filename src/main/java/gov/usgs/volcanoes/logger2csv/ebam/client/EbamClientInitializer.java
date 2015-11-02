/*
 * I waive copyright and related rights in the this work worldwide through the CC0 1.0 Universal
 * public domain dedication. https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */

package gov.usgs.volcanoes.logger2csv.ebam.client;

import java.io.IOException;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.CharsetUtil;

/**
 * Inializer for connection to eBAM.
 * 
 * @author Tom Parker
 *
 */
public class EbamClientInitializer extends ChannelInitializer<SocketChannel> {

  /**
   * Constructor.
   * 
   * @param logger My datalogger
   * @param dataFile The data file to poll
   * @param recordIndex The most recent record on disk, or -1 if none are found.
   */
  public EbamClientInitializer() {
    super();
  }

  @Override
  public void initChannel(final SocketChannel chan) throws IOException {
    final ChannelPipeline pipeline = chan.pipeline();

    // Decoders
    pipeline.addLast(new LineBasedFrameDecoder(1024, true, true));
    pipeline.addLast(new StringDecoder(CharsetUtil.US_ASCII));
    pipeline.addLast(new EbamEscapeDecoder());
    pipeline.addLast(new EbamHandler());

    // Encoders
    pipeline.addLast(new EbamEscapeEncoder());
    pipeline.addLast(new StringEncoder());
  }
}
