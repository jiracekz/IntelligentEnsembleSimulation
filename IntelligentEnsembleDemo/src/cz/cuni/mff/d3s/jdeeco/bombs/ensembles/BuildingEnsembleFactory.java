package cz.cuni.mff.d3s.jdeeco.bombs.ensembles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.cuni.mff.d3s.deeco.ensembles.EnsembleFormationException;
import cz.cuni.mff.d3s.deeco.ensembles.EnsembleInstance;
import cz.cuni.mff.d3s.deeco.ensembles.intelligent.HeterogeneousArrayException;
import cz.cuni.mff.d3s.deeco.ensembles.intelligent.MiniZincIntelligentEnsembleFactory;
import cz.cuni.mff.d3s.deeco.ensembles.intelligent.OutputVariableParseException;
import cz.cuni.mff.d3s.deeco.ensembles.intelligent.ScriptIdentifier;
import cz.cuni.mff.d3s.deeco.ensembles.intelligent.ScriptInputVariableRegistry;
import cz.cuni.mff.d3s.deeco.ensembles.intelligent.ScriptOutputVariableRegistry;
import cz.cuni.mff.d3s.deeco.ensembles.intelligent.UnsupportedVariableTypeException;
import cz.cuni.mff.d3s.deeco.knowledge.container.KnowledgeContainer;
import cz.cuni.mff.d3s.deeco.knowledge.container.KnowledgeContainerException;
import cz.cuni.mff.d3s.jdeeco.bombs.Parameters;
import cz.cuni.mff.d3s.jdeeco.bombs.knowledgetypes.Room;
import cz.cuni.mff.d3s.jdeeco.bombs.roles.Robot;

public class BuildingEnsembleFactory extends MiniZincIntelligentEnsembleFactory {

	public BuildingEnsembleFactory() {
		super("src/cz/cuni/mff/d3s/jdeeco/bombs/ensembles/building.mzn");
	}
	
	private List<Robot> robotList;

	@Override
	public int getSchedulingOffset() {
		return 0;
	}

	@Override
	public int getSchedulingPeriod() {
		return 5000;
	}

	@Override
	protected ScriptInputVariableRegistry parseInput(KnowledgeContainer knowledgeContainer) throws EnsembleFormationException {
		try {
			ScriptInputVariableRegistry result = new ScriptInputVariableRegistry();
			
			// components
			robotList = new ArrayList<>();
			
			Collection<Robot> robots = knowledgeContainer.getTrackedKnowledgeForRole(Robot.class);
			//Integer[] component_id = new Integer[robots.size()];
			ScriptIdentifier[] component_type = new ScriptIdentifier[robots.size()];
			Integer[] component_x = new Integer[robots.size()];
			Integer[] component_y = new Integer[robots.size()];
			
			int i = 0;
			for (Robot r : robots) {
				//Integer id = Integer.valueOf(r.id);
				robotList.add(r);
				//component_id[i] = id;
				component_type[i] = new ScriptIdentifier(r.type.toString());
				component_x[i] = r.x;
				component_y[i] = r.y;
				i++;
			}
			
			result.addVariable("component_count", robots.size());
			//result.addVariable("component_id", component_id);
			result.addVariable("component_type", component_type);
			result.addVariable("component_x", component_x);
			result.addVariable("component_y", component_y);
			
			// rooms
			Integer[] room_size = new Integer[Parameters.rooms.length];
			Integer[] room_x = new Integer[Parameters.rooms.length];
			Integer[] room_y = new Integer[Parameters.rooms.length];
			
			for (int j = 0; j < Parameters.rooms.length; j++) {
				Room r = Parameters.rooms[j];
				room_size[j] = r.size;
				room_x[j] = r.x;
				room_y[j] = r.y;
			}
			
			result.addVariable("room_count", Parameters.rooms.length);
			result.addVariable("room_size", room_size);
			result.addVariable("room_x", room_x);
			result.addVariable("room_y", room_y);
			
			// distances
			Integer[][] distances = new Integer[robots.size()][Parameters.rooms.length];
			i = 0;
			for (Robot r : robots) {
				for (int j = 0; j < Parameters.rooms.length; j++) {
					distances[i][j] = distance(r, Parameters.rooms[j]);
				}
				
				i++;
			}
			
			result.addVariable("distances", distances);
			
			return result;
			
		} catch (KnowledgeContainerException | UnsupportedVariableTypeException | HeterogeneousArrayException e) {
			throw new EnsembleFormationException(e);
		}
	}

	@Override
	protected Collection<EnsembleInstance> createInstancesFromOutput(ScriptOutputVariableRegistry scriptOutput) throws EnsembleFormationException {
		
		if (scriptOutput.isEmpty()) {
			return Collections.emptyList();
		}
		
		try {			
			BuildingEnsemble result = new BuildingEnsemble();
			Integer[] room_relay = scriptOutput.getArray1dValue("room_relay", Integer.class);
			Integer[] component_room = scriptOutput.getArray1dValue("component_room", Integer.class);
			
			for (int i = 0; i < room_relay.length; i++) {
				if (room_relay[i] == 0) { // means there is no ensemble for this room
					continue;
				}
				
				RoomEnsemble room = new RoomEnsemble(i + 1);
				room.relay = robotList.get(room_relay[i] - 1); // indexed from 0
				for (int j = 0; j < component_room.length; j++) {
					if (component_room[j] == i + 1 && j != room_relay[i] - 1) {
						room.explorers.add(robotList.get(j));
					}
				}
				
				result.roomTeams.add(room);
			}

			return Arrays.asList((EnsembleInstance) result);
			
		} catch (OutputVariableParseException | UnsupportedVariableTypeException e) {
			throw new EnsembleFormationException(e);
		}
		
		
	}
	
	private int distance(Robot robot, Room room) {
		return (int) Math.round(Math.sqrt(Math.pow(robot.x - room.x, 2) + Math.pow(robot.y - room.y, 2)));
	}

}
