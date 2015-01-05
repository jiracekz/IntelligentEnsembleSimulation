package cz.cuni.mff.d3s.demo.ensembles;

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
@PeriodicScheduling(period = 1000, offset = 500)
public class ReplicationCoordinationEnsemble {

	@Membership
	public static boolean membership(
			@In("coord.id") String coordId,
			@In("member.id") String memberId) {		
		return true;
	}
	
	@KnowledgeExchange
	public static void assignEnsembles(
	@In("coord.id") String coordId,
	@In("member.id") String memberId,
	@In("coord.soldierData") SoldierData coordinatorData,
	@InOut("member.everyone") ParamHolder<Map<String, SoldierData>> memberList,
	@In("coord.isOnline") Boolean coordIsOnline,
	@In("member.isOnline") Boolean memberIsOnline ) {
		
		if (!coordIsOnline || !memberIsOnline) return;
		
		memberList.value.put(coordId, coordinatorData.clone());
	}	
}
