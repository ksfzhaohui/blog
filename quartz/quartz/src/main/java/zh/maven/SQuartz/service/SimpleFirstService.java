package zh.maven.SQuartz.service;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleFirstService implements Serializable {

	private static final long serialVersionUID = 1L;

	public void service() {
		// System.out.println(new SimpleDateFormat("YYYYMMdd
		// HH:mm:ss").format(new Date()) + "---start SimpleFirstService");
		System.out.println(System.currentTimeMillis() + "---start SimpleFirstService");

		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(System.currentTimeMillis() + "---end SimpleFirstService");
		// System.out.println(new SimpleDateFormat("YYYYMMdd
		// HH:mm:ss").format(new Date()) + "---end SimpleFirstService");
	}
}