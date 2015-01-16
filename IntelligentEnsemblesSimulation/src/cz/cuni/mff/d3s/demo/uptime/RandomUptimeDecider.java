package cz.cuni.mff.d3s.demo.uptime;

import java.util.Random;

import cz.cuni.mff.d3s.demo.SimulationConstants;

public class RandomUptimeDecider implements ComponentUptimeDeciderGenerator {
	
	private static Random generator;
	
	static {
		generator = new Random(42);		
	}
	
	@Override
	public void generateUptimeData(boolean[][] uptimeData) {
		for (int iteration = 1; iteration < uptimeData.length; iteration++) {
			for(int componentId = 0; componentId < SimulationConstants.SoldierCount; componentId++) {
				// Copy state from the previous iteration
				uptimeData[iteration][componentId] = uptimeData[iteration - 1][componentId];					
			}
				
			
			// Check for random event this round
			int soldierDownIndex = -1;
			int soldierUpIndex = -1;
			if(generator.nextFloat() <= SimulationConstants.SoldierDownProbability) {
				soldierDownIndex = ComponentUptimeDecider.selectRandomWithState(uptimeData, iteration, true);
			}
			
			if (generator.nextFloat() <= SimulationConstants.SoldierUpProbability) {
				soldierUpIndex = ComponentUptimeDecider.selectRandomWithState(uptimeData, iteration, false);
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


}
