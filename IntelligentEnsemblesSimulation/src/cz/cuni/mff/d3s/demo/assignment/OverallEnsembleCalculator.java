package cz.cuni.mff.d3s.demo.assignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cz.cuni.mff.d3s.deeco.task.ProcessContext;
import cz.cuni.mff.d3s.demo.Coordinates;
import cz.cuni.mff.d3s.demo.SimulationConstants;
import cz.cuni.mff.d3s.demo.components.SoldierData;

public class OverallEnsembleCalculator {

	private static class DistanceAndGroup {
		public double distance;
		public String soldierId;
		public int groupId;
		
		public DistanceAndGroup(double distance, String soldierId, int groupId) {
			this.distance = distance;
			this.soldierId = soldierId;
			this.groupId = groupId;
		}
		
		public DistanceAndGroup() {
			this.distance = Double.MAX_VALUE;
		}
	}
	
	public static int[] calculateEnsembles(Map<String, SoldierData> everyone) {
		Map<String, SoldierData> remaining = filterOldKnowledge(everyone);		
		
		int[] result = new int[SimulationConstants.SoldierCount];
		for (int i = 0; i < result.length; i++)
			result[i] = -1;
		
		boolean[] round = new boolean[SimulationConstants.TargetCoordinates.length];
		
		while (true) {
			
			List<DistanceAndGroup> possibilities = new ArrayList<DistanceAndGroup>();
			
			for (int targetIndex = 0; targetIndex < SimulationConstants.TargetCoordinates.length; targetIndex++) {
				if (round[targetIndex])
					continue; // already has assigned a component in this round
				
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
				
				possibilities.add(new DistanceAndGroup(minDistance, closestSoldier, targetIndex));	
			}
			
			if (possibilities.isEmpty())
				break;
			
			DistanceAndGroup minDistanceRound = new DistanceAndGroup();
			for (DistanceAndGroup record : possibilities) {
				if (record.distance < minDistanceRound.distance) {
					minDistanceRound = record;
				}
			}
			
			result[Integer.parseInt(minDistanceRound.soldierId)] = minDistanceRound.groupId;
			remaining.remove(minDistanceRound.soldierId);
			
			// record it in the round variable and if all targets have one assigned soldier in this round,
			// start next round
			assert !round[minDistanceRound.groupId];
			round[minDistanceRound.groupId] = true;
			
			boolean all = true;
			for (boolean b : round) {
				if (!b) {
					all = false;
					break;
				}
			}
			
			if (all) {
				for (int i = 0; i < round.length; i++) {
					round[i] = false;
				}
			}
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
