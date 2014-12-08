package cz.cuni.mff.d3s.demo.components;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.deeco.task.ProcessContext;

@Component
public class SoldierDirectionsCenter {

	public HashMap<String, SoldierData> allSoldiers;
	
	// indexed by soldier ID
	public int[] ensembleIds;
	public SoldierRole[] roles;
	public HashSet<?>[] ensembleContents;
	
	public SoldierDirectionsCenter(int componentCount) {
		allSoldiers = new HashMap<String, SoldierData>();
		ensembleIds = new int[componentCount];
		roles = new SoldierRole[componentCount];
		ensembleContents = new HashSet<?>[componentCount];
		
		for (int i = 0; i < componentCount; i++) {
			ensembleIds[i] = -1;
			roles[i] = SoldierRole.Unassigned;
		}
	}
	
	@Process
	@PeriodicScheduling(period = 1000, offset = 900)
	public static void CalculateEnsembles(
			@In("allSoldiers") HashMap<String, SoldierData> allSoldiers,
			@InOut("ensembleIds") ParamHolder<int[]> ensembleIds,
			@InOut("roles") ParamHolder<SoldierRole[]> roles,
			@InOut("ensembleContents") ParamHolder<HashSet<?>[]> ensembleContents) {
		
		for (int i = 0; i < ensembleIds.value.length; i++) {
			ensembleIds.value[i] = -1;
			roles.value[i] = SoldierRole.Unassigned;
		}
		
		Map<String, SoldierData> newEveryone = new HashMap<>();
		
		// Filter out the old knowledge - could be done in a better way perhaps?
		for (Entry<String, SoldierData> entry : allSoldiers.entrySet()) {			
			if(entry.getValue().isAlive(ProcessContext.getTimeProvider().getCurrentMilliseconds())) {
				newEveryone.put(entry.getKey(), entry.getValue());
			}				
		}		
		
		for (String soldierId : newEveryone.keySet()) {
			
			int id = Integer.parseInt(soldierId);
			
			ParamHolder<SoldierRole> role = new ParamHolder<>();
			ParamHolder<Integer> ensembleId = new ParamHolder<>();
			ensembleContents.value[id] = Soldier.calculateEnsembles(soldierId, newEveryone, role, ensembleId);
			
			ensembleIds.value[id] = ensembleId.value.intValue();
			roles.value[id] = role.value;
		}
		
	}
	
}
