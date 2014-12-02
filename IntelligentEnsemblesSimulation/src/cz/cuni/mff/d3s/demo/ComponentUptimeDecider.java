package cz.cuni.mff.d3s.demo;

import java.util.Random;

import cz.cuni.mff.d3s.deeco.simulation.TimerTaskListener;
import cz.cuni.mff.d3s.deeco.simulation.scheduler.SimulationScheduler;
import cz.cuni.mff.d3s.deeco.simulation.task.SimulationStepTask;
import cz.cuni.mff.d3s.deeco.simulation.task.TimerTask;
import cz.cuni.mff.d3s.demo.components.Soldier;

public class ComponentUptimeDecider {
	private static float eventProbability = 0.6f;
	private static int eventPossibilityInterval = 1000;
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
	
	public void generateUptimeData()
	{
		if(generator.nextFloat() >= eventProbability) {
			System.out.println("No random events this round.");
		}
		
		uptimeData = new boolean[10][SimpleSimulationLauncher.SoldierCount];
		
		for (int i = 0; i < uptimeData.length; i++) {
			
		}
		
		Integer concernedComponentId = generator.nextInt(SimpleSimulationLauncher.SoldierCount);
	}
}
