package cz.cuni.mff.d3s.demo.audit.stats;

import java.util.Map;

import cz.cuni.mff.d3s.demo.audit.AuditData;
import cz.cuni.mff.d3s.demo.audit.SimulationController;

public class TotalMovesStatistic implements Statistic {

	@Override
	public String getName() {
		return "Total Moves";
	}

	@Override
	public String calculate(Map<String, AuditData> soldierData) {
		return String.format("%.1f", SimulationController.totalMoves);
	}

}
