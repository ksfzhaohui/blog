package com.rocketmq.longpolling;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class Client {

	public static void main(String[] args) {
		while (true) {
			HttpURLConnection connection = null;
			try {
				URL url = new URL("http://localhost:8080");
				connection = (HttpURLConnection) url.openConnection();
				connection.setReadTimeout(10000);
				connection.setConnectTimeout(3000);
				connection.setRequestMethod("GET");
				connection.connect();
				if (200 == connection.getResponseCode()) {
					BufferedReader reader = null;
					try {
						reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
						StringBuffer result = new StringBuffer();
						String line = null;
						while ((line = reader.readLine()) != null) {
							result.append(line);
						}

						System.out.println("时间:" + new Date().toString() + "result =  " + result);

					} finally {
						if (reader != null) {
							reader.close();
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
		}
	}

}
