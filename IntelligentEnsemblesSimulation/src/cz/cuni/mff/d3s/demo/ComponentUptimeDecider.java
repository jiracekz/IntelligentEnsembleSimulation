package cz.cuni.mff.d3s.demo;

import java.util.Random;

import cz.cuni.mff.d3s.deeco.simulation.TimerTaskListener;
import cz.cuni.mff.d3s.deeco.simulation.scheduler.SimulationScheduler;
import cz.cuni.mff.d3s.deeco.simulation.task.SimulationStepTask;
import cz.cuni.mff.d3s.deeco.simulation.task.TimerTask;
import cz.cuni.mff.d3s.demo.components.Soldier;

public class ComponentUptimeDecider {
	private static Random generator;
	
	private boolean[][] uptimeData;
	
	static {
		generator = new Random(42);		
	}
	
	public ComponentUptimeDecider(int iterationCount) {
		uptimeData = new boolean[iterationCount][SimulationConstants.SoldierCount];
	}
	
	public void setInitStateFor(int componentId, boolean initState) {
		uptimeData[0][componentId] = initState;
	}
	
	public boolean shouldBeOnline(int id, long time) {
		if(uptimeData == null)
			return true;
		
		return uptimeData[(int)(time / 1000)][id];
	}
	
	public void generateUptimeData()
	{		
		for (int iteration = 1; iteration < uptimeData.length; iteration++) {
			for(int componentId = 0; componentId < SimulationConstants.SoldierCount; componentId++) {
				// Copy state from the previous iteration
				uptimeData[iteration][componentId] = uptimeData[iteration - 1][componentId];					
			}
				
			
			// Check for random event this round
			int soldierDownIndex = -1;
			int soldierUpIndex = -1;
			if(generator.nextFloat() <= SimulationConstants.SoldierDownProbability) {
				soldierDownIndex = selectRandomWithState(iteration, true);
			}
			
			if (generator.nextFloat() <= SimulationConstants.SoldierUpProbability) {
				soldierUpIndex = selectRandomWithState(iteration, false);
			}
			
			if (soldierDownIndex >= 0) {
				assert uptimeData[iteration][soldierDownIndex];
				uptimeData[iteration][soldierDownIndex] = false;
			}
			
			if (soldierUpIndex >= 0) {
				assert !uptimeData[iteration][soldierUpIndex];
				uptimeData[iteration][soldierUpIndex] = true;
			}
		}		
	}
	
	private int selectRandomWithState(int iteration, boolean onlineState) {
		int num = 0;
		for (int i = 0; i < uptimeData[iteration].length; i++) {
			if (uptimeData[iteration][i] == onlineState) {
				num++;
			}
		}
		
		if (num == 0) {
			return -1;
		}
		
		int randomIndex = generator.nextInt(num);
		int resultIndex = 0;
		while (true) {
			if (uptimeData[iteration][resultIndex] == onlineState) {
				if (randomIndex == 0) {
					return resultIndex;
				} else {
					randomIndex--;
				}
			}
			
			resultIndex++;
		}
	}
}
