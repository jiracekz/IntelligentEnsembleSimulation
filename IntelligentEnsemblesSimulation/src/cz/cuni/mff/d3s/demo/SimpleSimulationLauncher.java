package cz.cuni.mff.d3s.demo;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessor;
import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;
import cz.cuni.mff.d3s.deeco.knowledge.CloningKnowledgeManagerFactory;
import cz.cuni.mff.d3s.deeco.model.runtime.api.RuntimeMetadata;
import cz.cuni.mff.d3s.deeco.model.runtime.custom.RuntimeMetadataFactoryExt;
import cz.cuni.mff.d3s.deeco.runtime.RuntimeFramework;
import cz.cuni.mff.d3s.deeco.simulation.DelayedKnowledgeDataHandler;
import cz.cuni.mff.d3s.deeco.simulation.DirectKnowledgeDataHandler;
import cz.cuni.mff.d3s.deeco.simulation.DirectSimulationHost;
import cz.cuni.mff.d3s.deeco.simulation.JDEECoSimulation;
import cz.cuni.mff.d3s.deeco.simulation.NetworkDataHandler;
import cz.cuni.mff.d3s.deeco.simulation.SimulationRuntimeBuilder;
import cz.cuni.mff.d3s.deeco.simulation.TimerTaskListener;
import cz.cuni.mff.d3s.demo.components.Soldier;
import cz.cuni.mff.d3s.demo.ensembles.ReplicationCoordinationEnsemble;

public class SimpleSimulationLauncher {

	private static JDEECoSimulation simulation;
	private static SimulationRuntimeBuilder builder;

	//public static Soldier[] soldiers;
	
	public static void run() throws AnnotationProcessorException {		
		System.out.println("Preparing simulation");

		// no delay when transferring knowledge
		NetworkDataHandler networkHandler = new DelayedKnowledgeDataHandler(274);
		//NetworkDataHandler networkHandler = new DirectKnowledgeHandler();
		simulation = new JDEECoSimulation(0, SimulationConstants.SimulationLength, networkHandler);

		builder = new SimulationRuntimeBuilder();
		
		RuntimeMetadata[] models = new RuntimeMetadata[SimulationConstants.SoldierCount];
		AnnotationProcessor[] processors = new AnnotationProcessor[SimulationConstants.SoldierCount];
		for (int i = 0; i < SimulationConstants.SoldierCount; i++) {
			models[i] = RuntimeMetadataFactoryExt.eINSTANCE.createRuntimeMetadata();
			processors[i] = new AnnotationProcessor(
					RuntimeMetadataFactoryExt.eINSTANCE, models[i], new CloningKnowledgeManagerFactory());
		}
		
		ComponentUptimeDecider decider = new ComponentUptimeDecider();
		decider.generateUptimeData(SimulationConstants.IterationCount);		
		
		Soldier[] soldiers = new Soldier[SimulationConstants.SoldierCount];
		for (int i = 0; i < SimulationConstants.SoldierCount /*- 1*/; i++) {
			soldiers[i] = new Soldier(i, true, decider);
			processors[i].process(soldiers[i]);			
		}
		/*
		soldiers[6] = new Soldier(6, false);
		processor.process(soldiers[6]);
		*/
		for (int i = 0; i < SimulationConstants.SoldierCount; i++) {
			processors[i].process(ReplicationCoordinationEnsemble.class);
		}
		
		DirectSimulationHost[] hosts = new DirectSimulationHost[SimulationConstants.SoldierCount];
		RuntimeFramework runtimes[] = new RuntimeFramework[SimulationConstants.SoldierCount];
		List<TimerTaskListener> listeners = Arrays.asList(new AuditListener(), (TimerTaskListener) networkHandler);
		List<TimerTaskListener> listeners0 = Arrays.asList((TimerTaskListener) networkHandler);
		for (int i = 0; i < SimulationConstants.SoldierCount; i++) {
			hosts[i] = simulation.getHost("Host" + i);
			runtimes[i] = builder.build(hosts[i], simulation, i == 0 ? listeners : listeners0, models[i], 
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
