package zh.maven.SQuartz.listener;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;

public class MyTriggerListener implements TriggerListener {

	@Override
	public String getName() {
		return "MyTriggerListener";
	}

	@Override
	public void triggerFired(Trigger trigger, JobExecutionContext context) {
		System.out.println("triggerFired:" + trigger.toString());
	}

	@Override
	public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
		return false;
	}

	@Override
	public void triggerMisfired(Trigger trigger) {
		System.out.println("triggerMisfired:" + trigger.toString());
	}

	@Override
	public void triggerComplete(Trigger trigger, JobExecutionContext context,
			CompletedExecutionInstruction triggerInstructionCode) {
		System.out.println("triggerComplete:" + trigger.toString());
	}

}
