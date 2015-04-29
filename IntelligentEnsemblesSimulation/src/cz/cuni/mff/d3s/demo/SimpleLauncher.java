package cz.cuni.mff.d3s.demo;

import java.security.KeyStoreException;

import cz.cuni.mff.d3s.deeco.annotations.processor.AnnotationProcessorException;
import cz.cuni.mff.d3s.deeco.runners.DEECoSimulation;
import cz.cuni.mff.d3s.deeco.runtime.DEECoException;
import cz.cuni.mff.d3s.deeco.runtime.DEECoNode;
import cz.cuni.mff.d3s.deeco.runtime.RuntimeFramework;
import cz.cuni.mff.d3s.deeco.timer.DiscreteEventTimer;
import cz.cuni.mff.d3s.deeco.timer.SimulationTimer;
import cz.cuni.mff.d3s.demo.audit.SimulationController;
import cz.cuni.mff.d3s.demo.components.Soldier;
import cz.cuni.mff.d3s.demo.components.SoldierDirectionsCenter;
import cz.cuni.mff.d3s.demo.ensembles.CentralizedCoordinationEnsemble;
import cz.cuni.mff.d3s.demo.ensembles.ReplicationCoordinationEnsemble;
import cz.cuni.mff.d3s.demo.ensembles.SquadEnsemble;
import cz.cuni.mff.d3s.demo.uptime.ComponentUptimeDecider;
import cz.cuni.mff.d3s.demo.uptime.NoEventUptimeDecider;
import cz.cuni.mff.d3s.jdeeco.network.Network;
import cz.cuni.mff.d3s.jdeeco.network.device.SimpleBroadcastDevice;
import cz.cuni.mff.d3s.jdeeco.network.l2.strategy.KnowledgeInsertingStrategy;
import cz.cuni.mff.d3s.jdeeco.position.PositionPlugin;
import cz.cuni.mff.d3s.jdeeco.publishing.DefaultKnowledgePublisher;

public class SimpleLauncher {
	
	public static void main(String[] args) throws AnnotationProcessorException, KeyStoreException, InstantiationException, IllegalAccessException, DEECoException {
		
		// for bad times
		//System.out.println(System.getProperty("java.class.path"));
		
		SimulationController controller = processArguments(args);
		
		System.out.println("Preparing simulation");		
		
		/* create main application container */
		SimulationTimer simulationTimer = new DiscreteEventTimer(); // also "new WallTimeSchedulerNotifier()"
		DEECoSimulation realm = new DEECoSimulation(simulationTimer);
		realm.addPlugin(new SimpleBroadcastDevice(1500, 500, Integer.MAX_VALUE));
		realm.addPlugin(Network.class);
		realm.addPlugin(DefaultKnowledgePublisher.class);
		realm.addPlugin(KnowledgeInsertingStrategy.class);
		
		ComponentUptimeDecider decider = new ComponentUptimeDecider(new NoEventUptimeDecider(), SimulationConstants.IterationCount);	
		
		Soldier[] soldiers = new Soldier[SimulationConstants.SoldierCount];
		RuntimeFramework[] frameworks = new RuntimeFramework[SimulationConstants.SoldierCount];
		for (int i = 0; i < SimulationConstants.SoldierCount; i++) {		
			PositionPlugin position = new PositionPlugin(0, 0);
			soldiers[i] = new Soldier(i, true, decider, position);
			position.setStaticPosition(soldiers[i].getSoldierData().coords.toPosition());
			decider.setInitStateFor(i, soldiers[i].isOnline);
			
			/* create first deeco node */
			DEECoNode deeco1 = realm.createNode(position);
			/* deploy components and ensembles */
			deeco1.deployComponent(soldiers[i]);
			deeco1.deployEnsemble(SquadEnsemble.class);
			if (SimulationConstants.IsCentralized) {
				deeco1.deployEnsemble(CentralizedCoordinationEnsemble.class);
			} else {
				deeco1.deployEnsemble(ReplicationCoordinationEnsemble.class);
			}
			
			frameworks[i] = deeco1.getRuntimeFramework();
		}

		decider.generateUptimeData();
		
		DEECoNode diagnosticNode = realm.createNode(new PositionPlugin(0, 0));
		diagnosticNode.getRuntimeFramework().getScheduler().addTask(new AuditListener(controller, frameworks)
				.getInitialTask(diagnosticNode.getRuntimeFramework().getScheduler()));
		
		
		if (SimulationConstants.IsCentralized) {
			// add directions center
			DEECoNode centralNode = realm.createNode(new PositionPlugin(SimulationConstants.FieldWidth / 2, SimulationConstants.FieldHeight / 2));
			centralNode.deployComponent(new SoldierDirectionsCenter(SimulationConstants.SoldierCount));
			centralNode.deployEnsemble(CentralizedCoordinationEnsemble.class);
		}
		
		System.out.println("Run the simulation");
		//Run the simulation
		realm.start(SimulationConstants.SimulationLength);
		System.out.println("Simulation Finished.");
		
		

/*

		// no delay when transferring knowledge
		NetworkDataHandler networkHandler = new DelayedKnowledgeDataHandler(1500);
		//NetworkDataHandler networkHandler = new RandomDelayedKnowledgeDataHandler(0, 3000);
		simulation = new JDEECoSimulation(0, SimulationConstants.SimulationLength, networkHandler);

		builder = new SimulationRuntimeBuilder();
		
		RuntimeMetadata[] models = new RuntimeMetadata[SimulationConstants.SoldierCount];
		AnnotationProcessor[] processors = new AnnotationProcessor[SimulationConstants.SoldierCount];
		for (int i = 0; i < SimulationConstants.SoldierCount; i++) {
			models[i] = RuntimeMetadataFactoryExt.eINSTANCE.createRuntimeMetadata();
			processors[i] = new AnnotationProcessor(
					RuntimeMetadataFactoryExt.eINSTANCE, models[i], new CloningKnowledgeManagerFactory());
		}
	
		
		Soldier[] soldiers = new Soldier[SimulationConstants.SoldierCount];
		for (int i = 0; i < SimulationConstants.SoldierCount; i++) {
			soldiers[i] = new Soldier(i, true, decider);
			decider.setInitStateFor(i, soldiers[i].isOnline);
			processors[i].processComponent(soldiers[i]);			
		}
		
		decider.generateUptimeData();
		
		for (int i = 0; i < SimulationConstants.SoldierCount; i++) {
			if (SimulationConstants.IsCentralized) {
				processors[i].processEnsemble(CentralizedCoordinationEnsemble.class);
			} else {
				processors[i].processEnsemble(ReplicationCoordinationEnsemble.class);
			}
		}
		
		DirectSimulationHost[] hosts = new DirectSimulationHost[SimulationConstants.SoldierCount];
		RuntimeFramework runtimes[] = new RuntimeFramework[SimulationConstants.SoldierCount];
		List<TimerTaskListener> listeners = Arrays.asList(new AuditListener(controller, runtimes), (TimerTaskListener) networkHandler);
		List<TimerTaskListener> listeners0 = Arrays.asList((TimerTaskListener) networkHandler);
		for (int i = 0; i < SimulationConstants.SoldierCount; i++) {
			hosts[i] = simulation.getHost("Host" + i);
			runtimes[i] = builder.build(hosts[i], simulation, i == 0 ? listeners : listeners0, models[i], 
					new AlwaysRebroadcastingKnowledgeDataManager(models[i].getEnsembleDefinitions(), null), 
					new CloningKnowledgeManagerFactory(), SecurityKeyManagerImpl.getInstance(), RatingsManagerImpl.getInstance());
			runtimes[i].start();
		}
		
		if (SimulationConstants.IsCentralized) {
			// add directions center
			RuntimeMetadata model = RuntimeMetadataFactoryExt.eINSTANCE.createRuntimeMetadata();
			AnnotationProcessor processor = new AnnotationProcessor(
					RuntimeMetadataFactoryExt.eINSTANCE, model, new CloningKnowledgeManagerFactory());
			processor.processComponent(new SoldierDirectionsCenter(SimulationConstants.SoldierCount));
			processor.processEnsemble(CentralizedCoordinationEnsemble.class);
			
			DirectSimulationHost host = simulation.getHost("Coordinator");
			RuntimeFramework runtime = builder.build(host, simulation, listeners0, model,
					new AlwaysRebroadcastingKnowledgeDataManager(model.getEnsembleDefinitions(),  null),
					new CloningKnowledgeManagerFactory(), SecurityKeyManagerImpl.getInstance(), RatingsManagerImpl.getInstance());
			runtime.start();
		}
		
		System.out.println("Run the simulation");
		//Run the simulation
		simulation.run();
		System.out.println("Simulation Finished.");
		*/
	}

	private static SimulationController processArguments(String[] args) {
		// argument 3 - the random seed
		// (optional, default = keep 42 in SimulationConstants)
		if (args.length > 3) {
			SimulationConstants.RandomSeed = Integer.parseInt(args[3]);
		}
		
		// argument 2 - the number of soldiers
		// (optional, default = keep default in SimulationConstants)
		if (args.length > 2) {
			SimulationConstants.SoldierCount = Integer.parseInt(args[2]);
		}
		
		// argument 1 - the centralized/decentralized version switch
		// "D" for decentralized, "C" for centralized, other values = keep default in SimulationConstants
		// (optional, default = keep default in SimulationConstants)
		if (args.length > 1) {
			if (args[1].equals("D")) {
				SimulationConstants.IsCentralized = false;
			} else if (args[1].equals("C")) {
				SimulationConstants.IsCentralized = true;
			}
		}
						
		// argument 0 - the output name (optional, default = "default")
		SimulationController result;
		if (args.length > 0) {
			result = new SimulationController(args[0]);
		} else {
			result = new SimulationController("default");
		}		
		return result;
	}

}
