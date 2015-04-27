package cz.cuni.mff.d3s.demo.ensembles;

import java.util.Map;

import cz.cuni.mff.d3s.deeco.annotations.CoordinatorRole;
import cz.cuni.mff.d3s.deeco.annotations.Ensemble;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.MemberRole;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.demo.components.SoldierData;
import cz.cuni.mff.d3s.demo.components.SoldierRole;

@Ensemble
@PeriodicScheduling(period = 1000, offset = 500)
//@CoordinatorRole(SoldierRole.class)
//@MemberRole(SoldierRole.class)
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
	@In("coord.everyone") Map<String, SoldierData> coordinatorData,
	@InOut("member.everyone") ParamHolder<Map<String, SoldierData>> memberList,
	@In("coord.isOnline") Boolean coordIsOnline,
	@In("member.isOnline") Boolean memberIsOnline ) {
		
		// FIXME: why does @In("coord.everyone.[coord.id]") not work??
		
		if (!coordIsOnline || !memberIsOnline) return;
		
		if (!memberList.value.containsKey(coordId)) {
			memberList.value.put(coordId, coordinatorData.get(coordId).clone());
		} else {
			memberList.value.replace(coordId, coordinatorData.get(coordId).clone());
		}
	}	
}
