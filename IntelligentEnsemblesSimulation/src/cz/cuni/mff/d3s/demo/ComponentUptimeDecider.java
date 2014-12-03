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
	
	public boolean shouldBeOnline(int id, long time) {
		if(uptimeData == null)
			return true;
		
		return uptimeData[(int)(time / 1000)][id];
	}
	
	public void generateUptimeData(int iterationCount)
	{
		uptimeData = new boolean[iterationCount][SimulationConstants.SoldierCount];		
		
		for (int iteration = 0; iteration < uptimeData.length; iteration++) {
			for(int componentId = 0; componentId < SimulationConstants.SoldierCount; componentId++) {
				// Default for first iteration: All components online
				boolean componentState = true;
				
				// Copy state from the previous iteration
				if(iteration != 0)
					componentState = uptimeData[iteration - 1][componentId];
				
				uptimeData[iteration][componentId] = componentState;					
			}
				
			
			// Check for random event this round
			if(generator.nextFloat() >= SimulationConstants.UptimeEventProbability)
				continue;	
			
			// Select the random component that fails or recovers and flip the state			
			Integer concernedComponentId = generator.nextInt(SimulationConstants.SoldierCount);
			uptimeData[iteration][concernedComponentId] = !uptimeData[iteration][concernedComponentId]; 
		}		
	}
}
