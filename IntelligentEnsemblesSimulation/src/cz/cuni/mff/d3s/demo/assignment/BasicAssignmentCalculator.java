package cz.cuni.mff.d3s.demo.assignment;

import java.util.Map;

import cz.cuni.mff.d3s.demo.components.SoldierData;

public class BasicAssignmentCalculator implements SoldierAssignmentCalculator {

	@Override
	public int assignEnsemble(String id, SoldierData soldierData,
			Map<String, SoldierData> everyone) {
		int[] ensembles = OverallEnsembleCalculator.calculateEnsembles(everyone);
		return ensembles[Integer.parseInt(id)];
	}

}
