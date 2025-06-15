import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class MazeSolverApp extends JFrame {
    private static final int DEFAULT_SIZE = 15;
    private int mazeSize = DEFAULT_SIZE;
    private char[][] maze;
    private JPanel mazePanel;
    private JComboBox<String> algorithmComboBox;
    private JButton solveButton;
    private JButton generateButton;
    private JTextField sizeTextField;
    private JLabel statusLabel;
    private JLabel memoryLabel;
    private JLabel timeLabel;
    
    private static final Color WALL_COLOR = Color.BLACK;
    private static final Color PATH_COLOR = Color.WHITE;
    private static final Color START_COLOR = Color.GREEN;
    private static final Color END_COLOR = Color.RED;
    private static final Color SOLUTION_COLOR = Color.BLUE;
    private static final Color VISITED_COLOR = new Color(200, 200, 255);
    
    public MazeSolverApp() {
        setTitle("Maze Solver Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        
        JLabel sizeLabel = new JLabel("Maze Size:");
        sizeTextField = new JTextField(5);
        sizeTextField.setText(String.valueOf(mazeSize));
        
        algorithmComboBox = new JComboBox<>();
        algorithmComboBox.addItem("BFS (Queue)");
        algorithmComboBox.addItem("DFS (Stack)");
        
        generateButton = new JButton("Generate Maze");
        solveButton = new JButton("Solve Maze");
        
        controlPanel.add(sizeLabel);
        controlPanel.add(sizeTextField);
        controlPanel.add(algorithmComboBox);
        controlPanel.add(generateButton);
        controlPanel.add(solveButton);
        
        // Status panel
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(3, 1));
        
        statusLabel = new JLabel("Ready");
        memoryLabel = new JLabel("Memory: 0 KB");
        timeLabel = new JLabel("Time: 0 ms");
        
        statusPanel.add(statusLabel);
        statusPanel.add(memoryLabel);
        statusPanel.add(timeLabel);
        
        // Maze panel
        mazePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawMaze(g);
            }
        };
        
        // Add components to frame
        add(controlPanel, BorderLayout.NORTH);
        add(mazePanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        
        // Generate initial maze
        maze = MazeSolver.generateMaze(mazeSize, mazeSize);
        
        // Add action listeners
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    mazeSize = Integer.parseInt(sizeTextField.getText().trim());
                    if (mazeSize < 5) {
                        mazeSize = 5;
                        sizeTextField.setText("5");
                    } else if (mazeSize > 100) {
                        mazeSize = 100;
                        sizeTextField.setText("100");
                    }
                    
                    long startMemory = getMemoryUsage();
                    long startTime = System.nanoTime();
                    
                    maze = MazeSolver.generateMaze(mazeSize, mazeSize);
                    
                    long endTime = System.nanoTime();
                    long endMemory = getMemoryUsage();
                    
                    updatePerformanceLabels(startTime, endTime, startMemory, endMemory);
                    statusLabel.setText("New maze generated");
                    
                    mazePanel.repaint();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(MazeSolverApp.this, 
                            "Please enter a valid number for maze size", 
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                solveMaze();
            }
        });
        
        pack();
        setSize(800, 600);
        setLocationRelativeTo(null);
    }
    
    private void solveMaze() {
        statusLabel.setText("Solving maze...");
        
        // Run in a separate thread to keep UI responsive
        new Thread(() -> {
            long startMemory = getMemoryUsage();
            long startTime = System.nanoTime();
            
            List<MazeSolver.Position> solution;
            boolean useQueue = algorithmComboBox.getSelectedIndex() == 0;
            
            if (useQueue) {
                solution = MazeSolver.solveMazeWithQueue(maze);
            } else {
                solution = MazeSolver.solveMazeWithStack(maze);
            }
            
            long endTime = System.nanoTime();
            long endMemory = getMemoryUsage();
            
            if (solution != null) {
                MazeSolver.markSolution(maze, solution);
                
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Maze solved! Path length: " + (solution.size() - 1) + " steps");
                    updatePerformanceLabels(startTime, endTime, startMemory, endMemory);
                    mazePanel.repaint();
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("No solution found!");
                    updatePerformanceLabels(startTime, endTime, startMemory, endMemory);
                });
            }
        }).start();
    }
    
    private void updatePerformanceLabels(long startTime, long endTime, long startMemory, long endMemory) {
        double executionTime = (endTime - startTime) / 1_000_000.0;
        long memoryUsed = (endMemory - startMemory) / 1024;
        
        timeLabel.setText(String.format("Time: %.2f ms", executionTime));
        memoryLabel.setText(String.format("Memory: %d KB", memoryUsed));
    }
    
    private void drawMaze(Graphics g) {
        if (maze == null) return;
        
        int width = mazePanel.getWidth();
        int height = mazePanel.getHeight();
        
        int cellWidth = width / maze[0].length;
        int cellHeight = height / maze.length;
        
        for (int row = 0; row < maze.length; row++) {
            for (int col = 0; col < maze[row].length; col++) {
                int x = col * cellWidth;
                int y = row * cellHeight;
                
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
                        g.setColor(Color.GRAY);
                }
                
                g.fillRect(x, y, cellWidth, cellHeight);
                
                g.setColor(Color.GRAY);
                g.drawRect(x, y, cellWidth, cellHeight);
            }
        }
    }
    
    private long getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MazeSolverApp().setVisible(true);
        });
    }
}