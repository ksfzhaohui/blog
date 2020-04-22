package com.netty.echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Handler implementation for the echo client. It initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
public class EchoClientHandler extends ChannelInboundHandlerAdapter {

	private final ByteBuf firstMessage;

	/**
	 * Creates a client-side handler.
	 */
	public EchoClientHandler() {
		firstMessage = Unpooled.buffer(EchoClient.SIZE);
		for (int i = 0; i < firstMessage.capacity(); i++) {
			firstMessage.writeByte((byte) i);
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		ctx.writeAndFlush(firstMessage);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		System.out.println("client receive msg="+msg);
		ctx.write(msg);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// Close the connection when an exception is raised.
		cause.printStackTrace();
		ctx.close();
	}
}
