package robot;

import map.MapConstants;
import robot.RobotConstants.DIRECTION;

public class RobotConstants {
	public static final int GOAL_ROW = MapConstants.GOAL_ROW;
	public static final int GOAL_COL = MapConstants.GOAL_COL;
	public static final int START_ROW = 1;
	public static final int START_COL = 1;
	public static final DIRECTION START_DIR = DIRECTION.EAST;
	public static final int SPEED = 50; 
	
	public static final int INFINITE_COST = 9999;
	public static final double MOVE_COST = 5;
	public static final double TURN_COST = 20;
	public static final double DIAGONAL_COST=Math.sqrt(50);
	
	
	public static final boolean REAL_RUN = true;
	public static final boolean TESTING_FAST = false;
	public static final int NUM_SENSORS = 6;
	
	public enum DIRECTION {
		NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST;
		
		public static DIRECTION getNext90(DIRECTION curDirection) {
			return values()[(curDirection.ordinal() + 2) % values().length];
		}
		
		public static DIRECTION getPrevious90(DIRECTION curDirection) {
			return values()[(curDirection.ordinal() + values().length - 2) % values().length];
		}
		
		public static DIRECTION getNext45(DIRECTION curDirection) {
			return values()[(curDirection.ordinal() + 1) % values().length];
		}
		
		public static DIRECTION getPrevious45(DIRECTION curDirection) {
			return values()[(curDirection.ordinal() + values().length - 1) % values().length];
		}
	}
	
	public enum MOVEMENT {
		FORWARD, BACKWARD, LEFT, RIGHT, DIAGONALLEFT, DIAGONALRIGHT, DIAGONALFORWARD, CALIBRATE, ERROR;
		
		public static String movementCommand(MOVEMENT m) {
			switch (m) {
			case FORWARD:
				return "1";
			case BACKWARD:
				return "s";
			case LEFT:
				return "a";
			case RIGHT:
				return "d";
			case DIAGONALLEFT:
				return "q";
			case DIAGONALRIGHT:
				return "e";
			case DIAGONALFORWARD:
				return "2";
			case CALIBRATE:
				return "c";
			case ERROR:
			default:
				return "EE";
			}
		}
		
	}
}
