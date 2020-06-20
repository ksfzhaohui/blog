package com.grpcj;

import java.io.IOException;

import com.grpcj.generate.TestRequest;
import com.grpcj.generate.TestResponse;
import com.grpcj.generate.TestServiceGrpc;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class TestServer {

	private int port = 8001;
	private Server server;

	private void start() throws IOException {
		server = ServerBuilder.forPort(port).addService((BindableService) new TestHelloWorldImpl()).build().start();

		System.out.println("------------------- 服务端服务已开启，等待客户端访问 -------------------");

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {

				System.err.println("*** shutting down gRPC server since JVM is shutting down");
				TestServer.this.stop();
				System.err.println("*** server shut down");
			}
		});
	}

	private void stop() {
		if (server != null) {
			server.shutdown();
		}
	}

	private void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		final TestServer server = new TestServer();
		// 启动服务
		server.start();
		// 服务一直在线，不关闭
		server.blockUntilShutdown();
	}

	// 定义一个实现服务接口的类
	private class TestHelloWorldImpl extends TestServiceGrpc.TestServiceImplBase {

		@Override
		public void sayHello(TestRequest TestRequest, StreamObserver<TestResponse> responseObserver) {
			// 具体其他丰富的业务实现代码
			System.err.println("server:" + TestRequest.getName());
			TestResponse reply = TestResponse.newBuilder().setMessage(("响应信息: " + TestRequest.getName())).build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}
	}
}
