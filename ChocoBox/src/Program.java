import java.util.ArrayList;
import java.util.List;

import org.chocosolver.solver.*;
import org.chocosolver.solver.constraints.*;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.search.solution.AllSolutionsRecorder;
import org.chocosolver.solver.search.solution.BestSolutionsRecorder;
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.*;

public class Program {

	public Program() {
		
	}

	public static void main(String[] args) {
		
		Solver solver = new Solver();
//		IntVar answer = VariableFactory.integer("meaningoflife", 0, 100, solver);
//		IntVar a = VariableFactory.fixed(6, solver);
//		IntVar zero = VariableFactory.fixed(0, solver);
//		
//		
//		
//		Constraint divisible = IntConstraintFactory.mod(answer, a, zero);
//		Constraint larger = IntConstraintFactory.arithm(answer, ">", 40);
//		
//		solver.post(divisible);
//		solver.post(larger);
//		solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, answer);
//		
//		Chatterbox.printSolutions(solver);	
//		
//		
		IntVar optDensity = VariableFactory.fixed(3, solver);
		int CurrentBuilding = 3;
		
		// Input configuration - available components, rooms, etc.
		Component[] comps = new Component[] {new Component(0, 3, ComponentType.Relay), new Component(1, 3, ComponentType.Explorer), new Component(2, 3, ComponentType.Relay), new Component(3, 3, ComponentType.Explorer), new Component(4, 3, ComponentType.Explorer)};
		Room[] rooms = new Room[] {new Room(10), new Room(40), new Room(12), new Room(10)};
		
		// CSP representation
		RoomEnsembleModel[] roomEnsembles = new RoomEnsembleModel[rooms.length];
		ComponentModel[] componentModels = new ComponentModel[comps.length];		

		
		for(Component c : comps) {
			componentModels[c.id] = new ComponentModel();
			ComponentModel comp = componentModels[c.id];
			
			IntVar buildingId = VariableFactory.fixed("component_" + c.id +"_building", c.building, solver);
			IntVar capability = VariableFactory.fixed("component_" + c.id +"_type", c.componentType.ordinal(), solver);
			componentModels[c.id].capability = capability;
			BoolVar isMember = VariableFactory.bool("component_" + c.id +"_in_building", solver);
			componentModels[c.id].inBuilding = isMember;
			LogicalConstraintFactory.reification(isMember, IntConstraintFactory.arithm(buildingId, "=", CurrentBuilding));
			
			comp.x = VariableFactory.fixed(c.x, solver);
			comp.y = VariableFactory.fixed(c.y, solver);
		}	
		
		for(int i = 0; i < rooms.length; ++i) {			
			Room r = rooms[i];
			
			roomEnsembles[i] = new RoomEnsembleModel();
			RoomEnsembleModel currentEns = roomEnsembles[i];
			
			BoolVar exists = VariableFactory.bool("room_" + i + "_exists", solver);
			roomEnsembles[i].exists = exists;
			
			IntVar relayId = VariableFactory.integer("room_" + i + "_relay", -1, comps.length - 1, solver);		
			roomEnsembles[i].relayId = relayId;
			
			currentEns.x = VariableFactory.fixed(rooms[i].x, solver);
			currentEns.y = VariableFactory.fixed(rooms[i].y, solver);	
			
			VariableFactory.integer("room_" + i + "_fitness", 0, 1000, solver);
			
			for(Component c : comps) {
				// TODO: Fitness function as defined by member distances from the room
			}
			
			// Comps chosen as relays must have the relay capability and must be in the same building
			for(int j = 0; j < comps.length; ++j) {				
				LogicalConstraintFactory.ifThen(IntConstraintFactory.arithm(relayId, "=", j), IntConstraintFactory.arithm(componentModels[j].capability, "=", ComponentType.Relay.ordinal()));
				LogicalConstraintFactory.ifThen(IntConstraintFactory.arithm(relayId, "=", j), LogicalConstraintFactory.and(componentModels[j].inBuilding, solver.ONE));
			}
			
			// Impossible to have an existing room ensemble without having a relay in it
			LogicalConstraintFactory.ifThen(IntConstraintFactory.arithm(relayId, "=", -1), LogicalConstraintFactory.and(exists, solver.ZERO));
			LogicalConstraintFactory.ifThen(LogicalConstraintFactory.and(exists, solver.ZERO), IntConstraintFactory.arithm(relayId, "=", -1));			
			
			SetVar explorers = VariableFactory.set("room_" + i + "_explorers", 0, comps.length - 1, solver);
			roomEnsembles[i].explorers = explorers;
			
			//SetVar members = VariableFactory.set("room_" + i + "_members", 0, comps.length, solver);			
		}
		
		
		// Component exclusivity
		for(int i = 0; i < rooms.length; ++i) {
			for(int j = 0; j < rooms.length; ++j) {
				if (i != j) {
					IntVar[] relayPair = new IntVar[] {roomEnsembles[i].relayId, roomEnsembles[j].relayId};
					LogicalConstraintFactory.ifThen(LogicalConstraintFactory.and(roomEnsembles[i].exists, roomEnsembles[j].exists), IntConstraintFactory.alldifferent(relayPair));
					LogicalConstraintFactory.ifThen(LogicalConstraintFactory.and(roomEnsembles[i].exists, roomEnsembles[j].exists), SetConstraintsFactory.disjoint(roomEnsembles[i].explorers, roomEnsembles[j].explorers));					
				}
				
				LogicalConstraintFactory.ifThen(LogicalConstraintFactory.and(roomEnsembles[i].exists, roomEnsembles[j].exists), LogicalConstraintFactory.not(SetConstraintsFactory.member(roomEnsembles[i].relayId, roomEnsembles[j].explorers)));
			}
		}		
		
		IntVar[] roomFitnessVars = new IntVar[rooms.length];
		
		for(int i = 0; i < rooms.length; ++i) {
			roomFitnessVars[i] = roomEnsembles[i].exists;
		}
		
		IntVar fitness = VariableFactory.integer("fitness", 0, 10000, solver);
		solver.post(IntConstraintFactory.sum(roomFitnessVars, fitness));
		
		long startTime = System.currentTimeMillis();
		
		solver.set(new BestSolutionsRecorder(fitness));		
		solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, fitness);				
		System.out.println("Done. Time taken: " + (System.currentTimeMillis() - startTime) + " ms.");
		
		for(Solution s : solver.getSolutionRecorder().getSolutions()) {
			printSolution(s);
		}
	}
	
	static void printSolution(Solution s) {
		String filtered = s.toString().replaceAll("\\S*TMP\\S*", "").replaceAll("\\s+", " ");
		System.out.println(filtered);
	}
	
}

enum ComponentType {
	Explorer,
	Defuser,
	Relay
}

class Component {
	int id;
	int building;
	int x;
	int y;	
	ComponentType componentType;
	
	public Component(int id, int building, ComponentType type) {
		this.id = id;
		this.building = building;
		this.componentType = type;
	}
}

class Room {
	int size;
	int x;
	int y;
	
	public Room(int size) {
		this.size = size;
	}
}

class RoomEnsembleModel {
	public BoolVar exists;
	public IntVar relayId;
	public SetVar explorers;
	public SetVar allMembers;
	public IntVar x;
	public IntVar y;
	public IntVar fitness;
}

class ComponentModel {
	public IntVar capability;
	public BoolVar inBuilding;
	public IntVar x;
	public IntVar y;
}

