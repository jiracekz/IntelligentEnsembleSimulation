package cz.cuni.mff.d3s.demo.audit.stats;

import java.util.Map;

import cz.cuni.mff.d3s.demo.audit.AuditData;

public interface Statistic {
	
	String getName();
	
	String calculate(Map<String, AuditData> soldierData);
	
}
