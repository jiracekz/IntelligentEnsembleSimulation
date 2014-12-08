package cz.cuni.mff.d3s.demo.components;

import java.util.Random;

import cz.cuni.mff.d3s.demo.SimulationConstants;

public class SoldierData {

	public long timestamp;
	
	public int knowledge;
	
	private static Random generator;
	
	static {
		generator = new Random(42);
	}
	
	public SoldierData(int knowledge) {
		this.knowledge = knowledge;
	}
	
	public SoldierData() {
		this (generator.nextInt(10000000));
	}
	
	public SoldierData clone() {
		SoldierData copy = new SoldierData(knowledge);
		copy.timestamp = timestamp;
		return copy;
	}
	
	public boolean isAlive(long currentTime) {
		long timeDiff = Math.abs(timestamp - currentTime);
		return (timeDiff <= SimulationConstants.KnowledgeTimeout);
	}
	
	@Override
	public String toString() {
		return "{k: " + knowledge + ", ts: " + timestamp + "}";
	}
	
}
