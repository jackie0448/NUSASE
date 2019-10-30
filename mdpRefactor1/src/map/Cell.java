package map;

/**
 * Represents each cell in the map grid.
 * @author Jesslyn Chew
 *
 */
public class Cell {
	private int row;
	private int col;
	
	private boolean isObstacle;
	private boolean isVirtualWall;
	private boolean isExplored = false;
	private double confidence = 0;
	private boolean isAccurate = false;
	 private boolean checkForImg= false;
	
	public Cell(int row, int col) {
		this.row = row;
		this.col = col;
	}

	public int getRow() {
		return row;
	}
	
	public int getCol() {
		return col;
	}
	 public boolean getCheckForImg() {
	    	return this.checkForImg;
	    }
	 
	 public void setCheckForImg(boolean val) {
	    	this.checkForImg=val;
	    }
	 
	public void forceConfidence(double val) {
		this.confidence = val;
	}
	
	public boolean getIsObstacle() {
		if (MapConstants.useConfidence) {
			if (confidence > 0)
				return true;
			else 
				return false;
		} else 
			return isObstacle;
	}

	public void setIsObstacle(boolean isObstacle) {
		if (!isAccurate)
			this.isObstacle = isObstacle;
	}

	public boolean getIsVirtualWall() {
		return isVirtualWall;
	}

	public void setIsVirtualWall(boolean isVirtualWall) {
		this.isVirtualWall = isVirtualWall;
	}

	public boolean getIsExplored() {
		return isExplored;
	}

	public void setIsExplored(boolean isExplored) {
		this.isExplored = isExplored;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		if (!isAccurate)
			this.confidence += confidence;
	}

	public boolean getIsAccurate() {
		return isAccurate;
	}

	public void setIsAccurate(boolean isAccurate) {
		this.isAccurate = isAccurate;
	}
}
