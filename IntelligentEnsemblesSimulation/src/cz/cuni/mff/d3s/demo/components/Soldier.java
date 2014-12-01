package cz.cuni.mff.d3s.demo.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cz.cuni.mff.d3s.deeco.annotations.*;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.demo.AuditData;
import cz.cuni.mff.d3s.demo.SimpleSimulationLauncher;
import cz.cuni.mff.d3s.demo.SimulationSnapshot;

@Component
public class Soldier {
	
	public String id;
	
	public SoldierRole role;
	
	public Integer ensembleId;
	
	public SoldierData soldierData;
	
	public Map<String, SoldierData> everyone;
	
	@Local
	public Integer auditIteration;
	
	public Soldier(Integer id) {
		this.id = id.toString();
		this.role = SoldierRole.Unassigned;
		this.ensembleId = -1;
		
		this.soldierData = new SoldierData();
		this.everyone = new HashMap<>();
		this.everyone.put(this.id, soldierData);
		
		this.auditIteration = 0;
		
		System.out.println("Created a soldier with id = " + this.id + "; knowledge = " + this.soldierData.knowledge);
	}
	
	@Process
	@PeriodicScheduling(period = 1000)
	public static void calculateEnsembles(@In("id") String id, @In("everyone") Map<String, SoldierData> everyone,
			@Out("role") ParamHolder<SoldierRole> role, @Out("ensembleId") ParamHolder<Integer> ensembleId, 
			@InOut("auditIteration") ParamHolder<Integer> auditIteration) {
		
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
				ensembleId.value = i / SimpleSimulationLauncher.SquadSize;
				offsetInSquad = i % SimpleSimulationLauncher.SquadSize;
			}
		}
		
		HashSet<Integer> ensembleMembers = new HashSet<>();
		for (int i = 0; i < SimpleSimulationLauncher.SquadSize; i++) {
			int soldierId = Integer.parseInt(orderedSoldiers.get(ensembleId.value * SimpleSimulationLauncher.SquadSize + i).getKey());
			ensembleMembers.add(soldierId);
		}
		
		
		if (offsetInSquad == 0) {
			role.value = SoldierRole.Leader;
		} else if (offsetInSquad % 2 == 1) {
			role.value = SoldierRole.Assault;
		} else {
			role.value = SoldierRole.Medic;
		}
		
		audit(id, ensembleId.value, role.value, ensembleMembers, auditIteration.value);
		auditIteration.value = auditIteration.value + 1;
	}
	
	protected static void audit(String id, Integer ensembleId, SoldierRole role, Set<Integer> ensembleMembers, 
			Integer auditIteration) {
		AuditData auditData = new AuditData();
		auditData.role = role;
		auditData.componentsInEnsemble = ensembleMembers;
		SimulationSnapshot.addSnapshot(Integer.parseInt(id), auditIteration, auditData);
	}
	
	
	@Process
	@PeriodicScheduling(period = 1000)
	public static void performDuties(@In("id") String id, @In("role") SoldierRole role, @In("ensembleId") Integer ensembleId)
	{			
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
