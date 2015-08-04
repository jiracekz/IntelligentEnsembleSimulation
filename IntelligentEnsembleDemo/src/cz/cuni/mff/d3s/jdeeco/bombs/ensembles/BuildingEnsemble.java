package cz.cuni.mff.d3s.jdeeco.bombs.ensembles;

import java.util.ArrayList;
import java.util.Collection;

import cz.cuni.mff.d3s.deeco.ensembles.EnsembleInstance;

public class BuildingEnsemble implements EnsembleInstance {

	//@Subensemble
	public Collection<RoomEnsemble> roomTeams;

	public BuildingEnsemble() {
		roomTeams = new ArrayList<>();
	}
	
	@Override
	public void performKnowledgeExchange() {
		for (RoomEnsemble roomEnsemble : roomTeams) {
			roomEnsemble.performKnowledgeExchange();
		}
	}

}
