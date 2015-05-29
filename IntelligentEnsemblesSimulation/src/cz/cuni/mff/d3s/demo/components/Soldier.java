package cz.cuni.mff.d3s.demo.components;

import java.util.HashMap;
import java.util.Map;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.Local;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.PlaysRole;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.deeco.task.ProcessContext;
import cz.cuni.mff.d3s.demo.Coordinates;
import cz.cuni.mff.d3s.demo.SimulationConstants;
import cz.cuni.mff.d3s.demo.assignment.BasicAssignmentCalculator;
import cz.cuni.mff.d3s.demo.assignment.OverallEnsembleCalculator;
import cz.cuni.mff.d3s.demo.assignment.ProbabilisticAssignmentCalculator;
import cz.cuni.mff.d3s.demo.assignment.SoldierAssignmentCalculator;
import cz.cuni.mff.d3s.demo.assignment.SoldierAssignmentMode;
import cz.cuni.mff.d3s.demo.audit.SimulationController;
import cz.cuni.mff.d3s.demo.uptime.ComponentUptimeDecider;
import cz.cuni.mff.d3s.jdeeco.position.PositionPlugin;

@Component
@PlaysRole(SoldierRole.class)
public class Soldier {
		
	public String id;
	
	//public SoldierRole role;
	
	//public Integer ensembleId;
		
	public Map<String, SoldierData> everyone;
	
	public Boolean isOnline;
	
	public Integer desiredEnsembleId;
	public Boolean desireResult;
	public Long desireTimestamp;
	
	@Local
	public ComponentUptimeDecider decider;
	
	@Local
	public PositionPlugin positionPlugin;

	public static SoldierAssignmentCalculator assignmentCalculator;
	
	static {
		assignmentCalculator = new ProbabilisticAssignmentCalculator();
		//assignmentCalculator = new BasicAssignmentCalculator();
	}
	
	public Soldier(Integer id, boolean isOnline, ComponentUptimeDecider decider, PositionPlugin positionPlugin) {
		this.id = id.toString();
		//this.role = SoldierRole.Unassigned;
		
		SoldierData soldierData = new SoldierData();
		this.everyone = new HashMap<>();
		this.everyone.put(this.id, soldierData);
		
		this.isOnline = isOnline;
		this.desiredEnsembleId = -1;
		this.desireResult = false; // so that we choose new ensemble in the beginning
		
		this.decider = decider;
		
		this.positionPlugin = positionPlugin;
		
		System.out.println("Created a soldier with id = " + this.id + "; coords = " + soldierData.coords);
	}
	
	public SoldierData getSoldierData() {
		return everyone.get(id);
	}
	
	@Process
	@PeriodicScheduling(period = 1000, offset = 1)
	public static void inferTeam(
			@In("id") String id,
			@In("everyone") Map<String, SoldierData> everyone,
			@InOut("everyone.[id].ensembleId") ParamHolder<Integer> ensembleId, 
			@InOut("desiredEnsembleId") ParamHolder<Integer> desiredEnsembleId,
			@InOut("desireResult") ParamHolder<Boolean> desireResult,
			@InOut("desireTimestamp") ParamHolder<Long> desireTimestamp,
			@In("isOnline") Boolean isOnline,
			@In("everyone.[id]") SoldierData soldierData) {
		
		if (!isOnline) {
			return;
		}
		
		if (SimulationConstants.IsCentralized) {
			return;
		}
				
		switch (SimulationConstants.AssignmentMode) {
		case AssignImmediately:
			ensembleId.value = assignmentCalculator.assignEnsemble(id, soldierData, everyone);
			break;
			
		case AskAnyone:
			if (desireResult.value != null && desireResult.value.booleanValue()) {
				// our desired ensemble was approved, go for it
				ensembleId.value = desiredEnsembleId.value;
				desireResult.value = null;
				desireTimestamp.value = null;
			} else if (desireResult.value != null && !desireResult.value.booleanValue()) {
				// our desired ensemble was rejected, find another one
				desiredEnsembleId.value = assignmentCalculator.assignEnsemble(id, soldierData, everyone);				
				desireResult.value = null;
				desireTimestamp.value = ProcessContext.getTimeProvider().getCurrentMilliseconds();
				if (desiredEnsembleId.value == ensembleId.value) {
					desiredEnsembleId.value = null;
					desireTimestamp.value = null;
				}
			} else if (desireTimestamp.value == null) {
				// we had no desire, let's verify whether we need a new ensemble
				desiredEnsembleId.value = assignmentCalculator.assignEnsemble(id, soldierData, everyone);
				if (desiredEnsembleId.value != ensembleId.value) {
					desireTimestamp.value = ProcessContext.getTimeProvider().getCurrentMilliseconds();
				} else {
					desiredEnsembleId.value = null;
				}
			} else if (desireTimestamp.value < ProcessContext.getTimeProvider().getCurrentMilliseconds() - SoldierAssignmentMode.AskTimeout) {
				// we had a desire, but noone responded
				ensembleId.value = desiredEnsembleId.value;
				desireResult.value = null;
				desireTimestamp.value = null;
			}
			
			break;
		case AskCoordinator:
			break;
		}
	}


	@Process
	@PeriodicScheduling(period = 1000, offset = 0)
	public static void updateState(
			@In("id") String id, 
			@In("decider") ComponentUptimeDecider decider, 
			@InOut("isOnline") ParamHolder<Boolean> isOnline,
			@InOut("everyone.[id].ensembleId") ParamHolder<Integer> ensembleId) {
		
		boolean newState = decider.shouldBeOnline(Integer.parseInt(id), ProcessContext.getTimeProvider().getCurrentMilliseconds());
		
		if(newState != isOnline.value)
			System.out.println("Random event! Soldier " + id + (newState ? " has recovered!" : " has been downed!"));		
		isOnline.value = newState;
		
		if (!isOnline.value) {
			ensembleId.value = -1;			
		}
	}
	
	
	@Process
	@PeriodicScheduling(period = 100, offset = 2)
	public static void performDuties(
			@In("id") String id,
			@InOut("everyone.[id]") ParamHolder<SoldierData> soldierData,
			@In("everyone.[id].ensembleId") Integer ensembleId,
			@In("isOnline") Boolean isOnline) {
		
		if (!isOnline) {
			//System.out.println("Soldier " + id + " is down.");
			return;
		}
		
		soldierData.value.timestamp = ProcessContext.getTimeProvider().getCurrentMilliseconds();
		
		if (ensembleId < 0)
			return;
			
		if (SimulationConstants.MoveSoldiers) {
			Coordinates target = SimulationConstants.TargetCoordinates[ensembleId];
			if (soldierData.value.coords.getDistanceTo(target) > SimulationConstants.MovementPerIteration) {
				soldierData.value.coords = soldierData.value.coords.moveVectorTo(target, SimulationConstants.MovementPerIteration);
				SimulationController.totalMoves += SimulationConstants.MovementPerIteration;
			} else {
				SimulationController.totalMoves += target.getDistanceTo(soldierData.value.coords);		
				soldierData.value.coords = target;
			}
		}
	}
	
//	public static void lead(String id, int ensembleId, HashSet<Integer> ensembleMembers) {
//		System.out.println("Soldier " + id + " is leading in team " + ensembleId + listMembers(id, ensembleMembers));
//	}
//	
//	public static void fight(String id, int ensembleId, HashSet<Integer> ensembleMembers)	{
//		System.out.println("Soldier " + id + " is fighting in team " + ensembleId + listMembers(id, ensembleMembers));
//	}
//	
//	public static void repair(String id, int ensembleId, HashSet<Integer> ensembleMembers) {
//		System.out.println("Soldier " + id + " is repairing in team " + ensembleId + listMembers(id, ensembleMembers));
//	}
//	
//	public static String listMembers(String id, HashSet<Integer> ensembleMembers) {
//		String result = " with";
//		for (Integer member : ensembleMembers) {
//			if (!member.equals(Integer.parseInt(id))) {
//				result += " " + member + ",";
//			}
//		}
//		
//		return result;
//	}
}
