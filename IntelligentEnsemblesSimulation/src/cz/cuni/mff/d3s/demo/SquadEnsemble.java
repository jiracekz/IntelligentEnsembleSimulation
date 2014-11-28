package cz.cuni.mff.d3s.demo;

import cz.cuni.mff.d3s.deeco.annotations.*;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;

@Ensemble
@PeriodicScheduling(period = 2000)
public class SquadEnsemble {
	@Membership
	public static boolean membership(
	@In("coord.myId") Integer cId,
	@In("member.myId") Integer mId,
	@In("coord.ensembleId") Integer coordinatorEnsembleId,
	@In("member.ensembleId") Integer memberEnsembleId) {		
		if (coordinatorEnsembleId == -1 || memberEnsembleId == -1)
			return false;
		if(cId < mId && coordinatorEnsembleId.equals(memberEnsembleId))	{
			System.out.println("Soldier " + mId + " is currently a member of ensemble " + memberEnsembleId + ".");
			return true;
		}
		
		return false;		
	}
	
	@KnowledgeExchange
	public static void exchange(
	@In("coord.myId") Integer coordId,
	@In("member.myId") Integer memberId,
	@In("member.ensembleId") Integer memberEnsembleId) {		
		
	}
}
