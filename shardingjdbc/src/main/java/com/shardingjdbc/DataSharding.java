package com.shardingjdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;

/**
 * 数据分片
 * 
 * @author ksfzhaohui
 *
 */
public class DataSharding {

	public static void main(String[] args) {
		// 配置真实数据源
		Map<String, DataSource> dataSourceMap = new HashMap<>();

		// 配置第一个数据源
		BasicDataSource dataSource1 = new BasicDataSource();
		dataSource1.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource1.setUrl("jdbc:mysql://localhost:3306/ds0");
		dataSource1.setUsername("root");
		dataSource1.setPassword("root");
		dataSourceMap.put("ds0", dataSource1);

		// 配置第二个数据源
		BasicDataSource dataSource2 = new BasicDataSource();
		dataSource2.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource2.setUrl("jdbc:mysql://localhost:3306/ds1");
		dataSource2.setUsername("root");
		dataSource2.setPassword("root");
		dataSourceMap.put("ds1", dataSource2);

		// 配置Order表规则
		TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration("t_order", "ds${0..1}.t_order${0..1}");

		// 配置分库 + 分表策略
		orderTableRuleConfig.setDatabaseShardingStrategyConfig(
				new InlineShardingStrategyConfiguration("user_id", "ds${user_id % 2}"));
		orderTableRuleConfig.setTableShardingStrategyConfig(
				new InlineShardingStrategyConfiguration("order_id", "t_order${order_id % 2}"));

		// 配置分片规则
		ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
		shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);

		// 省略配置order_item表规则...
		// ...

		// 获取数据源对象
		try {
			DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig,
					new Properties());
			Connection conn = dataSource.getConnection();
			String sql = "insert into t_order (user_id,order_id) values (?,?)";
			PreparedStatement preparedStatement = conn.prepareStatement(sql);
			for (int i = 1; i <= 10; i++) {
				preparedStatement.setInt(1, 10 + i);
				preparedStatement.setInt(2, 1001 + i);
				preparedStatement.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
