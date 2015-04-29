package cz.cuni.mff.d3s.demo.components;

import java.io.Serializable;
import java.util.Random;

import cz.cuni.mff.d3s.demo.Coordinates;
import cz.cuni.mff.d3s.demo.SimulationConstants;

public class SoldierData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7753397990285711644L;

	public long timestamp;
	
	public Coordinates coords;
	
	public Integer ensembleId;
	
	
	private static Random generator;
	
	static {
		generator = new Random(SimulationConstants.RandomSeed);
	}
	
	public SoldierData(double x, double y) {
		this.ensembleId = -1;
		this.coords = new Coordinates(x, y);
	}
	
	public SoldierData(Coordinates coords) {
		this.ensembleId = -1;
		this.coords = coords;
	}
	
	public SoldierData() {
		this (generator.nextDouble() * (SimulationConstants.FieldWidth - 200),
				generator.nextDouble() * (SimulationConstants.FieldHeight - 200));
	}
	
	public SoldierData clone() {
		SoldierData copy = new SoldierData(coords);
		copy.timestamp = timestamp;
		copy.ensembleId = ensembleId;
		return copy;
	}
	
	public boolean isAlive(long currentTime) {
		long timeDiff = Math.abs(timestamp - currentTime);
		return (timeDiff <= SimulationConstants.KnowledgeTimeout);
	}
	
	@Override
	public String toString() {
		return "{coords: " + coords + ", ensemble: " + ensembleId + ", ts: " + timestamp + "}";
	}	
}
