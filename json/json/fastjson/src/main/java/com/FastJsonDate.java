package com;

import java.util.Date;
import java.util.Locale;

import com.alibaba.fastjson.JSON;

public class FastJsonDate {
	public static void main(String[] args) {
//		Locale.setDefault(Locale.US);
//		GsonDateBean gsonDateBean = new GsonDateBean();
//		gsonDateBean.setDate(new Date());
//		String str = new Gson().toJson(gsonDateBean);
//		System.out.println(str);
		
		System.out.println("默认："+Locale.getDefault());
		System.out.println("重置语言环境：Locale.US");
		Locale.setDefault(Locale.US);
		String json = "{\"date\":\"2021-05-14 14:59:37\"}";
		String json2 = "{\"date\":\"2021年05月14日 14:59:37\"}";
		JacksonDateBean date = JSON.parseObject(json, JacksonDateBean.class);
		
		System.out.println(date);
	}
}


class JacksonDateBean {
	private Date date;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "GsonDateBean [date=" + date + "]";
	}

}
