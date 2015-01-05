package cz.cuni.mff.d3s.demo;

public class SimulationConstants {
	public static boolean IsCentralized = true;
	
	public static long KnowledgeTimeout = 4500;
	public static int SimulationLength = 60000;
	public static int SnapshotInterval = 1000;
	public static int IterationCount = SimulationConstants.SimulationLength / SimulationConstants.SnapshotInterval;
	public static int SoldierCount = 12;
	public static int SquadSize = 3;
	public static float SoldierDownProbability = 0.2f;
	public static float SoldierUpProbability = 0.2f;
	
	//TODO: Add Coordinates class (rewrite SoldierData) and a static list of goal coordinates 
}
