package zh.maven.SQuartz.trigger;

import java.util.Date;

import org.quartz.Calendar;
import org.quartz.ScheduleBuilder;
import org.quartz.impl.triggers.AbstractTrigger;

public class MyTrigger extends AbstractTrigger {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void setNextFireTime(Date nextFireTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPreviousFireTime(Date previousFireTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void triggered(Calendar calendar) {
		// TODO Auto-generated method stub

	}

	@Override
	public Date computeFirstFireTime(Calendar calendar) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean mayFireAgain() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Date getStartTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStartTime(Date startTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setEndTime(Date endTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public Date getEndTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getNextFireTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getPreviousFireTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getFireTimeAfter(Date afterTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getFinalFireTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean validateMisfireInstruction(int candidateMisfireInstruction) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void updateAfterMisfire(Calendar cal) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateWithNewCalendar(Calendar cal, long misfireThreshold) {
		// TODO Auto-generated method stub

	}

	@Override
	public ScheduleBuilder getScheduleBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

}
