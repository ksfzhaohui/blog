package com.dubboCommon;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.Serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProtobufSerialization implements Serialization {

	@Override
	public byte getContentTypeId() {
		return 10;
	}

	@Override
	public String getContentType() {
		return "x-application/protobuf";
	}

	@Override
	public ObjectOutput serialize(URL url, OutputStream out) throws IOException {
		return new ProtobufObjectOutput(out);
	}

	@Override
	public ObjectInput deserialize(URL url, InputStream is) throws IOException {
		return new ProtobufObjectInput(is);
	}
}
