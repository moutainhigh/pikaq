package io.github.pikaq.remoting.protocol.codec;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import io.github.pikaq.remoting.protocol.Packet;
import io.github.pikaq.remoting.protocol.PacketCodecHelper;
import io.github.pikaq.remoting.protocol.RemoteCommand;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

@ChannelHandler.Sharable
public class PacketCodecHandler extends MessageToMessageCodec<ByteBuf, RemoteCommand> {

	public static final PacketCodecHandler INSTANCE = new PacketCodecHandler();

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) {
		Packet packet = PacketCodecHelper.INSTANCE.decode(byteBuf);
		out.add(packet);
		LOG.debug("解码成功。{}", JSON.toJSONString(packet, true));
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, RemoteCommand remoteCommand, List<Object> out) {
		ByteBuf byteBuf = ctx.channel().alloc().ioBuffer();
		PacketCodecHelper.INSTANCE.encode(byteBuf, remoteCommand);
		out.add(byteBuf);
		LOG.debug("编码成功。{}", JSON.toJSONString(remoteCommand, true));
	}

	private static final Logger LOG = LoggerFactory.getLogger(PacketCodecHandler.class);
}