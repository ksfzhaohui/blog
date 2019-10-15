package com.mybatis.vo;

import java.util.Date;

public class FileAnalysis {

	private Long id;
	/** 文件类型 01:类型1，02:类型2 **/
	private String fileType;
	/** 文件名称 **/
	private String fileName;
	/** 文件路径 **/
	private String filePath;
	/** 文件状态 0初始化；1成功；2失败：3处理中 **/
	private String status;
	/** 上一次处理完成的位置 **/
	private Long position;
	private Date crtTime;
	private Date updTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}

	public Date getCrtTime() {
		return crtTime;
	}

	public void setCrtTime(Date crtTime) {
		this.crtTime = crtTime;
	}

	public Date getUpdTime() {
		return updTime;
	}

	public void setUpdTime(Date updTime) {
		this.updTime = updTime;
	}

}
