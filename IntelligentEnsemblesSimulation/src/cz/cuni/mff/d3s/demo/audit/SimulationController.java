package cz.cuni.mff.d3s.demo.audit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import cz.cuni.mff.d3s.deeco.annotations.pathparser.ParseException;
import cz.cuni.mff.d3s.deeco.annotations.pathparser.PathOrigin;
import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;
import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeNotFoundException;
import cz.cuni.mff.d3s.deeco.knowledge.ValueSet;
import cz.cuni.mff.d3s.deeco.model.runtime.api.KnowledgePath;
import cz.cuni.mff.d3s.deeco.task.KnowledgePathHelper;
import cz.cuni.mff.d3s.demo.Coordinates;
import cz.cuni.mff.d3s.demo.SimulationConstants;
import cz.cuni.mff.d3s.demo.audit.stats.NumOfGroupsCompleteStatistic;
import cz.cuni.mff.d3s.demo.audit.stats.OverallCostStatistic;
import cz.cuni.mff.d3s.demo.audit.stats.Statistic;
import cz.cuni.mff.d3s.demo.audit.stats.SumDistanceToTargetStatistic;
import cz.cuni.mff.d3s.demo.audit.stats.TotalMovesStatistic;
import cz.cuni.mff.d3s.demo.audit.stats.WellPlacedSoldiersStatistic;
import cz.cuni.mff.d3s.demo.components.SoldierData;


public class SimulationController {
	
	private String graphFileName;
	
	private FileWriter statsFileWriter;
	
	private static List<Statistic> statsList;
	
	public static double totalMoves = 0.0;
	
	static {
		statsList = new ArrayList<Statistic>();
		//statsList.add(new TotalMovesStatistic());
		//statsList.add(new SumDistanceToTargetStatistic());
		//statsList.add(new WellPlacedSoldiersStatistic());
		//statsList.add(new NumOfGroupsCompleteStatistic());
		statsList.add(new OverallCostStatistic());

	}
	
	public SimulationController(String outputName) {
		String outputDirName = "results/" + outputName;
		new File(outputDirName).mkdirs();
		
		graphFileName = outputDirName + "/audit%07d.dot";

		try {
			statsFileWriter = new FileWriter("results/stats_" + outputName + ".csv");
		
			statsFileWriter.write(String.format("PARAMETERS: IsCentralized=%b, SoldierCount=%d, RandomSeed=%d\n", 
					SimulationConstants.IsCentralized, SimulationConstants.SoldierCount, SimulationConstants.RandomSeed));
			
			statsFileWriter.write("Time;");
			for (Statistic stat : statsList) {
				statsFileWriter.write(stat.getName() + ";");
			}

			statsFileWriter.write("\n");
			statsFileWriter.flush();
			
		} catch (IOException e) {
			System.err.println("IO error while creating stats CSV file.");
		}		
	}
	
	//
	// Each time doAudit is called, following steps are performed:
	//
	// 1) Soldier status is printed out to console.
	//
	// 2) Soldier positions are printed out to DOT file which is later processed by GraphViz.
	//
	// 3) Statistical data are printed out to CSV file which can be later processed in Excel.
	//
	
	public void doAudit(long time, Map<String, KnowledgeManager> knowledgeManagers) {
		
		Map<String, AuditData> currentSoldierData;
		try {
			currentSoldierData = constructAuditData(knowledgeManagers);
		} catch (KnowledgeNotFoundException e) {
			System.err.println("SimulationController: Knowledge not found.");
			return;
		} catch (ParseException | AnnotationProcessorException e) {
			System.err.println("SimulationController: Invalid knowledge.");
			return;
		}
		
		printState(currentSoldierData);
		
		try {
			saveGraph(String.format(graphFileName, time), currentSoldierData);
		} catch (IOException ioe) {
			System.err.println("IO error while saving results.");
		}
		
		try {
			saveStats(statsFileWriter, time, currentSoldierData);
		} catch (IOException e) {
			System.err.println("IO error while saving stats.");
		}
	}
	
	// returns all components (even offline)
	private Map<String, AuditData> constructAuditData(Map<String, KnowledgeManager> knowledgeManagers) throws KnowledgeNotFoundException, ParseException, AnnotationProcessorException {
		Map<String, AuditData> result = new HashMap<>();

		for (Entry<String, KnowledgeManager> managerEntry : knowledgeManagers.entrySet()) {
			KnowledgeManager manager = managerEntry.getValue();
			KnowledgePath ensembleIdKnowledgePath = KnowledgePathHelper.createKnowledgePath("everyone.[id].ensembleId", PathOrigin.COMPONENT);
			KnowledgePath soldierDataKnowledgePath = KnowledgePathHelper.createKnowledgePath("everyone.[id]", PathOrigin.COMPONENT);
			KnowledgePath isOnlineKnowledgePath = KnowledgePathHelper.createKnowledgePath("isOnline", PathOrigin.COMPONENT);
			KnowledgePath everyoneKnowledgePath = KnowledgePathHelper.createKnowledgePath("everyone", PathOrigin.COMPONENT);
			
			ensembleIdKnowledgePath = KnowledgePathHelper.getAbsolutePath(ensembleIdKnowledgePath, manager);
			soldierDataKnowledgePath = KnowledgePathHelper.getAbsolutePath(soldierDataKnowledgePath, manager);
			isOnlineKnowledgePath = KnowledgePathHelper.getAbsolutePath(isOnlineKnowledgePath, manager);
			everyoneKnowledgePath = KnowledgePathHelper.getAbsolutePath(everyoneKnowledgePath, manager);
			
			ArrayList<KnowledgePath> knowledgePaths = new ArrayList<>();
			knowledgePaths.add(ensembleIdKnowledgePath);
			knowledgePaths.add(soldierDataKnowledgePath);
			knowledgePaths.add(isOnlineKnowledgePath);
			knowledgePaths.add(everyoneKnowledgePath);
			ValueSet valueSet = manager.get(knowledgePaths);
			AuditData auditData = new AuditData();
			auditData.soldierData = (SoldierData)valueSet.getValue(soldierDataKnowledgePath);
			auditData.ensembleId = (Integer)valueSet.getValue(ensembleIdKnowledgePath);
			auditData.isOnline = (Boolean)valueSet.getValue(isOnlineKnowledgePath);
			auditData.knowsAbout = ((Map<String, SoldierData>)valueSet.getValue(everyoneKnowledgePath)).keySet();
			result.put(managerEntry.getKey(), auditData);
		}
		
		return result;
	}
	
	//
	// printing state to console
	//
	
	private void printState(Map<String, AuditData> soldierData) {
		for (Entry<String, AuditData> soldierEntry : soldierData.entrySet()) {
			if (soldierEntry.getValue().isOnline) {
				ArrayList<String> knowsAbout = new ArrayList<String>(soldierEntry.getValue().knowsAbout);
				knowsAbout.sort(new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						return Integer.compare(Integer.parseInt(o1), Integer.parseInt(o2));
					}
				});
				
				System.out.printf("Soldier #%s: group = %d, coords = %s, knows %s\n", soldierEntry.getKey(), soldierEntry.getValue().ensembleId,
						soldierEntry.getValue().soldierData.coords.toString(), knowsAbout.toString());
			} else {
				System.out.printf("Soldier #%s: offline\n", soldierEntry.getKey());
			}
		}
	}
	
	//
	// printing positions to DOT file
	//
		
	private void saveGraph(String filename, Map<String, AuditData> soldierData) throws IOException	{				
		File outputFile = new File(filename);
		FileWriter fileWriter = new FileWriter(outputFile);
		PrintWriter writer = new PrintWriter(fileWriter);
		
		writer.println("digraph {");

		int[] numsInCorners = getGroupSizes(soldierData.values());
		
		// the four corners
		// WARNING: THIS ASSUMES THAT THERE ARE FOUR TARGETS IN A SPECIFIC ORDER!!
		writer.printf("a [label=\"%d\", pos=\"-10,-10\", shape=circle, width=0.004, height=0.004]\n", numsInCorners[0]);
		writer.printf("b [label=\"%d\", pos=\"-10,%f\", shape=circle, width=0.004, height=0.004]\n", numsInCorners[1], SimulationConstants.FieldHeight + 10);
		writer.printf("c [label=\"%d\", pos=\"%f,-10\", shape=circle, width=0.004, height=0.004]\n", numsInCorners[2], SimulationConstants.FieldWidth + 10);
		writer.printf("d [label=\"%d\", pos=\"%f,%f\", shape=circle, width=0.004, height=0.004]\n", numsInCorners[3], SimulationConstants.FieldWidth + 10, SimulationConstants.FieldHeight + 10);
		writer.println();
		
		// the target places
		int i = 0;
		for (Coordinates coords : SimulationConstants.TargetCoordinates) {
			writer.printf("T%d [pos=%s, shape=box, color=%s]\n", i, formatPos(coords), getColor(true, i));
			i++;
		}
		
		writer.println();
		
		for (Entry<String, AuditData> soldierEntry : soldierData.entrySet()) {
			AuditData soldier = soldierEntry.getValue();
			writer.printf("S%s [pos=%s, color=%s, width=0.02, height=0.02]\n", soldierEntry.getKey(), 
					formatPos(soldier.soldierData.coords), getColor(soldier.isOnline, soldier.ensembleId));
		}
		
		writer.println("}");
				
		writer.close();
	}
	
	private static String formatPos(Coordinates coords) {
		return String.format(Locale.US, "\"%f,%f\"", coords.getX(), coords.getY());
	}
	
	private static final String[] colors = new String[] { "red", "blue", "green", "purple" };
	
	private static String getColor(boolean isOnline, int groupId) {
		if (isOnline) {
			if (groupId >= 0) {
				return colors[groupId % colors.length];
			} else {
				return "black";
			}
		} else {
			return "grey";
		}
	}
	
	private static int[] getGroupSizes(Collection<AuditData> soldierData) {
		int[] result = new int[SimulationConstants.TargetCoordinates.length];
		for (AuditData soldier : soldierData) {
			if (soldier.isOnline && soldier.ensembleId >= 0) {
				result[soldier.ensembleId]++;
			}
		}
		
		return result;
	}
	
	//
	// printing statistical data
	//
	
	private void saveStats(FileWriter fileWriter, long time, Map<String, AuditData> soldierData) throws IOException {
		
		PrintWriter writer = new PrintWriter(fileWriter);
		
		writer.printf("%d;", time);
		for (Statistic stat : statsList) {
			writer.printf("%s;", stat.calculate(soldierData));
		}
		
		writer.println();		
		writer.flush();
		
	}
}
