package cz.cuni.mff.d3s.demo.components;

import java.util.Random;

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
	
	public boolean isAlive() {
		// TODO
		return true;
	}
	
	@Override
	public String toString() {
		return "{k: " + knowledge + ", ts: " + timestamp + "}";
	}
	
}
