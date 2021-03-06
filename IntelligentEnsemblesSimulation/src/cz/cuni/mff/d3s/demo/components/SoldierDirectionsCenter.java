package cz.cuni.mff.d3s.demo.components;

import java.util.HashMap;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.demo.assignment.OverallEnsembleCalculator;

@Component
public class SoldierDirectionsCenter {

	public String id;
	
	public HashMap<String, SoldierData> allSoldiers;
	
	// indexed by soldier ID
	public int[] ensembleIds;
	//public SoldierRole[] roles;
	
	public SoldierDirectionsCenter(int componentCount) {
		id = "<central>";
		
		allSoldiers = new HashMap<String, SoldierData>();
		ensembleIds = new int[componentCount];
		//roles = new SoldierRole[componentCount];
		
		for (int i = 0; i < componentCount; i++) {
			ensembleIds[i] = -1;
			//roles[i] = SoldierRole.Unassigned;
		}
	}
	
	@Process
	@PeriodicScheduling(period = 1000, offset = 900)
	public static void CalculateEnsembles(
			@In("allSoldiers") HashMap<String, SoldierData> allSoldiers,
			@InOut("ensembleIds") ParamHolder<int[]> ensembleIds) {
		
		ensembleIds.value = OverallEnsembleCalculator.calculateEnsembles(allSoldiers);
	}	
}
