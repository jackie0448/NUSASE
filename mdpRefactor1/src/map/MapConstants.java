package map;

/**
 * Constants used in the Map class.
 *
 * 
 */
public class MapConstants {
    public static final int MAP_ROWS = 20;      // total num of rows
    public static final int MAP_COLS = 15;      // total num of cols
    public static final int MAP_SIZE = MAP_ROWS*MAP_COLS;     // total num of cells
    public static final int GOAL_ROW = MAP_ROWS -2;      // row no. of goal cell
    public static final int GOAL_COL = MAP_COLS -2;      // col no. of goal cell
    public static final boolean useConfidence = true;
    public static final boolean useForceAccurate = true;
}
