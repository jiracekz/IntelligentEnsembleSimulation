package cz.cuni.mff.d3s.demo.ensembles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import cz.cuni.mff.d3s.deeco.annotations.*;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.demo.components.SoldierData;
import cz.cuni.mff.d3s.demo.components.SoldierRole;

@Ensemble
@PeriodicScheduling(period = 1000)
//@MemberRole(SoldierRole.class)
public class CentralizedCoordinationEnsemble {
	
	@Membership
	public static boolean membership(
			@In("coord.allSoldiers") HashMap<String, SoldierData> allSoldiers,
			@In("member.everyone") Map<String, SoldierData> soldierData) {		
		return allSoldiers != null;
	}
	
	@KnowledgeExchange
	public static void assignEnsembles(
			@InOut("coord.allSoldiers") ParamHolder<HashMap<String, SoldierData>> allSoldiers,
			@In("coord.ensembleIds") int[] ensembleIds,
			@In("member.id") String memberId,
			@In("member.everyone") Map<String, SoldierData> memberData,
			@In("member.isOnline") Boolean memberIsOnline,
			@InOut("member.ensembleId") ParamHolder<Integer> memberEnsembleId) {
		
		// FIXME: why does @In("member.everyone.[member.id]") not work??
		
		if (!memberIsOnline) return;
		
		// member tells the coordinator its data
		allSoldiers.value.put(memberId, memberData.get(memberId));
		
		// coordinator sets the member its settings
		int id = Integer.parseInt(memberId);
		memberEnsembleId.value = ensembleIds[id];
	}	
}
