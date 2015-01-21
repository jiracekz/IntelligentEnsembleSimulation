package cz.cuni.mff.d3s.demo.audit.stats;

import java.util.Map;

import cz.cuni.mff.d3s.demo.Coordinates;
import cz.cuni.mff.d3s.demo.SimulationConstants;
import cz.cuni.mff.d3s.demo.audit.AuditData;

public class SumDistanceToTargetStatistic implements Statistic {

	@Override
	public String getName() {
		return "Sum Distance";
	}

	@Override
	public String calculate(Map<String, AuditData> soldierData) {
		double sum = 0.0;
		for (AuditData ad : soldierData.values()) {
			if (ad.isOnline && ad.ensembleId >= 0) {
				Coordinates target = SimulationConstants.TargetCoordinates[ad.ensembleId];
				sum += target.getDistanceTo(ad.soldierData.coords);
			}
		}
		
		return String.format("%.1f", sum);
	}

}
