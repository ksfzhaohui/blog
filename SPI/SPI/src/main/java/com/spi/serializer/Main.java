package com.spi.serializer;

public class Main {

	public static void main(String[] args) throws Exception {
		Serialization serialization = SerializationManager.getSerialization("json");
		serialization.serialize(null);
		serialization.deserialize(null, null);

	}

}
