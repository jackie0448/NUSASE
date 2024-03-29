package utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.SwingWorker;

import map.Map;
import map.MapConstants;
import robot.RobotConstants;

/**
 * Read and generate map strings
 * 
 * Part 1: represents explored state. All cells are represented
 * Part 2: represents obstacle state. Only explored cells are represented
 * Didn't edit the map descriptor part is there is no need for more modification
 * Get android team to check
 * @author Jesslyn Chew
 *
 */
public class MapDescriptor {
	 /**
     * Reads filename.txt from disk and loads it into the passed Map object. Uses a simple binary indicator to
     * identify if a cell is an obstacle.
     */
    public static void loadMapFromDisk(Map map, String filename) {
        try {
            InputStream inputStream = new FileInputStream("maps/" + filename + ".txt");
            BufferedReader buf = new BufferedReader(new InputStreamReader(inputStream));

            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();
            while (line != null) {
                sb.append(line);
                line = buf.readLine();
            }

            String bin = sb.toString();
            int binPtr = 0;
            //reset the map
            if (!RobotConstants.REAL_RUN) 
            	map.resetMap();
            for (int row = MapConstants.MAP_ROWS - 1; row >= 0; row--) {
                for (int col = 0; col < MapConstants.MAP_COLS; col++) {
                    if (bin.charAt(binPtr) == '1') {
                    	map.setConfidenceCell(row, col, 1);
                    	map.setObstacleCell(row, col, true);
                    }
                    binPtr++;
                }
            }

            map.setAllExplored(); //without this obstacles will not paint
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to convert a binary string to a hex string.
     */
    private static String binToHex(String bin) {
        int dec = Integer.parseInt(bin, 2);

        return Integer.toHexString(dec);
    }

    /**
     * Generates Part 1 & Part 2 map descriptor strings from the passed Map object.
     */
    public static String[] generateMapDescriptor(Map map) {
        String[] ret = new String[2];

        StringBuilder Part1 = new StringBuilder();
        StringBuilder Part1_bin = new StringBuilder();
        Part1_bin.append("11");
        for (int r = 0; r < MapConstants.MAP_ROWS; r++) {
            for (int c = 0; c < MapConstants.MAP_COLS; c++) {
                if (map.getExploredCell(r,c))
                    Part1_bin.append("1");
                else
                    Part1_bin.append("0");

                if (Part1_bin.length() == 4) {
                    Part1.append(binToHex(Part1_bin.toString()));
                    Part1_bin.setLength(0);
                }
            }
        }
        Part1_bin.append("11");
        Part1.append(binToHex(Part1_bin.toString()));
//        System.out.println("P1: " + Part1.toString());
        ret[0] = Part1.toString();

        StringBuilder Part2 = new StringBuilder();
        StringBuilder Part2_bin = new StringBuilder();
        Part2_bin.append("11");
        for (int r = 0; r < MapConstants.MAP_ROWS; r++) {
            for (int c = 0; c < MapConstants.MAP_COLS; c++) {
//                if (map.getCell(r, c).getIsExplored()) {
                    if (map.getObstacleCell(r,c))
                        Part2_bin.append("1");
                    else
                        Part2_bin.append("0");

                    if (Part2_bin.length() == 4) {
                        Part2.append(binToHex(Part2_bin.toString()));
                        Part2_bin.setLength(0);
                    }
//                }
            }
        }
        Part2_bin.append("11");
        Part2.append(binToHex(Part2_bin.toString()));
//        System.out.println("P2: " + Part2.toString());
        ret[1] = Part2.toString();

        return ret;
    }

    public static String generatapForAndroid(Map map) {
		StringBuilder res = new StringBuilder();
		StringBuilder res_bin = new StringBuilder();
		StringBuilder total = new StringBuilder();
		StringBuilder res2 = new StringBuilder();
		StringBuilder res2_bin = new StringBuilder();

		
		//obstacles
		res_bin.append("11"); // to test

		for (int row = 19; row >= 0; row--) {
			for (int col = 0; col < 15; col++) {
				if (map.getExploredCell(row, col)) 
				
					res_bin.append("1");
				 else {
					res_bin.append("0");
				}

				if (res_bin.length() == 4) {
					res.append(binToHex(res_bin.toString()));
					res_bin.setLength(0);
				}
			
			}
			
		}
		res_bin.append("11");
		total.append(binToHex(res_bin.toString()));
		total.append(res.toString());
		total.append(",");

		

		for (int r = 0; r < MapConstants.MAP_ROWS; r++) {
			for (int c = 0; c < MapConstants.MAP_COLS; c++) {
				if (map.getExploredCell(r,c)) {
					if (map.getObstacleCell(r, c))
						res2_bin.append("1");
					else
						res2_bin.append("0");

					if (res2_bin.length() == 4) {
						res2.append(binToHex(res2_bin.toString()));
						res2_bin.setLength(0);
					}
				}
			}
		}
	
		res2.append(res2_bin.toString());
	
		total.append(res2);
		return total.toString();
	}

	
}
