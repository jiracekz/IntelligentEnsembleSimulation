package cz.cuni.mff.d3s.demo.ensembles;

import java.util.HashMap;
import java.util.Map;

import cz.cuni.mff.d3s.deeco.annotations.Ensemble;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.demo.components.SoldierData;

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
			@In("member.everyone.[member.id]") SoldierData memberData,
			@In("member.isOnline") Boolean memberIsOnline,
			@InOut("member.everyone.[member.id].ensembleId") ParamHolder<Integer> memberEnsembleId) {
		
		if (!memberIsOnline) return;
		
		// member tells the coordinator its data
		allSoldiers.value.put(memberId, memberData);
		
		// coordinator sets the member its settings
		int id = Integer.parseInt(memberId);
		memberEnsembleId.value = ensembleIds[id];
	}	
}
