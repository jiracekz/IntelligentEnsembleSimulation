package cz.cuni.mff.d3s.demo.assignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cz.cuni.mff.d3s.demo.Coordinates;
import cz.cuni.mff.d3s.demo.SimulationConstants;
import cz.cuni.mff.d3s.demo.components.SoldierData;

/*
 * See Documents/DEECo/protokol.txt for more detailed information about the exchange protocol.
 */

enum ReassignmentResult {
	MustStay, // the component must stay in the current ensemble, else hard conditions could be broken
	CanStay, // the component can stay in the ensemble, but can also leave without breaking hard conditions
	CannotStay // the component cannot stay in the ensemble without the hard conditions being broken
}

public class ProbabilisticAssignmentCalculator implements
		SoldierAssignmentCalculator {
	
	private final double CommonProbabilityPart = 0.1;
	private final double DecisionSpecificProbabilityPart = 0.9;
	private final int EnsembleCount = SimulationConstants.TargetCoordinates.length;
	
	private static Random generator;
		
	static {
		generator = new Random(SimulationConstants.RandomSeed);
	}
	
	public static double ensembleMembershipCost(int ensembleId, SoldierData soldierData) {
		if (ensembleId == -1)
			return 100000;
		return soldierData.coords.getDistanceTo(SimulationConstants.TargetCoordinates[ensembleId]);
	}
	
	public static int ensembleSize(int ensembleId, Map<String, SoldierData> everyone) {
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
		if (result == soldierData.ensembleId) {
			// no change, nothing to do
			return result;
		}
		
		// here the result is different than the current ensemble
		
		ReassignmentResult reassignment = keepEnsemble(soldierData, everyone);
		double costInCurrentEnsemble = ensembleMembershipCost(soldierData.ensembleId, soldierData);
		double costInNewEnsemble = ensembleMembershipCost(result, soldierData);
		int componentCountInNewEnsemble = ensembleSize(result, everyone);
		
		boolean canJoinNewEnsemble = (componentCountInNewEnsemble < SimulationConstants.MaxEnsembleSize);
		boolean randomChangeEnsemble = (generator.nextDouble() > 1.0 * EnsembleCount / SimulationConstants.SoldierCount);
		boolean switchToNewEnsemble = (reassignment == ReassignmentResult.CannotStay);
		switchToNewEnsemble |= (reassignment == ReassignmentResult.CanStay) && randomChangeEnsemble
				&& (componentCountInNewEnsemble < SimulationConstants.MinEnsembleSize
						|| canJoinNewEnsemble && costInNewEnsemble < costInCurrentEnsemble - 150);
		
		if (switchToNewEnsemble) {
			return result;
		} else {
			return soldierData.ensembleId;
		}
	}
	
	private int findBestEnsembleProbabilistic(SoldierData soldierData) {
		double[] distances = new double[EnsembleCount];
		double distanceSum = 0;
		for (int i = 0; i < EnsembleCount; i++) {
			distances[i] = 1.0 / (ensembleMembershipCost(i, soldierData) + 10);
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
		
		assert false;
		return EnsembleCount - 1;
	}
	
	/*
	 * @see ReassignmentResult
	 */
	private ReassignmentResult keepEnsemble(SoldierData soldierData, Map<String, SoldierData> everyone) {
		if (soldierData.ensembleId == -1)
			return ReassignmentResult.CannotStay;
		
		Coordinates myTarget = SimulationConstants.TargetCoordinates[soldierData.ensembleId];
		
		List<Double> distances = new ArrayList<Double>();
		for (SoldierData soldier : everyone.values()) {
			if (soldier.ensembleId.intValue() == soldierData.ensembleId) {
				distances.add(soldier.coords.getDistanceTo(myTarget));
			}
		}
		
		if (distances.size() <= SimulationConstants.MinEnsembleSize) {
			// it's OK, the ensemble is at the minimum size level
			return ReassignmentResult.MustStay;
		}
		
		Collections.sort(distances);
		double myDistance = soldierData.coords.getDistanceTo(myTarget);
		for (int i = 0; i < SimulationConstants.MaxEnsembleSize && i < distances.size(); i++) {
			if (myDistance < distances.get(i)) {
				// it's OK, we're close enough, someone another should go away
				if (i < SimulationConstants.MinEnsembleSize)
					return ReassignmentResult.MustStay;
				else
					return ReassignmentResult.CanStay;
			}
		}
		
		if (distances.size() > SimulationConstants.MaxEnsembleSize) {
			// the ensemble is overpopulated and we are too far
			return ReassignmentResult.CannotStay;
		} else {
			return ReassignmentResult.CanStay;
		}
	}

}
