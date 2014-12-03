package cz.cuni.mff.d3s.demo;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.demo.components.Soldier;
import cz.cuni.mff.d3s.demo.components.SoldierData;
import cz.cuni.mff.d3s.demo.components.SoldierRole;


public class SimulationController {
	
	private static int snapshotCount = SimpleSimulationLauncher.SimulationLength / SimpleSimulationLauncher.SnapshotInterval;
	private static String dataSeparator = "\t";
	
	private static AuditData[][] snapshots;
	private static boolean[][] soldiersOnline;
	private static SoldierData[][] soldierSnapshots;
	private static Float[][] snapshotCorrectness;
	
	static {
		snapshots = new AuditData[snapshotCount][SimpleSimulationLauncher.SoldierCount];
		soldiersOnline = new boolean[snapshotCount][SimpleSimulationLauncher.SoldierCount];
		soldierSnapshots = new SoldierData[snapshotCount][SimpleSimulationLauncher.SoldierCount];
		snapshotCorrectness = new Float[snapshotCount][SimpleSimulationLauncher.SoldierCount];
	}
	
	public static void addSnapshot(int soldierId, int iteration, AuditData auditData, SoldierData soldierData) {
		soldiersOnline[iteration][soldierId] = true;
		snapshots[iteration][soldierId] = auditData;
		soldierSnapshots[iteration][soldierId] = soldierData;
	}
	
	public static void doAudit(int iteration) {
		
		Map<String, SoldierData> currentSoldierData = new HashMap<>();
		
		for (int i = 0; i < SimpleSimulationLauncher.SoldierCount; i++) {
			if (soldiersOnline[iteration][i]) {
				currentSoldierData.put(i + "", soldierSnapshots[iteration][i]);
			}
		}
		
		for (int i = 0; i < SimpleSimulationLauncher.SoldierCount; i++) {									
			AuditData correctAuditData = new AuditData();

			if (!soldiersOnline[iteration][i]) continue;
			
			ParamHolder<SoldierRole> roleHolder = new ParamHolder<>();
			ParamHolder<Integer> ensembleIdHolder = new ParamHolder<>();
			correctAuditData.componentsInEnsemble = Soldier.calculateEnsembles(i + "", 
					currentSoldierData, roleHolder, ensembleIdHolder);
			correctAuditData.role = roleHolder.value;
			
			AuditData snapshotAuditData = snapshots[iteration][i];
			
			snapshotCorrectness[iteration][i] = compareAuditData(snapshotAuditData, correctAuditData);
			
		}
			
		System.out.printf("Audit result: %.2f %%\n", getOverallAuditValue(iteration) * 100);
	}
	
	private static float getOverallAuditValue(int iteration) {
		float sum = 0;
		int n = 0;
		for (int i = 0; i < SimpleSimulationLauncher.SoldierCount; i++) {
			if (snapshotCorrectness[iteration][i] != null) {
				sum += snapshotCorrectness[iteration][i];
				n++;
			}
		}
		
		return sum / n;
	}
	
	private static float compareAuditData(AuditData snapshot, AuditData correct) {
		
		return snapshot.role == correct.role && snapshot.componentsInEnsemble.equals(correct.componentsInEnsemble)
				? 1f : 0f;
		
	}
	
	public static void SaveResults(String filename) throws IOException
	{				
		File outputFile = new File(filename);
		FileWriter fileWriter = new FileWriter(outputFile);
		BufferedWriter writer = new BufferedWriter(fileWriter);
		
		for(int iteration = 0; iteration < snapshotCount; ++iteration) {
			for(int componentId = 0; componentId < SimpleSimulationLauncher.SoldierCount; ++componentId) {
				Float correctness = snapshotCorrectness[iteration][componentId]; 
				
				if(correctness != null)
					writer.write(correctness.toString());
				else
					writer.write("X");
				
				if(componentId != SimpleSimulationLauncher.SoldierCount)
					writer.write(", ");
			}
			
			writer.write("\n");			
		}
		
		writer.close();
	}
}
