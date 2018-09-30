package com.dubboCommon;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.alibaba.dubbo.common.serialize.ObjectOutput;

public class ProtobufObjectOutput implements ObjectOutput {

	private ObjectOutputStream outputStream;

	public ProtobufObjectOutput(OutputStream outputStream) throws IOException {
		this.outputStream = new ObjectOutputStream(outputStream);
	}

	@Override
	public void writeBool(boolean v) throws IOException {
		outputStream.writeBoolean(v);
	}

	@Override
	public void writeByte(byte v) throws IOException {
		outputStream.writeByte(v);
	}

	@Override
	public void writeShort(short v) throws IOException {
		outputStream.writeShort(v);
	}

	@Override
	public void writeInt(int v) throws IOException {
		outputStream.writeInt(v);
	}

	@Override
	public void writeLong(long v) throws IOException {
		outputStream.writeLong(v);
	}

	@Override
	public void writeFloat(float v) throws IOException {
		outputStream.writeFloat(v);
	}

	@Override
	public void writeDouble(double v) throws IOException {
		outputStream.writeDouble(v);
	}

	@Override
	public void writeBytes(byte[] v) throws IOException {
		outputStream.write(v);
	}

	@Override
	public void writeBytes(byte[] v, int off, int len) throws IOException {
		outputStream.write(v, off, len);
	}

	@Override
	public void writeUTF(String v) throws IOException {
		outputStream.writeUTF(v);
	}

	@Override
	public void writeObject(Object v) throws IOException {
		byte[] bytes = SerializationUtil.serialize(v);
		outputStream.write(bytes);
		outputStream.flush();
	}

	@Override
	public void flushBuffer() throws IOException {
		outputStream.flush();
	}
}