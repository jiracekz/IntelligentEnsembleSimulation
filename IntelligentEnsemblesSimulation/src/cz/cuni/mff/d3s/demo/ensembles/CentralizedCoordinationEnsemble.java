package cz.cuni.mff.d3s.demo.ensembles;

import java.util.HashMap;
import java.util.HashSet;
import cz.cuni.mff.d3s.deeco.annotations.*;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.demo.components.SoldierData;
import cz.cuni.mff.d3s.demo.components.SoldierRole;

@Ensemble
@PeriodicScheduling(period = 1000)
public class CentralizedCoordinationEnsemble {
	
	@Membership
	public static boolean membership(
			@In("coord.allSoldiers") HashMap<String, SoldierData> allSoldiers,
			@In("member.soldierData") SoldierData soldierData) {		
		return allSoldiers != null;
	}
	
	@KnowledgeExchange
	public static void assignEnsembles(
			@InOut("coord.allSoldiers") ParamHolder<HashMap<String, SoldierData>> allSoldiers,
			@In("coord.ensembleIds") int[] ensembleIds,
			@In("member.id") String memberId,
			@In("member.soldierData") SoldierData memberData,
			@In("member.isOnline") Boolean memberIsOnline,
			@InOut("member.ensembleId") ParamHolder<Integer> memberEnsembleId) {
		
		if (!memberIsOnline) return;
		
		// member tells the coordinator its data
		allSoldiers.value.put(memberId, memberData);
		
		// coordinator sets the member its settings
		int id = Integer.parseInt(memberId);
		memberEnsembleId.value = ensembleIds[id];
	}	
}
