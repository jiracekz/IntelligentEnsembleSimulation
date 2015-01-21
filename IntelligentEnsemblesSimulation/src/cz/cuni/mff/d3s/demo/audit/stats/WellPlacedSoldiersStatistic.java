package cz.cuni.mff.d3s.demo.audit.stats;

import java.util.Map;

import cz.cuni.mff.d3s.demo.Coordinates;
import cz.cuni.mff.d3s.demo.SimulationConstants;
import cz.cuni.mff.d3s.demo.audit.AuditData;

public class WellPlacedSoldiersStatistic implements Statistic {

	@Override
	public String getName() {
		return "Num of well-placed soldiers";
	}
	
	public static int[] getNumAtTargets(Map<String, AuditData> soldierData) {
		int[] numAtTarget = new int[SimulationConstants.TargetCoordinates.length];
		for (AuditData ad : soldierData.values()) {
			if (ad.isOnline && ad.ensembleId >= 0) {
				Coordinates target = SimulationConstants.TargetCoordinates[ad.ensembleId];
				if (target.getDistanceTo(ad.soldierData.coords) < 0.01) {
					numAtTarget[ad.ensembleId]++;
				}
			}
		}
		
		return numAtTarget;
	}
	
	@Override
	public String calculate(Map<String, AuditData> soldierData) {
		int[] numAtTarget = getNumAtTargets(soldierData);
		int sum = 0;;
		for (int n : numAtTarget) {
			sum += n;
		}
		
		return Integer.toString(sum);
	}

}
