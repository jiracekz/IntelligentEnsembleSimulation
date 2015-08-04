package cz.cuni.mff.d3s.jdeeco.bombs;

import cz.cuni.mff.d3s.jdeeco.bombs.components.RobotComponent;
import cz.cuni.mff.d3s.jdeeco.bombs.knowledgetypes.RobotRole;
import cz.cuni.mff.d3s.jdeeco.bombs.knowledgetypes.Room;

public class Parameters {

	public static Room[] rooms = new Room[] {
		new Room(10, 20, 0),
		new Room(40, 80, 0),
		new Room(15, 0, 50),
		new Room(10, 50, 100)
	};

	public static RobotComponent[] components = new RobotComponent[] { 
		new RobotComponent(1, RobotRole.RELAY, 0, 0),
		new RobotComponent(2, RobotRole.EXPLORER, 20, 50),
		new RobotComponent(3, RobotRole.RELAY, 50, 50),
		new RobotComponent(4, RobotRole.EXPLORER, 80, 50),
		new RobotComponent(5, RobotRole.EXPLORER, 0, 100),
		new RobotComponent(6, RobotRole.RELAY, 80, 100)
	};
}
