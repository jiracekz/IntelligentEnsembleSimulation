package cz.cuni.mff.d3s.demo;

public class SingleEventUptimeDecider implements
		ComponentUptimeDeciderGenerator {

	private int iteration;
	private int numToAdd;
	private int numToKill;
	
	public SingleEventUptimeDecider(int iteration, int numToAdd, int numToKill) {
		this.iteration = iteration;
		this.numToAdd = numToAdd;
		this.numToKill = numToKill;
	}
	
	@Override
	public void generateUptimeData(boolean[][] uptimeData) {
		for (int iteration = 1; iteration < uptimeData.length; iteration++) {
			for(int componentId = 0; componentId < SimulationConstants.SoldierCount; componentId++) {
				// Copy state from the previous iteration
				uptimeData[iteration][componentId] = uptimeData[iteration - 1][componentId];					
			}
			
			if (iteration == this.iteration) {
				int[] indicesToAdd = new int[numToAdd];
				for (int i = 0; i < numToAdd; i++) {
					indicesToAdd[i] = ComponentUptimeDecider.selectRandomWithState(uptimeData, iteration, false);
					if (indicesToAdd[i] >= 0)
						uptimeData[iteration][indicesToAdd[i]] = true;
				}
				
				for (int i = 0; i < numToAdd; i++) {
					if (indicesToAdd[i] >= 0)
						uptimeData[iteration][indicesToAdd[i]] = false;
				}
				
				int[] indicesToRemove = new int[numToKill];
				for (int i = 0; i < numToKill; i++) {
					indicesToRemove[i] = ComponentUptimeDecider.selectRandomWithState(uptimeData, iteration, true);
					if (indicesToRemove[i] >= 0)
						uptimeData[iteration][indicesToRemove[i]] = false;
				}
				
				for (int i = 0; i < numToAdd; i++) {
					if (indicesToAdd[i] >= 0)
						uptimeData[iteration][indicesToAdd[i]] = true;
				}
			}
		}
	}

}
