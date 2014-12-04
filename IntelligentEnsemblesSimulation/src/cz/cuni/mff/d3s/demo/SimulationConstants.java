package cz.cuni.mff.d3s.demo;

public class SimulationConstants {
	public static long KnowledgeTimeout = 600;
	public static int SimulationLength = 60000;
	public static int SnapshotInterval = 1000;
	public static int IterationCount = SimulationConstants.SimulationLength / SimulationConstants.SnapshotInterval;
	public static int SoldierCount = 3;
	public static int SquadSize = 3;
	public static float UptimeEventProbability = 0.3f;
}
