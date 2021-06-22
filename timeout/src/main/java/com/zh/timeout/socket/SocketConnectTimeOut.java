package com.zh.timeout.socket;

import java.awt.image.SampleModel;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class SocketConnectTimeOut {

	public static void main(String[] args) throws IOException, InterruptedException {
		Socket socket = new Socket();
//		SocketAddress endpoint = new InetSocketAddress("10.19.112.111", 8189);
		SocketAddress endpoint = new InetSocketAddress("127.0.0.11", 8080);
//		SocketAddress endpoint = new InetSocketAddress("www.baidu.com", 80);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		try {
			System.out.println("s:" + sdf.format(new Date()));
			socket.connect(endpoint, 2000);
			socket.setSoTimeout(1000);
//			System.out.println(socket.getSoTimeout());

			InputStream inStream = null;
			//OutputStream outputStream = null;
			try {
				inStream = socket.getInputStream();
				//outputStream = socket.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//Scanner in = new Scanner(inStream);
			//PrintWriter out = new PrintWriter(outputStream, true);
			inStream.read();
//			while (in.hasNextLine()) {
//				System.out.println(line);
//				out.println("zhaohui");
//				out.println("BYE");
//			}
			System.out.println("end");
		} catch (Exception e) {
			System.out.println("e:" + sdf.format(new Date()));
			e.printStackTrace();
		} finally {
			socket.close();
		}
		

	}
}
