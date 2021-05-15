package com.gson;

import java.util.Date;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonDate {
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
		GsonDateBean date = new Gson().fromJson(json, GsonDateBean.class);
//		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
//		GsonDateBean date = gson.fromJson(json, GsonDateBean.class);
		
		System.out.println(date);
	}
}


class GsonDateBean {
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
