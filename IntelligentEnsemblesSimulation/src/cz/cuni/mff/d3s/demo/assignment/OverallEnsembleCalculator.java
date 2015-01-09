package cz.cuni.mff.d3s.demo.assignment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import cz.cuni.mff.d3s.deeco.task.ProcessContext;
import cz.cuni.mff.d3s.demo.Coordinates;
import cz.cuni.mff.d3s.demo.SimulationConstants;
import cz.cuni.mff.d3s.demo.components.SoldierData;

public class OverallEnsembleCalculator {

	public static int[] calculateEnsembles(Map<String, SoldierData> everyone) {
		Map<String, SoldierData> remaining = filterOldKnowledge(everyone);		
		
		int[] result = new int[SimulationConstants.SoldierCount];
		for (int i = 0; i < result.length; i++)
			result[i] = -1;
		
		int targetIndex = 0;
		while (true) {
			Coordinates target = SimulationConstants.TargetCoordinates[targetIndex];

			double minDistance = Double.MAX_VALUE;
			String closestSoldier = null;
			for (Entry<String, SoldierData> soldierEntry : remaining.entrySet()) {
				double distanceToTarget = soldierEntry.getValue().coords.getDistanceTo(target);
				if (distanceToTarget < minDistance) {
					minDistance = distanceToTarget;
					closestSoldier = soldierEntry.getKey();
				}
			}
			
			if (closestSoldier == null) {
				break; // no more unassigned soldiers
			}
			
			result[Integer.parseInt(closestSoldier)] = targetIndex;
			remaining.remove(closestSoldier);
			
			// we rotate the targets
			targetIndex = (targetIndex + 1) % SimulationConstants.TargetCoordinates.length;
		}
				
		/*
		for (int i = 0; i < SimulationConstants.SoldierCount; i++) {
			result[i] = i % SimulationConstants.TargetCoordinates.length;
		}
		*/
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
