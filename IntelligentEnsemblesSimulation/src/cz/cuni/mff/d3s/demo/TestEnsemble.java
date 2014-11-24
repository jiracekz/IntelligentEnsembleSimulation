package cz.cuni.mff.d3s.demo;

import cz.cuni.mff.d3s.deeco.annotations.Ensemble;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;

@Ensemble
@PeriodicScheduling(period = 1000)
public class TestEnsemble {
	
	@Membership
	public static boolean membership(
			@In("coord.myId") Integer cId,
			@In("member.myId") Integer mId,
			@In("coord.ensembleId") Integer coordinatorEnsembleId,
			@In("member.ensembleId") Integer memberEnsembleId) {
		return cId < mId && coordinatorEnsembleId.equals(memberEnsembleId);
	}
	
	@KnowledgeExchange
	public static void exchange(
			@In("coord.myId") Integer coordId,
			@In("member.myId") Integer memberId,
			@In("coord.groupId") Integer coordGroupId,
			@Out("member.groupId") ParamHolder<Integer> memberGroupId) {
		memberGroupId.value = coordGroupId;
		System.out.println("Component " + memberId + " now belongs into group " + coordGroupId);
	}
}
