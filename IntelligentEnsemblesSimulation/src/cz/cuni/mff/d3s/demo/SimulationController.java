package cz.cuni.mff.d3s.demo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeNotFoundException;
import cz.cuni.mff.d3s.deeco.knowledge.ReadOnlyKnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.ValueSet;
import cz.cuni.mff.d3s.deeco.model.runtime.api.KnowledgePath;
import cz.cuni.mff.d3s.demo.components.SoldierData;


public class SimulationController {
	
	private static String graphFileName = "results/audit%07d.txt";
	
	public static void doAudit(long time, Map<String, KnowledgeManager> knowledgeManagers) {
		
		Map<String, AuditData> currentSoldierData;
		try {
			currentSoldierData = constructAuditData(knowledgeManagers);
		} catch (KnowledgeNotFoundException e) {
			System.out.println("Knowledge not found.");
			return;
		}
		
		printState(currentSoldierData);
		try {
			saveGraph(String.format(graphFileName, time), currentSoldierData);
		} catch (IOException ioe) {
			System.out.println("IO error while saving results.");
		}
		
		// TODO write also some stats to another file
	}
	
	// returns all components (even offline)
	private static Map<String, AuditData> constructAuditData(Map<String, KnowledgeManager> knowledgeManagers) throws KnowledgeNotFoundException {
		Map<String, AuditData> result = new HashMap<>();

		for (Entry<String, KnowledgeManager> managerEntry : knowledgeManagers.entrySet()) {
			KnowledgeManager manager = managerEntry.getValue();
			KnowledgePath soldierDataKnowledgePath = null;
			KnowledgePath ensembleIdKnowledgePath = null;
			for (KnowledgePath path : manager.getKnowledgePaths()) {
				String pathStr = path.toString();
				if (pathStr.equals("soldierData")) {
					assert soldierDataKnowledgePath == null;
					soldierDataKnowledgePath = path;
				} else if (pathStr.equals("ensembleId")) {
					assert ensembleIdKnowledgePath == null;
					ensembleIdKnowledgePath = path;
				}
			}
			assert soldierDataKnowledgePath != null;
			assert ensembleIdKnowledgePath != null;
			
			ArrayList<KnowledgePath> knowledgePaths = new ArrayList<>();
			knowledgePaths.add(soldierDataKnowledgePath);
			knowledgePaths.add(ensembleIdKnowledgePath);
			ValueSet valueSet = manager.get(knowledgePaths);
			AuditData auditData = new AuditData();
			auditData.soldierData = (SoldierData)valueSet.getValue(soldierDataKnowledgePath);
			auditData.ensembleId = (Integer)valueSet.getValue(ensembleIdKnowledgePath);
			result.put(managerEntry.getKey(), auditData);
		}
		
		return result;
	}
	
	private static void printState(Map<String, AuditData> soldierData) {
		for (Entry<String, AuditData> soldierEntry : soldierData.entrySet()) {
			System.out.printf("Soldier #%s: group = %d, coords = %s\n", soldierEntry.getKey(), soldierEntry.getValue().ensembleId,
					soldierEntry.getValue().soldierData.coords.toString());
		}
	}
		
	private static void saveGraph(String filename, Map<String, AuditData> soldierData) throws IOException	{				
		File outputFile = new File(filename);
		FileWriter fileWriter = new FileWriter(outputFile);
		PrintWriter writer = new PrintWriter(fileWriter);
		
		writer.println("digraph {");

		// the four corners
		writer.println("a [pos=\"0,0\", style=invis]");
		writer.printf ("b [pos=\"0,%f\", style=invis]\n", SimulationConstants.FieldHeight);
		writer.printf ("c [pos=\"%f,0\", style=invis]\n", SimulationConstants.FieldWidth);
		writer.printf ("b [pos=\"%f,%f\", style=invis]\n", SimulationConstants.FieldWidth, SimulationConstants.FieldHeight);
		writer.println();
		
		// the target places
		int i = 0;
		for (Coordinates coords : SimulationConstants.TargetCoordinates) {
			writer.printf("T%d [pos=%s, shape=box, color=%s]\n", i, formatPos(coords), getColor(i));
			i++;
		}
		
		writer.println();
		
		for (Entry<String, AuditData> soldierEntry : soldierData.entrySet()) {
			AuditData soldier = soldierEntry.getValue();
			writer.printf("S%s [pos=%s, color=%s, width=0.02, height=0.02]\n", soldierEntry.getKey(), 
					formatPos(soldier.soldierData.coords), getColor(soldier.ensembleId));
		}
		
		writer.println("}");
				
		writer.close();
	}
	
	private static String formatPos(Coordinates coords) {
		return String.format("\"%f,%f\"", coords.getX(), coords.getY());
	}
	
	private static final String[] colors = new String[] { "red", "blue", "green", "purple" };
	
	private static String getColor(int groupId) {
		if (groupId >= 0) {
			return colors[groupId % colors.length];
		} else {
			return "grey";
		}
	}
}
