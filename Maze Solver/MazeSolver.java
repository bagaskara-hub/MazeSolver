import java.util.*;
import java.io.*;
import java.util.ArrayList;

// 1. Definisi simbol
public class MazeSolver {
    public static final char WALL = '#';
    public static final char PATH = '.';
    public static final char START = 'S';
    public static final char END = 'E';
    public static final char VISITED = 'V';
    public static final char SOLUTION = '*';

    public static class Position {
        int row, col;
        Position parent;

        public Position(int row, int col) {
            this.row = row;
            this.col = col;
            this.parent = null;
        }

        public Position(int row, int col, Position parent) {
            this.row = row;
            this.col = col;
            this.parent = parent;
        }

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

        @Override
        public String toString() {
            return "(" + row + ", " + col + ")";
        }
    }

    // 2. Directions: Up, Right, Down, Left
    private static final int[][] DIRECTIONS = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Maze Solver Program ===");
        System.out.print("Enter maze size (rows columns): ");
        int rows = scanner.nextInt();
        int cols = scanner.nextInt();
        
        char[][] maze = generateMaze(rows, cols);
        
        System.out.println("Generated Maze:");
        printMaze(maze);
        
        System.out.println("\nSelect algorithm:");
        System.out.println("1. Breadth-First Search (Queue)");
        System.out.println("2. Depth-First Search (Stack)");
        
        System.out.print("Your choice: ");
        int choice = scanner.nextInt();
        
        long startTime = System.nanoTime();
        long startMemory = getMemoryUsage();
        
        List<Position> solution = null;
        
        if (choice == 1) {
            System.out.println("\nSolving with BFS (Queue)...");
            solution = solveMazeWithQueue(maze);
        } else {
            System.out.println("\nSolving with DFS (Stack)...");
            solution = solveMazeWithStack(maze);
        }
        
        long endTime = System.nanoTime();
        long endMemory = getMemoryUsage();
        
        if (solution != null) {
            System.out.println("\nMaze solved successfully!");
            markSolution(maze, solution);
            System.out.println("\nSolution:");
            printMaze(maze);
            System.out.println("\nPath length: " + (solution.size() - 1) + " steps");
        } else {
            System.out.println("\nNo solution found for the maze!");
        }
        
        System.out.println("\nPerformance Metrics:");
        System.out.println("Execution time: " + (endTime - startTime) / 1_000_000.0 + " ms");
        System.out.println("Memory used: " + (endMemory - startMemory) / 1024 + " KB");
        
        scanner.close();
    }

    // 3. Generate a random maze with given dimensions
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
        // Create a list of directions in random order
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
    
    // 4. Make sure there's a path to the end point
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
    
    // Check if a position is valid
    private static boolean isValidCell(char[][] maze, int row, int col) {
        return row >= 0 && row < maze.length && col >= 0 && col < maze[0].length;
    }

    // BFS - using Queue
    public static List<Position> solveMazeWithQueue(char[][] maze) {
        int rows = maze.length;
        int cols = maze[0].length;
        
        Position start = findPosition(maze, START);
        Position end = findPosition(maze, END);
        
        if (start == null || end == null) {
            return null;
        }
        
        char[][] mazeCopy = copyMaze(maze);
        // 5. Algoritma BFS (solveMazeWithQueue)
        Queue<Position> queue = new LinkedList<>();
        boolean[][] visited = new boolean[rows][cols];
        
        queue.add(start);
        visited[start.row][start.col] = true;
        
        while (!queue.isEmpty()) {
            Position current = queue.poll();
            
            if (current.row == end.row && current.col == end.col) {
                return reconstructPath(current);
            }
            
            for (int[] dir : DIRECTIONS) {
                int newRow = current.row + dir[0];
                int newCol = current.col + dir[1];
                
                if (isValidMove(mazeCopy, newRow, newCol, visited)) {
                    visited[newRow][newCol] = true;
                    if (mazeCopy[newRow][newCol] != START && mazeCopy[newRow][newCol] != END) {
                        mazeCopy[newRow][newCol] = VISITED;
                    }
                    queue.add(new Position(newRow, newCol, current));
                }
            }
        }
        
        return null; // No path found
    }

    // DFS - using Stack
    public static List<Position> solveMazeWithStack(char[][] maze) {
        int rows = maze.length;
        int cols = maze[0].length;
        
        Position start = findPosition(maze, START);
        Position end = findPosition(maze, END);
        
        if (start == null || end == null) {
            return null;
        }
        
        char[][] mazeCopy = copyMaze(maze);
        // 6. Algoritma DFS (solveMazeWithStack)
        Stack<Position> stack = new Stack<>();
        boolean[][] visited = new boolean[rows][cols];
        
        stack.push(start);
        visited[start.row][start.col] = true;
        
        while (!stack.isEmpty()) {
            Position current = stack.pop();
            
            if (current.row == end.row && current.col == end.col) {
                return reconstructPath(current);
            }
            
            for (int[] dir : DIRECTIONS) {
                int newRow = current.row + dir[0];
                int newCol = current.col + dir[1];
                
                if (isValidMove(mazeCopy, newRow, newCol, visited)) {
                    visited[newRow][newCol] = true;
                    if (mazeCopy[newRow][newCol] != START && mazeCopy[newRow][newCol] != END) {
                        mazeCopy[newRow][newCol] = VISITED;
                    }
                    stack.push(new Position(newRow, newCol, current));
                }
            }
        }
        
        return null; // No path found
    }

    // Check if a move is valid
    private static boolean isValidMove(char[][] maze, int row, int col, boolean[][] visited) {
        return isValidCell(maze, row, col) && 
               (maze[row][col] == PATH || maze[row][col] == END) && 
               !visited[row][col];
    }

    // Find a specific character in the maze
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

    // Reconstruct the path from end to start
    private static List<Position> reconstructPath(Position end) {
        List<Position> path = new LinkedList<>();
        Position current = end;
        
        while (current != null) {
            path.add(0, current);
            current = current.parent;
        }
        
        return path;
    }

    // Mark the solution path on the maze
    public static void markSolution(char[][] maze, List<Position> path) {
        for (Position pos : path) {
            if (maze[pos.row][pos.col] != START && maze[pos.row][pos.col] != END) {
                maze[pos.row][pos.col] = SOLUTION;
            }
        }
    }

    // Create a deep copy of the maze
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