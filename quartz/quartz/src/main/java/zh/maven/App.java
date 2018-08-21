package zh.maven;

import java.util.Date;

import org.quartz.CronTrigger;
import org.quartz.DateBuilder;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.MutableTrigger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import zh.maven.SQuartz.listener.MyJobListener;

public class App {
	public static void main(String[] args) {
		final AbstractApplicationContext context = new ClassPathXmlApplicationContext("quartz.xml");
//		final StdScheduler scheduler = (StdScheduler) context.getBean("scheduler");
//		try {
//			Thread.sleep(4000);
//			scheduler.pauseTriggers(GroupMatcher.triggerGroupEquals("firstCronGroup"));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
}