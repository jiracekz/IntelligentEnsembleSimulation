package cz.cuni.mff.d3s.demo.assignment;

public enum SoldierAssignmentMode {
	AssignImmediately,
	AskAnyone,
	AskCoordinator;
	
	public static final int AskTimeout = 5000;
}
