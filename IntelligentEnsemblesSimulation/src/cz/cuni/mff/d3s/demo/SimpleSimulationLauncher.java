package cz.cuni.mff.d3s.demo;

import java.util.ArrayList;
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
import cz.cuni.mff.d3s.demo.ensembles.SquadEnsemble;

public class SimpleSimulationLauncher {

	public static int SimulationLength = 60000;
	public static int SnapshotInterval = 1000;
	public static int SoldierCount = 6;
	public static int SquadSize = 3;

	private static JDEECoSimulation simulation;
	private static SimulationRuntimeBuilder builder;
	private static AnnotationProcessor processor;

	public static void run() throws AnnotationProcessorException {		
		System.out.println("Preparing simulation");

		// no delay when transferring knowledge
		NetworkKnowledgeDataHandler networkHandler = new DirectKnowledgeDataHandler();
		simulation = new JDEECoSimulation(0, SimulationLength, networkHandler);

		builder = new SimulationRuntimeBuilder();
		
		RuntimeMetadata model = RuntimeMetadataFactoryExt.eINSTANCE
				.createRuntimeMetadata();		
		
		processor = new AnnotationProcessor(
				RuntimeMetadataFactoryExt.eINSTANCE, model, new CloningKnowledgeManagerFactory());
		
//		TestComponent component = new TestComponent(1, 1001, 101);
//		TestComponent component2 = new TestComponent(2, 1001, 102);
//		TestComponent component3 = new TestComponent(3, 1002, 103);
//		processor.process(component);
//		processor.process(component2);
//		processor.process(component3);	
//		
//		processor.process(TestEnsemble.class);
		
		for (int i = 0; i < SimpleSimulationLauncher.SoldierCount; i++) {
			Soldier s = new Soldier(i);
			processor.process(s);			
		}
		
		processor.process(ReplicationCoordinationEnsemble.class);
		//processor.process(SquadEnsemble.class);
		//processor.process(CentralizedCoordinationEnsemble.class);
		
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
	}

	

}
