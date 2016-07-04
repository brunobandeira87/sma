package action;

public enum Direction {

	NORTH("north"),
	SOUTH("south"),
	NORTHWEST("northwest"),
	SOUTHWEST("southwest"),
	NORTHEAST("northeast"),
	SOUTHEAST("southeast"),
	WEST("west"),
	EAST("east");
	
	private String value;
	
	Direction(String value) {
		this.setValue(value);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
