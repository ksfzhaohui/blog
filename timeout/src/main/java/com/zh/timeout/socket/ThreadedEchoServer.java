package com.zh.timeout.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ThreadedEchoServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int i=1;
		try {
			ServerSocket s=new ServerSocket(8189);
			//每当程序建立一个新的套接字连接，将创建一个新的线程来处理服务器和该客户端之间的连接，而
			//主程序将立即返回并等待下一个连接
			while(true){
				Socket incoming=s.accept();
				System.out.println("Spawning"+i);
				Runnable r=new ThreadedEchoHandler(incoming);
				Thread t=new Thread(r);
				t.start();
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

class ThreadedEchoHandler implements Runnable {

	private Socket incoming;

	public ThreadedEchoHandler(Socket i) {
		incoming = i;
	}

	@Override
	public void run() {

		try {
			try {
				InputStream inStream = incoming.getInputStream();
				Thread.sleep(100000);
				OutputStream outStream = incoming.getOutputStream();
				Scanner in = new Scanner(inStream);
				PrintWriter out = new PrintWriter(outStream, true);
				out.println("Hello! Enter BYE to Exit");
				boolean done = false;
				while (!done && in.hasNextLine()) {
					String line = in.nextLine();
					out.println("Echo:" + line);
					if (line.trim().equals("BYE"))
						done = true;
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				incoming.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
