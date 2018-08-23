package zh.maven.SQuartz.service;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SecondService implements Serializable {

	private static final long serialVersionUID = 1L;

	public void service() {
		System.out.println(new SimpleDateFormat("YYYYMMdd HH:mm:ss").format(new Date()) + "---start SecondService");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(new SimpleDateFormat("YYYYMMdd HH:mm:ss").format(new Date()) + "---end SecondService");
	}
}