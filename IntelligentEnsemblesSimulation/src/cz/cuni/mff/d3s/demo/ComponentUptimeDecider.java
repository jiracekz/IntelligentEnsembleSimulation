package cz.cuni.mff.d3s.demo;

import java.util.Random;


public class ComponentUptimeDecider {
	
	private ComponentUptimeDeciderGenerator generator;
	private boolean[][] uptimeData;
	
	private static Random random;
	
	static {
		random = new Random(42);
	}
	
	public ComponentUptimeDecider(ComponentUptimeDeciderGenerator generator, int iterationCount) {
		this.generator = generator;
		uptimeData = new boolean[iterationCount][SimulationConstants.SoldierCount];
	}
	
	public void setInitStateFor(int componentId, boolean initState) {
		uptimeData[0][componentId] = initState;
	}
	
	public boolean shouldBeOnline(int id, long time) {
		
		return true;
/*		if(uptimeData == null)
			return true;
		
		return uptimeData[(int)(time / 1000)][id];*/
	}
	
	public void generateUptimeData() {		
		generator.generateUptimeData(uptimeData);
	}
	
	public static int selectRandomWithState(boolean[][] uptimeData, int iteration, boolean onlineState) {
		int num = 0;
		for (int i = 0; i < uptimeData[iteration].length; i++) {
			if (uptimeData[iteration][i] == onlineState) {
				num++;
			}
		}
		
		if (num == 0) {
			return -1;
		}
		
		int randomIndex = random.nextInt(num);
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
