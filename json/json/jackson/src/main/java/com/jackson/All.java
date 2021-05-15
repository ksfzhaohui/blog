package com.jackson;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class All {

	/**
	 * 基本类型
	 */
	private int pInt;
	private Integer pInteger;
	private long pLong;
	private Long pLONG;
	private double pDouble;
	private Double pDOUBLE;
	private byte pByte;
	private Byte pBYTE;
	private float pFloat;
	private Float pFLOAT;
	private short pShort;
	private Short pSHORT;
	private boolean pBoolean;
	private Boolean pBOOLEAN;
	private char pChar;
	private Character pCHAR;

	private AtomicInteger pAtomicInteger;
	private AtomicLong pAtomicLong;
	private AtomicBoolean pAtomicBoolean;
	private String pString;
	private Date pDate;
	private ByteBuffer pByteBuffer;

	/**
	 * 数组
	 */
	private boolean[] booleanArray;
	private byte[] byteArray;
	private char[] charArray;
	private short[] shortArray;
	private int[] intArray;
	private long[] longArray;
	private float[] floatArray;
	private double[] doubleArray;
	private String[] stringArray;

	private List<?> list;
	private Map<?, ?> map;

	public int getpInt() {
		return pInt;
	}

	public void setpInt(int pInt) {
		this.pInt = pInt;
	}

	public Integer getpInteger() {
		return pInteger;
	}

	public void setpInteger(Integer pInteger) {
		this.pInteger = pInteger;
	}

	public long getpLong() {
		return pLong;
	}

	public void setpLong(long pLong) {
		this.pLong = pLong;
	}

	public Long getpLONG() {
		return pLONG;
	}

	public void setpLONG(Long pLONG) {
		this.pLONG = pLONG;
	}

	public double getpDouble() {
		return pDouble;
	}

	public void setpDouble(double pDouble) {
		this.pDouble = pDouble;
	}

	public Double getpDOUBLE() {
		return pDOUBLE;
	}

	public void setpDOUBLE(Double pDOUBLE) {
		this.pDOUBLE = pDOUBLE;
	}

	public byte getpByte() {
		return pByte;
	}

	public void setpByte(byte pByte) {
		this.pByte = pByte;
	}

	public Byte getpBYTE() {
		return pBYTE;
	}

	public void setpBYTE(Byte pBYTE) {
		this.pBYTE = pBYTE;
	}

	public float getpFloat() {
		return pFloat;
	}

	public void setpFloat(float pFloat) {
		this.pFloat = pFloat;
	}

	public Float getpFLOAT() {
		return pFLOAT;
	}

	public void setpFLOAT(Float pFLOAT) {
		this.pFLOAT = pFLOAT;
	}

	public short getpShort() {
		return pShort;
	}

	public void setpShort(short pShort) {
		this.pShort = pShort;
	}

	public Short getpSHORT() {
		return pSHORT;
	}

	public void setpSHORT(Short pSHORT) {
		this.pSHORT = pSHORT;
	}

	public boolean ispBoolean() {
		return pBoolean;
	}

	public void setpBoolean(boolean pBoolean) {
		this.pBoolean = pBoolean;
	}

	public Boolean getpBOOLEAN() {
		return pBOOLEAN;
	}

	public void setpBOOLEAN(Boolean pBOOLEAN) {
		this.pBOOLEAN = pBOOLEAN;
	}

	public char getpChar() {
		return pChar;
	}

	public void setpChar(char pChar) {
		this.pChar = pChar;
	}

	public Character getpCHAR() {
		return pCHAR;
	}

	public void setpCHAR(Character pCHAR) {
		this.pCHAR = pCHAR;
	}

	public AtomicInteger getpAtomicInteger() {
		return pAtomicInteger;
	}

	public void setpAtomicInteger(AtomicInteger pAtomicInteger) {
		this.pAtomicInteger = pAtomicInteger;
	}

	public AtomicLong getpAtomicLong() {
		return pAtomicLong;
	}

	public void setpAtomicLong(AtomicLong pAtomicLong) {
		this.pAtomicLong = pAtomicLong;
	}

	public AtomicBoolean getpAtomicBoolean() {
		return pAtomicBoolean;
	}

	public void setpAtomicBoolean(AtomicBoolean pAtomicBoolean) {
		this.pAtomicBoolean = pAtomicBoolean;
	}

	public String getpString() {
		return pString;
	}

	public void setpString(String pString) {
		this.pString = pString;
	}

	public Date getpDate() {
		return pDate;
	}

	public void setpDate(Date pDate) {
		this.pDate = pDate;
	}

	public ByteBuffer getpByteBuffer() {
		return pByteBuffer;
	}

	public void setpByteBuffer(ByteBuffer pByteBuffer) {
		this.pByteBuffer = pByteBuffer;
	}

	public boolean[] getBooleanArray() {
		return booleanArray;
	}

	public void setBooleanArray(boolean[] booleanArray) {
		this.booleanArray = booleanArray;
	}

	public byte[] getByteArray() {
		return byteArray;
	}

	public void setByteArray(byte[] byteArray) {
		this.byteArray = byteArray;
	}

	public char[] getCharArray() {
		return charArray;
	}

	public void setCharArray(char[] charArray) {
		this.charArray = charArray;
	}

	public short[] getShortArray() {
		return shortArray;
	}

	public void setShortArray(short[] shortArray) {
		this.shortArray = shortArray;
	}

	public int[] getIntArray() {
		return intArray;
	}

	public void setIntArray(int[] intArray) {
		this.intArray = intArray;
	}

	public long[] getLongArray() {
		return longArray;
	}

	public void setLongArray(long[] longArray) {
		this.longArray = longArray;
	}

	public float[] getFloatArray() {
		return floatArray;
	}

	public void setFloatArray(float[] floatArray) {
		this.floatArray = floatArray;
	}

	public double[] getDoubleArray() {
		return doubleArray;
	}

	public void setDoubleArray(double[] doubleArray) {
		this.doubleArray = doubleArray;
	}

	public String[] getStringArray() {
		return stringArray;
	}

	public void setStringArray(String[] stringArray) {
		this.stringArray = stringArray;
	}

	public List<?> getList() {
		return list;
	}

	public void setList(List<?> list) {
		this.list = list;
	}

	public Map<?, ?> getMap() {
		return map;
	}

	public void setMap(Map<?, ?> map) {
		this.map = map;
	}

}
