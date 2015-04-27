package cz.cuni.mff.d3s.demo;

import java.io.Serializable;

import cz.cuni.mff.d3s.jdeeco.position.Position;

public class Coordinates implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3768230178609887328L;
	
	private double x;
	private double y;
	
	public Coordinates(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getDistanceTo(Coordinates target) {
		Coordinates diff = new Coordinates(target.getX() - this.getX(), target.getY() - this.getY());
		return Math.sqrt((double)diff.getX() * diff.getX() + (double)diff.getY() * diff.getY());
	}
	
	public Coordinates moveVectorTo(Coordinates target, double length) {
		Coordinates diff = new Coordinates(target.getX() - this.getX(), target.getY() - this.getY());
		if (diff.getX() == 0 && diff.getY() == 0) {
			return diff;
		}
		
		// normalize
		double distance = getDistanceTo(target);
		double ratio = length / distance;
		diff.x *= ratio;
		diff.y *= ratio;

		// add
		return new Coordinates(x + diff.x, y + diff.y);
	}
	
	public Position toPosition() {
		return new Position(x, y, 0);
	}
	
	@Override
	public String toString() {
		return String.format("(%.2f, %.2f)", x, y);
	}
	
}
