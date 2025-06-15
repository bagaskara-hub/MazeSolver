import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

// class declaration: it is a window that can contain other GUI components
public class MazeSolverApp extends JFrame {
    private static final int DEFAULT_SIZE = 15; // default maze size 15
    private int mazeSize = DEFAULT_SIZE; // maze size can be changed by user input
    private char[][] maze; // stores the maze as a 2D array
    // GUI components for maze
    private JPanel mazePanel;
    private JComboBox<String> algorithmComboBox;
    private JButton solveButton;
    private JButton generateButton;
    private JTextField sizeTextField;
    private JLabel statusLabel;
    private JLabel memoryLabel;
    private JLabel timeLabel;
    
    // defines colors used to draw maze parts
    private static final Color WALL_COLOR = Color.BLACK;
    private static final Color PATH_COLOR = Color.WHITE;
    private static final Color START_COLOR = Color.GREEN;
    private static final Color END_COLOR = Color.RED;
    private static final Color SOLUTION_COLOR = Color.BLUE;
    private static final Color VISITED_COLOR = new Color(200, 200, 255);
    
    /* CONSTRUCTOR */
    // main method that sets up GUI window and components
    public MazeSolverApp() {
        setTitle("Maze Solver Application"); // window title
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close app
        setLayout(new BorderLayout()); // organizes components
        
        /* CONTROL PANEL */
        // top bar for input controls
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        
        // creates a label and text field for user input
        JLabel sizeLabel = new JLabel("Maze Size:");
        sizeTextField = new JTextField(5);
        sizeTextField.setText(String.valueOf(mazeSize));
        
        // dropdown for choosing algorithm (BFS/DFS)
        algorithmComboBox = new JComboBox<>();
        algorithmComboBox.addItem("BFS (Queue)");
        algorithmComboBox.addItem("DFS (Stack)");
        
        // generate & solve buttons
        generateButton = new JButton("Generate Maze");
        solveButton = new JButton("Solve Maze");
        
        // adds all control elements to the top bar
        controlPanel.add(sizeLabel);
        controlPanel.add(sizeTextField);
        controlPanel.add(algorithmComboBox);
        controlPanel.add(generateButton);
        controlPanel.add(solveButton);
        
        // Status panel
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(3, 1)); // creates vertical layout with 3 labels
        
        // displays current status, memory usage, and time
        statusLabel = new JLabel("Ready");
        memoryLabel = new JLabel("Memory: 0 KB");
        timeLabel = new JLabel("Time: 0 ms");
        
        // adds status labels to bottom of window
        statusPanel.add(statusLabel);
        statusPanel.add(memoryLabel);
        statusPanel.add(timeLabel);
        
        // Maze panel: draws maze visually
        mazePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawMaze(g);
            }
        };
        
        // Add components to window 
        add(controlPanel, BorderLayout.NORTH);
        add(mazePanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        
        // Generate initial maze
        maze = MazeSolver.generateMaze(mazeSize, mazeSize);
        
        // Add action listeners to generate maze button
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // read and parse maze size from input text field
                    mazeSize = Integer.parseInt(sizeTextField.getText().trim());
                    // clamp maze size to minimum of 5 and maximum of 100
                    if (mazeSize < 5) {
                        mazeSize = 5;
                        sizeTextField.setText("5"); // update input field
                    } else if (mazeSize > 100) {
                        mazeSize = 100;
                        sizeTextField.setText("100");
                    }
                    
                    // record memory usage before generation
                    long startMemory = getMemoryUsage();
                    // record start time in nanoseconds
                    long startTime = System.nanoTime();
                    
                    // generate a new maze of specified size
                    maze = MazeSolver.generateMaze(mazeSize, mazeSize);
                    
                    // record time and memory after generation
                    long endTime = System.nanoTime();
                    long endMemory = getMemoryUsage();
                    
                    // update status label
                    updatePerformanceLabels(startTime, endTime, startMemory, endMemory);
                    statusLabel.setText("New maze generated");
                    
                    // repaint the panel to display new maze
                    mazePanel.repaint();
                } catch (NumberFormatException ex) {
                    // show error text if the input is not a valid number
                    JOptionPane.showMessageDialog(MazeSolverApp.this, 
                            "Please enter a valid number for maze size", 
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // add action listener to the solve maze button
        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                solveMaze(); // call the solveMaze() method when button is clicked
            }
        });
        
        pack(); // let the layout manager calculate the optimal layout size
        setSize(800, 600); // set window size
        setLocationRelativeTo(null); // center application window on screen
    }
    
    /* SOLVEMAZE METHOD */
    private void solveMaze() {
        // update the status bar to indicate maze has started solving
        statusLabel.setText("Solving maze...");
        
        // Run in a separate thread to keep UI responsive
        new Thread(() -> {
            // record memory and time usage before solving
            long startMemory = getMemoryUsage();
            long startTime = System.nanoTime();
            
            // create a list to hold solution path
            List<MazeSolver.Position> solution;
            // check which algorithm chosen (BFS = 0, DFS = 1)
            boolean useQueue = algorithmComboBox.getSelectedIndex() == 0;
            
            // solve maze using chosen algorithm
            if (useQueue) {
                solution = MazeSolver.solveMazeWithQueue(maze); // BFS
            } else {
                solution = MazeSolver.solveMazeWithStack(maze); // DFS
            }
            
            // record memory and time usage after solving
            long endTime = System.nanoTime();
            long endMemory = getMemoryUsage();
            
            // if solution found
            if (solution != null) {
                MazeSolver.markSolution(maze, solution); // mark path on maze
                
                // update the UI on the Event Dispatch Thread
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Maze solved! Path length: " + (solution.size() - 1) + " steps");
                    updatePerformanceLabels(startTime, endTime, startMemory, endMemory);
                    mazePanel.repaint(); // redraw maze with the solution
                });
            } else { // no solution found
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("No solution found!");
                    updatePerformanceLabels(startTime, endTime, startMemory, endMemory);
                });
            }
        }).start(); // start the background thread
    }
    
    private void updatePerformanceLabels(long startTime, long endTime, long startMemory, long endMemory) {
        // calculate time in milliseconds
        double executionTime = (endTime - startTime) / 1_000_000.0;
        // calculate memory in kilobytes
        long memoryUsed = (endMemory - startMemory) / 1024;
        
        // update the labels in the GUI
        timeLabel.setText(String.format("Time: %.2f ms", executionTime));
        memoryLabel.setText(String.format("Memory: %d KB", memoryUsed));
    }
    
    private void drawMaze(Graphics g) {
        // dont draw anything if the maze is not initialized
        if (maze == null) return;
        
        // get the dimensions of the panel
        int width = mazePanel.getWidth();
        int height = mazePanel.getHeight();
        
        // calculate the width & height of each cell
        int cellWidth = width / maze[0].length;
        int cellHeight = height / maze.length;
        
        // loop through each cell in maze
        for (int row = 0; row < maze.length; row++) {
            for (int col = 0; col < maze[row].length; col++) {
                // calculate top left corner of the cell
                int x = col * cellWidth;
                int y = row * cellHeight;
                
                // set the color based on the maze cell type
                switch (maze[row][col]) {
                    case MazeSolver.WALL:
                        g.setColor(WALL_COLOR);
                        break;
                    case MazeSolver.PATH:
                        g.setColor(PATH_COLOR);
                        break;
                    case MazeSolver.START:
                        g.setColor(START_COLOR);
                        break;
                    case MazeSolver.END:
                        g.setColor(END_COLOR);
                        break;
                    case MazeSolver.SOLUTION:
                        g.setColor(SOLUTION_COLOR);
                        break;
                    case MazeSolver.VISITED:
                        g.setColor(VISITED_COLOR);
                        break;
                    default:
                        g.setColor(Color.GRAY); // default color
                }
                
                // fill the cell with the selected color
                g.fillRect(x, y, cellWidth, cellHeight);
                
                // draw a gray border around the cell
                g.setColor(Color.GRAY);
                g.drawRect(x, y, cellWidth, cellHeight);
            }
        }
    }
    
    /* MEASURE JVM MEMORY USE */
    private long getMemoryUsage() {
        // get a reference to the Java memory manager
        Runtime runtime = Runtime.getRuntime();
        // return used memory = total - free
        return runtime.totalMemory() - runtime.freeMemory();
    }
    
    /* START THE APPLICATION */
    public static void main(String[] args) {
        // start the app safely on the Swing Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new MazeSolverApp().setVisible(true); // show the window
        });
    }
}