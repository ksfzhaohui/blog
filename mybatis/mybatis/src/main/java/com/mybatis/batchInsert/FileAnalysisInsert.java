package com.mybatis.batchInsert;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransientConnectionException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import com.mybatis.mapper.FielAnalysisMapper;
import com.mybatis.mapper.FileOrderMapper;
import com.mybatis.vo.FileAnalysis;
import com.mybatis.vo.FileOrder;
import com.mysql.jdbc.StringUtils;

public class FileAnalysisInsert {

	/**
	 * 默认一个批次提交的文件大小
	 **/
	private static final int TEMP_FILE_SIZE = 2 * 1024 * 1024;
	/**
	 * 默认指定查询换行符的缓冲区
	 **/
	private static final int BUFF_SIZE = 300;

	/**
	 * 一个批次提交的文件大小
	 **/
	private int batchFileSize = TEMP_FILE_SIZE;
	/**
	 * 指定查询换行符的缓冲区
	 **/
	private int buffSize = BUFF_SIZE;

	private SqlSessionFactory sqlSessionFactory;

	public FileAnalysisInsert(SqlSessionFactory sqlSessionFactory) {
		this.sqlSessionFactory = sqlSessionFactory;
	}

	public void batchInsertOrder(FileAnalysis fileAnalysis) throws Exception {
		FileInputStream fis = null;
		FileChannel inputChannel = null;
		String file = fileAnalysis.getFilePath() + File.separator + fileAnalysis.getFileName();
		long startPosition = fileAnalysis.getPosition() == null ? 0L : fileAnalysis.getPosition();
		String tempFile = file + System.currentTimeMillis() + "tmp";
		try {
			fis = new FileInputStream(file);
			inputChannel = fis.getChannel();
			final long fileSize = inputChannel.size();
			if (startPosition >= fileSize) {
				System.out.println("===文件已经解析完，无需执行解析逻辑===");
			} else {
				int count = (int) ((fileSize - startPosition) % batchFileSize == 0
						? (fileSize - startPosition) / batchFileSize : (fileSize - startPosition) / batchFileSize + 1);

				ByteBuffer byteBuffer = ByteBuffer.allocate(buffSize); // 申请一个缓存区
				long endPosition = batchFileSize + startPosition - buffSize;// 子文件结束位置

				long startTime, endTime;
				for (int i = 0; i < count; i++) {
					startTime = System.currentTimeMillis();
					if (i + 1 != count) {
						int read = inputChannel.read(byteBuffer, endPosition);// 读取数据
						readW: while (read != -1) {
							byteBuffer.flip();// 切换读模式
							byte[] array = byteBuffer.array();
							for (int j = 0; j < array.length; j++) {
								byte b = array[j];
								if (b == 10 || b == 13) { // 判断\n\r
									endPosition += j;
									break readW;
								}
							}
							endPosition += buffSize;
							byteBuffer.clear(); // 重置缓存块指针
							read = inputChannel.read(byteBuffer, endPosition);
						}
					} else {
						endPosition = fileSize; // 最后一个文件直接指向文件末尾
					}

					FileOutputStream fos = null;
					FileChannel outputChannel = null;
					try {
						fos = new FileOutputStream(tempFile);
						outputChannel = fos.getChannel();
						inputChannel.transferTo(startPosition, endPosition - startPosition, outputChannel);// 通道传输文件数据
					} finally {
						if (fos != null) {
							fos.close();
						}
						if (outputChannel != null) {
							outputChannel.close();
						}
					}
					endTime = System.currentTimeMillis();
					System.out.println("===临时文件生成花费:" + (endTime - startTime) + "ms===");
					startTime = System.currentTimeMillis();
					processBatchOrders(fileAnalysis, new File(tempFile), startPosition, endPosition);
					startPosition = endPosition + 1;
					System.out.println("===插入完成,下一次开始位置:" + startPosition + "===");
					endPosition += batchFileSize;
				}
			}
			updateFileAnalysisStatus(fileAnalysis, "1");
		} catch (Exception e) {
			// SQLTransientConnectionException和SQLNonTransientConnectionException,SQLTimeoutException可以进行重试
			if (e.getCause() instanceof SQLTransientConnectionException
					|| e.getCause() instanceof SQLNonTransientConnectionException
					|| e.getCause() instanceof SQLTimeoutException) {
				System.err.println("===RetryException wait retry,cause:" + e.getCause() + "===");
			} else {
				updateFileAnalysisStatus(fileAnalysis, "2");
			}
			System.err.println(getClass().getName() + " analysis error");
			throw e;
		} finally {
			if (fis != null) {
				fis.close();
			}
			if (inputChannel != null) {
				inputChannel.close();
			}
			// 删除临时文件
			File tmpFile = new File(tempFile);
			if (tmpFile.exists()) {
				tmpFile.delete();
			}
		}
	}

	protected void processBatchOrders(FileAnalysis fileAnalysis, File file, long startPosition, long endPosition)
			throws Exception {
		List<FileOrder> orderList = new ArrayList<FileOrder>();
		BufferedInputStream fis = null;
		BufferedReader reader = null;
		try {
			fis = new BufferedInputStream(new FileInputStream(file));
			reader = new BufferedReader(new InputStreamReader(fis, "utf-8"), 5 * 1024 * 1024);// 用5M的缓冲读取文本文件
			String row;
			int num = 0;
			while ((row = reader.readLine()) != null) {
				if (!StringUtils.isEmptyOrWhitespaceOnly(row)) {
					String order[] = row.split(",");
					FileOrder fileOrder = new FileOrder();
					fileOrder.setField1(order[0]);
					fileOrder.setField2(order[1]);
					fileOrder.setField3(order[2]);
					fileOrder.setField4(order[3]);
					fileOrder.setField5(order[4]);
					fileOrder.setField6(order[5]);
					fileOrder.setField7(order[6]);
					fileOrder.setField8(order[7]);
					fileOrder.setField9(order[8]);
					fileOrder.setField10(order[9]);
					fileOrder.setField11(order[10]);
					fileOrder.setField12(order[11]);
					fileOrder.setField13(order[12]);
					fileOrder.setField14(order[13]);
					fileOrder.setField15(order[14]);
					fileOrder.setField16(order[15]);
					fileOrder.setField17(order[16]);
					fileOrder.setField18(order[17]);
					fileOrder.setCrtTime(new Date());
					fileOrder.setUpdTime(new Date());
					orderList.add(fileOrder);
					num++;
				}
			}
			System.out.println("本次更新数据：" + num + "条");
		} finally {
			if (fis != null) {
				fis.close();
			}
			if (reader != null) {
				reader.close();
			}
		}

		// 保存订单和解析位置保证在一个事务中
		SqlSession session = sqlSessionFactory.openSession();
		try {
			long startTime = System.currentTimeMillis();
			FielAnalysisMapper fielAnalysisMapper = session.getMapper(FielAnalysisMapper.class);
			FileOrderMapper fileOrderMapper = session.getMapper(FileOrderMapper.class);
			fileOrderMapper.batchInsert(orderList);

			// 更新上次解析到的位置，同时指定更新时间
			fileAnalysis.setPosition(endPosition + 1);
			fileAnalysis.setStatus("3");
			fileAnalysis.setUpdTime(new Date());
			fielAnalysisMapper.updateFileAnalysis(fileAnalysis);
			session.commit();
			long endTime = System.currentTimeMillis();
			System.out.println("===插入数据花费:" + (endTime - startTime) + "ms===");
		} finally {
			session.close();
		}
	}

	private void updateFileAnalysisStatus(FileAnalysis fileAnalysis, String status) {
		fileAnalysis.setStatus(status);
		SqlSession session = sqlSessionFactory.openSession();
		try {
			FielAnalysisMapper mapper = session.getMapper(FielAnalysisMapper.class);
			mapper.updateFileAnalysis(fileAnalysis);
			session.commit();
		} finally {
			session.close();
		}
	}

}
