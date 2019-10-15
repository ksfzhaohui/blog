package com.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.mybatis.vo.FileOrder;

public interface FileOrderMapper {

	public void batchInsert(@Param("items") List<FileOrder> items);

}
