package cz.cuni.mff.d3s.demo;


public class SimulationSnapshot {
	
	private static int snapshotCount = SimpleSimulationLauncher.SimulationLength / SimpleSimulationLauncher.SnapshotInterval;
	
	private static AuditData[][] snapshot;
	
	static {
		snapshot = new AuditData[snapshotCount][SimpleSimulationLauncher.SoldierCount];
	}
	
	public static void addSnapshot(int soldierId, int iteration, AuditData auditData) {
		snapshot[iteration][soldierId] = auditData;
	}
}
