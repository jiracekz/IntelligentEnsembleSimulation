package cz.cuni.mff.d3s.demo;

public class SimulationConstants {
	public static boolean IsCentralized = false;
	
	public static long KnowledgeTimeout = 10000;
	public static int SimulationLength = 120002;
	public static int SnapshotInterval = 1000;
	public static int IterationCount = SimulationConstants.SimulationLength / SimulationConstants.SnapshotInterval;
	public static int SoldierCount = 32;
	public static float SoldierDownProbability = 0.2f;
	public static float SoldierUpProbability = 0.0f;
	
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
	
	public static int MinEnsembleSize = SoldierCount / TargetCoordinates.length - 2;
	public static int MaxEnsembleSize = SoldierCount / TargetCoordinates.length + 2;
	 
}
