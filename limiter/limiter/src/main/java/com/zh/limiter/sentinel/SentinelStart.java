package com.zh.limiter.sentinel;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

public class SentinelStart {

	public static void main(String[] args) throws InterruptedException {
		// 配置规则.
		initFlowRules();

		while (true) {
			// 1.5.0 版本开始可以直接利用 try-with-resources 特性，自动 exit entry
			try (Entry entry = SphU.entry("HelloWorld")) {
				// 被保护的逻辑
				System.out.println("hello world");
			} catch (BlockException ex) {
				// 处理被流控的逻辑
				System.out.println("blocked!");
			}
			Thread.sleep(10);
		}
	}

	private static void initFlowRules() {
		List<FlowRule> rules = new ArrayList<>();
		FlowRule rule = new FlowRule();
		rule.setResource("HelloWorld");
		rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
		// Set limit QPS to 20.
		rule.setCount(20);
		rules.add(rule);
		FlowRuleManager.loadRules(rules);
	}
}
