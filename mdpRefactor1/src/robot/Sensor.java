package robot;

import map.Map;
import map.MapConstants;
import robot.RobotConstants.DIRECTION;

/**
 * Represents a sensor mounted on the robot.
 * @author Jesslyn Chew
 *
 */
public class Sensor {
	private final int lowerRange;
	private final int upperRange;
	private int sensorPosRow;
	private int sensorPosCol;
	private DIRECTION sensorDir;
	private final String id;
	
	public Sensor(int lowerRange, int upperRange, int row, int col, DIRECTION dir, String id) {
		this.lowerRange = lowerRange;
		this.upperRange = upperRange;
		this.sensorPosRow = row;
		this.sensorPosCol = col;
		this.sensorDir = dir;
		this.id = id;
	}
	
	/**
	 * Adjust sensor positioning based on the current robot direction
	 */
	public void setSensor(int row, int col, DIRECTION dir) {
		this.sensorPosRow = row;
		this.sensorPosCol = col;
		this.sensorDir = dir;
	}
	
	// Simulator Sensing
	/**
	 * Return the obstacle grid distance, 
	 * 0 if no obstacle is detected. 
	 */
	public int sense(Map exploredMap, Map realMap) {
		switch (sensorDir) {
		case NORTH:
			return getSensorVal(exploredMap,realMap,1,0);
		case EAST:
			return getSensorVal(exploredMap,realMap,0,1);
		case SOUTH:
			return getSensorVal(exploredMap,realMap,-1,0);
		case WEST: 
			return getSensorVal(exploredMap,realMap,0,-1);
		}
		return 0;
	}
	
    public void senseReal(Map exploredMap, int sensorVal) {
    	System.out.println("Running senseReal.");
        switch (sensorDir) {
            case NORTH:
                processSensorVal(exploredMap, sensorVal, 1, 0);
                break;
            case EAST:
            	System.out.println("Running east, before processSensorVal");
                processSensorVal(exploredMap, sensorVal, 0, 1);
                break;
            case SOUTH:
                processSensorVal(exploredMap, sensorVal, -1, 0);
                break;
            case WEST:
                processSensorVal(exploredMap, sensorVal, 0, -1);
                break;
        }
    }
	
    // TODO: Merge process and get
    private void processSensorVal(Map exploredMap, int sensorVal, int rowInc, int colInc) {
    	// If not long range
    	if (!id.equals("LRW")) {
    		int row = this.sensorPosRow + (rowInc);
			int col = this.sensorPosCol + (colInc);
			double d;
			
			if (!exploredMap.checkValidCoordinates(row, col)) return;
			if(exploredMap.getCell(row, col).getIsAccurate()) return;
			
			
			// If "accurate"
    		if (sensorVal == -1) {
    			exploredMap.setExploredCell(row, col, true);
    			exploredMap.getCell(row, col).setIsAccurate(true);
				exploredMap.getCell(row, col).forceConfidence(1);
				
				return;
    		}
    		
    		for (int i = lowerRange; i <= upperRange; i ++) {
    			row = this.sensorPosRow + rowInc * i;
    			col = this.sensorPosCol + colInc * i;
    			
    			if (!exploredMap.checkValidCoordinates(row, col)) return;
    			if(exploredMap.getCell(row, col).getIsAccurate()) return;
    			exploredMap.setExploredCell(row, col, true);
    			
    			switch (i) {
				case 1:
					d = 4;
					break;
				case 2: 
				default:
					d = 3;
					break;
				}
    			
    			// If the position has a block
    			if (sensorVal == i) {
    				exploredMap.setConfidenceCell(row, col, Math.pow(10, d));
    				exploredMap.setObstacleCell(row, col, true);
    				return;
    			} else {
    				exploredMap.setConfidenceCell(row, col, -Math.pow(10,d));
    				exploredMap.setObstacleCell(row, col, false);
    			}
    		}
    		
    	}
    	// If Long range
    	else {
    		
    		// Blind spot
    		for (int i = 1; i < this.lowerRange; i++) {
		          int row = this.sensorPosRow + (rowInc * i);
		          int col = this.sensorPosCol + (colInc * i);
		          
		          if (!exploredMap.checkValidCoordinates(row, col)) return;
		          if(exploredMap.getCell(row, col).getIsAccurate()) return;
		          if (exploredMap.getCell(row, col).getIsObstacle()) {
		          	//dont trust any values that the long range give for i=1 all th way to its lower range, so set default 
		          	//confidence as d=1
		          	return;
		        
		          }
		      }
    		
    		for (int i = lowerRange; i <= upperRange; i ++) {
    			int row = this.sensorPosRow + (rowInc * i);
    			int col = this.sensorPosCol + (colInc * i);
    			int d;
    			
    			if (!exploredMap.checkValidCoordinates(row, col)) return;
    			if(exploredMap.getCell(row, col).getIsAccurate()) return;
    			
    			
    			exploredMap.setExploredCell(row, col, true);
    			
    			switch (i) {
    			case 5: 
    				d = 1;
    				break;
    			case 4:
    				d = 1;
    				break;
    			case 3:
    				d = 2;
    				break;
    			case 2:
    			default:
    				d = 3;
    			}
    			
    			// If the position has a block
    			if (sensorVal == i) {
    				exploredMap.setConfidenceCell(row, col, Math.pow(10, d));
    				exploredMap.setObstacleCell(row, col, true);
    				return;
    			} else {
    				exploredMap.setConfidenceCell(row, col, -Math.pow(10,d));
    				exploredMap.setObstacleCell(row, col, false);
    			}
    		}
    		
    	}
    }
    
    
    
	/**
	 * Set cells in the map as obstacles or free accordingly 
	 * Return the obstacle grid distance
	 * @param exploredMap
	 * @param realMap
	 * @param rowInc
	 * @param colInc
	 * @return
	 */
	private int getSensorVal(Map exploredMap, Map realMap, int rowInc, int colInc) {
		for (int i = 0; i < this.lowerRange; i ++) {
			int row = this.sensorPosRow + (rowInc * i);
			int col = this.sensorPosCol + (colInc * i);
			
			if (! realMap.checkValidCoordinates(row, col)) return 0;
			if (realMap.getObstacleCell(row, col))
				return 0;
		}
		for (int i = this.lowerRange; i <= this.upperRange; i ++) {
			int row = this.sensorPosRow + (rowInc * i);
			int col = this.sensorPosCol + (colInc * i);
			
			if (!exploredMap.checkValidCoordinates(row, col)) return i;
			// TODO: Is accurate check
			
			exploredMap.setExploredCell(row,col,true);
			
			if (realMap.getObstacleCell(row, col)) {
				exploredMap.setObstacleCell(row, col, true);
				return i;
			} else {
				exploredMap.setObstacleCell(row, col, false);
			}
		}
		return 0;
	}
	
	// Real Run Sensing
}
