package cz.cuni.mff.d3s.demo.ensembles;

import java.util.Random;

import cz.cuni.mff.d3s.deeco.annotations.*;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.demo.SimulationConstants;

@Ensemble
@PeriodicScheduling(period = 10000)
public class CentralizedCoordinationEnsemble {
	
	public static int SquadCount = 2;
	public static Random generator;
	
	static {
		generator = new Random(42);
	}
	
	@Membership
	public static boolean membership(@In("coord.myId") Integer cId,
			@In("member.myId") Integer mId) {		
		return (cId == mId - 1 || (cId == SimulationConstants.SoldierCount - 1 && mId == 0));
	}
	
	@KnowledgeExchange
	public static void assignEnsembles(
	@In("coord.myId") Integer coordId,
	@In("member.myId") Integer memberId,
	@In("coord.ensembleId") Integer coordEnsembleId,
	@InOut("member.ensembleId") ParamHolder<Integer> memberEnsembleId) {
		int newEnsemble = generator.nextInt(SquadCount);
		memberEnsembleId.value = new Integer(newEnsemble);
		System.out.println("Soldier " + memberId + " is now assigned to ensemble " + newEnsemble);
	}
}
