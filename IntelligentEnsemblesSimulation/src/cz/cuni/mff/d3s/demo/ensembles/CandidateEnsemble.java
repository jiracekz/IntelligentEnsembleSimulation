package cz.cuni.mff.d3s.demo.ensembles;

import java.util.Map;

import cz.cuni.mff.d3s.deeco.annotations.Ensemble;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.demo.SimulationConstants;
import cz.cuni.mff.d3s.demo.assignment.ProbabilisticAssignmentCalculator;
import cz.cuni.mff.d3s.demo.components.SoldierData;

@Ensemble
@PeriodicScheduling(period = 1000)
public class CandidateEnsemble {

	@Membership
	public static boolean membership(
			@In("coord.desiredEnsembleId") Integer desiredEnsembleId,
			@In("member.everyone.[member.id].ensembleId") Integer ensembleId
			) {
		return ensembleId.equals(desiredEnsembleId);
	}
	
	@KnowledgeExchange
	public static void evaluateDesire(
			@In("coord.id") String desirerId,
			@In("coord.desiredEnsembleId") Integer ensembleId,
			@InOut("coord.desireResult") ParamHolder<Boolean> desireResult,
			@InOut("member.everyone") ParamHolder<Map<String, SoldierData>> everyone
			) {
		int ensembleSize = ProbabilisticAssignmentCalculator.ensembleSize(ensembleId, everyone.value);
		if (ensembleSize >= SimulationConstants.MaxEnsembleSize) {
			desireResult.value = false;
		} else if (desireResult.value == null) {
			desireResult.value = true;
			everyone.value.get(desirerId).ensembleId = ensembleId;
		}
	}
}
