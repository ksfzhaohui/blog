package zh.maven.SQuartz.listener;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

public class MyJobListener implements JobListener {

	@Override
	public String getName() {
		return "MyJobListener";
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		System.out.println("jobToBeExecuted:" + context.getJobDetail());
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {
		System.out.println("jobExecutionVetoed:" + context.getJobDetail());
	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		System.out.println("jobWasExecuted:" + context.getJobDetail());
	}

}
