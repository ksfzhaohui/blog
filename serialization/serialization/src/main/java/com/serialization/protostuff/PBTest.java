package com.serialization.protostuff;

import java.math.BigDecimal;

import com.serialization.bean.Bean1;
import com.serialization.bean.Bean2;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

@SuppressWarnings("unchecked")
public class PBTest {

	public static void main(String[] args) {
		Bean1 bean1 = new Bean1("haha1", new BigDecimal("1.00"));
		Bean2 bean2 = new Bean2("haha2", new BigDecimal("2.00"));

//		LinkedBuffer buffer1 = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
//		Schema schema1 = RuntimeSchema.createFrom(bean1.getClass());
//		byte[] bytes1 = ProtostuffIOUtil.toByteArray(bean1, schema1, buffer1);
//
//		Bean1 bean11 = new Bean1();
//		ProtostuffIOUtil.mergeFrom(bytes1, bean11, schema1);
//		System.out.println(bean11.toString());

		LinkedBuffer buffer2 = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
		Schema schema2 = RuntimeSchema.createFrom(bean2.getClass());
		byte[] bytes2 = ProtostuffIOUtil.toByteArray(bean2, schema2, buffer2);

		Bean2 bean22 = new Bean2();
		ProtostuffIOUtil.mergeFrom(bytes2, bean22, schema2);
		System.out.println(bean22.toString());

	}
}
