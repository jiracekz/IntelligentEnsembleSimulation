package cz.cuni.mff.d3s.demo.assignment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import cz.cuni.mff.d3s.deeco.task.ProcessContext;
import cz.cuni.mff.d3s.demo.SimulationConstants;
import cz.cuni.mff.d3s.demo.components.SoldierData;

public class OverallEnsembleCalculator {

	public static int[] calculateEnsembles(Map<String, SoldierData> everyone) {
		everyone = filterOldKnowledge(everyone);		
		
		//TODO: Implement
		int[] result = new int[SimulationConstants.SoldierCount];
		for (int i = 0; i < SimulationConstants.SoldierCount; i++) {
			result[i] = i % SimulationConstants.TargetCoordinates.length;
		}
		
		return result;
	}

	public static Map<String, SoldierData> filterOldKnowledge(Map<String, SoldierData> everyone) {
		Map<String, SoldierData> newEveryone = new HashMap<>();

		// Filter out the old knowledge - could be done in a better way perhaps?
		for (Entry<String, SoldierData> entry : everyone.entrySet()) {			
			if(entry.getValue().isAlive(ProcessContext.getTimeProvider().getCurrentMilliseconds())) {
				newEveryone.put(entry.getKey(), entry.getValue());
			}				
		}
		
		return newEveryone;
	}
}
