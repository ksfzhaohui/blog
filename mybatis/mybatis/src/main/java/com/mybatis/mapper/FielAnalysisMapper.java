package com.mybatis.mapper;

import com.mybatis.vo.FileAnalysis;

public interface FielAnalysisMapper {

	public FileAnalysis selectFileAnalysis(String fileType);

	public void updateFileAnalysis(FileAnalysis fileAnalysis);

}
