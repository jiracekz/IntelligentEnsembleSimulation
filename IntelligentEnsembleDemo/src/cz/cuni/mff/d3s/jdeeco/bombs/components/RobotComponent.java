package cz.cuni.mff.d3s.jdeeco.bombs.components;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.PlaysRole;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.jdeeco.bombs.knowledgetypes.RobotRole;
import cz.cuni.mff.d3s.jdeeco.bombs.roles.Robot;

@Component
@PlaysRole(Robot.class)
public class RobotComponent {

	public String id;
	
	public RobotRole type;
	
	public Integer x;
	
	public Integer y;
	
	// filled in by ensemble knowledge exchange
	public RobotRole actualRole;
	
	public Integer roomId;

	public RobotComponent(Integer id, RobotRole type, Integer x, Integer y) {
		super();
		this.id = id.toString();
		this.type = type;
		this.x = x;
		this.y = y;
		
		actualRole = RobotRole.UNDEFINED;
		roomId = 0;
	}
	
	@Process
	@PeriodicScheduling(period = 1000)
	public static void printStatus(@In("id") String id, @In("actualRole") RobotRole actualRole, @In("roomId") Integer roomId) {
		System.out.printf("Robot %s: %s in room %d\n", id, actualRole.toString(), roomId);
	}

}
