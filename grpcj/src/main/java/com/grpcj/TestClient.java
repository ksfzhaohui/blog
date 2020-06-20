package com.grpcj;

import java.util.concurrent.TimeUnit;

import com.grpcj.generate.TestRequest;
import com.grpcj.generate.TestResponse;
import com.grpcj.generate.TestServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class TestClient {

	private final ManagedChannel channel;
	private final TestServiceGrpc.TestServiceBlockingStub blockingStub;

	public TestClient(String host, int port) {
		channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

		blockingStub = TestServiceGrpc.newBlockingStub(channel);
	}

	public void shutdown() throws InterruptedException {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}

	public void greet(String name) {
		TestRequest request = TestRequest.newBuilder().setName(name).build();
		TestResponse response = blockingStub.sayHello(request);
		System.out.println(response.getMessage());

	}

	public static void main(String[] args) throws InterruptedException {
		TestClient client = new TestClient("127.0.0.1", 8001);
		System.out.println("-------------------客户端开始访问请求-------------------");
		for (int i = 0; i < 10; i++) {
			client.greet("你若想生存，绝处也能缝生: " + i);
		}
	}
}
