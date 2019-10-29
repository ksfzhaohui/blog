package com.mybatis.batchInsert;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 模拟生成千万条数据
 * 
 * @author hui.zhao.cfs
 *
 */
public class FileCreateMain {

	public static void main(String[] args) throws IOException {
		FileWriter out = new FileWriter(new File("D://xxxxxxx//orders.txt"));
		for (int i = 0; i < 100; i++) {
			out.write(
					"vaule1,vaule2,vaule3,vaule4,vaule5,vaule6,vaule7,vaule8,vaule9,vaule10,vaule11,vaule12,vaule13,vaule14,vaule15,vaule16,vaule17,vaule18");
			out.write(System.getProperty("line.separator"));
		}
		out.close();
	}

}
