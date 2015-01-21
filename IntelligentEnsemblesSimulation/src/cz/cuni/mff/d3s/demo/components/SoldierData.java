package cz.cuni.mff.d3s.demo.components;

import java.util.Random;

import cz.cuni.mff.d3s.demo.Coordinates;
import cz.cuni.mff.d3s.demo.SimulationConstants;

public class SoldierData {

	public long timestamp;
	
	public Coordinates coords;
	
	private static Random generator;
	
	static {
		generator = new Random(SimulationConstants.RandomSeed);
	}
	
	public SoldierData(double x, double y) {
		this.coords = new Coordinates(x, y);
	}
	
	public SoldierData(Coordinates coords) {
		this.coords = coords;
	}
	
	public SoldierData() {
		this (generator.nextDouble() * SimulationConstants.FieldWidth, generator.nextDouble() * SimulationConstants.FieldHeight);
	}
	
	public SoldierData clone() {
		SoldierData copy = new SoldierData(coords);
		copy.timestamp = timestamp;
		return copy;
	}
	
	public boolean isAlive(long currentTime) {
		long timeDiff = Math.abs(timestamp - currentTime);
		return (timeDiff <= SimulationConstants.KnowledgeTimeout);
	}
	
	@Override
	public String toString() {
		return "{coords: " + coords + ", ts: " + timestamp + "}";
	}	
}
