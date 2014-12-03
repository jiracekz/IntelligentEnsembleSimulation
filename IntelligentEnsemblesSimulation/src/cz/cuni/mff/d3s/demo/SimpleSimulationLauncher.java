package cz.cuni.mff.d3s.demo;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessor;
import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;
import cz.cuni.mff.d3s.deeco.knowledge.CloningKnowledgeManagerFactory;
import cz.cuni.mff.d3s.deeco.model.runtime.api.RuntimeMetadata;
import cz.cuni.mff.d3s.deeco.model.runtime.custom.RuntimeMetadataFactoryExt;
import cz.cuni.mff.d3s.deeco.runtime.RuntimeFramework;
import cz.cuni.mff.d3s.deeco.simulation.DirectKnowledgeDataHandler;
import cz.cuni.mff.d3s.deeco.simulation.DirectSimulationHost;
import cz.cuni.mff.d3s.deeco.simulation.JDEECoSimulation;
import cz.cuni.mff.d3s.deeco.simulation.NetworkKnowledgeDataHandler;
import cz.cuni.mff.d3s.deeco.simulation.SimulationRuntimeBuilder;
import cz.cuni.mff.d3s.deeco.simulation.TimerTaskListener;
import cz.cuni.mff.d3s.demo.components.Soldier;
import cz.cuni.mff.d3s.demo.ensembles.ReplicationCoordinationEnsemble;

public class SimpleSimulationLauncher {

	public static int SimulationLength = 60000;
	public static int SnapshotInterval = 1000;
	public static int SoldierCount = 2;
	public static int SquadSize = 3;

	private static JDEECoSimulation simulation;
	private static SimulationRuntimeBuilder builder;

	//public static Soldier[] soldiers;
	
	public static void run() throws AnnotationProcessorException {		
		System.out.println("Preparing simulation");

		// no delay when transferring knowledge
		NetworkKnowledgeDataHandler networkHandler = new DirectKnowledgeDataHandler();
		simulation = new JDEECoSimulation(0, SimulationLength, networkHandler);

		builder = new SimulationRuntimeBuilder();
		
		RuntimeMetadata[] models = new RuntimeMetadata[SoldierCount];
		AnnotationProcessor[] processors = new AnnotationProcessor[SoldierCount];
		for (int i = 0; i < SoldierCount; i++) {
			models[i] = RuntimeMetadataFactoryExt.eINSTANCE.createRuntimeMetadata();
			processors[i] = new AnnotationProcessor(
					RuntimeMetadataFactoryExt.eINSTANCE, models[i], new CloningKnowledgeManagerFactory());
		}
		
		ComponentUptimeDecider decider = new ComponentUptimeDecider();
		
		
		Soldier[] soldiers = new Soldier[SoldierCount];
		for (int i = 0; i < SimpleSimulationLauncher.SoldierCount /*- 1*/; i++) {
			soldiers[i] = new Soldier(i, true, decider);
			processors[i].process(soldiers[i]);			
		}
		/*
		soldiers[6] = new Soldier(6, false);
		processor.process(soldiers[6]);
		*/
		for (int i = 0; i < SoldierCount; i++) {
			processors[i].process(ReplicationCoordinationEnsemble.class);
		}
		
		DirectSimulationHost[] hosts = new DirectSimulationHost[SoldierCount];
		RuntimeFramework runtimes[] = new RuntimeFramework[SoldierCount];
		List<TimerTaskListener> listeners = Arrays.asList(new AuditListener());
		for (int i = 0; i < SoldierCount; i++) {
			hosts[i] = simulation.getHost("Host" + i);
			runtimes[i] = builder.build(hosts[i], simulation, i == 0 ? listeners : null, models[i], 
					new AlwaysRebroadcastingKnowledgeDataManager(models[i].getEnsembleDefinitions(), null), 
					new CloningKnowledgeManagerFactory());
			runtimes[i].start();
		}

		System.out.println("Run the simulation");
		//Run the simulation
		simulation.run();
		System.out.println("Simulation Finished.");
		
	
		try {
			SimulationController.SaveResults("results.csv");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

}
