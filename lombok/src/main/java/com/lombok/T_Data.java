package com.lombok;

import lombok.Data;

/**
 * 注解在类上，相当于同时使用了@ToString、@EqualsAndHashCode、@Getter、@Setter和@RequiredArgsConstrutor这些注解，对于POJO类十分有用
 * 
 * @author hui.zhao
 *
 */
@Data
public class T_Data {
    private String name;
}
