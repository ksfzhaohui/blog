package com.spi.serializer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

public class SerializationManager {

	private static Map<String, Serialization> map = new HashMap<>();

	static {
		loadInitialSerializer();
	}

	private static void loadInitialSerializer() {
		ServiceLoader<Serialization> loadedSerializations = ServiceLoader.load(Serialization.class);
		Iterator<Serialization> iterator = loadedSerializations.iterator();

		try {
			while (iterator.hasNext()) {
				Serialization serialization = iterator.next();
				map.put(serialization.getName(), serialization);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static Serialization getSerialization(String name) {
		return map.get(name);
	}
}
