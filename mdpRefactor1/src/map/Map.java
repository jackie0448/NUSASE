package map;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JPanel;

import robot.Robot;
import robot.RobotConstants;
import robot.RobotConstants.DIRECTION;


/**
 * Represents the entire map grid for the arena.
 * @author Jesslyn Chew
 *
 */

public class Map extends JPanel {
	private Cell[][] grid;
	
	// declaration of robot
	private Robot bot;
	
	public Map(Robot bot) {
		// TODO: reset bot 
		this.bot = bot;
		
		grid = new Cell[MapConstants.MAP_ROWS][MapConstants.MAP_COLS];
		for (int row = 0; row < grid.length; row++) {
			for (int col = 0; col < grid[0].length; col ++) {
				grid[row][col] = new Cell(row, col);
				
				// Set virtual walls of the arena
				if (row == 0 | col == 0 | row == MapConstants.MAP_ROWS - 1 | col == MapConstants.MAP_COLS - 1) {
					grid[row][col].setIsVirtualWall(true);
				} else {
					grid[row][col].setIsVirtualWall(false);
				}
			}
		}
	}
	
	public void resetMap() {
		grid = new Cell[MapConstants.MAP_ROWS][MapConstants.MAP_COLS];
		for (int row = 0; row < grid.length; row++) {
			for (int col = 0; col < grid[0].length; col ++) {
				grid[row][col] = new Cell(row, col);
				
				// Set virtual walls of the arena
				if (row == 0 | col == 0 | row == MapConstants.MAP_ROWS - 1 | col == MapConstants.MAP_COLS - 1) {
					grid[row][col].setIsVirtualWall(true);
				} else {
					grid[row][col].setIsVirtualWall(false);
				}
			}
		}
	}
	
	public void setAllUnexplored() {
		for (int row = 0; row < grid.length; row ++) {
			for (int col = 0; col < grid[0].length; col ++) {
				grid[row][col].setIsExplored(false);
				if(inStartZone(row,col)) {
					grid[row][col].setIsExplored(true);
					grid[row][col].setIsObstacle(false);
					grid[row][col].setIsAccurate(true);
					grid[row][col].forceConfidence(-1);
			}
				if(inGoalZone(row,col)) {
					grid[row][col].setIsExplored(true);
					grid[row][col].setIsObstacle(false);
					grid[row][col].setIsAccurate(true);
					grid[row][col].forceConfidence(-1);
			}
			
			}
		}
	}
	
	public void setAllExplored() {
		for (int row = 0; row < grid.length; row ++) {
			for (int col = 0; col < grid[0].length; col ++) {
				grid[row][col].setIsExplored(true);
				if(inStartZone(row,col)) {
					grid[row][col].setIsExplored(true);
					grid[row][col].setIsObstacle(false);
					grid[row][col].setIsAccurate(true);
					grid[row][col].forceConfidence(-1);
			}
				if(inGoalZone(row,col)) {
					grid[row][col].setIsExplored(true);
					grid[row][col].setIsObstacle(false);
					grid[row][col].setIsAccurate(true);
					grid[row][col].forceConfidence(-1);
			}
			}
		}
	}
	
	public void setEmptyExplored() {
		for (int row = 0; row < grid.length; row ++) {
			for (int col = 0; col < grid[0].length; col ++) {
				if (!grid[row][col].getIsExplored()) {
					grid[row][col].setIsExplored(true);
					grid[row][col].setIsObstacle(false);
			}
		}
			}
	}
	
	
	public boolean getObstacleCell(int row, int col) {
		return grid[row][col].getIsObstacle();
	}
	
	/**
	 * Set the cell as an obstacle and the surrounding cells as virtual walls 
	 * or reset cell and surrounding virtual walls
	 * @param row
	 * @param col
	 * @param val
	 */
	public void setObstacleCell(int row, int col, boolean val) {
		if (inStartZone(row, col) || inGoalZone(row, col))
			return;
		
		if (MapConstants.useConfidence) {
			val = grid[row][col].getIsObstacle();
		}
		else 
		{
			grid[row][col].setIsObstacle(val);
		}
			
		for (int i = -1; i <= 1; i ++) {
			for (int j = -1; j <= 1; j ++) {
				int sRow = row + i;
				int sCol = col + j;
				if (!(i == 0 && j == 0) && checkValidCoordinates(sRow,sCol)) {
					if (!obstacleAround(sRow,sCol) || val)
						grid[sRow][sCol].setIsVirtualWall(val);
				}
			}
		}
	}
	
	public boolean obstacleAround(int row, int col) {
		if (row == 0 || row == MapConstants.MAP_ROWS - 1 || col == 0 || col == MapConstants.MAP_COLS - 1)
			return true;
		for (int i = -1; i <= 1; i ++) {
			for (int j = -1; j <= 1; j ++) {
				int sRow = row + i;
				int sCol = col + j;
				if (!(i == 0 && j == 0) && checkValidCoordinates(sRow,sCol)) {
					if (grid[sRow][sCol].getIsObstacle())
						return true;
				}
			}
		}
		return false;
	}
	
	public Cell getCell(int row, int col) {
		return grid[row][col];
	}
	
	public boolean getVirtualCell(int row, int col) {
		return grid[row][col].getIsVirtualWall();
	}
	
	public void setVirtualCell(int row, int col, boolean val) {
		grid[row][col].setIsVirtualWall(val);
	}
	
	public boolean getExploredCell(int row, int col) {
		return grid[row][col].getIsExplored();
	}
	
	public void setExploredCell(int row, int col, boolean val) {
		grid[row][col].setIsExplored(val);
	}
	
	public boolean getAccurateCell(int row, int col) {
		return grid[row][col].getIsAccurate();
	}
	
	public void setAccurateCell(int row, int col, boolean val) {
		grid[row][col].setIsAccurate(val);
	}
	
	public double getConfidenceCell(int row, int col) {
		return grid[row][col].getConfidence();
	}
	
	public void setConfidenceCell(int row, int col, double val) {
		grid[row][col].setConfidence(val);
	}
	
	public boolean inStartZone(int row, int col) {
		return row >= 0 && row <= 2 && col >= 0 && col <= 2;
	}
	
	public boolean inGoalZone(int row, int col) {
		return row <= MapConstants.GOAL_ROW + 1 && row >= MapConstants.GOAL_ROW - 1 && col <= MapConstants.GOAL_COL + 1 && col >= MapConstants.GOAL_COL - 1;
	}
	
	public boolean checkValidCoordinates(int row, int col) {
		return row >= 0 && col >= 0 && row < MapConstants.MAP_ROWS && col < MapConstants.MAP_COLS;
	}
	
	/**
	 * Overrides JComponent's paintComponent() method. 
	 * Creates a 2D array of _DisplayCell objects to store the current map state.
	 * It paints square cells for the grid with the appropriate colors as well as the robot on-screen.
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// Create a two-dimensional array of _DisplayCell objects for rendering
		_DisplayCell[][] _mapCells = new _DisplayCell[MapConstants.MAP_ROWS][MapConstants.MAP_COLS];
		for (int mapRow = 0; mapRow < MapConstants.MAP_ROWS; mapRow ++) {
			for (int mapCol = 0; mapCol < MapConstants.MAP_COLS; mapCol ++) {
				_mapCells[mapRow][mapCol] = new _DisplayCell(mapCol * GraphicsConstants.CELL_SIZE, mapRow * GraphicsConstants.CELL_SIZE, GraphicsConstants.CELL_SIZE);
			}
		}
				
		// Paint the cells with the appropriate colors 
		for (int mapRow = 0; mapRow < MapConstants.MAP_ROWS; mapRow ++) {
			for (int mapCol = 0; mapCol < MapConstants.MAP_COLS; mapCol ++) {
				Color cellColor;
				
				if (inStartZone(mapRow, mapCol))
					cellColor = GraphicsConstants.C_START;
				else if (inGoalZone(mapRow,mapCol))
					cellColor = GraphicsConstants.C_GOAL;
				else if (! grid[mapRow][mapCol].getIsExplored())
					cellColor = GraphicsConstants.C_UNEXPLORED;
				// TODO: checkstart?
				else if (grid[mapRow][mapCol].getIsObstacle())
					cellColor = GraphicsConstants.C_OBSTACLE;
				else if (grid[mapRow][mapCol].getIsVirtualWall())
					cellColor = GraphicsConstants.C_EXPLORED;
				else 
					cellColor = GraphicsConstants.C_FREE;
				
				g.setColor(cellColor);
                g.fillRect(_mapCells[mapRow][mapCol].cellX + GraphicsConstants.MAP_X_OFFSET, _mapCells[mapRow][mapCol].cellY, _mapCells[mapRow][mapCol].cellSize, _mapCells[mapRow][mapCol].cellSize);
			}
		}
		
		// Paint the robot on-screen (TODO: Clean up)
		g.setColor(GraphicsConstants.C_ROBOT);
        int r = bot.getRobotPosRow();
        int c = bot.getRobotPosCol();
        g.fillOval((c - 1) * GraphicsConstants.CELL_SIZE + GraphicsConstants.ROBOT_X_OFFSET + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - (r * GraphicsConstants.CELL_SIZE + GraphicsConstants.ROBOT_Y_OFFSET), GraphicsConstants.ROBOT_W, GraphicsConstants.ROBOT_H);
        
		g.setColor(GraphicsConstants.C_ROBOT_DIR);		
        RobotConstants.DIRECTION d = bot.getRobotPosDir();
        switch (d) {
            case NORTH:
                g.fillOval(c * GraphicsConstants.CELL_SIZE + 15 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE - 15, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
            case EAST:
                g.fillOval(c * GraphicsConstants.CELL_SIZE +40 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE +10, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
            case SOUTH:
                g.fillOval(c * GraphicsConstants.CELL_SIZE + 15 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE + 35, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
            case WEST:
                g.fillOval(c * GraphicsConstants.CELL_SIZE - 20 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE + 10, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
            case SOUTHEAST:
            	 g.fillOval(c * GraphicsConstants.CELL_SIZE + 40 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE + 40, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
            	 break;
            case NORTHEAST:
            	 g.fillOval(c * GraphicsConstants.CELL_SIZE + 40 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE - 8, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
            	 break;
            case SOUTHWEST:
            	 g.fillOval(c * GraphicsConstants.CELL_SIZE -10 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE + 40, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
            	 break;
            case NORTHWEST: 
            	g.fillOval(c * GraphicsConstants.CELL_SIZE -12 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE -7, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
            	break;
        }
		
	}
	
	public class _DisplayCell {
		public final int cellX;
		public final int cellY;
		public final int cellSize;
		
		public _DisplayCell(int borderX, int borderY, int borderSize) {
			this.cellX = borderX + GraphicsConstants.CELL_LINE_WEIGHT;
			this.cellY = GraphicsConstants.MAP_H - (borderY - GraphicsConstants.CELL_LINE_WEIGHT);
			this.cellSize = borderSize - (GraphicsConstants.CELL_LINE_WEIGHT * 2);
		}
	}
	
 	public boolean isFree(int curRow, int curCol, DIRECTION dir) {
 	    int r,c,i;
 	    r = -10;
 	    c = -10;
 	    i = -10; 
 	    if (dir == DIRECTION.NORTH) {
 	         r = curRow + 2;
 	        for (i = -1; i <= 1; i ++) {
 	        	c = curCol + i;
 	            if (!checkValidCoordinates(r, c) || getObstacleCell(r, c) || !getExploredCell(r,c)) {
 	                return false;
 	            }
 	        } 
 	        return true;
 	    }
 	    else if (dir == DIRECTION.SOUTH) {
 	        r = curRow - 2;
 	        for (i = -1; i <= 1; i ++) {
 	        	c = curCol + i;
 	            if (!checkValidCoordinates(r, c) || getObstacleCell(r, c) || !getExploredCell(r,c)) {
 	                return false;
 	            }
 	        }
 	       return true;
 	    }
 	    else if (dir == DIRECTION.EAST) {
 	        c = curCol + 2;
 	        for (i = -1; i <= 1; i ++) {
 	        	r = curRow + i;
 	            if (!checkValidCoordinates(r, c) || getObstacleCell(r, c) || !getExploredCell(r,c)) {
 	                return false;
 	            }
 	        }    
 	       return true;
 	    }
 	    else if (dir == DIRECTION.WEST) {
 	        c = curCol - 2;
 	        for (i = -1; i <= 1; i ++) {
 	        	r = curRow + i;
 	            if (!checkValidCoordinates(r, c) || getObstacleCell(r, c) || !getExploredCell(r,c)) {
 	                return false;
 	            }
 	        }
 	       return true;
 	    }
 	    return false;
 	}
 	
 	public boolean canVisit(Cell cell) {
		int row = cell.getRow();
		int col = cell.getCol();
		
		for (int i = -1; i  <= 1; i ++) {
			for (int j = -1; j <= 1; j ++) {
				if (!checkValidCoordinates(row + i, col + j) || !getExploredCell(row + i, col + j) || getObstacleCell(row + i, col + j)) {
					return false;
				}
			}
		}
		return true;	
	}
 	
 	class CellNCost implements Comparable<CellNCost> {
 		private Cell cell;
 		private Integer cost;
 		
 		public CellNCost(Cell cell, Integer cost) {
 			this.cell = cell;
 			this.cost = cost;
 		}
 		
 		public Cell getCell() {
 			return cell;
 		}
 		
 		public Integer getCost() {
 			return cost;
 		}
 		
 		@Override 
 		public int compareTo(CellNCost o) {
 			return this.getCost().compareTo(o.getCost());
 		}
 	}
 	
 	public ArrayList<Cell> getFastestCells(Cell curCell) {
 		ArrayList<Cell> result = new ArrayList<Cell>();
 		ArrayList<CellNCost> ls = new ArrayList<CellNCost>();
 		Cell tempCell;
 		Integer tempDist;
 		
 		// Add all unexplored cells 
 		for (int row = 0; row < MapConstants.MAP_ROWS; row++) {
            for (int col = 0; col < MapConstants.MAP_COLS; col++) {
            	tempCell = grid[row][col];
            	if (!tempCell.getIsExplored() && clearForRobot(row,col)) {
            		tempDist = costH(tempCell,curCell.getRow(),curCell.getCol());
                    CellNCost temp = new CellNCost(tempCell, tempDist);
                    ls.add(temp);
            	}
            }
 		}
 		
 		// Sort unexplored cells 
 		Collections.sort(ls);
 		
 		// For each unexplored cell, get explored cell 
 		for (int i = 0; i < ls.size(); i ++) {
 			Cell explored = nearestExplored(ls.get(i).getCell());
 			// If found, return
 			if (explored != null) {
 				result.add(ls.get(i).getCell());
 				result.add(explored);
 			}
 			//for e
 			
 		}
 		
 		return result;
 	}
  	
 	// For fastest path in exploration (TODO: Clean)
    /**
     * Return the nearest unexplored cell from current cell's location by creating a heuristic from cur location to all cells
     */
    public Cell nearestUnexplored(Cell curCell) {
        double dist = 1000, tempDist;
        Cell nearest = null, tempCell;

        for (int row = 0; row < MapConstants.MAP_ROWS; row++) {
            for (int col = 0; col < MapConstants.MAP_COLS; col++) {
                tempCell = grid[row][col];
                tempDist = costH(tempCell,curCell.getRow(),curCell.getCol()); //getting the heuristic based on the current cell
                if ((!tempCell.getIsExplored()) && (tempDist < dist)) {
                	// Check whether clear for robot 
                	if (clearForRobot(row,col)) {
                		nearest = tempCell;
                        dist = tempDist;
                	}
                }
            }
        }
        return nearest;
    }
    
    /**
     * Return the nearest explored cell from a current cell's location (in this case is unexplored cell)
     */
    public Cell nearestExplored(Cell loc) {
        double dist = 1000, tempDist;
           Cell explored = null, tempCell;
           int row,col;
        // Find a valid explored
           // Top row
           for (int i = -1; i <= 1; i ++) {
               row = 2 + loc.getRow();
            col = loc.getCol() + i;
            if (checkValidCoordinates(row,col) ) {
             tempCell = grid[row][col];
             System.out.println(canVisit(tempCell));
             tempDist = costH(tempCell,loc.getRow(),loc.getCol());
             if (canVisit(tempCell) && dist>tempDist) {
              explored = tempCell;
             }
            }
           }
           // Bottom row 

           for (int i = -1; i <= 1; i ++) {
            row = -2 + loc.getRow();
            col = loc.getCol() + i;
            if (checkValidCoordinates(row,col) ) {
             tempCell = grid[row][col];
             tempDist = costH(tempCell,loc.getRow(),loc.getCol());
             if (canVisit(tempCell) && dist>tempDist) {
              explored = tempCell;
             }
            }
           }
           // Left col
           for (int i = -1; i <= 1; i ++) {
            row = loc.getRow() + i;
            col = -2 + loc.getCol();
            if (checkValidCoordinates(row,col) ) {
             tempCell = grid[row][col];
             tempDist = costH(tempCell,loc.getRow(),loc.getCol());
             if (canVisit(tempCell) && dist>tempDist) {
              explored = tempCell;
             }
            }
           }
           // right col 
           for (int i = -1; i <= 1; i ++) {
            row = loc.getRow() + i;
            col = -2 + loc.getCol();
            if (checkValidCoordinates(row,col) ) {
             tempCell = grid[row][col];
             tempDist = costH(tempCell,loc.getRow(),loc.getCol());
             if (canVisit(tempCell) && dist>tempDist) {
              explored = tempCell;
             }
            }
           }
        return explored;
       }
    
    
    private Integer costH(Cell b, int goalRow, int goalCol) {
        // Heuristic: The no. of moves will be equal to the difference in the row and column values.
        double movementCost = (Math.abs(goalCol - b.getCol()) + Math.abs(goalRow - b.getRow())) * RobotConstants.MOVE_COST;

        if (movementCost == 0) return 0;

        // Heuristic: If b is not in the same row or column, one turn will be needed.
        double turnCost = 0;
        if (goalCol - b.getCol() != 0 || goalRow - b.getRow() != 0) {
            turnCost = RobotConstants.TURN_COST;
        }

        return new Integer((int)(movementCost + turnCost));
    }
    
    /**
     * Check whether a particular grid is clear for robot to move through
     * @param row
     * @param col
     * @return true if the cell, its left and its right are valid and non-obstacle cell
     */
    public boolean clearForRobot(int row, int col) {
     	Cell checkCell=null;
     	int counter =0;
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
            	checkCell= new Cell(r,c);
                if (!checkValidCoordinates(r,c) || checkCell.getIsObstacle()) {
                	counter++;
                	if(counter==3) {
                    return false;
                	}
                }
            }
        }
        return true;
    }
 	
}
