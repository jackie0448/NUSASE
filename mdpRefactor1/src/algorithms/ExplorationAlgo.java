package algorithms;

import static utils.MapDescriptor.generateMapDescriptor;

import java.util.ArrayList;

import javax.swing.SwingWorker;

import map.Cell;
import map.Map;
import map.MapConstants;
import robot.Robot;
import robot.RobotConstants;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;
import utils.CommMgr;
import utils.MapDescriptor;

public class ExplorationAlgo {
	private final Map exploredMap;
	private final Map realMap;
	private final Robot bot;
	private Calibration cab;
	
	/**
	 * Number of grids explored, not percentage
	 */
	private int exploredArea;
	
	class androidMapMsg extends SwingWorker<Integer, String> {
		protected Integer doInBackground() {
			
			String[] androidStrings = MapDescriptor.generateMapDescriptor(exploredMap);
    		System.out.print("Sending to Android: ");
    		System.out.println(androidStrings[0] + "," + androidStrings[1]+ "," + bot.getRobotPosDir() + ","+ Integer.toString(bot.getRobotPosRow())+ "," + Integer.toString(bot.getRobotPosCol()));
    		CommMgr.getCommMgr().sendMsg("AN",androidStrings[0] + "," + androidStrings[1]+ "," + bot.getRobotPosDir() + ","+ Integer.toString(bot.getRobotPosRow())+ "," + Integer.toString(bot.getRobotPosCol()));
			return 333; 
		}
	}
	
	public ExplorationAlgo(Map exploredMap, Map realMap, Robot bot) {
		this.exploredMap = exploredMap;
		this.realMap = realMap;
		this.bot = bot;
		this.exploredArea = 0;
		this.cab = new Calibration(bot,exploredMap,realMap);
	}
	
	/**
	 * Exploration main algorithm to call
	 */
	public void runExploration() {
		
		Calibration.starting=true;
		System.out.println("Starting Exploration...");

		// TODO: ensure that it stops at the 6min mark
		
		do {
			if(Calibration.calibrationMode == false) {
            	senseAndRepaint();
            	System.out.println("After sense and repaint");
            }
			
			System.out.println("Executing movement...");
			
			// Calibration
			Calibration.calibrateComplex();
			System.out.println("Calibration Check done.");
			new androidMapMsg().execute();
			nextMove();
			
			// Force isAccurate 
			if (MapConstants.useForceAccurate) {
				for (int i = -1; i <= 1; i ++ ) {
					for (int j = -1; j <= 1; j ++) {
						int botRow = bot.getRobotPosRow() + i;
						int botCol = bot.getRobotPosCol() + j;
						exploredMap.setAccurateCell(botRow, botCol, true);
						exploredMap.getCell(botRow,botCol).forceConfidence(-1);
					}
				}
			}
			
			// Logs 
			exploredArea = calculateAreaExplored();
			
			System.out.println("==================================");
			System.out.println(String.format("Current Position : Row %d Col %d", bot.getRobotPosRow(), bot.getRobotPosCol()));
			System.out.println("Current Direction: " + bot.getRobotPosDir());
			System.out.println("Number of grids explored: " + exploredArea + " out of " + MapConstants.MAP_SIZE);
			
		
		// TODO - NOTICE: For the while loop below, change && to || if you dont want to break from right wall if 100% coverage is reached				
		} while ((exploredArea <= MapConstants.MAP_SIZE) && !((bot.getRobotPosCol() == 1) && (bot.getRobotPosRow() == 1)));
		
		senseAndRepaint();
		System.out.println("Rightwall Hug complete!");
		//
		
		
		
				// Go to unexplored areas
		while (calculateAreaExplored() < MapConstants.MAP_SIZE) {
			System.out.println("inside line 91 loop");
			if (goToUnexplored() != true)
				break;
			System.out.println("line 94 statement");
		}
		System.out.println("line 96");
		// Move back to start zone
		if (bot.getRobotPosRow() != 1 || bot.getRobotPosCol() != 1) {
			System.out.println("Going back to start zone.");
			FastestPathAlgo4 returnToStart = new FastestPathAlgo4(exploredMap, bot, realMap);
			System.out.println("fastest path obj created");
	        returnToStart.runFastestPath(RobotConstants.START_ROW, RobotConstants.START_COL);
	        exploredMap.repaint();
		}
		System.out.println("back at startzone after fastest path");
		
		// Set all unexplored to explored 
		//exploredMap.setEmptyExplored();
		
		// Calibration at start zone
		Calibration.cCounterCorner=100;
		Calibration.doCalibration(DIRECTION.EAST,DIRECTION.SOUTH,DIRECTION.WEST);
		exploredMap.repaint();
		
		System.out.println("Exploration Complete!");
		String[] map = generateMapDescriptor(exploredMap);
		CommMgr.getCommMgr().sendMsg("AN",map[0] + "," + map[1]+ "," + bot.getRobotPosDir() + ","+ Integer.toString(bot.getRobotPosRow())+ "," + Integer.toString(bot.getRobotPosCol()));
		
		CommMgr.getCommMgr().sendMsg("u", null);
		//CommMgr.getCommMgr().sendMsg("AN","P1:" + map[0] + ","+map[1]+'/'); 
		//CommMgr.getCommMgr().sendMsg("AN","1mazeend");
	}
	
	
	public int calculateAreaExplored() {
		int result = 0;
		for (int row = 0; row < MapConstants.MAP_ROWS; row ++) {
			for (int col = 0; col < MapConstants.MAP_COLS; col ++) {
				if (exploredMap.getExploredCell(row, col)) 
					result ++;
			}
		}
		return result;
	}
	
	public void senseAndRepaint() {
		bot.setSensors();
		
		// Sensor readings 
		if (RobotConstants.REAL_RUN) {
			CommMgr.getCommMgr().sendMsg("AR", CommMgr.SENSOR_DATA);
		} 
		bot.sense(exploredMap, realMap);
		
		// Repaint
		exploredMap.repaint();
	}
	
	public void nextMove() {
		
		DIRECTION dirCheck; 
		int row = bot.getRobotPosRow();
		int col = bot.getRobotPosCol();
		
		// Handle phantom blocks
		if (checkForPhantomWall()) {
            System.out.println("--Phantom loop wall found--");
            moveOutOfPhantomWall();
        }
		// Move Right
		dirCheck = bot.findNewDirection(MOVEMENT.RIGHT);
		if (exploredMap.isFree(row,col,dirCheck)) {
			System.out.println("Rotate to the right.");
			bot.move(MOVEMENT.RIGHT);
			dirCheck = bot.findNewDirection(MOVEMENT.FORWARD);
			if (exploredMap.isFree(row,col,dirCheck)) bot.move(MOVEMENT.FORWARD);
			return;
		}
		
		// Move Forward
		dirCheck = bot.findNewDirection(MOVEMENT.FORWARD);
		if (exploredMap.isFree(row,col,dirCheck)) {
			System.out.println("Moving to the front.");
			bot.move(MOVEMENT.FORWARD);
			return;
		}
		
		// Move Left
		dirCheck = bot.findNewDirection(MOVEMENT.LEFT);
		if (exploredMap.isFree(row,col,dirCheck)) {
			System.out.println("Rotate to the left.");
			bot.move(MOVEMENT.LEFT);
			dirCheck = bot.findNewDirection(MOVEMENT.FORWARD);
			if (exploredMap.isFree(row,col,dirCheck)) bot.move(MOVEMENT.FORWARD);
			return;
		}
		
		// Move Backwards
		System.out.println("Rotate to the back.");
		bot.move(MOVEMENT.BACKWARD);
		return;
	}
	
    private boolean checkForPhantomWall() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        int tempRow;
        int tempCol;
        Cell tempCell = null;
        boolean northFree = false, southFree = false, eastFree = false, westFree = false;

        // North cell
        tempRow = botRow + 1;
        tempCol = botCol;
        if (exploredMap.checkValidCoordinates(tempRow, tempCol)) {
        	tempCell = exploredMap.getCell(tempRow, tempCol);
        	northFree = exploredMap.canVisit(tempCell);
        }
        
        // South cell
        tempRow = botRow - 1;
        tempCol = botCol;
        if (exploredMap.checkValidCoordinates(tempRow, tempCol)) {
        	tempCell = exploredMap.getCell(tempRow, tempCol);
        	southFree = exploredMap.canVisit(tempCell);
        }
        
        // West cell
        tempRow = botRow;
        tempCol = botCol - 1;
        if (exploredMap.checkValidCoordinates(tempRow, tempCol)) {
        	tempCell = exploredMap.getCell(tempRow, tempCol);
        	westFree = exploredMap.canVisit(tempCell);
        }
        
        // East cell 
        tempRow = botRow;
        tempCol = botCol + 1;
        if (exploredMap.checkValidCoordinates(tempRow, tempCol)) {
        	tempCell = exploredMap.getCell(tempRow, tempCol);
        	eastFree = exploredMap.canVisit(tempCell);
        }
        
        switch (bot.getRobotPosDir()) {
            case NORTH:
                return eastFree && southFree && isExploredAndNotObstacle(botRow - 2, botCol + 2);
            case EAST:
                return southFree && westFree && isExploredAndNotObstacle(botRow - 2, botCol - 2);
            case SOUTH:
                return westFree && northFree && isExploredAndNotObstacle(botRow + 2, botCol - 2);
            case WEST:
                return northFree && eastFree && isExploredAndNotObstacle(botRow + 2, botCol + 2);
        }
        return false;
    }
    
    private boolean isExploredAndNotObstacle(int r, int c) {
        if (exploredMap.checkValidCoordinates(r, c)) { 
            Cell tmp = exploredMap.getCell(r, c);
            return (tmp.getIsExplored() && !tmp.getIsObstacle());
        }
        return false;
    }
	
    private void moveOutOfPhantomWall() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        /*
            Illustration
            ________________
            |       |       |
            |       |       |
            |   1   |   2   |
            |  <--  |  -->  |
            |       |       |
            |_______|_______|

   			If robot is in part 1, it will try to go the right wall
            If robot is in part 2, it will try to go the left wall
         
        */
        int counter = 0;
        if (botCol <= MapConstants.MAP_COLS / 2) {
            // Go to left wall
        	Calibration.turnBotDirection(DIRECTION.WEST);
            for (int i = botCol; i >= 0; i -= 1) {
                System.out.println(String.format("Current Robot position Row: %d Col: %d i %d Counter: %d", botRow, botCol, i, counter));
                if (!checkColObstacleInRow(botRow, i)) break;
                counter++;
            }
            // Minus 2 to allow the robot just 1 block away from the obstacle
            counter -= 2;
            if (counter <= 0) {
                Calibration.turnBotDirection(DIRECTION.SOUTH);
            }
        } else {
            // Go to right wall
        	Calibration.turnBotDirection(DIRECTION.EAST);
            for (int i = botCol; i < MapConstants.MAP_COLS; i += 1) {
            	 System.out.println(String.format("Current Robot position Row: %d Col: %d i %d Counter: %d", botRow, botCol, i, counter));
                if (!checkColObstacleInRow(botRow, i)) break;
                counter++;
            }
            // Minus 2 to allow the robot just 1 block away from the obstacle
            counter -= 2;
            if (counter <= 0) {
            	Calibration.turnBotDirection(DIRECTION.NORTH);
            }
        }
        System.out.println(String.format("Counter %d", counter));
        for (int i = 0; i < counter; i++) {
            bot.move(MOVEMENT.FORWARD);
        }
    }
    
    public boolean checkColObstacleInRow(int botRow, int column) {
        System.out.println(String.format("Row %d Col %d isExploredAndNotObstacle %B %B %B", botRow, column, isExploredAndNotObstacle(botRow - 1, column), isExploredAndNotObstacle(botRow, column), isExploredAndNotObstacle(botRow + 1, column)));
        System.out.println(String.format("Row %d Col %d isObstacle %B %B %B", botRow, column, exploredMap.getCell(botRow - 1, column).getIsObstacle(), exploredMap.getCell(botRow, column).getIsObstacle(), exploredMap.getCell(botRow + 1, column).getIsObstacle()));
        return isExploredAndNotObstacle(botRow - 1, column) && isExploredAndNotObstacle(botRow, column) && isExploredAndNotObstacle(botRow + 1, column);
    }
    
    
	public boolean goToUnexplored() {
		Cell curCell = exploredMap.getCell(bot.getRobotPosRow(),bot.getRobotPosCol());
		DIRECTION obstacleDir=null;
		
		ArrayList<Cell> result = exploredMap.getFastestCells(curCell);
		
        if (result.isEmpty()) {
            System.out.println("No nearest unexplored found.");
            return false;
        }
        // From nearest unexplored, get nearest explored
        Cell nearestUnexp = result.get(0);
        Cell nearestExp = result.get(1);
        
     	System.out.println("Nearest Unexplored Row : "+nearestUnexp.getRow()+ " Col: "+nearestUnexp.getCol());
     	System.out.println("Nearest Explored Row : "+nearestExp.getRow()+ " Col: "+nearestExp.getCol());
       
        FastestPathAlgo4 goToPoint = new FastestPathAlgo4(exploredMap, bot, realMap);
        goToPoint.runFastestPath(nearestExp.getRow(),nearestExp.getCol());
        senseAndRepaint();
        
        boolean dirFlag = false;

     // Check if at its right
     for (int i = -1; i <= 1; i ++) {
     	int tempRow = nearestExp.getRow() + i;
     	int tempCol = nearestExp.getCol() + 2;
     	if (tempRow == nearestUnexp.getRow() && tempCol == nearestUnexp.getCol()) {
     	 	obstacleDir = DIRECTION.EAST;
     	 	dirFlag = true;
     	}
     }

     // Check if at its left
     if (dirFlag == false) {
     	for (int i = -1; i <= 1; i ++) {
     		int tempRow = nearestExp.getRow() + i;
     		int tempCol = nearestExp.getCol() - 2;
     		if (tempRow == nearestUnexp.getRow() && tempCol == nearestUnexp.getCol()) {
     		 obstacleDir = DIRECTION.WEST;
     		dirFlag = true;
     		}
     	}
     }

     // Check top
     if (dirFlag == false) {
    	 for(int i= -1; i <= 1; i++) {
    			int tempRow = nearestExp.getRow() + 2;
    	     	int tempCol = nearestExp.getCol()+i;
    	     	if (tempRow == nearestUnexp.getRow() && tempCol == nearestUnexp.getCol()) {
    	     	 obstacleDir = DIRECTION.NORTH;
    	     	dirFlag = true;
    	 }
     	
     	}
     }

     // Check bottom
     if (dirFlag == false) {
    	 for(int i = -1; i <= 1 ; i++) {
    			int tempRow = nearestExp.getRow() - 2;
    	     	int tempCol = nearestExp.getCol()+i;
    	     	if (tempRow == nearestUnexp.getRow() && tempCol == nearestUnexp.getCol()) {
    	     	 obstacleDir = DIRECTION.SOUTH;
    	     	dirFlag = true;
    	 }
     	
     	}
     }
    Calibration.turnBotDirection(obstacleDir);
     	return true;
        
     
	}
}
