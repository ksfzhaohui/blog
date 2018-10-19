package com.serialization.jackson;

import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.serialization.bean.Bean2;

/**
 * 自定义反序列化类
 * 
 * @author hui.zhao.cfs
 *
 */
public class Bean2Deserializer extends StdDeserializer<Bean2> {
	private static final long serialVersionUID = 1L;

	protected Bean2Deserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public Bean2 deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		Bean2 bean2 = new Bean2();
		p.nextToken();
		do {
			String propName = p.getCurrentName();
			if (propName == null) {
				break;
			}
			if (p.getCurrentTokenId() != JsonTokenId.ID_FIELD_NAME && propName.equals("p1")) {
				bean2.setP1(p.getText());
			} else if (p.getCurrentTokenId() != JsonTokenId.ID_FIELD_NAME && propName.equals("p2")) {
				bean2.setP2(new BigDecimal(p.getText()));
			}
		} while (p.nextToken() != null);
		return bean2;
	}

}
