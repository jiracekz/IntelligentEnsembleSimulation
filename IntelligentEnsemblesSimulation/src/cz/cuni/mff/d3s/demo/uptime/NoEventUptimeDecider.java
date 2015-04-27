package cz.cuni.mff.d3s.demo.uptime;

import cz.cuni.mff.d3s.demo.SimulationConstants;

public class NoEventUptimeDecider implements ComponentUptimeDeciderGenerator {

	public NoEventUptimeDecider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void generateUptimeData(boolean[][] uptimeData) {
		// just copy the default settings
		for (int iteration = 1; iteration < uptimeData.length; iteration++) {
			for(int componentId = 0; componentId < SimulationConstants.SoldierCount; componentId++) {
				// Copy state from the previous iteration
				uptimeData[iteration][componentId] = uptimeData[iteration - 1][componentId];					
			}
		}
	}

}
