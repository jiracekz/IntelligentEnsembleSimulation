package cz.cuni.mff.d3s.demo;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.demo.components.Soldier;
import cz.cuni.mff.d3s.demo.components.SoldierData;
import cz.cuni.mff.d3s.demo.components.SoldierRole;


public class SimulationController {
	//TODO: REWRITE
	private static String dataSeparator = "\t";
	
	private static AuditData[][] snapshots;
	private static boolean[][] soldiersOnline;
	private static SoldierData[][] soldierSnapshots;
	
	static {
		snapshots = new AuditData[SimulationConstants.IterationCount][SimulationConstants.SoldierCount];
		soldiersOnline = new boolean[SimulationConstants.IterationCount][SimulationConstants.SoldierCount];
		soldierSnapshots = new SoldierData[SimulationConstants.IterationCount][SimulationConstants.SoldierCount];
	}
	
	public static void addSnapshot(int soldierId, int iteration, AuditData auditData, SoldierData soldierData) {
		soldiersOnline[iteration][soldierId] = true;
		snapshots[iteration][soldierId] = auditData;
		soldierSnapshots[iteration][soldierId] = soldierData;
	}
	
	public static void doAudit(int iteration) {
		
		Map<String, SoldierData> currentSoldierData = new HashMap<>();
		
		for (int i = 0; i < SimulationConstants.SoldierCount; i++) {
			if (soldiersOnline[iteration][i]) {
				currentSoldierData.put(i + "", soldierSnapshots[iteration][i]);
			}
		}
		
		for (int i = 0; i < SimulationConstants.SoldierCount; i++) {									
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
			
		float auditResult = getOverallAuditValue(iteration);
		System.out.printf("Audit result: %.2f %%\n", getOverallAuditValue(iteration) * 100);
		System.out.println("-------------");
		
		if (auditResult == Float.NaN) {
			auditResult = 1;
		}
		
		auditResults[iteration] = auditResult;
	}
	
	private static float getOverallAuditValue(int iteration) {
		float sum = 0;
		int n = 0;
		for (int i = 0; i < SimulationConstants.SoldierCount; i++) {
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
	
	public static void PrintOverallResult() {
		float totalResult = 0f;
		for (float result : auditResults) {
			totalResult += result;
		}
		
		totalResult /= auditResults.length;
		System.out.printf("Average audit result: %.2f %%\n", totalResult * 100);
	}
	
	public static void SaveResults(String filename) throws IOException
	{				
		File outputFile = new File(filename);
		FileWriter fileWriter = new FileWriter(outputFile);
		BufferedWriter writer = new BufferedWriter(fileWriter);
		
		for(int iteration = 0; iteration < SimulationConstants.IterationCount; ++iteration) {
			for(int componentId = 0; componentId < SimulationConstants.SoldierCount; ++componentId) {
				Float correctness = snapshotCorrectness[iteration][componentId]; 
				
				if(correctness != null)
					writer.write(correctness.toString());
				else
					writer.write("X");
				
				if(componentId != SimulationConstants.SoldierCount)
					writer.write(", ");
			}
			
			writer.write("\n");			
		}
		
		writer.close();
	}
}
