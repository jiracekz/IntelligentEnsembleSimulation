package cz.cuni.mff.d3s.demo.assignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cz.cuni.mff.d3s.demo.Coordinates;
import cz.cuni.mff.d3s.demo.SimulationConstants;
import cz.cuni.mff.d3s.demo.components.SoldierData;

public class ProbabilisticAssignmentCalculator implements
		SoldierAssignmentCalculator {
	
	private final double CommonProbabilityPart = 0.1;
	private final double DecisionSpecificProbabilityPart = 0.9;
	private final int EnsembleCount = SimulationConstants.TargetCoordinates.length;
	
	private static Random generator;
		
	static {
		generator = new Random(SimulationConstants.RandomSeed);
	}
	
	private static double ensembleMembershipCost(int ensembleId, SoldierData soldierData) {
		return soldierData.coords.getDistanceTo(SimulationConstants.TargetCoordinates[ensembleId]);
	}
	
	private static int ensembleSize(int ensembleId, Map<String, SoldierData> everyone) {
		int size = 0;
		for (SoldierData soldier : everyone.values()) {
			if (soldier.ensembleId.intValue() == ensembleId) {
				size++;
			}
		}
		
		return size;
	}
	
	// If the component is not assigned it returns a suitable ensemble id (chosen by weighted random).
	// If the component is already assigned it returns either the current ensemble id, or a new ensemble id
	// if the new ensemble would improve the cost function value (the new id is chosen randomly, it is possible
	// that the function returns current ensemble id even though there is an ensemble with lower cost).
	// If the current ensemble is overpopulated and the component figures out it should leave the ensemble,
	// new ensemble id is chosen (but it can be identical to the original one).
	@Override
	public int assignEnsemble(String id, SoldierData soldierData,
			Map<String, SoldierData> everyone) {
		
		if(EnsembleCount <= 0)
			return -1;
		else if(EnsembleCount == 1)
			return 0;
		
		int result = findBestEnsembleProbabilistic(soldierData);
		if (!keepEnsemble(soldierData, everyone)
				|| (ensembleMembershipCost(result, soldierData) < ensembleMembershipCost(soldierData.ensembleId, soldierData)
						&& ensembleSize(soldierData.ensembleId, everyone) > SimulationConstants.MinEnsembleSize)
						&& ensembleSize(result, everyone) < SimulationConstants.MaxEnsembleSize) {
			return result;
		} else {
			return soldierData.ensembleId;
		}
	}
	
	private int findBestEnsembleProbabilistic(SoldierData soldierData) {
		double[] distances = new double[EnsembleCount];
		double distanceSum = 0;
		for (int i = 0; i < EnsembleCount; i++) {
			distances[i] = 1.0 / ensembleMembershipCost(i, soldierData);
			distanceSum += distances[i];
		}
		
		double[] probabilities = new double[EnsembleCount];
		for (int i = 0; i < EnsembleCount; i++) {
			probabilities[i] = 1.0 / EnsembleCount * CommonProbabilityPart // common part
					+ distances[i] / distanceSum * DecisionSpecificProbabilityPart;
		}
		
		double randomValue = generator.nextDouble();
		double probabilityRangeEnd = 0;
		for (int i = 0; i < EnsembleCount; i++) {
			probabilityRangeEnd += probabilities[i];
			if (randomValue < probabilityRangeEnd) {
				return i;
			}
		}
		
		// this should not happen, probabilityRangeEnd should reach 1.0
		assert false;
		return EnsembleCount - 1;
	}
	
	// True if there is no need to change ensemble (the current one does not violate hard conditions).
	// In case the hard conditions are violated there still may be no need to abandon the ensemble,
	// the result is based on the position of this soldier and on the other components (from
	// the same ensemble) positions.
	private boolean keepEnsemble(SoldierData soldierData, Map<String, SoldierData> everyone) {
		if (soldierData.ensembleId == -1)
			return false;
		
		Coordinates myTarget = SimulationConstants.TargetCoordinates[soldierData.ensembleId];
		
		List<Double> distances = new ArrayList<Double>();
		for (SoldierData soldier : everyone.values()) {
			if (soldier.ensembleId.intValue() == soldierData.ensembleId) {
				distances.add(soldier.coords.getDistanceTo(myTarget));
			}
		}
		
		if (distances.size() <= SimulationConstants.MaxEnsembleSize) {
			// it's OK, the ensemble is not overpopulated
			return true;
		}
		
		Collections.sort(distances);
		double myDistance = soldierData.coords.getDistanceTo(myTarget);
		for (int i = 0; i < SimulationConstants.MaxEnsembleSize; i++) {
			if (myDistance < distances.get(i)) {
				// it's OK, we're close enough, someone another should go away
				return true;
			}
		}
		
		// the ensemble is overpopulated and we are too far
		return false;
	}

}
