package cz.cuni.mff.d3s.demo.audit.stats;

import java.util.Map;

import cz.cuni.mff.d3s.demo.Coordinates;
import cz.cuni.mff.d3s.demo.SimulationConstants;
import cz.cuni.mff.d3s.demo.audit.AuditData;

public class NumOfGroupsCompleteStatistic implements Statistic {

	@Override
	public String getName() {
		return "Num. Groups Complete";
	}


	@Override
	public String calculate(Map<String, AuditData> soldierData) {
		int[] numAtTarget = WellPlacedSoldiersStatistic.getNumAtTargets(soldierData);
		int numComplete = 0;
		
		for (int num : numAtTarget) {
			if (num >= SimulationConstants.SoldierCount / SimulationConstants.TargetCoordinates.length &&
					num <= (SimulationConstants.SoldierCount - 1) / SimulationConstants.TargetCoordinates.length + 1) {
				numComplete++;
			}
		}
		
		return Integer.toString(numComplete);
	}

}
