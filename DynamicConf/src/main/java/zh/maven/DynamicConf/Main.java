package zh.maven.DynamicConf;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

	public static void main(String[] args) throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "spring-config.xml" });
		Person person = (Person) context.getBean("person");
		System.out.println(person.toString());
		// ZKWatcher zkwatcher = (ZKWatcher) context.getBean("zkwatcher");
		// while (true) {
		// Person p = new Person(zkwatcher.getKeyValue("/a2/m1"),
		// zkwatcher.getKeyValue("/a3/m1/v2"),
		// zkwatcher.getKeyValue("/a3/m1/v2/t2"));
		// System.out.println(p.toString());
		//
		// Thread.sleep(2000);
		// }
	}
}
