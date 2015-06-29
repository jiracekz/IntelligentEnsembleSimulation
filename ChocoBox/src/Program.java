import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

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
		IntVar optDensityPromile = VariableFactory.fixed(6000 / 75, solver);
		//int CurrentBuilding = 3;
		
		// Input configuration - available components, rooms, etc.
		Component[] comps = new Component[] {new Component(0, 3, ComponentType.Relay, 0, 0), new Component(1, 3, ComponentType.Explorer, 20, 50), new Component(2, 3, ComponentType.Relay, 50, 50), new Component(3, 3, ComponentType.Explorer, 80, 50), new Component(4, 3, ComponentType.Explorer, 0, 100), new Component(5, 3, ComponentType.Relay, 80, 100)};
		Room[] rooms = new Room[] {new Room(10, 20, 0), new Room(40, 80, 0), new Room(15, 0, 50), new Room(10, 50, 0)};
		
		// CSP representation
		RoomEnsembleModel[] roomEnsembles = new RoomEnsembleModel[rooms.length];
		ComponentModel[] componentModels = new ComponentModel[comps.length];		

		
		for(Component c : comps) {
			componentModels[c.id] = new ComponentModel();
			ComponentModel comp = componentModels[c.id];
			
			IntVar compId = VariableFactory.fixed("component_" + c.id + "_id_TMP", c.id, solver);
			componentModels[c.id].id = compId;
			
			//IntVar buildingId = VariableFactory.fixed("component_" + c.id +"_building", c.building, solver);
			IntVar capability = VariableFactory.fixed("component_" + c.id +"_type_TMP", c.componentType.ordinal(), solver);
			componentModels[c.id].capability = capability;
			//BoolVar isMember = VariableFactory.bool("component_" + c.id +"_in_building", solver);
			//componentModels[c.id].inBuilding = isMember;
			//LogicalConstraintFactory.reification(isMember, IntConstraintFactory.arithm(buildingId, "=", CurrentBuilding));
			
			comp.x = VariableFactory.fixed(c.x, solver);
			comp.y = VariableFactory.fixed(c.y, solver);
		}	
		
		for(int i = 0; i < rooms.length; ++i) {						
			roomEnsembles[i] = new RoomEnsembleModel();
			RoomEnsembleModel currentEns = roomEnsembles[i];
			
			BoolVar exists = VariableFactory.bool("room_" + i + "_exists", solver);
			roomEnsembles[i].exists = exists;
			
			IntVar relayId = VariableFactory.integer("room_" + i + "_relay", -1, comps.length - 1, solver);		
			roomEnsembles[i].relayId = relayId;
			
			IntVar roomSize = VariableFactory.fixed(rooms[i].size, solver);
			
			currentEns.x = VariableFactory.fixed(rooms[i].x, solver);
			currentEns.y = VariableFactory.fixed(rooms[i].y, solver);	
			
			// Comps chosen as relays must have the relay capability and must be in the same building
			for(int j = 0; j < comps.length; ++j) {				
				LogicalConstraintFactory.ifThen(IntConstraintFactory.arithm(relayId, "=", j), IntConstraintFactory.arithm(componentModels[j].capability, "=", ComponentType.Relay.ordinal()));
				//LogicalConstraintFactory.ifThen(IntConstraintFactory.arithm(relayId, "=", j), LogicalConstraintFactory.and(componentModels[j].inBuilding, solver.ONE));
			}
			
			// Successful assignment of the relay ID means that the ensemble exists
			LogicalConstraintFactory.ifThen(IntConstraintFactory.arithm(relayId, "=", -1), LogicalConstraintFactory.not(LogicalConstraintFactory.and(exists)));
			
			SetVar explorers = VariableFactory.set("room_" + i + "_explorers", 0, comps.length - 1, solver);
			roomEnsembles[i].explorers = explorers;
			
			SetVar members = VariableFactory.set("room_" + i + "_members", 0, comps.length - 1, solver);			
			roomEnsembles[i].allMembers = members;
			SetVar relayIdSet = VariableFactory.set("room_" + i + "_relay_set_TMP", 0, comps.length - 1, solver);

			// if the ensemble DOES exist
			LogicalConstraintFactory.ifThenElse(LogicalConstraintFactory.and(exists), LogicalConstraintFactory.and( 
					// relay id is set (!= -1)
					IntConstraintFactory.arithm(relayId, "!=", -1),
					
					// relay is not in the explorers list
					LogicalConstraintFactory.not(SetConstraintsFactory.member(relayId, explorers)),
					
					// auxiliary one-element set for relay id
					SetConstraintsFactory.int_values_union(new IntVar[] { relayId }, relayIdSet),
					
					// members = relay + explorers
					SetConstraintsFactory.union(new SetVar[] {explorers,  relayIdSet}, members)
					),
				
			// if the ensemble does NOT exist
				LogicalConstraintFactory.and(
					// members and explorers are empty
					LogicalConstraintFactory.not(SetConstraintsFactory.notEmpty(explorers)),
					LogicalConstraintFactory.not(SetConstraintsFactory.notEmpty(members)),
					
					// relay id is set to -1
					IntConstraintFactory.arithm(relayId, "=", -1)
					));
			
			
			IntVar fitness = VariableFactory.integer("room_" + i + "_fitness", -141, 0, solver);
			currentEns.fitness = fitness;
			
			IntVar[] componentFitnesses = new IntVar[comps.length];
			for(int j = 0; j < comps.length; j++) {
				componentFitnesses[j] = VariableFactory.integer("comp_" + comps[j].id + "_room_" + i + "_fitness_TMP", -141, 0, solver);
				LogicalConstraintFactory.ifThenElse(SetConstraintsFactory.member(componentModels[j].id, members),
						IntConstraintFactory.arithm(componentFitnesses[j], "=", -comps[j].distanceTo(rooms[i])), 
						IntConstraintFactory.arithm(componentFitnesses[j], "=", 0)
						);
			}
			
			IntVar componentFitSum = VariableFactory.integer("room_" + i + "_compfit_sum_TMP", -1000, 0, solver);
			IntVar roomCardinality = VariableFactory.integer("room_" + i + "_cardinality_TMP", 0, comps.length, solver);
			
			LogicalConstraintFactory.ifThenElse(LogicalConstraintFactory.and(exists), 
					LogicalConstraintFactory.and(
							IntConstraintFactory.sum(componentFitnesses, componentFitSum),
							SetConstraintsFactory.cardinality(members, roomCardinality),
							IntConstraintFactory.eucl_div(componentFitSum, roomCardinality, fitness)
					),
					IntConstraintFactory.arithm(fitness, "=", 0)
					);
			
			IntVar density = VariableFactory.integer("room_" + i + "_density", 0, 1000, solver);
			IntVar densityDeviation = VariableFactory.integer("room_" + i + "_density_dev", 0, 1000, solver);
			IntVar densityDevSqr = VariableFactory.integer("room_" + i + "_density_dev_sqr", 0, 1000000, solver);
			roomEnsembles[i].squareDensityDeviation = densityDevSqr;
			LogicalConstraintFactory.ifThenElse(LogicalConstraintFactory.and(exists), 
					LogicalConstraintFactory.and(
							IntConstraintFactory.eucl_div(roomCardinality, roomSize, density),
							IntConstraintFactory.distance(density, optDensityPromile, "=", densityDeviation),
							IntConstraintFactory.times(densityDeviation, densityDeviation, densityDevSqr)
					),
					IntConstraintFactory.times(optDensityPromile, optDensityPromile, densityDevSqr)
					);
		}		

		SetVar[] memberSets = new SetVar[rooms.length];
		for(int i = 0; i < rooms.length; i++) {
			memberSets[i] = roomEnsembles[i].allMembers;
		}
		
		// all components are placed in an ensemble
		SetVar membersUnion = VariableFactory.set("membersUnion", 0, comps.length, solver);
		solver.post(SetConstraintsFactory.union(memberSets, membersUnion));
		for (int i = 0; i < comps.length; i++) {
			solver.post(SetConstraintsFactory.member(componentModels[i].id, membersUnion));
		}
		
		// members do not overlap
		solver.post(SetConstraintsFactory.all_disjoint(memberSets));
		
		BoolVar[] roomExists = new BoolVar[rooms.length];
		for (int i = 0; i < rooms.length; i++) {
			roomExists[i] = roomEnsembles[i].exists;
		}
		
		IntVar ensembleCount = VariableFactory.integer("ensemble_count", 0, rooms.length, solver);
		//IntVar weightedEnsembleCount = VariableFactory.integer("weighted_ensemble_count", 0, rooms.length * 100, solver);
		solver.post(IntConstraintFactory.sum(roomExists, ensembleCount));
		//solver.post(IntConstraintFactory.times(ensembleCount, 100, weightedEnsembleCount));
		
		
		IntVar[] roomFitnessVars = new IntVar[rooms.length];
		for(int i = 0; i < rooms.length; ++i) {
			roomFitnessVars[i] = roomEnsembles[i].fitness;
		}
		
		IntVar roomFitness = VariableFactory.integer("rooms_fitness", -1000, 0, solver);
		IntVar weightedRoomFitness = VariableFactory.integer("weighted_rooms_fitness", -10000, 0, solver);
		solver.post(IntConstraintFactory.sum(roomFitnessVars, roomFitness));
		solver.post(IntConstraintFactory.times(roomFitness, 1, weightedRoomFitness));
		
		IntVar[] densityDeviationSqrs = new IntVar[rooms.length];
		for(int i = 0; i < rooms.length; ++i) {
			densityDeviationSqrs[i] = roomEnsembles[i].squareDensityDeviation;
		}
		
		IntVar densityDeviationSum = VariableFactory.integer("density_deviation_sum", 0, 1000000, solver);
		IntVar weightedDensityDeviationSum = VariableFactory.integer("weighted_density_deviation", -10000, 0, solver);
		solver.post(IntConstraintFactory.sum(densityDeviationSqrs, densityDeviationSum));
		solver.post(IntConstraintFactory.eucl_div(densityDeviationSum, VariableFactory.fixed(-100, solver), weightedDensityDeviationSum));
		
		IntVar fitness = VariableFactory.integer("fitness", -10000, 0, solver);
		solver.post(IntConstraintFactory.sum(new IntVar[] {weightedDensityDeviationSum, weightedRoomFitness}, fitness));
		
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
	
	static int distance(int x1, int y1, int x2, int y2) {
		int dx = x1 - x2;
		int dy = y1 - y2;
		return (int) Math.sqrt(dx * dx + dy * dy);
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
	
	public Component(int id, int building, ComponentType type, int x, int y) {
		this.id = id;
		this.building = building;
		this.componentType = type;
		this.x = x;
		this.y = y;
	}
	
	public int distanceTo(Room room) {
		return Program.distance(x, y, room.x, room.y);
	}
}

class Room {
	int size;
	int x;
	int y;
	
	public Room(int size, int x, int y) {
		this.size = size;
		this.x = x;
		this.y = y;
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
	public IntVar squareDensityDeviation;
}

class ComponentModel {
	public IntVar id;
	public IntVar capability;
	//public BoolVar inBuilding;
	public IntVar x;
	public IntVar y;
}

