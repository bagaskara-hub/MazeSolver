import java.util.*;
import java.io.*;
import java.util.ArrayList;

/* 1. SYMBOL DEFINITION */ 
public class MazeSolver { // MazeSolver is the main class
    public static final char WALL = '#';
    public static final char PATH = '.';
    public static final char START = 'S';
    public static final char END = 'E';
    public static final char VISITED = 'V';
    public static final char SOLUTION = '*';

    // position class: coordinates
    public static class Position {
        int row, col;
        Position parent;

        // start node
        public Position(int row, int col) {
            this.row = row;
            this.col = col;
            this.parent = null;
        }

        // path tracing
        public Position(int row, int col, Position parent) {
            this.row = row;
            this.col = col;
            this.parent = parent;
        }

        // allows comparison an use in sets/maps
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Position position = (Position) obj;
            return row == position.row && col == position.col;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }

        // allows printing of coordinates later on
        @Override
        public String toString() {
            return "(" + row + ", " + col + ")";
        }
    }

    /*  2. DIRECTIONS: UP, RIGHT, DOWN, LEFT */
    private static final int[][] DIRECTIONS = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

    // main method: for user interaction & execution
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // uses scanner to get user input
        
        System.out.println("=== Maze Solver Program ===");
        System.out.print("Enter maze size (rows columns): "); // asks for maze size
        int rows = scanner.nextInt(); // reads number of rows from the user
        int cols = scanner.nextInt(); // reads number of columns from the user
        
        char[][] maze = generateMaze(rows, cols); // generates maze of requested size
        
        // prints generated maze
        System.out.println("Generated Maze:");
        printMaze(maze);
        
        // prompts the user to select BFS or DFS algorithm
        System.out.println("\nSelect algorithm:");
        System.out.println("1. Breadth-First Search (Queue)");
        System.out.println("2. Depth-First Search (Stack)");
        
        System.out.print("Your choice: ");
        int choice = scanner.nextInt();
        
        // records performance start time and memory
        long startTime = System.nanoTime();
        long startMemory = getMemoryUsage();
        
        // performs either BFS or DFS based on user choice
        List<Position> solution = null;
        
        if (choice == 1) {
            System.out.println("\nSolving with BFS (Queue)...");
            solution = solveMazeWithQueue(maze);
        } else {
            System.out.println("\nSolving with DFS (Stack)...");
            solution = solveMazeWithStack(maze);
        }
        
        // records performance end time and memory
        long endTime = System.nanoTime();
        long endMemory = getMemoryUsage();
        
        // if solution exists: mark and print path, otherwise notify no path found
        if (solution != null) {
            System.out.println("\nMaze solved successfully!");
            markSolution(maze, solution);
            System.out.println("\nSolution:");
            printMaze(maze);
            System.out.println("\nPath length: " + (solution.size() - 1) + " steps");
        } else {
            System.out.println("\nNo solution found for the maze!");
        }
        
        // display runtime and memory used
        System.out.println("\nPerformance Metrics:");
        System.out.println("Execution time: " + (endTime - startTime) / 1_000_000.0 + " ms");
        System.out.println("Memory used: " + (endMemory - startMemory) / 1024 + " KB");
        
        scanner.close();
    }

    /* 3. GENERATE A RANDOM MAZE WITH GIVEN DIMENSIONS */ 

    // ensures minimum maze size is 5x5
    public static char[][] generateMaze(int rows, int cols) {
        if (rows < 5) rows = 5;
        if (cols < 5) cols = 5;
        
        char[][] maze = new char[rows][cols];
        Random random = new Random();
        
        // Fill with walls
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                maze[i][j] = WALL;
            }
        }
        
        // Create a path using a simple algorithm
        int startRow = 1;
        int startCol = 1;
        int endRow = rows - 2;
        int endCol = cols - 2;
        
        // Mark start and end
        maze[startRow][startCol] = START;
        maze[endRow][endCol] = END;
        
        // Generate paths
        generatePaths(maze, startRow, startCol);
        
        // Ensure there is a path to the end
        ensurePathToEnd(maze, endRow, endCol);
        
        return maze;
    }
    
    // Recursive method to generate paths in the maze
    private static void generatePaths(char[][] maze, int row, int col) {
        // Create a list of directions in random order and tries them
        List<Integer> directions = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        Collections.shuffle(directions);
        
        for (int dir : directions) {
            int newRow = row + DIRECTIONS[dir][0] * 2;
            int newCol = col + DIRECTIONS[dir][1] * 2;
            
            if (isValidCell(maze, newRow, newCol) && maze[newRow][newCol] == WALL) {
                // Mark the wall in between as a path
                maze[row + DIRECTIONS[dir][0]][col + DIRECTIONS[dir][1]] = PATH;
                maze[newRow][newCol] = PATH;
                generatePaths(maze, newRow, newCol);
            }
        }
    }
    
    /* 4. MAKE SURE THERE'S A PATH TO THE END POINT */
    private static void ensurePathToEnd(char[][] maze, int endRow, int endCol) {
        if (maze[endRow][endCol] == END) {
            // Create paths next to the end point if needed
            for (int[] dir : DIRECTIONS) {
                int newRow = endRow + dir[0];
                int newCol = endCol + dir[1];
                
                if (isValidCell(maze, newRow, newCol) && maze[newRow][newCol] == WALL) {
                    maze[newRow][newCol] = PATH;
                    break;
                }
            }
        }
    }
    
    // Check if a position is valid (inside bounds)
    private static boolean isValidCell(char[][] maze, int row, int col) {
        return row >= 0 && row < maze.length && col >= 0 && col < maze[0].length;
    }

    /* BFS - using Queue */ 

    // declares method to solve using BFS and returns a list representing the path from start to end
    public static List<Position> solveMazeWithQueue(char[][] maze) { 
        // gets number of rows and columns in maze
        int rows = maze.length;
        int cols = maze[0].length;
        
        // locates start and end of maze using helper function
        Position start = findPosition(maze, START);
        Position end = findPosition(maze, END);
        
        // if either start/end is not found: return null (invalid maze)
        if (start == null || end == null) {
            return null;
        }
        
        // make a deep copy of the maze to avoid modifying the original maze
        char[][] mazeCopy = copyMaze(maze);

        /* 5. BFS algorithm (solveMazeWithQueue) */ 
        Queue<Position> queue = new LinkedList<>(); // initializes a queue to process positions layer by layer
        boolean[][] visited = new boolean[rows][cols]; // initializes a 2D array to track which cells have been visited
        
        // adds start position to queue and marks it as visited
        queue.add(start);
        visited[start.row][start.col] = true;
        
        // BFS MAIN LOOP
        while (!queue.isEmpty()) { // while there are positions to explore in the queue
            Position current = queue.poll(); // gets and removes the next position to explore
            
            // if the current position is the end, call reconstructPath to trace back the path from end to start
            if (current.row == end.row && current.col == end.col) {
                return reconstructPath(current);
            }
            
            // try all 4 directions & compute next cell's coordinates
            for (int[] dir : DIRECTIONS) {
                int newRow = current.row + dir[0];
                int newCol = current.col + dir[1];
                
                // check if next cell is within bounds, not a wall & not visited yet
                if (isValidMove(mazeCopy, newRow, newCol, visited)) {
                    // marks cell as visited
                    visited[newRow][newCol] = true; 
                    if (mazeCopy[newRow][newCol] != START && mazeCopy[newRow][newCol] != END) {
                        mazeCopy[newRow][newCol] = VISITED; // mark cell visited for visual feedback
                    }
                    // add new position to queue
                    queue.add(new Position(newRow, newCol, current));
                }
            }
        }
        
        return null; // No path found
    }

    /* DFS - using Stack */ 
    // declares method to solve using DFS and returns a list representing the path from start to end
    public static List<Position> solveMazeWithStack(char[][] maze) {
        // determines number of rows and columns in maze
        int rows = maze.length;
        int cols = maze[0].length;
        
        // finds coordinates of start and end points
        Position start = findPosition(maze, START);
        Position end = findPosition(maze, END);
        
        // if start/end missing, return null (invalid maze)
        if (start == null || end == null) {
            return null;
        }
        
        // make a deep copy of the maze to avoid modifying the original maze.
        char[][] mazeCopy = copyMaze(maze);

        /* 6.  DFS algorithm (solveMazeWithStack) */ 
        Stack<Position> stack = new Stack<>(); // creates stack to hold maze positions for DFS
        boolean[][] visited = new boolean[rows][cols]; // initializes a visited 2D array to keep track of explored cells
        
        // starts DFS by pushing the start position and marking it as visited
        stack.push(start);
        visited[start.row][start.col] = true;
        
        // DFS LOOP
        while (!stack.isEmpty()) { // loop until no more nodes to explore
            Position current = stack.pop(); // get current position from top of stack
            
            if (current.row == end.row && current.col == end.col) {
                return reconstructPath(current); // found the end, reconstruct and return the path
            }
            
            // explore adjacent positions
            for (int[] dir : DIRECTIONS) {
                // compute coordinates of adjacent cells
                int newRow = current.row + dir[0];
                int newCol = current.col + dir[1];
                
                if (isValidMove(mazeCopy, newRow, newCol, visited)) { // check if valid coordinate
                    // marks new cell as visited
                    visited[newRow][newCol] = true; 
                    if (mazeCopy[newRow][newCol] != START && mazeCopy[newRow][newCol] != END) {
                        mazeCopy[newRow][newCol] = VISITED; // mark cell visited for visual feedback
                    }
                    stack.push(new Position(newRow, newCol, current)); // push this position with a link to its parent
                }
            }
        }
        
        return null; // No path found
    }

    // Check if a move is valid: it must be path or end and not visited
    private static boolean isValidMove(char[][] maze, int row, int col, boolean[][] visited) {
        return isValidCell(maze, row, col) && 
               (maze[row][col] == PATH || maze[row][col] == END) && 
               !visited[row][col];
    }

    // Find a specific character in the maze: start or end
    private static Position findPosition(char[][] maze, char target) { 
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                if (maze[i][j] == target) {
                    return new Position(i, j);
                }
            }
        }
        return null;
    }

    // Reconstruct the path from end to start using .parent
    private static List<Position> reconstructPath(Position end) {
        List<Position> path = new LinkedList<>();
        Position current = end;
        
        while (current != null) {
            path.add(0, current);
            current = current.parent;
        }
        
        return path;
    }

    // Mark the solution path on the maze with *
    public static void markSolution(char[][] maze, List<Position> path) {
        for (Position pos : path) {
            if (maze[pos.row][pos.col] != START && maze[pos.row][pos.col] != END) {
                maze[pos.row][pos.col] = SOLUTION;
            }
        }
    }

    // Create a deep copy of the maze for safe editing
    private static char[][] copyMaze(char[][] original) {
        char[][] copy = new char[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            copy[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return copy;
    }

    // Print the maze
    private static void printMaze(char[][] maze) {
        for (char[] row : maze) {
            System.out.println(new String(row));
        }
    }

    // Get current memory usage
    private static long getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}