import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

/**
 * This class represents the Minesweeper game with a GUI.
 */
public class MinesweeperGame extends JFrame {
    private int rows;
    private int cols;
    private int numMines;
    private JButton[][] buttons;
    private boolean[][] mines;
    private boolean[][] revealed;
    private boolean[][] marked;
    private int numRevealed;
    private int numMarked;
    private boolean gameOver;
    private Timer timer;
    private int seconds;

    /**
     * Constructor to initialize the Minesweeper game.
     *
     * @param rows      The number of rows in the game board.
     * @param cols      The number of columns in the game board.
     * @param numMines  The number of mines in the game.
     */
    public MinesweeperGame(int rows, int cols, int numMines) {
        this.rows = rows;
        this.cols = cols;
        this.numMines = numMines;
        this.buttons = new JButton[rows][cols];
        this.mines = new boolean[rows][cols];
        this.revealed = new boolean[rows][cols];
        this.marked = new boolean[rows][cols];
        this.numRevealed = 0;
        this.numMarked = 0;
        this.gameOver = false;
        this.seconds = 0;

        // Set up the game board
        JPanel boardPanel = new JPanel(new GridLayout(rows, cols));
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(30, 30));
                button.addMouseListener(new ButtonClickListener(i, j));
                buttons[i][j] = button;
                boardPanel.add(button);
            }
        }

        // Set up the timer
        JLabel timerLabel = new JLabel("Time: 0 seconds");
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                seconds++;
                timerLabel.setText("Time: " + seconds + " seconds");
                if (isGameLost()) {
                    gameOver = true;
                    explodeMines();
                    timer.stop();
                }
            }
        });

        // Set up the main frame
        setTitle("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(boardPanel, BorderLayout.CENTER);
        add(timerLabel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // Start the timer
        timer.start();

        // Place the mines randomly
        placeMines();
    }

    /**
     * Places the mines randomly on the game board.
     */
    private void placeMines() {
        Random random = new Random();
        int count = 0;
        while (count < numMines) {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);
            if (!mines[row][col]) {
                mines[row][col] = true;
                count++;
            }
        }
    }

    /**
     * Checks if the game is lost.
     *
     * @return Returns true if the game is lost, false otherwise.
     */
    private boolean isGameLost() {
        if (rows == 6 && cols == 9 && seconds >= 60) {
            return true;
        }
        if (rows == 12 && cols == 18 && seconds >= 180) {
            return true;
        }
        if (rows == 21 && cols == 26 && seconds >= 660) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the game is won.
     *
     * @return Returns true if the game is won, false otherwise.
     */
    private boolean isGameWon() {
        int numCells = rows * cols;
        int numSafeCells = numCells - numMines;
        return numRevealed == numSafeCells;
    }

    /**
     * Reveals the cell at the specified position.
     *
     * @param row The row index of the cell.
     * @param col The column index of the cell.
     */
    private void revealCell(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols || revealed[row][col] || marked[row][col]) {
            return;
        }

        revealed[row][col] = true;
        numRevealed++;
        buttons[row][col].setEnabled(false);

        if (mines[row][col]) {
            gameOver = true;
            explodeMines();
            timer.stop();
            return;
        }

        if (isGameWon()) {
            gameOver = true;
            timer.stop();
            JOptionPane.showMessageDialog(this, "Congratulations! You won the game!");
            return;
        }

        int numAdjacentMines = countAdjacentMines(row, col);
        if (numAdjacentMines > 0) {
            buttons[row][col].setText(Integer.toString(numAdjacentMines));
        } else {
            revealCell(row - 1, col - 1);
            revealCell(row - 1, col);
            revealCell(row - 1, col + 1);
            revealCell(row, col - 1);
            revealCell(row, col + 1);
            revealCell(row + 1, col - 1);
            revealCell(row + 1, col);
            revealCell(row + 1, col + 1);
        }
    }

    /**
     * Counts the number of adjacent mines to the specified cell.
     *
     * @param row The row index of the cell.
     * @param col The column index of the cell.
     * @return Returns the number of adjacent mines.
     */
    private int countAdjacentMines(int row, int col) {
        int count = 0;
        if (row > 0 && col > 0 && mines[row - 1][col - 1]) count++;
        if (row > 0 && mines[row - 1][col]) count++;
        if (row > 0 && col < cols - 1 && mines[row - 1][col + 1]) count++;
        if (col > 0 && mines[row][col - 1]) count++;
        if (col < cols - 1 && mines[row][col + 1]) count++;
        if (row < rows - 1 && col > 0 && mines[row + 1][col - 1]) count++;
        if (row < rows - 1 && mines[row + 1][col]) count++;
        if (row < rows - 1 && col < cols - 1 && mines[row + 1][col + 1]) count++;
        return count;
    }

    /**
     * Marks or unmarks the cell as a mine at the specified position.
     *
     * @param row The row index of the cell.
     * @param col The column index of the cell.
     */
    private void markCell(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols || revealed[row][col]) {
            return;
        }

        if (!marked[row][col]) {
            marked[row][col] = true;
            numMarked++;
            buttons[row][col].setText("X");
            buttons[row][col].setForeground(Color.RED);
        } else {
            marked[row][col] = false;
            numMarked--;
            buttons[row][col].setText("");
        }
    }

    /**
     * Explodes all the mines on the game board.
     */
    private void explodeMines() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (mines[i][j] && !marked[i][j]) {
                    buttons[i][j].setText("X");
                    buttons[i][j].setForeground(Color.RED);
                }
            }
        }
    }

    /**
     * ActionListener for the game board buttons.
     */
    private class ButtonClickListener extends MouseAdapter {
        private int row;
        private int col;

        public ButtonClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (gameOver) {
                return;
            }

            if (SwingUtilities.isLeftMouseButton(e)) {
                revealCell(row, col);
            } else if (SwingUtilities.isRightMouseButton(e)) {
                if (e.getClickCount() == 1) {
                    markCell(row, col);
                } else if (e.getClickCount() == 2 && revealed[row][col] && countAdjacentMines(row, col) > 0) {
                    autoRevealAdjacentCells(row, col);
                }
            }
        }
    }

    /**
     * Automatically reveals all the adjacent cells that are supposed to not have any mines.
     *
     * @param row The row index of the cell.
     * @param col The column index of the cell.
     */
    private void autoRevealAdjacentCells(int row, int col) {
        int numAdjacentMines = countAdjacentMines(row, col);
        int numAdjacentMarked = countAdjacentMarked(row, col);
        if (numAdjacentMarked == numAdjacentMines) {
            revealCell(row - 1, col - 1);
            revealCell(row - 1, col);
            revealCell(row - 1, col + 1);
            revealCell(row, col - 1);
            revealCell(row, col + 1);
            revealCell(row + 1, col - 1);
            revealCell(row + 1, col);
            revealCell(row + 1, col + 1);
        }
    }

    /**
     * Counts the number of adjacent cells that are marked as mines.
     *
     * @param row The row index of the cell.
     * @param col The column index of the cell.
     * @return Returns the number of adjacent marked cells.
     */
    private int countAdjacentMarked(int row, int col) {
        int count = 0;
        if (row > 0 && col > 0 && marked[row - 1][col - 1]) count++;
        if (row > 0 && marked[row - 1][col]) count++;
        if (row > 0 && col < cols - 1 && marked[row - 1][col + 1]) count++;
        if (col > 0 && marked[row][col - 1]) count++;
        if (col < cols - 1 && marked[row][col + 1]) count++;
        if (row < rows - 1 && col > 0 && marked[row + 1][col - 1]) count++;
        if (row < rows - 1 && marked[row + 1][col]) count++;
        if (row < rows - 1 && col < cols - 1 && marked[row + 1][col + 1]) count++;
        return count;
    }

    /**
     * Main method to start the Minesweeper game.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Prompt the user to choose the game difficulty
                String[] options = {"Beginner", "Intermediate", "Advanced"};
                int choice = JOptionPane.showOptionDialog(null, "Choose the game difficulty", "Minesweeper", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                // Set the board size and number of mines based on the user's choice
                int rows, cols, numMines;
                if (choice == 0) {
                    rows = 6;
                    cols = 9;
                    numMines = 11;
                } else if (choice == 1) {
                    rows = 12;
                    cols = 18;
                    numMines = 36;
                } else {
                    rows = 21;
                    cols = 26;
                    numMines = 92;
                }

                // Create an instance of the Minesweeper game
                new MinesweeperGame(rows, cols, numMines);
            }
        });
    }
}