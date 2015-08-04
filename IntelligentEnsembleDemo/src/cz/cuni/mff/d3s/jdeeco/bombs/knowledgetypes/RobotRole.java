package cz.cuni.mff.d3s.jdeeco.bombs.knowledgetypes;

public enum RobotRole {
	UNDEFINED,
	EXPLORER,
	RELAY,
	DEFUSER;
	
	@Override
	public String toString() {
		if (this == UNDEFINED) {
			return "<undef>";
		} else if (this == EXPLORER) {
			return "explorer";
		} else if (this == RELAY) {
			return "relay";
		} else if (this == DEFUSER) {
			return "defuser";
		} else {
			return "<bad_role>";
		}
	}
}