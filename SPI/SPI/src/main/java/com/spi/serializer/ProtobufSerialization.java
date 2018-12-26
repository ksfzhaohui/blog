package com.spi.serializer;

public class ProtobufSerialization implements Serialization {

	@Override
	public byte[] serialize(Object obj) throws Exception {
		System.out.println("protobuf serialize");
		return null;
	}

	@Override
	public <T> T deserialize(byte[] param, Class<T> clazz) throws Exception {
		System.out.println("protobuf deserialize");
		return null;
	}

	@Override
	public String getName() {
		return "protobuf";
	}

}
