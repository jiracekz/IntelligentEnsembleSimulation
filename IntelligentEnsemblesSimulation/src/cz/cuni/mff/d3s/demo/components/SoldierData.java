package cz.cuni.mff.d3s.demo.components;

import java.util.Random;

import cz.cuni.mff.d3s.demo.SimulationConstants;

public class SoldierData {

	public long timestamp;
	
	public int x;
	public int y;
	
	private static Random generator;
	
	static {
		generator = new Random(42);
	}
	
	public SoldierData(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public SoldierData() {
		this (generator.nextInt(1000), generator.nextInt(1000));
	}
	
	public SoldierData clone() {
		SoldierData copy = new SoldierData(x, y);
		copy.timestamp = timestamp;
		return copy;
	}
	
	public boolean isAlive(long currentTime) {
		long timeDiff = Math.abs(timestamp - currentTime);
		return (timeDiff <= SimulationConstants.KnowledgeTimeout);
	}
	
	@Override
	public String toString() {
		return "{x: " + x + " y: " + y + ", ts: " + timestamp + "}";
	}	
}
