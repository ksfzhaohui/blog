package com.netty.transport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class PlainNioServer {
	
	public void serve(int port) throws IOException {
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		ServerSocket ssocket = serverChannel.socket();
		InetSocketAddress address = new InetSocketAddress(port);
		ssocket.bind(address);
		Selector selector = Selector.open();
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		final ByteBuffer msg = ByteBuffer.wrap("Hi!\r\n".getBytes());
		for (;;) {
			try {
				selector.select();
			} catch (IOException ex) {
				ex.printStackTrace();
				// handle exception
				break;
			}
			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = readyKeys.iterator();
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				iterator.remove();
				try {
					if (key.isAcceptable()) {
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel client = server.accept();
						client.configureBlocking(false);
						client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, msg.duplicate());
						System.out.println("Accepted connection from " + client);
					}
					if (key.isWritable()) {
						SocketChannel client = (SocketChannel) key.channel();
						ByteBuffer buffer = (ByteBuffer) key.attachment();
						while (buffer.hasRemaining()) {
							if (client.write(buffer) == 0) {
								break;
							}
						}
						client.close();
					}
				} catch (IOException ex) {
					key.cancel();
					try {
						key.channel().close();
					} catch (IOException cex) {
						// ignore on close
					}
				}
			}
		}
	}
}
