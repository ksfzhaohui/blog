package zh.maven.SQuartz.task;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import zh.maven.SQuartz.service.SecondService;

public class SecondTask extends QuartzJobBean {

	private SecondService secondService;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		secondService.service();
	}

	public void setSecondService(SecondService secondService) {
		this.secondService = secondService;
	}

}
