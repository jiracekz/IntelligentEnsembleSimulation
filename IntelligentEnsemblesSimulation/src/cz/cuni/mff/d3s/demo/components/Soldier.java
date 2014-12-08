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

@Component
public class Soldier {
	
	public String id;
	
	public SoldierRole role;
	
	public Integer ensembleId;
	
	public SoldierData soldierData;
	
	public Map<String, SoldierData> everyone;
	
	@Local
	public Integer auditIteration;	
	
	public Boolean isOnline;
	
	@Local
	public ComponentUptimeDecider decider;
	
	public Soldier(Integer id, boolean isOnline, ComponentUptimeDecider decider) {
		this.id = id.toString();
		this.role = SoldierRole.Unassigned;
		this.ensembleId = -1;
		
		this.soldierData = new SoldierData();
		this.everyone = new HashMap<>();
		this.everyone.put(this.id, soldierData);
		
		this.auditIteration = 0;
		this.isOnline = isOnline;
		
		this.decider = decider;
		
		System.out.println("Created a soldier with id = " + this.id + "; knowledge = " + this.soldierData.knowledge);
	}
	
	@Process
	@PeriodicScheduling(period = 1000)
	public static void inferTeamAndRole(@In("id") String id, @InOut("everyone") ParamHolder<Map<String, SoldierData>> everyone,
			@Out("role") ParamHolder<SoldierRole> role, @Out("ensembleId") ParamHolder<Integer> ensembleId, 
			@InOut("auditIteration") ParamHolder<Integer> auditIteration, @In("isOnline") Boolean isOnline,
			@InOut("soldierData") ParamHolder<SoldierData> soldierData) {
		
		if (!isOnline) {
			auditIteration.value = auditIteration.value + 1;
			return;
		}
		
		Map<String, SoldierData> newEveryone = new HashMap<>();
		newEveryone.put(id, soldierData.value);
		
		// Filter out the old knowledge - could be done in a better way perhaps?
		for(Entry<String, SoldierData> entry : everyone.value.entrySet())
		{
			if(entry.getKey().equals(id))
				continue;
			
			if(entry.getValue().isAlive(ProcessContext.getTimeProvider().getCurrentMilliseconds()))
			{
				newEveryone.put(entry.getKey(), entry.getValue());
			}				
		}		
		
		HashSet<Integer> ensembleMembers = calculateEnsembles(id, newEveryone, role, ensembleId);
		
		audit(id, ensembleId.value, role.value, ensembleMembers, auditIteration.value, soldierData.value);
		auditIteration.value = auditIteration.value + 1;
		everyone.value = newEveryone;
		
		soldierData.value.timestamp = ProcessContext.getTimeProvider().getCurrentMilliseconds();
	}

	public static HashSet<Integer> calculateEnsembles(String id, Map<String, SoldierData> everyone, 
			ParamHolder<SoldierRole> role, ParamHolder<Integer> ensembleId) {
		
		List<Entry<String, SoldierData>> orderedSoldiers = new ArrayList<Entry<String, SoldierData>>(everyone.entrySet());
		orderedSoldiers.sort(new Comparator<Entry<String, SoldierData>>() {
			@Override
			public int compare(Entry<String, SoldierData> o1,
					Entry<String, SoldierData> o2) {
				return ((Integer) o1.getValue().knowledge).compareTo(o2.getValue().knowledge) ;
			}
		});
		
		int offsetInSquad = -1;
		for (int i = 0; i < orderedSoldiers.size(); i++) {
			if (id.equals(orderedSoldiers.get(i).getKey())) {
				ensembleId.value = i / SimulationConstants.SquadSize;
				offsetInSquad = i % SimulationConstants.SquadSize;
			}
		}
		
		HashSet<Integer> ensembleMembers = new HashSet<>();
		for (int i = 0; i < SimulationConstants.SquadSize; i++) {
			int index = ensembleId.value * SimulationConstants.SquadSize + i;
			if (index >= orderedSoldiers.size())
				break;
			
			int soldierId = Integer.parseInt(orderedSoldiers.get(index).getKey());
			ensembleMembers.add(soldierId);
		}
				
		if (offsetInSquad == 0) {
			role.value = SoldierRole.Leader;
		} else if (offsetInSquad % 2 == 1) {
			role.value = SoldierRole.Assault;
		} else {
			role.value = SoldierRole.Medic;
		}
		
		return ensembleMembers;
	}
	
	protected static void audit(String id, Integer ensembleId, SoldierRole role, Set<Integer> ensembleMembers, 
			Integer auditIteration, SoldierData soldierData) {
		AuditData auditData = new AuditData();
		auditData.role = role;
		auditData.componentsInEnsemble = ensembleMembers;
		SimulationController.addSnapshot(Integer.parseInt(id), auditIteration, auditData, soldierData);
	}
	
	@Process
	@PeriodicScheduling(period = 1000, offset = 100)
	public static void updateState(@In("id") String id, @In("decider") ComponentUptimeDecider decider, @InOut("isOnline") ParamHolder<Boolean> isOnline) {
		
		boolean newState = decider.shouldBeOnline(Integer.parseInt(id), ProcessContext.getTimeProvider().getCurrentMilliseconds());
		
		if(newState != isOnline.value)
			System.out.println("Random event! Soldier " + id + (newState ? " has recovered!" : " has been downed!"));		
		isOnline.value = newState;		
	}
	
	
	@Process
	@PeriodicScheduling(period = 1000, offset = 1)
	public static void performDuties(@In("id") String id, @In("role") SoldierRole role,
			@In("ensembleId") Integer ensembleId, @In("isOnline") Boolean isOnline) {
		
		if (!isOnline) {
			System.out.println("Soldier " + id + " is down.");
			return;
		}
		
		switch (role) {
			case Leader:
				lead(id, ensembleId);
				break;
			case Assault:
				fight(id, ensembleId);
				break;
			case Medic:
				repair(id, ensembleId);
				break;
			default:
				System.out.println("Soldier " + id + " is waiting for squad assignment.");
				break;		
		}
	}
	
	public static void lead(String id, int ensembleId)
	{
		System.out.println("Soldier " + id + " is leading in team " + ensembleId);
	}
	
	public static void fight(String id, int ensembleId)
	{
		System.out.println("Soldier " + id + " is fighting in team " + ensembleId);
	}
	
	public static void repair(String id, int ensembleId)
	{
		System.out.println("Soldier " + id + " is repairing in team " + ensembleId);
	}
}
