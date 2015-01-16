package cz.cuni.mff.d3s.demo;

import java.util.Collection;
import java.util.HashMap;

import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.ReadOnlyKnowledgeManager;
import cz.cuni.mff.d3s.deeco.runtime.RuntimeFramework;
import cz.cuni.mff.d3s.deeco.simulation.TimerTaskListener;
import cz.cuni.mff.d3s.deeco.simulation.scheduler.SimulationScheduler;
import cz.cuni.mff.d3s.deeco.simulation.task.SimulationStepTask;
import cz.cuni.mff.d3s.deeco.simulation.task.TimerTask;
import cz.cuni.mff.d3s.demo.audit.SimulationController;

public class AuditListener implements TimerTaskListener {

	RuntimeFramework[] runtimes;
	
	public AuditListener(RuntimeFramework[] runtimes) {
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
		
		SimulationController.doAudit(time, knowledgeManagers);
		
		SimulationStepTask task = (SimulationStepTask) triger;
		task.scheduleNextExecutionAfter(SimulationConstants.SnapshotInterval);
	}

	@Override
	public TimerTask getInitialTask(SimulationScheduler scheduler) {
		return new SimulationStepTask(scheduler, this, 3);
	}

}
