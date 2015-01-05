package cz.cuni.mff.d3s.demo.assignment;

import java.util.Map;

import cz.cuni.mff.d3s.demo.components.SoldierData;


public interface SoldierAssignmentCalculator {
	int AssignEnsemble(String id, SoldierData soldierData, Map<String, SoldierData> everyone);
}
