package cz.cuni.mff.d3s.demo;

import java.util.Collection;
import java.util.HashMap;

import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeManager;
import cz.cuni.mff.d3s.deeco.runtime.RuntimeFramework;
import cz.cuni.mff.d3s.deeco.scheduler.Scheduler;
import cz.cuni.mff.d3s.deeco.task.CustomStepTask;
import cz.cuni.mff.d3s.deeco.task.TimerTask;
import cz.cuni.mff.d3s.deeco.task.TimerTaskListener;
import cz.cuni.mff.d3s.demo.audit.SimulationController;

public class AuditListener implements TimerTaskListener {

	private SimulationController simulationController;
	
	private RuntimeFramework[] runtimes;
	
	public AuditListener(SimulationController simulationController, RuntimeFramework[] runtimes) {
		this.simulationController = simulationController;
		this.runtimes = runtimes;
	}
	
	@Override
	public void at(long time, Object triger) {

		System.out.println("Audit at " + time);
		
		HashMap<String, KnowledgeManager> knowledgeManagers = new HashMap<>();
		for (int i = 0; i < runtimes.length; i++) {
			Collection<KnowledgeManager> locals = runtimes[i].getContainer().getLocals();
			KnowledgeManager manager = locals.iterator().next();
			knowledgeManagers.put(Integer.toString(i), manager);
		}
		
		simulationController.doAudit(time, knowledgeManagers);
		
		CustomStepTask task = (CustomStepTask) triger;
		task.scheduleNextExecutionAfter(SimulationConstants.SnapshotInterval);
	}

	@Override
	public TimerTask getInitialTask(Scheduler scheduler) {
		return new CustomStepTask(scheduler, this, 3);
	}

}
