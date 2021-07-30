package com.zh.script.groovy;

import java.util.Map;

public class ScriptRequest {
	private String expression;
	private Map<String, Object> paramMap;

	@Override
	public String toString() {
		return "ScriptRequest{" + "expression='" + expression + '\'' + ", paramMap=" + paramMap + '}';
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public Map<String, Object> getParamMap() {
		return paramMap;
	}

	public void setParamMap(Map<String, Object> paramMap) {
		this.paramMap = paramMap;
	}
}
