package algorithms;

import map.Cell;
import map.Map;
import map.MapConstants;
import robot.Robot;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;
//
public class Calibration {
	public static int cCounterCorner = 0;
	public static final int cCornerLimit = 1;
	public static final int cFlatLimit = 4;
	public static int cCounterFlat = 0;
	public static boolean calibrationMode=false;
	private static Robot bot;
	private static Map exploredMap;
	private static Map realMap;
	public static boolean starting = false;
	
	public Calibration(Robot bot, Map exploredMap,Map realMap) {
		this.bot = bot;
		this.exploredMap = exploredMap;
		this.realMap = realMap;
	}
	
	public static void doCalibration(DIRECTION robotDir, DIRECTION w1, DIRECTION w2) {
   	 if (starting) {
   		 starting = false;
   		 return;
   	 }
   	calibrationMode=true;
   	 if (w1 == w2 && cCounterFlat >= cFlatLimit) {
   		 System.out.println("calculate area for cali"+ calculateAreaExplored());
   	    // Single Calibration
   		turnBotDirection(w1);
   	    bot.move(MOVEMENT.CALIBRATE);
   	    turnBotDirection(robotDir);
   	    cCounterFlat = 0;
   	 }   
   	 else if (w1 != w2 && cCounterCorner >= cCornerLimit){
   		System.out.println("calculate area for cali"+ calculateAreaExplored());
   	    // Double Calibration, 
   	    turnBotDirection(w1);
   	    bot.move(MOVEMENT.CALIBRATE);
   	    turnBotDirection(w2);
   	    bot.move(MOVEMENT.CALIBRATE);
   	    turnBotDirection(robotDir);
   	    cCounterCorner = 0;
   	    cCounterFlat = 0;
   	 }
   	 calibrationMode=false;
   	}

   	public static void calibrateComplex() {
   	    DIRECTION curDir = bot.getRobotPosDir();
   	    int curRow = bot.getRobotPosRow();
   	    int curCol = bot.getRobotPosCol();
   	    DIRECTION w1;
   	    
   	    // Border Calibration
   	    if (curRow == 1 || curRow == MapConstants.MAP_ROWS - 2 || curCol == 1 
   	        || curCol == MapConstants.MAP_COLS - 2) {
   	        // CORNER CALIBRATION
   	        // Bottom-left
   	        if (curRow == 1 && curCol == 1) {
   	            doCalibration(curDir,DIRECTION.WEST,DIRECTION.SOUTH);
   	            return;
   	        } 
   	        // Bottom-right
   	        else if (curRow == 1 && curCol == MapConstants.MAP_COLS - 2) {
   	            doCalibration(curDir,DIRECTION.SOUTH,DIRECTION.EAST);
   	            return;
   	        }
   	        // Top-left
   	        else if (curRow == MapConstants.MAP_ROWS - 2 && curCol == 1)  {
   	            doCalibration(curDir,DIRECTION.NORTH,DIRECTION.WEST);
   	            return;
   	        }
   	        // Top-right
   	        else if (curRow == MapConstants.MAP_ROWS - 2 && curCol == MapConstants.MAP_COLS - 2) {
   	            doCalibration(curDir,DIRECTION.EAST,DIRECTION.NORTH);
   	            return;
   	        }

   	        // WALL CALIBRATION
   	        // Bottom Wall
   	        if (curRow == 1) {
   	            w1 = DIRECTION.SOUTH;

   	            // Check for Front Wall (RIGHT)
   	            if (hasWall(curRow,curCol,DIRECTION.EAST)) {
   	                doCalibration(curDir,w1,DIRECTION.EAST);
   	                
   	            }
   	            else if (hasWall(curRow,curCol,DIRECTION.WEST)) {
   	                doCalibration(curDir,w1,DIRECTION.WEST);
   	                
   	            }
   	            else {
   	                doCalibration(curDir,w1,w1);
   	            }
   	        }

   	        // Right Wall
   	        else if (curCol == MapConstants.MAP_COLS - 2) {
   	            w1 = DIRECTION.EAST;

   	            // Check for Front Wall (UP)
   	            if (hasWall(curRow,curCol,DIRECTION.NORTH)) {
   	                doCalibration(curDir,w1,DIRECTION.NORTH);
   	            }
   	            else if (hasWall(curRow,curCol,DIRECTION.SOUTH)) {
   	                doCalibration(curDir,w1,DIRECTION.SOUTH);
   	            }
   	            else {
   	                doCalibration(curDir,w1,w1);
   	            }
   	        }

   	        // Top Wall
   	        else if (curRow == MapConstants.MAP_ROWS - 2) {
   	            w1 = DIRECTION.NORTH;

   	            // Check for Front Wall (LEFT)
   	            if (hasWall(curRow,curCol,DIRECTION.WEST)) {
   	                doCalibration(curDir,w1,DIRECTION.WEST);
   	            }
   	            else if (hasWall(curRow,curCol,DIRECTION.EAST)) {
   	                doCalibration(curDir,w1,DIRECTION.EAST);
   	            }
   	            else {
   	                doCalibration(curDir,w1,w1);
   	            }
   	        }

   	        // Left Wall
   	        else if (curCol == 1) {
   	            w1 = DIRECTION.WEST;

   	            // Check for Front Wall (DOWN)
   	            if (hasWall(curRow,curCol,DIRECTION.SOUTH)) {
   	                doCalibration(curDir,w1,DIRECTION.SOUTH);
   	            }
   	            else if (hasWall(curRow,curCol,DIRECTION.NORTH)) {
   	                doCalibration(curDir,w1,DIRECTION.NORTH);
   	            }
   	            else {
   	                doCalibration(curDir,w1,w1);
   	            }
   	        }
   	    }

   	   //  Non-border Calibration
   	    else {
   	        boolean checkFront = false;
   	        boolean checkRight = false;
   	        boolean checkLeft = false;
   	        DIRECTION front = curDir;
   	        DIRECTION left = curDir;
   	        DIRECTION right = curDir;

   	        if (curDir == DIRECTION.NORTH) {
   	            left = DIRECTION.WEST;
   	            right = DIRECTION.EAST;
   	        } 
   	        else if (curDir == DIRECTION.SOUTH) {
   	            left = DIRECTION.EAST;
   	            right = DIRECTION.WEST;
   	        }
   	        else if (curDir == DIRECTION.EAST) {
   	            left = DIRECTION.NORTH;
   	            right = DIRECTION.SOUTH;
   	        }
   	        else if (curDir == DIRECTION.WEST) {
   	            left = DIRECTION.SOUTH;
   	            right = DIRECTION.NORTH;
   	        }
   	        
   	        checkFront = hasWall(curRow,curCol,curDir);
   	        checkRight = hasWall(curRow,curCol,right);
   	        checkLeft = hasWall(curRow,curCol,left);
   	        
   	        System.out.println("FRONT: " + Boolean.toString(checkFront));
   	        System.out.println("RIGHT: " + Boolean.toString(checkRight));
   	        System.out.println("LEFT: " + Boolean.toString(checkLeft));

   	        if (checkRight && checkFront) {
   	            doCalibration(curDir,right,front);
   	        	System.out.println("Right-Front wall calibration");
   	        }
   	        else if (checkLeft && checkFront) {
   	            doCalibration(curDir,left,front);
   	        	System.out.println("Left-Front wall calibration");
   	        }
   	        else if (checkFront) {
   	        	doCalibration(curDir,front,front);
   	        	System.out.println("Front wall calibration");
   	        }
   	        else if (checkRight) {
   	        	doCalibration(curDir,right,right);
   	        	System.out.println("Right wall calibration");
   	        }
   	        else if (checkLeft) {
   	        	doCalibration(curDir,left,left);
   	        	System.out.println("Left wall calibration");
   	        }
   	        else {
   	        	System.out.println("No calibration around obstacles");
   	        }
   	    }
   	}
   	    
   	 public static boolean hasWall(int curRow, int curCol, DIRECTION dir) {
  	    int r,c,i;
  	    r = -1;
  	    c = -1;
  	    i = -1; 
  	    Cell cell;

  	    if (dir == DIRECTION.NORTH) {
  	         r = curRow + 2;
  	        for (i = -1; i <= 1; i ++) {
  	        	c = curCol + i;
  	        	cell = exploredMap.getCell(r, c);
  	        	System.out.println("ROW: " + Integer.toString(r) + "COL: " + Integer.toString(c) + "HasOBSTACLE: " + cell.getIsObstacle());
  	            if (!cell.getIsObstacle()) {
  	                return false;
  	            }
  	        } 
  	        return true;
  	    }
  	    else if (dir == DIRECTION.SOUTH) {
  	        r = curRow - 2;
  	        for (i = -1; i <= 1; i ++) {
  	        	c = curCol + i;
  	        	cell = exploredMap.getCell(r, c);
  	        	System.out.println("ROW: " + Integer.toString(r) + "COL: " + Integer.toString(c) + "HasOBSTACLE: " + cell.getIsObstacle());
  	            if (!cell.getIsObstacle()) {
  	                return false;
  	            }
  	        }
  	       return true;
  	    }
  	    else if (dir == DIRECTION.EAST) {
  	        c = curCol + 2;
  	        for (i = -1; i <= 1; i ++) {
  	        	r = curRow + i;
  	        	cell = exploredMap.getCell(r, c);
  	        	System.out.println("ROW: " + Integer.toString(r) + "COL: " + Integer.toString(c) + "HasOBSTACLE: " + cell.getIsObstacle());
  	            if (!cell.getIsObstacle()) {
  	                return false;
  	            }
  	        }    
  	       return true;
  	    }
  	    else if (dir == DIRECTION.WEST) {
  	        c = curCol - 2;
  	        for (i = -1; i <= 1; i ++) {
  	        	r = curRow + i;
  	        	cell = exploredMap.getCell(r, c);
  	        	System.out.println("ROW: " + Integer.toString(r) + "COL: " + Integer.toString(c) + "HasOBSTACLE: " + cell.getIsObstacle());
  	            if (!cell.getIsObstacle()) {
  	                return false;
  	            }
  	        }
  	       return true;
  	    }
  	    return false;
  	}
   	 
     /**
      * The robot turns to the required direction.
      */
     public static void turnBotDirection(DIRECTION targetDir) {
         System.out.println(bot.getRobotPosDir().toString() + "-----> to ---->" + targetDir.toString());
         
         DIRECTION curDir = bot.getRobotPosDir();
 		// Clockwise
 		int temp1 = Math.abs(targetDir.ordinal() + DIRECTION.values().length - curDir.ordinal()) % DIRECTION.values().length;
 		// Counter-clockwise
 		int temp2 = DIRECTION.values().length - temp1;
 		
 		if (temp1 == 0 || temp2 == 0) {
 			return;
 		} else if (temp1 < temp2) {
 			// Move clockwise
 			bot.move(MOVEMENT.RIGHT);
 		} else if (temp1 == temp2) {
 			// Move 180 degrees 
 			bot.move(MOVEMENT.RIGHT);
 			bot.move(MOVEMENT.RIGHT);
 		} else {
 			// MOVE counter-clockwise
 			bot.move(MOVEMENT.LEFT);
 		}
     }
	
     public static int calculateAreaExplored() {
 		int result = 0;
 		for (int row = 0; row < MapConstants.MAP_ROWS; row ++) {
 			for (int col = 0; col < MapConstants.MAP_COLS; col ++) {
 				if (exploredMap.getExploredCell(row, col)) 
 					result ++;
 			}
 		}
 		return result;
 	}
	
}
