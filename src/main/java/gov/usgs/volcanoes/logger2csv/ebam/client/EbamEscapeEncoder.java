package gov.usgs.volcanoes.logger2csv.ebam.client;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

@Sharable
public class EbamEscapeEncoder extends MessageToMessageEncoder<CharSequence> {

  private static final char ESC = 0x1b;

  @Override
  protected void encode(ChannelHandlerContext ctx, CharSequence msg, List<Object> out)
      throws Exception {
    if (msg.length() == 0) {
      return;
    } 
    
    char[] outMsg = new char[msg.length() + 1];
    outMsg[0] = ESC;
    for (int i=0; i < msg.length(); i++)
      outMsg[i+1] = msg.charAt(i);
    
    out.add(ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(outMsg), Charset.defaultCharset()));
  }
}
