package cz.cuni.mff.d3s.demo.components;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.Local;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.deeco.task.ProcessContext;
import cz.cuni.mff.d3s.demo.AuditData;
import cz.cuni.mff.d3s.demo.ComponentUptimeDecider;
import cz.cuni.mff.d3s.demo.SimulationConstants;
import cz.cuni.mff.d3s.demo.SimulationController;
import cz.cuni.mff.d3s.demo.assignment.BasicAssignmentCalculator;
import cz.cuni.mff.d3s.demo.assignment.SoldierAssignmentCalculator;

@Component
public class Soldier {
	
	public String id;
	
	//public SoldierRole role;
	
	public Integer ensembleId;
	
	public SoldierData soldierData;
	
	public Map<String, SoldierData> everyone;
	
	//@Local
	public Integer auditIteration;	
	
	public Boolean isOnline;
	
	//@Local
	public ComponentUptimeDecider decider;

	public static SoldierAssignmentCalculator assignmentCalculator;
	
	static {
		assignmentCalculator = new BasicAssignmentCalculator();
	}
	
	public Soldier(Integer id, boolean isOnline, ComponentUptimeDecider decider) {
		this.id = id.toString();
		//this.role = SoldierRole.Unassigned;
		this.ensembleId = -1;
		
		this.soldierData = new SoldierData();
		this.everyone = new HashMap<>();
		this.everyone.put(this.id, soldierData);
		
		this.auditIteration = 0;
		this.isOnline = isOnline;
		
		this.decider = decider;
		
		System.out.println("Created a soldier with id = " + this.id + "; x = " + this.soldierData.x + "; y = " + this.soldierData.y);
	}
	
	@Process
	@PeriodicScheduling(period = 1000)
	public static void inferTeamAndRole(
			@In("id") String id,
			@InOut("everyone") ParamHolder<Map<String, SoldierData>> everyone,
			@InOut("ensembleId") ParamHolder<Integer> ensembleId, 
			@InOut("auditIteration") ParamHolder<Integer> auditIteration,
			@In("isOnline") Boolean isOnline,
			@InOut("soldierData") ParamHolder<SoldierData> soldierData) {
		
		if (!isOnline) {
			return;
		}
		
		if (SimulationConstants.IsCentralized) {
			return;
		}
		
		Map<String, SoldierData> newEveryone = new HashMap<>();
		//newEveryone.put(id, soldierData.value);
		newEveryone = everyone.value;
		
		// Filter out the old knowledge - could be done in a better way perhaps?
//		for(Entry<String, SoldierData> entry : everyone.value.entrySet()) {
//			if(entry.getKey().equals(id))
//				continue;
//			
//			if(entry.getValue().isAlive(ProcessContext.getTimeProvider().getCurrentMilliseconds())) {
//				newEveryone.put(entry.getKey(), entry.getValue());
//			}				
//		}		
		
		
		ensembleId.value = assignmentCalculator.AssignEnsemble(id, soldierData.value, newEveryone);
	}

	
	
	@Process
	@PeriodicScheduling(period = 1000, offset = 1)
	public static void audit(
			@In("isOnline") Boolean isOnline,
			@In("id") String id,
			@In("ensembleId") Integer ensembleId,
			@InOut("soldierData") ParamHolder<SoldierData> soldierData,
			@InOut("auditIteration") ParamHolder<Integer> auditIteration) {
		
		if (isOnline) {
			AuditData auditData = new AuditData();		
			SimulationController.addSnapshot(Integer.parseInt(id), auditIteration.value, auditData, soldierData.value);
		}
		
		auditIteration.value = auditIteration.value + 1;
	}

	
//	@Process
//	@PeriodicScheduling(period = 1000, offset = 100)
//	public static void updateState(
//			@In("id") String id, 
//			@In("decider") ComponentUptimeDecider decider, 
//			@InOut("isOnline") ParamHolder<Boolean> isOnline,
//			@InOut("role") ParamHolder<SoldierRole> role,
//			@InOut("ensembleId") ParamHolder<Integer> ensembleId) {
//		
//		boolean newState = decider.shouldBeOnline(Integer.parseInt(id), ProcessContext.getTimeProvider().getCurrentMilliseconds());
//		
//		if(newState != isOnline.value)
//			System.out.println("Random event! Soldier " + id + (newState ? " has recovered!" : " has been downed!"));		
//		isOnline.value = newState;
//		
//		if (!isOnline.value) {
//			role.value = SoldierRole.Unassigned;
//			ensembleId.value = -1;			
//		}
//	}
	
	
	@Process
	@PeriodicScheduling(period = 1000, offset = 2)
	public static void performDuties(
			@In("id") String id,
			@InOut("soldierData") ParamHolder<SoldierData> soldierData,
			@In("ensembleId") Integer ensembleId,
			@In("isOnline") Boolean isOnline,
			@In("ensembleMembers") HashSet<Integer> ensembleMembers) {
		
		if (!isOnline) {
			//System.out.println("Soldier " + id + " is down.");
			return;
		}
		
		soldierData.value.timestamp = ProcessContext.getTimeProvider().getCurrentMilliseconds();
		
		//TODO: Calculate position
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
