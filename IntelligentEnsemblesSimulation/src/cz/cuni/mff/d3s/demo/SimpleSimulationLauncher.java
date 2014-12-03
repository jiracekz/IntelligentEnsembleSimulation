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
	private static AnnotationProcessor processor;

	//public static Soldier[] soldiers;
	
	public static void run() throws AnnotationProcessorException {		
		System.out.println("Preparing simulation");

		// no delay when transferring knowledge
		NetworkDataHandler networkHandler = new DirectKnowledgeDataHandler();
		simulation = new JDEECoSimulation(0, SimulationConstants.SimulationLength, networkHandler);

		builder = new SimulationRuntimeBuilder();
		
		RuntimeMetadata model = RuntimeMetadataFactoryExt.eINSTANCE
				.createRuntimeMetadata();		
		
		processor = new AnnotationProcessor(
				RuntimeMetadataFactoryExt.eINSTANCE, model, new CloningKnowledgeManagerFactory());
		
		
		ComponentUptimeDecider decider = new ComponentUptimeDecider();
		decider.generateUptimeData(SimulationConstants.IterationCount);		
		
		Soldier[] soldiers = new Soldier[SimulationConstants.SoldierCount];
		for (int i = 0; i < SimulationConstants.SoldierCount /*- 1*/; i++) {
			soldiers[i] = new Soldier(i, true, decider);
			processor.process(soldiers[i]);			
		}
		/*
		soldiers[6] = new Soldier(6, false);
		processor.process(soldiers[6]);
		*/
		processor.process(ReplicationCoordinationEnsemble.class);
		
		DirectSimulationHost host = simulation.getHost("Host");
		List<TimerTaskListener> listeners = Arrays.asList(new AuditListener());		
		RuntimeFramework runtime = builder.build(host, simulation, listeners, model, 
				new AlwaysRebroadcastingKnowledgeDataManager(model.getEnsembleDefinitions(), null), 
				new CloningKnowledgeManagerFactory());
		runtime.start();

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
