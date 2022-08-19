import java.io.*;

// Recursive Backtracking Algorithm

class Sudoku {

    int SIZE, N;

    /* Numbers not yet revealed are stored as 0.*/
    int Grid[][];

    public boolean checkForTarget(int index, int target, boolean row, boolean col) {
        if (row) {
            for (int colIndex = 0; colIndex < N; ++colIndex) {
                if (Grid[index][colIndex] == target) {
                    return false;
                }
            }
        } else {
            for (int rowIndex = 0; rowIndex < N; ++rowIndex) {
                if (Grid[rowIndex][index] == target) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkSquares(int row, int col, int target) {
        int isoRow = (row/SIZE) * SIZE;
        int isoCol = (col/SIZE) * SIZE;
        for (int i = isoRow; i < (isoRow + SIZE); ++i) {
            for (int j = isoCol; j < (isoCol + SIZE); ++j) {
                if (Grid[i][j] == target) {
                    return false;
                }
            }
        }
        return true;
    }

    public void findValidNum(int row, int col) throws Exception {
        if (row >= N) throw new Exception("Solved.");

        if (Grid[row][col] != 0) {
            findNextValid(row, col);
        }
        else {
            for (int guess = 1; guess < N + 1; ++guess) {
                if (checkForTarget(row, guess, true, false) &&
                        checkForTarget(col, guess, false, true) &&
                        checkSquares(row, col, guess)) {
                    Grid[row][col] = guess;
                    findNextValid(row, col);
                }
            }
            Grid[row][col] = 0;
        }
    }

    public void findNextValid(int row, int col) throws Exception{
        if (col < (N - 1)) {
            findValidNum(row, col + 1);
        } else {
            findValidNum(row + 1, 0);
        }
    }

    public void solve() {
        long time = System.nanoTime();
        try{findValidNum(0,0);}
        catch(Exception e){}

        // Print out the running time in MILLISECONDS of this solve() method
        System.out.println("Runtime: " + (System.nanoTime() - time)/1e6);
    }

    /* Constructs an empty grid of param x param.  */
    public Sudoku(int param) {
        SIZE = param;
        N = param * param;
        Grid = new int[N][N];
        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < N; ++j) {
                Grid[i][j] = 0;
            }
        }
    }

    static int readInput(InputStream in) throws Exception {
        int result = 0;
        boolean done = false;
        while (!done) {
            String input = inputBuffer(in);
            try {
                result = Integer.parseInt(input);
                done = true;
            } catch(Exception e) {
                // Convert 'x' words into 0's
                if(input.compareTo("x") == 0 || input.compareTo("?") == 0) {
                    result = 0;
                    done = true;
                }
            }
        }
        return result;
    }

    static String inputBuffer(InputStream in) throws Exception {
        StringBuffer result = new StringBuffer();
        int currentChar = in.read();
        String whiteSpace = " \t\r\n";

        while(whiteSpace.indexOf(currentChar) > -1) {
            currentChar = in.read();
        }

        while(whiteSpace.indexOf(currentChar) == -1) {
            result.append((char) currentChar);
            currentChar = in.read();
        }
        return result.toString();
    }


    /* This function reads a Sudoku puzzle from the input stream in.  The Sudoku
     * grid is filled in one row at at time, from left to right.  All non-valid
     * characters are ignored by this function and may be used in the Sudoku file
     * to increase its legibility. */
    public void read(InputStream in) throws Exception {
        for(int i = 0; i < N; i++) {
            for(int j = 0; j < N; j++) {
                Grid[i][j] = readInput(in);
            }
        }
    }


    /* Helper function for the printing of Sudoku puzzle.  This function will print
     * out text, preceded by enough ' ' characters to make sure that the printint out
     * takes at least width characters.  */
    void printFixedWidth(String text, int width) {
        for(int i = 0; i < width - text.length(); i++) {
            System.out.print(" ");
        }
        System.out.print(text);
    }


    /* The print() function outputs the Sudoku grid to the standard output, using
     * a bit of extra formatting to make the result clearly readable. */
    public void print() {
        // Compute the number of digits necessary to print out each number in the Sudoku puzzle
        int digits = (int) Math.floor(Math.log(N) / Math.log(10)) + 1;

        // Create a dashed line to separate the boxes
        int lineLength = (digits + 1) * N + 2 * SIZE - 3;
        StringBuffer line = new StringBuffer();
        for(int lineInit = 0; lineInit < lineLength; lineInit++) {
            line.append('-');
        }
        // Go through the Grid, printing out its values separated by spaces
        for(int i = 0; i < N; i++) {
            for(int j = 0; j < N; j++) {
                printFixedWidth(String.valueOf( Grid[i][j] ), digits);
                // Print the vertical lines between boxes
                if((j < N-1) && ((j+1) % SIZE == 0)) {
                    System.out.print(" |");
                }
                System.out.print( " " );
            }
            System.out.println();

            // Print the horizontal line between boxes
            if((i < N-1) && ((i+1) % SIZE == 0)) {
                System.out.println(line.toString());
            }
        }
    }


    /* The main function reads in a Sudoku puzzle from the standard input,
     * unless a file name is provided as a run-time argument, in which case the
     * Sudoku puzzle is loaded from that file.  It then solves the puzzle, and
     * outputs the completed puzzle to the standard output. */
    public static void main(String args[]) throws Exception {
        InputStream in;
        if(args.length > 0) {
            in = new FileInputStream(args[0]);
        }
        else {
            in = System.in;

            // The first number in all Sudoku files must represent the size of the puzzle.  See
            // the example files for the file format.
            int puzzleSize = readInput(in);
            if (puzzleSize > 100 || puzzleSize < 1) {
                System.out.println("Error: The Sudoku puzzle size must be between 1 and 100.");
                System.exit(-1);
            }

            Sudoku s = new Sudoku(puzzleSize);

            // read the rest of the Sudoku puzzle
            s.read(in);

            // Solve the puzzle.  We don't currently check to verify that the puzzle can be
            // successfully completed.  You may add that check if you want to, but it is not
            // necessary.

            s.solve();

            // Print out the (hopefully completed!) puzzle
            s.print();
        }
    }
}

