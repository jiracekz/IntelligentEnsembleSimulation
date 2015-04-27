/**
 * 
 */
package cz.cuni.mff.d3s.demo;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import cz.cuni.mff.d3s.deeco.network.AbstractHost;
import cz.cuni.mff.d3s.deeco.network.DataReceiver;
import cz.cuni.mff.d3s.deeco.scheduler.Scheduler;
import cz.cuni.mff.d3s.deeco.simulation.NetworkDataHandler;
import cz.cuni.mff.d3s.deeco.task.TimerTaskListener;
import cz.cuni.mff.d3s.deeco.simulation.scheduler.SimulationScheduler;
import cz.cuni.mff.d3s.deeco.simulation.task.KnowledgeUpdateTask;
import cz.cuni.mff.d3s.deeco.task.TimerTask;

/**
 * @author Michal Kit
 * 
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class RandomDelayedKnowledgeDataHandler extends NetworkDataHandler implements TimerTaskListener {

	protected final Map<String, SimulationScheduler> schedulers;
	protected final long minDelay;
	protected final long maxDelay;
	
	private static Random generator;
	
	static {
		generator = new Random(SimulationConstants.RandomSeed);
	}
	
	public RandomDelayedKnowledgeDataHandler(long minDelay, long maxDelay) {
		this.minDelay = minDelay;
		this.maxDelay = maxDelay;
		assert this.maxDelay - this.minDelay >= 0 && this.maxDelay - this.minDelay < Integer.MAX_VALUE - 1;
		this.schedulers = new HashMap<>();
	}
	
	protected long getDelay() {
		return generator.nextInt((int)(maxDelay - minDelay + 1)) + minDelay;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cz.cuni.mff.d3s.deeco.network.KnowledgeDataSender#broadcastKnowledgeData
	 * (java.util.List)
	 */
	@Override
	public void networkBroadcast(AbstractHost from, Object data, Map<AbstractHost, Collection<DataReceiver>> receivers) {
		SimulationScheduler scheduler = schedulers.get(from.getHostId());
		//System.out.println("Broadcast request: " + from.getHostId() + " at " + scheduler.getCurrentMilliseconds());
		scheduler.addTask(new KnowledgeUpdateTask(scheduler, this, from.getHostId(), data, flattenReceivers(receivers.values()), getDelay()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cz.cuni.mff.d3s.deeco.network.KnowledgeDataSender#sendKnowledgeData(java
	 * .util.List, java.lang.String)
	 */
	@Override
	public void  networkSend(AbstractHost from, Object data, AbstractHost recipientHost, Collection<DataReceiver> recipientReceivers) {
		SimulationScheduler scheduler = schedulers.get(from.getHostId());
		scheduler.addTask(new KnowledgeUpdateTask(scheduler, this, data, recipientReceivers, getDelay()));
	}

	@Override
	public void at(long time, Object triger) {
		KnowledgeUpdateTask task = (KnowledgeUpdateTask) triger;
		double rssi = (task.getFrom() == null) ? DEFAULT_IP_RSSI : DEFAULT_MANET_RSSI;
		for (DataReceiver receiver : task.getReceivers()) {
			receiver.checkAndReceive(task.getData(), rssi);
		}
	}

	@Override
	public TimerTask getInitialTask(Scheduler scheduler) {
		schedulers.put(scheduler.getHost().getHostId(), scheduler);
		return null;
	}
	
	protected Collection<DataReceiver> flattenReceivers(Collection<Collection<DataReceiver>> receivers) {
		if (receivers == null) {
			return null;
		}
		Collection<DataReceiver> result = new HashSet<>();
		for (Collection<DataReceiver> innerReceivers: receivers) {
			result.addAll(innerReceivers);
		}
		return result;
	}
}
