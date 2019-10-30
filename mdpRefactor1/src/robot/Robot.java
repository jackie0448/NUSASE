package robot;

import java.util.concurrent.TimeUnit;

import algorithms.Calibration;
import map.Map;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;
import utils.CommMgr;
import utils.MapDescriptor;

public class Robot {
	private int posRow;
	private int posCol;
	private DIRECTION posDir;
	
	private boolean realRun;
	private int delay;
	private int stepCounter = 0;
	
	private final Sensor SRNorthLeft;
	private final Sensor SRNorthCenter;
	private final Sensor SRNorthRight;
	private final Sensor SRWest;
	private final Sensor SREast;
	private final Sensor LRWest;
	
	
	public Robot(int row, int col, boolean realRun) {
		posRow = row;
		posCol = col;
		posDir = RobotConstants.START_DIR;
		delay = RobotConstants.SPEED;
		this.realRun = realRun;
		
		/* Naming convention: SR/LR - North/South/West/East - Left/Center/Right block */
		SRNorthLeft = new Sensor(1,2, this.posRow + 1, this.posCol - 1, this.posDir, "SRNL");
		SRNorthCenter = new Sensor(1,2, this.posRow + 1, this.posCol, this.posDir, "SRNC");
		SRNorthRight = new Sensor(1,2, this.posRow + 1, this.posCol + 1, this.posDir, "SRNR");
		SRWest = new Sensor(1,2, this.posRow + 1, this.posCol - 1, findNewDirection(MOVEMENT.LEFT), "SRW");
		SREast = new Sensor(1,2, this.posRow + 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT), "SRE");
		LRWest = new Sensor(2,5, this.posRow + 1, this.posCol - 1, findNewDirection(MOVEMENT.LEFT), "LRW");
	}
	
	public void setRobotPos(int row, int col) {
		posRow = row;
		posCol = col;
	}
	
	public int getRobotPosRow() {
		return posRow;
	}
	
	public void setRobotPosRow(int row) {
		posRow = row;
	}
	
	public int getRobotPosCol() {
		return posCol;
	}
	
	public void setRobotPosCol(int col) {
		posCol = col;
	}
	
	public DIRECTION getRobotPosDir() {
		return posDir;
	}
	
	public void setRobotPosDir(DIRECTION dir) {
		posDir = dir;
	}
	
	public boolean getRealRun() {
		return realRun;
	}
	
	public void setRealRun(boolean val) {
		realRun = val;
	}
	
	public int getDelay() {
		return delay;
	}
	
	public void setDelay(int val) {
		delay = val;
	}
	
	/**
	 * Use current direction and give movement to find new direction
	 */
	public DIRECTION findNewDirection(MOVEMENT m) {
		switch (m) {
		case RIGHT:
			return DIRECTION.getNext90(posDir);
		case LEFT:
			return DIRECTION.getPrevious90(posDir);
		case DIAGONALLEFT:
			return DIRECTION.getPrevious45(posDir);
		case DIAGONALRIGHT:
			return DIRECTION.getNext45(posDir);
		case BACKWARD:
			return DIRECTION.getNext90(DIRECTION.getNext90(posDir));
		}
		return posDir;
	}
	
	/**
	 * Adjust sensors based on sensor direction
	 */
    public void setSensors() {
		switch (posDir) {
		case NORTH:
			SRNorthLeft.setSensor(this.posRow + 1, this.posCol - 1, this.posDir);
			SRNorthCenter.setSensor(this.posRow + 1, this.posCol, this.posDir);
			SRNorthRight.setSensor(this.posRow + 1, this.posCol + 1, this.posDir);
			SREast.setSensor(this.posRow + 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT));
			SRWest.setSensor(this.posRow + 1, this.posCol - 1, findNewDirection(MOVEMENT.LEFT));
			LRWest.setSensor(this.posRow, this.posCol - 1, findNewDirection(MOVEMENT.LEFT));
			break;
		case EAST:
			SRNorthLeft.setSensor(this.posRow + 1, this.posCol + 1, this.posDir);
			SRNorthCenter.setSensor(this.posRow, this.posCol + 1, this.posDir);
			SRNorthRight.setSensor(this.posRow - 1, this.posCol + 1, this.posDir);
			SREast.setSensor(this.posRow - 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT));
			SRWest.setSensor(this.posRow + 1, this.posCol + 1, findNewDirection(MOVEMENT.LEFT));
			LRWest.setSensor(this.posRow + 1, this.posCol, findNewDirection(MOVEMENT.LEFT));
			break;
		case SOUTH:
			SRNorthLeft.setSensor(this.posRow - 1, this.posCol + 1, this.posDir);
			SRNorthCenter.setSensor(this.posRow - 1, this.posCol, this.posDir);
			SRNorthRight.setSensor(this.posRow - 1, this.posCol - 1, this.posDir);
			SREast.setSensor(this.posRow - 1, this.posCol - 1, findNewDirection(MOVEMENT.RIGHT));
			SRWest.setSensor(this.posRow - 1, this.posCol + 1, findNewDirection(MOVEMENT.LEFT));
			LRWest.setSensor(this.posRow, this.posCol + 1, findNewDirection(MOVEMENT.LEFT));
			break;
		case WEST:
			SRNorthLeft.setSensor(this.posRow - 1, this.posCol - 1, this.posDir);
			SRNorthCenter.setSensor(this.posRow, this.posCol - 1, this.posDir);
			SRNorthRight.setSensor(this.posRow + 1, this.posCol - 1, this.posDir);
			SREast.setSensor(this.posRow + 1, this.posCol - 1, findNewDirection(MOVEMENT.RIGHT));
			SRWest.setSensor(this.posRow - 1, this.posCol - 1, findNewDirection(MOVEMENT.LEFT));
			LRWest.setSensor(this.posRow - 1, this.posCol, findNewDirection(MOVEMENT.LEFT));
			break;
		}
	}
    
    /**
     * RESULT: SRWest, SREast, SRNorthLeft, SRNorthCenter, SRNorthRight, LR [if any]
     * @param explorationMap
     * @param realMap
     * @return
     */
    public void sense(Map explorationMap, Map realMap) {
    	int[] result = new int[RobotConstants.NUM_SENSORS + 1];
    	
    	if (!realRun) {
    		result[0] = SRWest.sense(explorationMap,realMap);
    		result[1] = SREast.sense(explorationMap,realMap);
    		result[2] = SRNorthLeft.sense(explorationMap,realMap);
    		result[3] = SRNorthCenter.sense(explorationMap,realMap);
    		result[4] = SRNorthRight.sense(explorationMap,realMap);
    		result[5] = LRWest.sense(explorationMap,realMap);

    		result[RobotConstants.NUM_SENSORS] = stepCounter;
    		stepCounter ++;
    		
    		System.out.print("Simulated Sensor Readings: ");
    		for (int c = 0; c < RobotConstants.NUM_SENSORS; c ++) {
    			System.out.print(result[c] + ",");
    		}
    		System.out.println(result[RobotConstants.NUM_SENSORS]);
    		
    		String[] androidStrings = MapDescriptor.generateMapDescriptor(explorationMap);
    		System.out.print("Sending to Android: ");
    		System.out.println(androidStrings[0] + "," + androidStrings[1]+ "," + this.getRobotPosDir() + ","+ Integer.toString(this.getRobotPosRow())+ "," + Integer.toString(this.getRobotPosCol()));
    		
    	}
    	else {
    		String[] msgArr;
        	CommMgr comm = CommMgr.getCommMgr();
        	System.out.println("Before dowhile");
        	
        	do {
        		String realMsg = comm.recvMsg();
        		System.out.println(realMsg);
        		msgArr = realMsg.split(",");
        		System.out.println(msgArr[RobotConstants.NUM_SENSORS] + " : " + stepCounter);
        		
        		for (int i = 0; i < RobotConstants.NUM_SENSORS; i ++) {
        			try {
            			result[i] = Integer.parseInt(msgArr[i]);
            		} catch (Exception e) {
            			result[i] = 0;
            			 System.out.println("Result["+i+"] set to 0");
            		}
        		}
        		
//        		String[] androidStrings = MapDescriptor.generateMapDescriptor(explorationMap);
//        		System.out.print("Sending to Android: ");
//        		System.out.println(androidStrings[0] + "," + androidStrings[1]+ "," + this.getRobotPosDir() + ","+ Integer.toString(this.getRobotPosRow())+ "," + Integer.toString(this.getRobotPosCol()));
//        		comm.sendMsg("AN",androidStrings[0] + "," + androidStrings[1]+ "," + this.getRobotPosDir() + ","+ Integer.toString(this.getRobotPosRow())+ "," + Integer.toString(this.getRobotPosCol()));
//        		
        	}while (Integer.parseInt(msgArr[RobotConstants.NUM_SENSORS]) != stepCounter);
    	
        	SRWest.senseReal(explorationMap, result[0]);
        	SREast.senseReal(explorationMap, result[1]);
        	SRNorthLeft.senseReal(explorationMap, result[2]);
        	SRNorthCenter.senseReal(explorationMap, result[3]);
        	SRNorthRight.senseReal(explorationMap, result[4]);
        	LRWest.senseReal(explorationMap, result[5]);
        	stepCounter ++; 	
    	}
    }
    
    /**
     * Takes in a movement and move the robot accordingly by changing its position
     */
    public void move(MOVEMENT m) {
    	if (!realRun) {
    		// Emulate real movement by pausing execution
    		try {
    			TimeUnit.MILLISECONDS.sleep(delay);
    		} catch (InterruptedException e) {
    			System.out.println("Something went wrong in Robot.move()!");
    		}
    	}
    	
    	switch (m) {
        case FORWARD:
        	Calibration.cCounterFlat ++;
			Calibration.cCounterCorner ++;
            switch (posDir) {
                case NORTH:
                    posRow++;
                    break;
                case EAST:
                    posCol++;
                    break;
                case SOUTH:
                    posRow--;
                    break;
                case WEST:
                    posCol--;
                    break;   	
            }
            break;
        case BACKWARD:
            posDir = findNewDirection(m);
            break;
        case DIAGONALRIGHT:
        	posDir= findNewDirection(m);
        	break;
        case DIAGONALLEFT:
        	posDir=findNewDirection(m);
        	break;
        case DIAGONALFORWARD:
            switch (posDir) {
            case NORTHEAST:
            	posRow++;
            	posCol++;
            	break;
            case NORTHWEST:
            	posRow++;
            	posCol--;
            	break;
            case SOUTHEAST:
            	posRow--;
            	posCol++;
            	break;
            case SOUTHWEST:
            	posRow--;
            	posCol--;
            	break;     	
            }
            break;
        case RIGHT:
        case LEFT:
        	posDir = findNewDirection(m);
            break;
        case CALIBRATE:
        	break;
        default:
        	System.out.println("MOVEMENT not recognised in Robot.move()!");
        	break;
    	}
    	
    	if (!realRun) {
    		System.out.println("Move Command: " + MOVEMENT.movementCommand(m));
    	}
    	else {
    		
    		CommMgr comm = CommMgr.getCommMgr();
    		comm.sendMsg("AR",(MOVEMENT.movementCommand(m)));
    	}
    }
}
