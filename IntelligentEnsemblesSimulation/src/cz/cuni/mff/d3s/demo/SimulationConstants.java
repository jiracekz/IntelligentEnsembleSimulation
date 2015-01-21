package cz.cuni.mff.d3s.demo;

public class SimulationConstants {
	public static boolean IsCentralized = true;
	
	public static long KnowledgeTimeout = 6000;
	public static int SimulationLength = 60000;
	public static int SnapshotInterval = 1000;
	public static int IterationCount = SimulationConstants.SimulationLength / SimulationConstants.SnapshotInterval;
	public static int SoldierCount = 12;
	public static float SoldierDownProbability = 0.2f;
	public static float SoldierUpProbability = 0.2f;
	
	public static int RandomSeed = 42;
	
	public static double MovementPerIteration = 2;
	public static double FieldWidth = 1000;
	public static double FieldHeight = 1000;
	public static Coordinates[] TargetCoordinates = new Coordinates[] {
		new Coordinates(10, 10),
		new Coordinates(10, FieldHeight - 10),
		new Coordinates(FieldWidth - 10, 10),
		new Coordinates(FieldWidth - 10, FieldHeight - 10)
	};
	 
}
