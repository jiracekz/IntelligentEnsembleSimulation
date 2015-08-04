package cz.cuni.mff.d3s.jdeeco.bombs.ensembles;

import java.util.ArrayList;
import java.util.Collection;

import cz.cuni.mff.d3s.deeco.ensembles.EnsembleInstance;
import cz.cuni.mff.d3s.jdeeco.bombs.knowledgetypes.RobotRole;
import cz.cuni.mff.d3s.jdeeco.bombs.roles.Robot;

public class RoomEnsemble implements EnsembleInstance {

	public Integer roomId;
	
	public Robot relay;
	
	public Collection<Robot> explorers;
	
	public RoomEnsemble(Integer roomId) {
		this.roomId = roomId;
		explorers = new ArrayList<Robot>();
	}

	@Override
	public void performKnowledgeExchange() {
		relay.actualRole = RobotRole.RELAY;
		relay.roomId = roomId;
		
		for (Robot explorer : explorers) {
			explorer.actualRole = RobotRole.EXPLORER;
			explorer.roomId = roomId;
		}
	}

}
