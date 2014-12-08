package cz.cuni.mff.d3s.demo;

import cz.cuni.mff.d3s.deeco.simulation.TimerTaskListener;
import cz.cuni.mff.d3s.deeco.simulation.scheduler.SimulationScheduler;
import cz.cuni.mff.d3s.deeco.simulation.task.SimulationStepTask;
import cz.cuni.mff.d3s.deeco.simulation.task.TimerTask;

public class AuditListener implements TimerTaskListener {

	@Override
	public void at(long time, Object triger) {
		// TODO Auto-generated method stub
		System.out.println("Audit at " + time);
		SimulationController.doAudit((int)(time / SimulationConstants.SnapshotInterval));
		
		SimulationStepTask task = (SimulationStepTask) triger;
		task.scheduleNextExecutionAfter(SimulationConstants.SnapshotInterval);
	}

	@Override
	public TimerTask getInitialTask(SimulationScheduler scheduler) {
		return new SimulationStepTask(scheduler, this, 3);
	}

}
