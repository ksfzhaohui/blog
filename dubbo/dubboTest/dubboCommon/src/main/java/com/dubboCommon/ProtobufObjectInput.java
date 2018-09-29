package com.dubboCommon;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.dubbo.common.serialize.ObjectInput;

import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class ProtobufObjectInput implements ObjectInput {

	private ObjectInputStream input;

	public ProtobufObjectInput(InputStream inputStream) throws IOException {
		this.input = new ObjectInputStream(inputStream);
	}

	@Override
	public boolean readBool() throws IOException {
		return input.readBoolean();
	}

	@Override
	public byte readByte() throws IOException {
		return input.readByte();
	}

	@Override
	public short readShort() throws IOException {
		return input.readShort();
	}

	@Override
	public int readInt() throws IOException {
		return input.readInt();
	}

	@Override
	public long readLong() throws IOException {
		return input.readLong();
	}

	@Override
	public float readFloat() throws IOException {
		return input.readFloat();
	}

	@Override
	public double readDouble() throws IOException {
		return input.readDouble();
	}

	@Override
	public byte[] readBytes() throws IOException {
		int len = input.readInt();
		if (len < 0) {
			return null;
		} else if (len == 0) {
			return new byte[] {};
		} else {
			byte[] b = new byte[len];
			input.readFully(b);
			return b;
		}
	}

	@Override
	public String readUTF() throws IOException {
		return input.readUTF();
	}

	@Override
	public Object readObject() throws IOException, ClassNotFoundException {
		return input.readObject();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T readObject(Class<T> clazz) throws IOException {
		try {
			if (clazz == Map.class) {
				clazz = (Class<T>) HashMap.class;
			}
			Schema schema = RuntimeSchema.getSchema(clazz);
			T obj = clazz.newInstance();
			byte[] buffer = new byte[input.available()];
			input.read(buffer);
			ProtobufIOUtil.mergeFrom(buffer, obj, schema);
			return obj;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T readObject(Class<T> clazz, Type type) throws IOException, ClassNotFoundException {
		try {
			Schema schema = RuntimeSchema.getSchema(clazz);
			T obj = clazz.newInstance();
			byte[] buffer = new byte[input.available()];
			input.read(buffer);
			ProtobufIOUtil.mergeFrom(buffer, obj, schema);
			return obj;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
}
