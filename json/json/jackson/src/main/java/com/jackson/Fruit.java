package com.jackson;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jackson.impl.Apple;
import com.jackson.impl.Banana;

/**
 * @JsonTypeInfo来处理多态
 * 
 * @author hui.zhao
 *
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(value = { @JsonSubTypes.Type(value = Apple.class, name = "a"),
        @JsonSubTypes.Type(value = Banana.class, name = "b") })
public interface Fruit {

}
