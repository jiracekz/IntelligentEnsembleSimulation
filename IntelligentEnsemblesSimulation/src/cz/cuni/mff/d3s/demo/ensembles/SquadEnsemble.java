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
@PeriodicScheduling(period = 1000)
public class SquadEnsemble {
	@Membership
	public static boolean membership(
	@In("coord.id") String cId,
	@In("member.id") String mId,
	@In("coord.everyone.[coord.id].ensembleId") Integer coordinatorEnsembleId,
	@In("member.everyone.[member.id].ensembleId") Integer memberEnsembleId) {		
		if (coordinatorEnsembleId == -1 || memberEnsembleId == -1)
			return false;
		if(coordinatorEnsembleId.equals(memberEnsembleId))	{
			return true;
		}
		
		return false;		
	}
	
	@KnowledgeExchange
	public static void exchange(
	@In("coord.id") String coordId,
	@In("member.id") String memberId,
	@In("coord.everyone.[coord.id]") SoldierData coordinatorData,
	@InOut("member.everyone") ParamHolder<Map<String, SoldierData>> memberList,
	@In("coord.isOnline") Boolean coordIsOnline,
	@In("member.isOnline") Boolean memberIsOnline ) {	
		
		// just do the data replication (only in higher frequency)
		ReplicationCoordinationEnsemble.assignEnsembles(coordId, memberId, coordinatorData, memberList, coordIsOnline, memberIsOnline);
	}
}
