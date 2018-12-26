package com.spi.serializer;

public class JsonSerialization implements Serialization {

	@Override
	public byte[] serialize(Object obj) throws Exception {
		System.out.println("json serialize");
		return null;
	}

	@Override
	public <T> T deserialize(byte[] param, Class<T> clazz) throws Exception {
		System.out.println("json deserialize");
		return null;
	}

	@Override
	public String getName() {
		return "json";
	}

}
