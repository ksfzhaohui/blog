package zh.maven.SQuartz.task;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import zh.maven.SQuartz.service.CalendarFirstService;

public class CalendarFirstTask extends QuartzJobBean {

	private CalendarFirstService firstService;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		firstService.service();
	}

	public CalendarFirstService getFirstService() {
		return firstService;
	}

	public void setFirstService(CalendarFirstService firstService) {
		this.firstService = firstService;
	}

}
