package cz.cuni.mff.d3s.jdeeco.bombs.roles;

import cz.cuni.mff.d3s.deeco.annotations.Role;
import cz.cuni.mff.d3s.jdeeco.bombs.knowledgetypes.RobotRole;


@Role
public class Robot {

	public String id;
	
	public RobotRole type;
	
	public Integer x;
	
	public Integer y;
	
	// filled in by ensemble knowledge exchange
	public RobotRole actualRole;
	
	public Integer roomId;

}
