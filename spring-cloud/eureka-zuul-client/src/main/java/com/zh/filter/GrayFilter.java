package com.zh.filter;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Configuration;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import io.jmnarloch.spring.cloud.ribbon.support.RibbonFilterContextHolder;

@Configuration
public class GrayFilter extends ZuulFilter {

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();
		String ip = request.getRemoteAddr();
		// ipv6本地地址，也就是127.0.0.1
		if ("0:0:0:0:0:0:0:1".equals(ip)) {
			RibbonFilterContextHolder.getCurrentContext().add("route", "1");
		} else {
			RibbonFilterContextHolder.getCurrentContext().add("route", "2");
		}
		return null;
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public String filterType() {
		return PRE_TYPE;
	}

	@Override
	public int filterOrder() {
		return 0;
	}
}
