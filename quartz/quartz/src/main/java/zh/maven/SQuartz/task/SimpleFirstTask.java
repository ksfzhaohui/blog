package zh.maven.SQuartz.task;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import zh.maven.SQuartz.service.SimpleFirstService;

//@DisallowConcurrentExecution
public class SimpleFirstTask extends QuartzJobBean {

	private SimpleFirstService firstService;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		firstService.service();
	}

	public SimpleFirstService getFirstService() {
		return firstService;
	}

	public void setFirstService(SimpleFirstService firstService) {
		this.firstService = firstService;
	}

}
