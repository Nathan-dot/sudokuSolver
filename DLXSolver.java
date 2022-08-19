import java.util.*;
//https://stackoverflow.com/questions/1518335/the-dancing-links-algorithm-an-explanation-that-is-less-explanatory-but-more-o


public class DLXSolver {

    public static int[][] solve(int[][] grid) {
        List<DancingNode> solution = new ArrayList<>();
        ColumnNode branch = applyDLX(grid);
        boolean isSolved = solutionExists(branch, solution);
        if (isSolved) {
            return recreate(grid, solution);
        }
        return null;
    }

    public static int[][] recreate(int[][] grid, List<DancingNode> solution) {
        int len = grid.length;
        int[][] solved = new int[len][len];
        for (int i = 0; i < solution.size(); ++i) {
            int[] posVal = getLeftDancingNode(solution.get(i), len);
            solved[posVal[0]][posVal[1]] = posVal[2];
        }

        for (int row = 0; row < len; ++row) {
            for (int col = 0; col < len; ++col) {
                if (grid[row][col] >= 1) {
                    solved[row][col] = grid[row][col];
                }
            }
        }
        return solved;
    }

    public static boolean solutionExists(ColumnNode branch, List<DancingNode> solution) {
        if (branch.right != branch) {
            ColumnNode newBranch = selectColumnNode(branch);
            newBranch.proceed();
            for (DancingNode row = newBranch.down; row != newBranch; row = row.down) {
                solution.add(row);
                for (DancingNode constraint = row.right; constraint != row; constraint = constraint.right) {
                    constraint.column.proceed();
                }

                boolean reachedSolution = solutionExists(branch, solution);
                if (reachedSolution) {
                    return true;
                }
                // Must backtrack as no solutions reached
                solution.remove(solution.size() - 1);
                for (DancingNode constraint = row.right; constraint != row; constraint = constraint.right) {
                    constraint.column.backtrack();
                }
            }
            newBranch.backtrack();
            return false;
        }
        return true;
    }

    public static ColumnNode selectColumnNode(ColumnNode branch) {
        ColumnNode selected = null;
        int min = Integer.MAX_VALUE;
        for (ColumnNode node = branch.right.column; node != branch; node = node.right.column) {
            if (node.size >= min) {
                continue;
            }
            selected = node;
            min = node.size;
        }
        return selected;
    }

    public static ColumnNode applyDLX(int[][] grid) {
        int[][] sudokuMatrix = createSudokuMatrix(grid);
        int n = grid.length;
        int count = 0;
        ColumnNode branch = new ColumnNode(-1);
        List<ColumnNode> columns = new ArrayList<>(4 * n * n);
        while (count < sudokuMatrix.length) {
            ColumnNode node = new ColumnNode(count);
            branch = branch.createRightNeighbour(node).column;
            columns.add(node);
            ++count;
        }
        branch = branch.right.column;
        for (int[] constraints : sudokuMatrix) {
            DancingNode prev = null;
            for (int i = 0; i < constraints.length; ++i) {
                if (constraints[i] != 1) {
                    continue;
                }
                ColumnNode a = columns.get(i);
                DancingNode node = new DancingNode(a);
                a.up.createDownNeighbour(node);
                ++a.size;
                if (prev == null) {
                    prev = node;
                }
                prev = prev.createRightNeighbour(node);
            }
        }
        for (ColumnNode a : columns) {
            if (a.size == 0) {
                a.proceed();
            }
        }
        return branch;
    }

    public static int[][] createSudokuMatrix(int[][] grid) {
        int len = grid.length;
        // 4 * n * n: each row needs every value (n * n)
        //+ each col needs every value (n * n)
        //+ each subgrid needs every value (n * n)
        //+ each cell needs to be occupied (n * n)
        int[][] sudokuMatrix = new int[len * len * len][4 * len * len];
        initSudokuMatrix(sudokuMatrix, len);
        // remove constraints that are already satisfied by the initial board
        // and remove all val x pos combinations that satisfy that constraint (as they cannot exist anymore)
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] <= 0) {
                    continue;
                }
                int val = grid[row][col];
                int index = (row * len * len) + (col * len) + (val - 1);
                // loop through all 'headers'/constraints to find all constraints this val x pos combination satisfies
                for (int branch = 0; branch < 4 * len * len; ++branch) {
                    if (sudokuMatrix[index][branch] != 1) continue;  // val pos combination does not satisfy constraint
                    // found a constraint that is satisfied by this val x pos combination
                    // remove all positions that satisfy this constraint
                    for (int i = 0; i < len * len * len; i++) {
                        if (i == index) continue;   // we need this row for later to find all the other constraints
                        if (sudokuMatrix[i][branch] != 1) continue;
                        for (int j = 0; j < 4 * len * len; j++) {
                            sudokuMatrix[i][j] = 0;
                        }
                    }
                    sudokuMatrix[index][branch] = 0;
                }
            }
        }

        return sudokuMatrix;
    }

    // cover matrix headers are cells -> rows -> cols -> subgrids
    public static void initSudokuMatrix(int[][] cover, int len) {
        int SUBGRID_SIZE = (int) Math.round(Math.sqrt(len));
        for (int row = 0; row < len; ++row) {
            for (int col = 0; col < len; ++col) {
                for (int val = 0; val < len; val++) {
                    int index = (row * len * len) + (col * len) + (val);
                    // cell constraint, 1 value in a cell satisfies its cellconstraint
                    cover[index][(row * len) + col] = 1;
                    // row constraint, 1 value satisfies a single value x row constraint
                    cover[index][len * len + (row * len) + val] = 1;
                    // col constraint, 1 value satisfies a single value x col constraint
                    cover[index][2 * len * len + (col * len) + val] = 1;
                    // subgrid constraint, 1 value satisfies a single value for a subgrid
                    // subgrid is defined as a values [0-n) from left to right, top to bottom
                    int subgrid = ((row/SUBGRID_SIZE)*SUBGRID_SIZE) + (col/SUBGRID_SIZE);
                    cover[index][3 * len * len + (subgrid * len) + val] = 1;
                }
            }
        }
    }

    public static int[] getLeftDancingNode(DancingNode node, int len) {
        // get utmost left DancingNode, as its column is the cell constraint with which we can get its position
        DancingNode cellConstraint = node;
        for (DancingNode a = node.left; a != node; a = a.left) {
            if (a.column.id >= cellConstraint.column.id) {
                continue;
            }
            cellConstraint = a;
        }
        int row = cellConstraint.column.id / len;
        int col = cellConstraint.column.id % len;
        // the DancingNode right of cellconstraint is in its row constraint column, whose id = n*n + row*n + val
        int val = (cellConstraint.right.column.id - len * len) % (len + 1);
        return new int[] {row, col, val};
    }
}

class DancingNode {
    public DancingNode up, down, left, right;
    public ColumnNode column;

    public DancingNode() {
        up = down = left = right = this;
    }
    public DancingNode(ColumnNode a) {
        this();
        this.column = a;
    }

    public DancingNode createDownNeighbour(DancingNode node) {
        node.down = this.down;
        node.down.up = node;
        node.up = this;
        this.down = node;
        return node;
    }

    public DancingNode createRightNeighbour(DancingNode node) {
        node.right = this.right;
        node.right.left = node;
        node.left = this;
        this.right = node;
        return node;
    }

    public void removeTopBottom() {
        this.up.down = this.down;
        this.down.up = this.up;
    }
    public void reinsertTopBottom() {
        this.up.down = this;
        this.down.up = this;
    }

    public void removeLeftRight() {
        this.left.right = this.right;
        this.right.left = this.left;
    }
    public void reinsertLeftRight() {
        this.left.right = this;
        this.right.left = this;
    }
}

class ColumnNode extends DancingNode {
    public int id;
    public int size;

    public ColumnNode(int id) {
        super();
        this.id = id;
        this.size = 0;
        this.column = this;
    }

    public void proceed() {
        removeLeftRight();
        for (DancingNode i = this.down; i != this; i = i.down) {
            for (DancingNode j = i.right; j != i; j = j.right) {
                j.removeTopBottom();
                --j.column.size;
            }
        }
    }
    public void backtrack() {
        for (DancingNode i = this.down; i != this; i = i.down) {
            for (DancingNode j = i.right; j != i; j = j.right) {
                j.reinsertTopBottom();
                ++j.column.size;
            }
        }
        reinsertLeftRight();
    }
}