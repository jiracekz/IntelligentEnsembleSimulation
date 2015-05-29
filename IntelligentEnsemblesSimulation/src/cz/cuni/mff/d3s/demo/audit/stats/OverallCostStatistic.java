package cz.cuni.mff.d3s.demo.audit.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import cz.cuni.mff.d3s.demo.SimulationConstants;
import cz.cuni.mff.d3s.demo.assignment.ProbabilisticAssignmentCalculator;
import cz.cuni.mff.d3s.demo.audit.AuditData;
import cz.cuni.mff.d3s.demo.components.SoldierData;

public class OverallCostStatistic implements Statistic {

	@Override
	public String getName() {
		return "Overall Cost per Component";
	}
		
	private static Map<String, SoldierData> map(Map<String, AuditData> everyone) {
		Map<String, SoldierData> soldierDataMap = new HashMap<>();
		for (Entry<String, AuditData> entry : everyone.entrySet()) {
			soldierDataMap.put(entry.getKey(), entry.getValue().soldierData);
		}
		
		return soldierDataMap;
	}
	
	@Override
	public String calculate(Map<String, AuditData> soldierData) {
		Map<String, SoldierData> everyone = map(soldierData);
				
		for (int i = 0; i < SimulationConstants.TargetCoordinates.length; i++) {
			int ensembleSize = ProbabilisticAssignmentCalculator.ensembleSize(i, everyone);
			if (ensembleSize < SimulationConstants.MinEnsembleSize || ensembleSize > SimulationConstants.MaxEnsembleSize) {
				return "x";
			}
		}
		
		int costSum = 0;
		for (SoldierData soldier : everyone.values()) {
			costSum += ProbabilisticAssignmentCalculator.ensembleMembershipCost(soldier.ensembleId, soldier);
		}
		
		return Integer.toString(costSum / soldierData.size());
	}

}
