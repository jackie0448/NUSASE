package algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import map.Cell;
import map.Map;
import map.MapConstants;
import robot.Robot;
import robot.RobotConstants;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;
import utils.CommMgr;

public class FastestPathAlgo4 {
	private Map exploredMap;
	private Map realMap;
	private Robot robot;
	private double[][] gCosts;
	private Stack<Cell> fastPath;
	// For 4 way, explorationMode is always true
	public boolean explorationMode = true;
	
	private HashMap<Cell, Cell> parents= new HashMap<>();
	private ArrayList<Cell>visited= new ArrayList<Cell>();
	private ArrayList<Cell> toVisitQueue= new ArrayList<Cell>();
	
	public FastestPathAlgo4(Map exploredMap, Robot robot, Map realMap) {
		this.realMap = realMap;
		this.exploredMap = exploredMap;
		this.robot = robot;
		this.gCosts = new double[MapConstants.MAP_ROWS][MapConstants.MAP_COLS];
	}
	
	/** 
	 * Main fastest path function
	 */
	public void runFastestPath(int goalRow, int goalCol) {
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		System.out.println("Executing fastest path...");
		int startRow = robot.getRobotPosRow();
		int startCol = robot.getRobotPosCol();
    	
    	// Start -> Goal
    	ASTARSearch(startRow,startCol,goalRow,goalCol);
    	
    	// Get fastest path (TODO: Clean up)
    	fastPath= getPath(goalRow, goalCol);
    	printFastPath(fastPath);
		MoveRobot(fastPath, goalRow, goalCol);
	}
	
	/**
	 * ASTAR search algorithm
	 */
	public void ASTARSearch(int startRow, int startCol, int goalRow, int goalCol) {
		// Initalise gCost
		initGCost();
		gCosts[startRow][startCol] = 0;
		
		// Add start node
		Cell curCell = exploredMap.getCell(startRow,startCol);
		toVisitQueue.add(curCell);
		DIRECTION curDir = robot.getRobotPosDir();
		
		while(!toVisitQueue.isEmpty()) {
			curCell= minCost(goalRow,goalCol);
			
			if (parents.containsKey(curCell)) {
				curDir = getNextDir(parents.get(curCell),curCell);
			}
			
			visited.add(curCell);
			toVisitQueue.remove(curCell);
			
			// Goal is reached
			if(visited.contains(exploredMap.getCell(goalRow, goalCol))) {
				System.out.println("Goal cell has been reached. Fastest path found");
				return;
			}
			
			else {
				ArrayList<Cell> neighbours;
				neighbours= getNeighbours(curCell);
				for(Cell c : neighbours) {
					if(visited.contains(c)) {
						continue;
					}
					else if (!(toVisitQueue.contains(c))) {
						parents.put(c,curCell);
						gCosts[c.getRow()][c.getCol()]=gCosts[curCell.getRow()][curCell.getCol()]+getGCost(curCell,c,curDir);
						toVisitQueue.add(c);
					}
					// If neighbour in queue
					else {
						double curNeighbourGCost= gCosts[c.getRow()][c.getCol()];
						double gCostToCompare= gCosts[curCell.getRow()][curCell.getCol()]+ getGCost(curCell,c,curDir);
						
						if(gCostToCompare<curNeighbourGCost) {
							gCosts[c.getRow()][c.getCol()]= gCostToCompare;
							parents.put(c, curCell);
						}
					}
				}
			}
		}
		// Exit while loop without finding goal
		System.out.println("path not found");
	}
	
	/**
	 * Initialise the GCost at the start
	 * TODO: try without setting to 0
	 */
	public void initGCost()	{
		this.gCosts = new double[MapConstants.MAP_ROWS][MapConstants.MAP_COLS];
		for (int i = 0; i < MapConstants.MAP_ROWS; i++) {
			for (int j = 0; j < MapConstants.MAP_COLS; j++) {
				Cell cell = exploredMap.getCell(i, j);
				if (!exploredMap.canVisit(cell)) {
					gCosts[i][j] = RobotConstants.INFINITE_COST;
				} 
				else {
					gCosts[i][j] = 0;
				}
			}
		}
	}
	
	/**
	 * Behaves like a priority queue 
	 * @param goalRow
	 * @param goalCol
	 * @return
	 */
	private Cell minCost(int goalRow, int goalCol) {
		double minCost= RobotConstants.INFINITE_COST;
		int size= toVisitQueue.size();
		Cell minCostCell= null; 
		for (int i=0;i<size;i++) {
			int row= toVisitQueue.get(i).getRow();
			int col= toVisitQueue.get(i).getCol();
			double gCost= gCosts[row][col];
			double hCost= heuristicCost(toVisitQueue.get(i),goalRow,goalCol);
			double fCost= gCost+hCost;
			if(fCost<minCost) {
				minCost=fCost;
				minCostCell=toVisitQueue.get(i);
			}
		}
		return minCostCell;
	}
	
	/**
	 * Heuristic for mahanttan distance
	 */
	private double heuristicCost(Cell refCell, int goalRow, int goalCol) {
		double strMoveCost= ((Math.abs(goalRow-refCell.getRow())+ Math.abs(goalCol-refCell.getCol()))* RobotConstants.MOVE_COST);
    	double rowDiff= Math.abs((refCell.getRow()- goalRow));
    	double colDiff= Math.abs((refCell.getCol()-goalCol));
    	double diagonalMoveCost= Math.min(rowDiff, colDiff);
    	double finalHurCost= (diagonalMoveCost*RobotConstants.DIAGONAL_COST)+ (RobotConstants.MOVE_COST*(strMoveCost-(2*diagonalMoveCost)));
    	return finalHurCost;
	}
	
	private DIRECTION getNextDir(Cell curCell, Cell neighbour) {
		int curRow = curCell.getRow();
		int curCol = curCell.getCol();
		int nRow = neighbour.getRow();
		int nCol = neighbour.getCol();
		
		DIRECTION nextDir;
		
		// North
		if (nRow-curRow == 1) {
			nextDir = DIRECTION.NORTH;
		}	
		// South
		else if (nRow-curRow == -1) {
			nextDir = DIRECTION.SOUTH;
		}
		// East
		else if (nCol-curCol == 1) {
			nextDir = DIRECTION.EAST;
		}
		// West
		else {
			nextDir = DIRECTION.WEST;
		}
		return nextDir;
	}
	
	private double getGCost(Cell curCell, Cell neighbour, DIRECTION curDir) {
		DIRECTION nextDir= getNextDir(curCell, neighbour);
    	double turnCost;
    	int dirDiff= Math.abs(curDir.ordinal()-nextDir.ordinal());
    	int diff2= DIRECTION.values().length-dirDiff;
    	if(dirDiff>diff2) {
    		turnCost= diff2* RobotConstants.TURN_COST;
    	}
    	else {
    		turnCost=dirDiff*RobotConstants.TURN_COST;
    	}
    	if(nextDir.ordinal()%2 == 1) {
    		turnCost+= RobotConstants.DIAGONAL_COST;
    	}
    	else {
    		turnCost+= RobotConstants.MOVE_COST;
    	}
    	return turnCost+10;
	}
	
	private ArrayList<Cell> getNeighbours(Cell curCell){
		ArrayList<Cell> neighbours = new ArrayList<Cell>();
		int curRow= curCell.getRow();
		int curCol= curCell.getCol();
		
		for (int i = -1; i <= 1; i ++) {
			for (int j = -1; j <= 1; j ++) {
				int nRow = curRow + i;
				int nCol = curCol + j;
				
				if (!(i == 0 && j == 0) && exploredMap.checkValidCoordinates(nRow,nCol)) {
					Cell neighbourCell = exploredMap.getCell(nRow, nCol);

					// For non-diagonal
					if ((i == 0 || j == 0) && exploredMap.canVisit(neighbourCell)) {
						neighbours.add(neighbourCell);
//						System.out.println("ROW: " + nRow + ", COL: " + nCol);
					}
				}
			}
		}
		return neighbours;
	}
	
	/**
	 * Returns a stack that contains all the correct cells for fastest path
	 * @param goalRow
	 * @param goalCol
	 * @return
	 */
	private Stack<Cell> getPath(int goalRow, int goalCol) {
		Stack<Cell> actualPath = new Stack<>();
		Cell tempCell = exploredMap.getCell(goalRow, goalCol);
		while (true) {
			actualPath.push(tempCell);
			tempCell = parents.get(tempCell);
			if (tempCell == null) {
				break;
			}
		}
		return actualPath;
	}
	
	/**
	 * For debugging, TODO: remove later
	 * @param path
	 */
    private void printFastPath(Stack<Cell> path) { //this mtd just print out which cell the robot must travel from
        System.out.println("The number of steps is: " + (path.size() - 1) + "\n");

        Stack<Cell> pathForPrint = (Stack<Cell>) path.clone();
        Cell temp;
        System.out.println("Path:");
        while (!pathForPrint.isEmpty()) {
            temp = pathForPrint.pop();
            if (!pathForPrint.isEmpty()) System.out.print("(" + temp.getRow() + ", " + temp.getCol() + ") --> ");
            else System.out.print("(" + temp.getRow() + ", " + temp.getCol() + ")");
        }

        System.out.println("\n");
    }
	
	private void MoveRobot(Stack<Cell> path, int goalRow, int goalCol) {
		StringBuilder outputString = new StringBuilder();
		
		Cell temp = path.pop();
		DIRECTION targetDir;
		
		ArrayList<MOVEMENT> movements = new ArrayList<>();
		MOVEMENT m;

		Robot tempBot = new Robot(1, 1, false);
		tempBot.setRobotPosDir(robot.getRobotPosDir());
		tempBot.setRobotPos(robot.getRobotPosRow(), robot.getRobotPosCol());
		tempBot.setDelay(0);
        
        while ((tempBot.getRobotPosRow() != goalRow) || (tempBot.getRobotPosCol() != goalCol)) {
        	if (tempBot.getRobotPosRow() == temp.getRow() && tempBot.getRobotPosCol() == temp.getCol()) {
                temp = path.pop();
                System.out.println("CELL: row("+temp.getRow()+") col("+temp.getCol()+")");
            }
        	
        	targetDir = getNextDir(exploredMap.getCell(tempBot.getRobotPosRow(), tempBot.getRobotPosCol()),temp);
        	// rotate
        	if (tempBot.getRobotPosDir() != targetDir) {
        		DIRECTION curDir = tempBot.getRobotPosDir();
        		// Clockwise
        		int temp1 = Math.abs(targetDir.ordinal() + DIRECTION.values().length - curDir.ordinal()) % DIRECTION.values().length;
        		// Counter-clockwise
        		int temp2 = DIRECTION.values().length - temp1;
        		
        		if (temp1 < temp2) {
        			// Move clockwise
         			m = MOVEMENT.RIGHT;
        		}
        		else if (temp1 == temp2) {
        			tempBot.move(MOVEMENT.RIGHT);
        			movements.add(MOVEMENT.RIGHT);
        			tempBot.move(MOVEMENT.RIGHT);
        			movements.add(MOVEMENT.RIGHT);
        			this.exploredMap.repaint();
        			continue;
        		}
        		
        		else {
        			// MOVE counter-clockwise
        			m = MOVEMENT.LEFT;
        		}
        		tempBot.move(m);
				movements.add(m);
				this.exploredMap.repaint();
				outputString.append(MOVEMENT.movementCommand(m));
				System.out.println("Robot direction after rotation: " + tempBot.getRobotPosDir());
        	}
        	else {
	        	// move forward
	        	if(tempBot.getRobotPosDir()== DIRECTION.NORTHEAST ||tempBot.getRobotPosDir()== DIRECTION.SOUTHEAST
	    				||tempBot.getRobotPosDir()== DIRECTION.SOUTHWEST|| tempBot.getRobotPosDir()== DIRECTION.NORTHWEST) {
	    			m = MOVEMENT.DIAGONALFORWARD;
	    		}
	    		else {
	    			m = MOVEMENT.FORWARD;
	    		}
	        	tempBot.move(m);
	        	this.exploredMap.repaint();
	        	movements.add(m);
	        	outputString.append(MOVEMENT.movementCommand(m));
	        	System.out.println("Robot position after forward: " + temp.getRow() + ", " + temp.getCol());
        	}
        }
        System.out.println("Output String: "+outputString);
        
        System.out.println("Actual Robot Fastest Path (NOT TEMP)");
        for (MOVEMENT x : movements) {
        	// "Exploration" mimic 
        	System.out.println("exploration mimic in fastest path");
        	System.out.println(x);
    			if(Calibration.calibrationMode == false) {
                	senseAndRepaint();
                	System.out.println("After sense and repaint");
                }
    			
    			
    			System.out.println("Executing movement...");
    			
    			// Calibration
    			Calibration.calibrateComplex();
    			System.out.println("Calibration Check done.");

    			robot.move(x);
    			
    			// Force isAccurate 
    			if (MapConstants.useForceAccurate) {
    				for (int i = -1; i <= 1; i ++ ) {
    					for (int j = -1; j <= 1; j ++) {
    						int botRow = robot.getRobotPosRow() + i;
    						int botCol = robot.getRobotPosCol() + j;
    						exploredMap.setAccurateCell(botRow, botCol, true);
    						exploredMap.getCell(botRow,botCol).forceConfidence(-1);
    					}
    				}
    			}
        	
        }
	}
	
	public void senseAndRepaint() {
		robot.setSensors();
		
		// Sensor readings 
		if (RobotConstants.REAL_RUN) {
			CommMgr.getCommMgr().sendMsg("AR", CommMgr.SENSOR_DATA);
		} 
		
		// Force to not sense if fully explored
		if (calculateAreaExplored() != MapConstants.MAP_SIZE) {
			robot.sense(exploredMap, realMap);
		}
		
		// Repaint
		exploredMap.repaint();
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
}