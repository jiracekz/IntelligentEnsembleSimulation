package cz.cuni.mff.d3s.demo;

import cz.cuni.mff.d3s.deeco.annotations.*;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;

@Component
public class Soldier {
	public Integer myId;
	public SoldierRole role;
	public Integer ensembleId;
	
	
	public Soldier(int id) {
		this.myId = new Integer(id);
		this.role = SoldierRole.Unassigned;
		this.ensembleId = -1;
		
		System.out.println("Created a soldier with id = " + this.myId + ".");
	}
	
	@Process
	@PeriodicScheduling(period = 1000)
	public static void performDuties(@In("myId") Integer id, @InOut("role") ParamHolder<SoldierRole> role)
	{		
		if(true)
			return;
		switch (role.value) {
			case Leader:
				lead(id);
				break;
			case Assault:
				fight(id);
				break;
			case Medic:
				repair(id);
				break;
			default:
				System.out.println("Soldier " + id + " is waiting for squad assignment.");
				break;		
		}
	}
	
	public static void lead(Integer id)
	{
		System.out.println("Soldier " + id + "is leading.");
	}
	
	public static void fight(Integer id)
	{
		System.out.println("Soldier " + id + "is fighting.");
	}
	
	public static void repair(Integer id)
	{
		System.out.println("Soldier " + id + "is repairing.");
	}
}
