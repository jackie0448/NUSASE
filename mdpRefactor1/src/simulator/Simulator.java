 package simulator;

import static utils.MapDescriptor.generateMapDescriptor;
import static utils.MapDescriptor.loadMapFromDisk;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import algorithms.Calibration;
import algorithms.ExplorationAlgo;
import algorithms.ExplorationAlgoIR;
import algorithms.FastestPathAlgo8;
import algorithms.FastestPathAlgo4;
import map.Map;
import map.MapConstants;
import robot.Robot;
import robot.RobotConstants;
import robot.RobotConstants.DIRECTION;
import utils.CommMgr;

public class Simulator {
	private static JFrame _appFrame = null;
	private static JPanel _mapCards = null;
	private static JPanel _buttons = null;
	private static BoxLayout boxLayout = null;
	
	// Declare robot 
	private static Robot bot = null;
	
	// Declare map
	private static Map realMap = null;
	private static Map exploredMap = null;
	
	// Declare algorithm
	private static ExplorationAlgo exploration = null;
	//private static ExplorationAlgoIR explorationIR= null;
	
	// Declare communication management 
	private static final CommMgr comm = CommMgr.getCommMgr();
	
	// Declare display
	private static String mapName="";
	private static JButton btn_Exploration;
	private static JButton btn_FastestPath;
	private static Container contentPane;
	private static BoxLayout boxLayout2 = null;
	private static JPanel _displayPanel = null;
	private static JPanel loadMap;
	
	// Declare waypoint
	private static int wpRow = RobotConstants.GOAL_ROW;
	private static int wpCol = RobotConstants.GOAL_COL;
	
	/**
	 * Initialise the different maps and displays the application
	 */
	public static void main(String[] args) {
		// Initialise robot
		bot = new Robot(RobotConstants.START_ROW, RobotConstants.START_COL, RobotConstants.REAL_RUN);
		
		// If it is a real run
		if (RobotConstants.REAL_RUN) {
			// Open connection
			comm.openConnection();
		} else {
			// Initialise Real Map 
			realMap = new Map(bot);
			realMap.setAllUnexplored();
		}
		
		// Initialise exploration map
		exploredMap = new Map(bot);
		exploredMap.setAllUnexplored();
		if (RobotConstants.TESTING_FAST) {
			loadMapFromDisk(exploredMap,"fastestfile"); 
//			exploredMap = realMap;
			exploredMap.setAllExplored();
		} 
		
		// Algorithm
		exploration = new ExplorationAlgo(exploredMap, realMap, bot);
		//eplorationIR = new ExplorationAlgoIR(exploredMap, realMap, bot);
				
		// Calibration at the start [real run]
		Calibration.cCounterCorner = 100;
        Calibration.doCalibration(bot.getRobotPosDir(),DIRECTION.SOUTH,DIRECTION.WEST);
		
		
		// Display all UI
		displayEverything();
		
		// Fastest path
		
		// Way point
		
		
		if (RobotConstants.REAL_RUN) {
			class realFastestPath extends SwingWorker<Integer, String> {
				protected Integer doInBackground() {
					if (!RobotConstants.TESTING_FAST) {
						while (true) {
							System.out.println("Waiting for FP_START...");
							String msg = comm.recvMsg();
							if (msg.contains("f")) {
								break;
							}
						}
					}
					System.out.println("waypoint row:" + wpRow);
			        System.out.println("waypoint col:" + wpCol);
			        CommMgr.getCommMgr().sendMsg("AR","m");
			        FastestPathAlgo8 fastestPath;
			        fastestPath= new FastestPathAlgo8(exploredMap,bot);
					// START --> WAYPOINT
			        fastestPath.runFastestPath(RobotConstants.START_ROW, RobotConstants.START_COL,wpRow,wpCol);
			        System.out.println("Executing waypoint");
			        FastestPathAlgo8 fastestPathB;
			        fastestPathB= new FastestPathAlgo8(exploredMap,bot);
			        // WAYPOINT --> END
			        //fastestPath= new FastestPathAlgo8(exploredMap,bot);
			        fastestPathB.runFastestPath(wpRow,wpCol,RobotConstants.GOAL_ROW,RobotConstants.GOAL_COL);
					
					return 333; 
				}
			}
			
			class wayPoint extends SwingWorker<Integer,String>{
				protected Integer doInBackground() throws Exception{
					while (true){
						System.out.println("Waiting for Waypoint...");
						String wayPointMsg= comm.recvMsg();
						if(wayPointMsg.contains("wp")) {
							try {
								String [] temp= wayPointMsg.split(":");
			 		               String [] wpXy = temp[1].split(",");
			 		               wpCol= Integer.parseInt(wpXy[0]);
			 		               wpRow= Integer.parseInt(wpXy[1]);
			 		               System.out.println("wpRow: " + wpRow);
			 		               System.out.println("wpCol: " + wpCol);
							} catch (Exception e) {
								System.out.println("Exception for waypoint.");
							}
							break;
						}
					}
					return 444;
				}
			}
			
			class realExploration extends SwingWorker<Integer, String> {
				protected Integer doInBackground() throws Exception {
			
				
					  System.out.println("wpRow: " + wpRow);
		               System.out.println("wpCol: " + wpCol);
					int row, col;
					row = RobotConstants.START_ROW;
			        col = RobotConstants.START_COL;
			        bot.setRobotPos(row, col);
			        
			        exploredMap.repaint();
			        
			        
			        //explorationIR.runExploration();
			        exploration.runExploration();
			        generateMapDescriptor(exploredMap);
			        
			       //new wayPoint().execute();
			        new realFastestPath().execute();
			        
			        return 666;
				}
			}
			
			if (!RobotConstants.TESTING_FAST) {
				while (true) {
					System.out.println("Waiting1 for EX_START...");
					
		            String msg = comm.recvMsg();
		            System.out.println(msg);
		            
		            if (msg.equals("x")) break;
		            else if(msg.contains("wp")) {
						try {
							String [] temp= msg.split(":");
		 		               String [] wpXy = temp[1].split(",");
		 		               wpCol= Integer.parseInt(wpXy[0]);
		 		               wpRow= Integer.parseInt(wpXy[1]);
		 		               System.out.println("wpRow: " + wpRow);
		 		               System.out.println("wpCol: " + wpCol);
						} catch (Exception e) {
							System.out.println("Exception for waypoint.");
						}
						
					}
		        }
	        	try {
	        		System.out.println("before explo");
	        		new realExploration().execute();
	        	} catch(Exception e) {
	        		System.out.println(" RealExploration Exception");
	        	}
			}
			else {
				while (true) {
					String msg = comm.recvMsg();
		            System.out.println(msg);
		            
					if (msg.equals("f")) break;
					else if(msg.contains("wp")) {
						try {
							String [] temp= msg.split(":");
		 		               String [] wpXy = temp[1].split(",");
		 		               wpCol= Integer.parseInt(wpXy[0]);
		 		               wpRow= Integer.parseInt(wpXy[1]);
		 		               System.out.println("wpRow: " + wpRow);
		 		               System.out.println("wpCol: " + wpCol);
						} catch (Exception e) {
							System.out.println("Exception for waypoint.");
						}
					}
				}
				try {
					new realFastestPath().execute();
				} catch(Exception e) {
	        		System.out.println(" RealFastest Exception");
	        	}
			}
		}	
	}
	
	private static void resetSimulator() {
		bot = new Robot(RobotConstants.START_ROW, RobotConstants.START_COL, RobotConstants.REAL_RUN);
		
		
		// Initialise Real Map 
		realMap = new Map(bot);
		realMap.setAllUnexplored();
		
		// Initialise exploration map
		exploredMap = new Map(bot);
		exploredMap.setAllUnexplored();
		
		// Initialise algorithm
						
		// Fastest path
		
		// Way point
		
		// Exploration
		exploration = new ExplorationAlgo(exploredMap, realMap, bot);
		//explorationIR = new ExplorationAlgoIR(exploredMap, realMap, bot);
	}
	
	/**
	 * Initialise the application display
	 */
	private static void displayEverything() {
		// Initialise main frame for display
		_appFrame = new JFrame();
		_appFrame.setTitle("Group 11 MDP Simulator");
		
		// Create the CardLayout for storing the different maps
		_mapCards = new JPanel(new CardLayout());
		_mapCards.setSize(new Dimension(850,700));
		
		// Create the JPannel for the buttons
		_buttons = new JPanel();
		
		// Create the BoxLayout for the button
		BoxLayout boxlayout = new BoxLayout(_buttons, BoxLayout.Y_AXIS);
		_buttons.setLayout(boxlayout);
		_buttons.setBorder(new EmptyBorder(40,0,0,0));
		
		// Create a panel to hold different panels
		_displayPanel = new JPanel();
		BoxLayout boxlayout2 = new BoxLayout(_displayPanel, BoxLayout.Y_AXIS);
		_buttons.setLayout(boxlayout2);
		_displayPanel.setBorder(new CompoundBorder(new EmptyBorder(0,0,0,150),BorderFactory.createTitledBorder("Settings")));
		_displayPanel.setPreferredSize(new Dimension(650, 500));
		
		// Add _mapCards & _buttons to the main frame/s content pane
		Container contentPane = _appFrame.getContentPane();
        contentPane.add(_mapCards, BorderLayout.CENTER);
        contentPane.add(_displayPanel, BorderLayout.LINE_END);
        
		// Intialise the main map view
        initMainLayout();
        
		// Initialise the buttons
        initButtonsLayout();
        
		// Display the application 
		Rectangle rect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		_appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_appFrame.setSize(rect.width, rect.height);
		_appFrame.setVisible(true);
	}
	
	/**
	 * Initialise the main map view
	 */
	private static void initMainLayout() {
		if (!RobotConstants.REAL_RUN) {
			_mapCards.add(realMap,"REAL_MAP");
		}
		_mapCards.add(exploredMap,"EXPLORED_MAP");
		
		CardLayout cl = (CardLayout) _mapCards.getLayout();
		if (!RobotConstants.REAL_RUN) {
			cl.show(_mapCards, "REAL_MAP");
		} else {
			cl.show(_mapCards, "EXPLORED_MAP");
		}
	}
	
	/**
	 * Initalise button view
	 */
	private static void initButtonsLayout() {
		_buttons.setLayout(new GridLayout(0,1));
		
		// Select map template button
		if (!RobotConstants.REAL_RUN) {
			// Reset robot
			bot.setRobotPos(1, 1);
			bot.setRobotPosDir(DIRECTION.EAST);
			exploredMap.repaint();
			// TODO: to properly reset, must end all threads
			
			loadMap = new JPanel();
			loadMap.setBorder(new CompoundBorder(new EmptyBorder(20, 0, 0, 0),BorderFactory.createTitledBorder("Map")));
			loadMap.setPreferredSize(new Dimension(450, 100));
			
			final JTextField loadTF = new JTextField(15);
			JButton loadMapButton = new JButton("Load");
			loadMapButton.setFont(new Font("sansserif", Font.BOLD, 13));
			loadMapButton.setBackground(Color.DARK_GRAY);
			loadMapButton.setForeground(Color.WHITE);
			loadMapButton.setPreferredSize(new Dimension(70, 20));
			
			loadMapButton.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					// Remove the current map
					contentPane = _appFrame.getContentPane();
					contentPane.remove(loadMap);
					_appFrame.invalidate();
					_appFrame.validate();
					
					if(loadTF.getText()!=null && !loadTF.getText().equalsIgnoreCase("")) {
						// TODO: fix reset
						
						mapName =loadTF.getText();
						System.out.println("Trying to load '"+ mapName +".txt' map template...");
						loadMapFromDisk(realMap,mapName); 
						loadMapFromDisk(exploredMap,mapName);
						// TODO: Fix this logic 
						exploredMap.setAllUnexplored();
						btn_Exploration.setEnabled(true);
						CardLayout cl = ((CardLayout) _mapCards.getLayout());
						cl.show(_mapCards, "REAL_MAP");
						realMap.repaint();
						_appFrame.repaint();
					} else {
						JOptionPane.showMessageDialog(_appFrame, "Please enter a map name", "Map name not entered",JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			
			JLabel fileName = new JLabel("Map name: ");
			fileName.setFont(new Font("sansserif", Font.PLAIN, 15));
			fileName.setPreferredSize(new Dimension(160, 50));
			
			loadMap.add(fileName,BorderLayout.WEST);
			loadMap.add(loadTF);
			loadMap.add(loadMapButton);
			loadMap.setVisible(true);
			loadMap.setAlignmentX(JPanel.LEFT_ALIGNMENT);
			_displayPanel.add(loadMap);
			_appFrame.invalidate();
			_appFrame.validate();
			
		}
		
		/**
		 * Exploration Class for Multi-threading
		 */
		class Exploration extends SwingWorker<Integer, String> {
			protected Integer doInBackground() throws Exception {
				int row, col;
				// Run exploration
				exploration.runExploration();
				//explorationIR = new ExplorationAlgoIR(exploredMap, realMap, bot);
				
				return 111;
				
			}
		}
		
		// Exploration Button
		btn_Exploration = new JButton("Exploration");
		btn_Exploration.setBackground(Color.DARK_GRAY);
		btn_Exploration.setForeground(Color.WHITE);
		btn_Exploration.setOpaque(true);
		btn_Exploration.setBorderPainted(false);
		btn_Exploration.setPreferredSize(new Dimension(450, 50));
		formatButton(btn_Exploration);
		btn_Exploration.addMouseListener(new MouseAdapter() {
			 public void mousePressed(MouseEvent e) {
				 if (!btn_Exploration.isEnabled()) return;
				 CardLayout cl = ((CardLayout) _mapCards.getLayout());
				 cl.show(_mapCards, "EXPLORED_MAP");
				 // TODO: Remember to enable if reset of exploration ends
				 btn_Exploration.setEnabled(false); 
				 System.out.println("Exploration Button Selected.");
				 new Exploration().execute();
			 }
		 });
		_buttons.add(btn_Exploration);
		_buttons.add(Box.createRigidArea(new Dimension(0, 10)));
		 
		/**
		 * Fastest Path Class for Multi-threading 
		 */
		class FastestPath extends SwingWorker<Integer, String> {
			protected Integer doInBackground() throws Exception {
				if (! RobotConstants.REAL_RUN)
					bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
				exploredMap.repaint();
				
				// Set way point
				FastestPathAlgo8 fastestPath;
		        fastestPath= new FastestPathAlgo8(exploredMap,bot);
				// START --> WAYPOINT
		        fastestPath.runFastestPath(RobotConstants.START_ROW, RobotConstants.START_COL,wpRow,wpCol);
		        System.out.println("Executing waypoint");
		        FastestPathAlgo8 fastestPathB;
		        fastestPathB= new FastestPathAlgo8(exploredMap,bot);
		        // WAYPOINT --> END
		        //fastestPath= new FastestPathAlgo8(exploredMap,bot);
		        fastestPathB.runFastestPath(wpRow,wpCol,RobotConstants.GOAL_ROW,RobotConstants.GOAL_COL);
				
				return 222;
			}
		}
		
		// Fastest Path Button
		btn_FastestPath = new JButton("Fastest Path");
		btn_FastestPath.setBackground(Color.DARK_GRAY);
		btn_FastestPath.setForeground(Color.WHITE);
		btn_FastestPath.setOpaque(true);
		btn_FastestPath.setBorderPainted(false);
		btn_FastestPath.setPreferredSize(new Dimension(450, 50));
		formatButton(btn_FastestPath);
		btn_FastestPath.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				CardLayout cl = ((CardLayout) _mapCards.getLayout());
				cl.show(_mapCards, "EXPLORED_MAP");
				System.out.println("Fastest Path Button Selected.");
				new FastestPath().execute();
			}
		});
		_buttons.add(btn_FastestPath);
		_buttons.add(Box.createRigidArea(new Dimension(0, 10)));
		
		 _displayPanel.add(_buttons);
		 _appFrame.invalidate();
		 _appFrame.validate();
	}
	
	/**
	 * Set particular properties for all the JButtons
	 */
	private static void formatButton(JButton btn) {
		btn.setFont(new Font("sansserif", Font.BOLD, 15));
		btn.setSize(new Dimension(120,100));
		btn.setFocusPainted(false);
	}
	
}
