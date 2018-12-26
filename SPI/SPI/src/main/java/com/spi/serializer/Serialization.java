package com.spi.serializer;

public interface Serialization {

	/**
	 * 序列化
	 * 
	 * @param obj
	 * @return
	 */
	public byte[] serialize(Object obj) throws Exception;

	/**
	 * 反序列化
	 * 
	 * @param param
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public <T> T deserialize(byte[] param, Class<T> clazz) throws Exception;

	/**
	 * 序列化名称
	 * 
	 * @return
	 */
	public String getName();

}
