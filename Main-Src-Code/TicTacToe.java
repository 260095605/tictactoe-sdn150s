import javax.swing.*; //All the Swing GUI Classes:JFrame,JOptionPane,JButton..etc
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent; // the object Java gives us whem a button is clicked(it holds info about the click)
import java.awt.event.ActionListener; // for the code that runs when the button is clicked
import java.util.ArrayList;// A list that can grow in size(used to collect the empty squares)
import java.util.Locale;
import java.util.Random; //Allows us ot pick a random number(used by a computer)

public class TicTacToe {// the class our whole game lives inside this one class called TicTacToe.
    // ---------- Components from the GUI designer ----------
    private JPanel panel1;
    private JRadioButton twoPlayerRadio;
    private JRadioButton VsComputer;
    private JLabel statusLabel;
    private JLabel resultsLabel;
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JButton button4;
    private JButton button5;
    private JButton button6;
    private JButton button7;
    private JButton button8;
    private JButton button9;
    private JComboBox difficultycomboBox1;
    private JLabel scoreLabel1;
    private JButton newGameButton1;
    private JButton resetScoreButton1;

    // ---------- Game variables ----------
    //These store information about the game while it si being played
    private JButton[] buttons = new JButton[9]; //An array that will hold the 9 board button in oder(index 0 to 8)
    private String[] board = new String[9];
    private String currentPlayer = "X"; //dtermines who's turn it is,Player X always start
    private boolean gameActive = true; // true = game is running & false= game is over

    // Score counters (they are only reset by the Reset Score button)
    private int scoreX = 0; // how many games has X won
    private int scoreO = 0; // how many games has O or computer won
    private int scoreTies = 0; // how many games ended in a drw

    // The 8 winning lines: rows, columns, diagonals
    private int[][] winningLines = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // 3 horizontal rows
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // 3 vertical columns
            {0, 4, 8}, {2, 4, 6} // 2 diagonals
    };

    // ---------- Constructor ----------
    public TicTacToe() {
        // Fill the difficulty drop-down
        difficultycomboBox1.addItem("Easy");
        difficultycomboBox1.addItem("Medium");
        difficultycomboBox1.addItem("Hard");

        // A ButtonGroup makes radiobutton work together:when one is selected the other one is automatically unselected
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(twoPlayerRadio);// add firdt radio button on the group
        modeGroup.add(VsComputer); // add second radio bbutton on the gruop
        twoPlayerRadio.setSelected(true);//starts the game in twopalyer mode
        difficultycomboBox1.setEnabled(false);//Difficulty is only needed against the computer so it will be grey(not active)

        // Puting the 9 buttons in an array (index 0 = top left, index 8 = bottom right)
        buttons[0] = button1;
        buttons[1] = button2;
        buttons[2] = button3;
        buttons[3] = button4;
        buttons[4] = button5;
        buttons[5] = button6;
        buttons[6] = button7;
        buttons[7] = button8;
        buttons[8] = button9;

        // Each board button calls cellClicked with its position in the buttons array
        //When an array is clicked the actionperfomed runs
        ActionListener cellListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < 9; i++) {
                    if (e.getSource() == buttons[i]) { // this here tells us which button was clicked
                        cellClicked(i);
                    }
                }
            }
        };
        for (int i = 0; i < 9; i++) { //Attach the shares listner to each of the 9 buttons
            buttons[i].addActionListener(cellListener);
        }

        // Two-Player Mode selected
        twoPlayerRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                difficultycomboBox1.setEnabled(false); // no computer so the dificulty box will be grey
                initializeGame();//start a frash board for the new mode
            }
        });

        // AI Mode selected
        VsComputer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                difficultycomboBox1.setEnabled(true);// the user can now choose levels of difficulty
                initializeGame();// start a frsh board for the new mode
            }
        });

        // New Game button
        newGameButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initializeGame(); //clear the board but scores are kept
            }
        });

        // Reset Score button
        resetScoreButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scoreX = 0;         //set all 3 counters back to zero
                scoreO = 0;
                scoreTies = 0;
                updateScoreboard(); //show zero on the screen
            }
        });

        initializeGame();
    }

    // ---------- PHASE 1: Setting the stage ----------
    private void initializeGame() {
        // Empty board
        for (int i = 0; i < 9; i++) {
            board[i] = "";      //empty the memory of the boxx
            buttons[i].setText(""); //Remove the X and O on the board
            buttons[i].setEnabled(true); //make the button clikable again
        }

        currentPlayer = "X"; //makes X to always goes first
        gameActive = true; //for making the game to be running again

        updateScoreboard();  //rfesh the score Text
        statusLabel.setText("Current Turn: Player X"); //Tell player who's turn it is
        if (resultsLabel != null) {
            resultsLabel.setText("Status: Active");
        }
    }

    // ---------- PHASE 2: Handling a move ----------
    private void cellClicked(int cellIndex) {
        // Ignore clicks if the game is over
        if (!gameActive) {
            return;
        }

        // Square already taken, warn the player and stop
        if (!board[cellIndex].equals("")) {
            JOptionPane.showMessageDialog(null, "Square is occupied. Choose another.");
            return;
        }

        // Place the current player's token
        board[cellIndex] = currentPlayer; //saves the move in the board memory
        buttons[cellIndex].setText(currentPlayer); //shows the X or O button

        checkGameStatus(); //check if the move won the game,drew the game or if the play continues

        // If AI mode is selected, the game is still going and it is the computer's turn (O)
        if (gameActive && VsComputer.isSelected() && currentPlayer.equals("O")) {
            computerMove();
        }
    }

    // ---------- PHASE 3: Checking the game status ----------
    private void checkGameStatus() {
        // Step 1: did the current player win?
        if (checkWinCondition(currentPlayer)) {
            gameActive = false; //stop then stops the game
            // This will add one point to the winner
            if (currentPlayer.equals("X")) {
                scoreX++;
            } else {
                scoreO++;
            }
            //This below here decide which name to show for the winner
            String winnerName = "Player " + currentPlayer;
            if (VsComputer.isSelected() && currentPlayer.equals("O")) {
                winnerName = "Computer"; //in AI mode O is thw winner
            }

            statusLabel.setText(winnerName + " Wins!");//Shows winner on the screen
            if (resultsLabel != null) {
                resultsLabel.setText("Status: Finished");
            }
            JOptionPane.showMessageDialog(null, winnerName + " Wins!");//pops up a message for a winner
            handleGameOver(); // update the score and ask the user if they want to play again
            return; //it stops here nothing to check anymore
        }

        // Step 2: is it a draw (when all squares are full and no winner )
        if (boardIsFull()) {
            gameActive = false; //This will stop the game
            scoreTies++; //Add point to number of draws currently available

            statusLabel.setText("It's a Draw / Tie!");
            if (resultsLabel != null) {
                resultsLabel.setText("Status: Finished");
            }
            JOptionPane.showMessageDialog(null, "It's a Draw / Tie!");
            handleGameOver();
            return;
        }

        // Step 3: the game continues, change the player
        if (currentPlayer.equals("X")) {
            currentPlayer = "O";
        } else {
            currentPlayer = "X";
        }
        statusLabel.setText("Current Turn: Player " + currentPlayer); //show who's turn is next
    }

    // Checks if the given player(X or O) has 3 in a row,returns true if they do if not false
    private boolean checkWinCondition(String player) {
        for (int[] line : winningLines) { //This looks at each of the 8 winning lines
            if (board[line[0]].equals(player) //if all 3 squares of the lines belong to a player thay have won
                    && board[line[1]].equals(player)
                    && board[line[2]].equals(player)) {
                return true; //if found the winner then we are done returning true
            }
        }
        return false; //here we checked for all 8 lines none was compled ,retuens false
    }

    // True when no empty squares are left(checking if every square is taken or not)
    private boolean boardIsFull() {
        for (int i = 0; i < 9; i++) {
            if (board[i].equals("")) { //if there is an an empty square
                return false; //This means the board is not full
            }
        }
        return true; //so this part here means no empty square were found
    }

    // ---------- PHASE 4: Game over and playing again ----------
    private void handleGameOver() {
        // Show the updated scores
        updateScoreboard();

        // Ask if the user wants to play again
        int answer = JOptionPane.showConfirmDialog(null, "Do you want to play again?",
                "Play Again", JOptionPane.YES_NO_OPTION);

        if (answer == JOptionPane.YES_OPTION) {
            initializeGame();// if yes start a anew round
        } else { //all the 9 buttons will be disabled so nothing can be clicked
            // Disable the game board (New Game will turn it back on)
            for (int i = 0; i < 9; i++) {
                buttons[i].setEnabled(false);
            }
        }
    }

    private void updateScoreboard() { //Writes the current score on the screen on the score label
        if (scoreLabel1 != null) {
            scoreLabel1.setText("Score X: " + scoreX + "  |  Score O: " + scoreO + "  |  Ties: " + scoreTies);
        }
    }

    // ---------- Computer player (O) ----------
    private void computerMove() {
        String level = (String) difficultycomboBox1.getSelectedItem(); // this reads which difficuly level is selected
        int move = -1; //This the square the computer will choose,-1 means none choosen yet

        if (level.equals("Easy")) {
            // Easy: any empty square
            move = randomMove();
        } else if (level.equals("Medium")) {
            // Medium: win if possible, otherwise block X, otherwise random
            move = findWinningSquare("O");
            if (move == -1) {
                move = findWinningSquare("X");
            }
            if (move == -1) {
                move = randomMove();
            }
        } else {
            // Hard: win, block, take the centre, otherwise random
            move = findWinningSquare("O");
            if (move == -1) {
                move = findWinningSquare("X");
            }
            if (move == -1 && board[4].equals("")) {
                move = 4;
            }
            if (move == -1) {
                move = randomMove();
            }
        }

        // now this place the computer's token on the chosen square
        board[move] = "O";
        buttons[move].setText("O");
        checkGameStatus(); // this checks if the computer won or drew or pass the turn to X
    }

    // Finds an empty square that would give the player 3 in a row (or -1 if none)
    private int findWinningSquare(String player) {
        for (int[] line : winningLines) { //checks each of the 8 lines
            int count = 0;
            int emptySquare = -1;

            for (int i = 0; i < 3; i++) { //lokks at the 3 squares of the line
                if (board[line[i]].equals(player)) {
                    count++;
                } else if (board[line[i]].equals("")) {
                    emptySquare = line[i]; //this rembers where the empty square is
                }
            }

            if (count == 2 && emptySquare != -1) {
                return emptySquare;
            }
        }
        return -1;
    }

    // Picks a random empty square
    private int randomMove() {
        ArrayList<Integer> emptySquares = new ArrayList<Integer>();
        for (int i = 0; i < 9; i++) {
            if (board[i].equals("")) {
                emptySquares.add(i);
            }
        }
        Random random = new Random();
        return emptySquares.get(random.nextInt(emptySquares.size()));
    }

    // The button names in the designer are not in board order (for example the top row
    // is button1, button3, button7), so we sort the buttons by where they are on screen:
    // top to bottom, then left to right. This makes index 0 the top-left square
    // and index 8 the bottom-right square, which the winning lines rely on.
    private void arrangeButtonsByPosition() {
        for (int i = 0; i < 8; i++) {
            for (int j = i + 1; j < 9; j++) {
                int yI = SwingUtilities.convertPoint(buttons[i].getParent(), buttons[i].getLocation(), panel1).y;
                int xI = SwingUtilities.convertPoint(buttons[i].getParent(), buttons[i].getLocation(), panel1).x;
                int yJ = SwingUtilities.convertPoint(buttons[j].getParent(), buttons[j].getLocation(), panel1).y;
                int xJ = SwingUtilities.convertPoint(buttons[j].getParent(), buttons[j].getLocation(), panel1).x;

                // swap if button j should come before button i
                if (yJ < yI || (yJ == yI && xJ < xI)) {
                    JButton temp = buttons[i];
                    buttons[i] = buttons[j];
                    buttons[j] = temp;
                }
            }
        }
    }

    // ---------- Program entry point ----------
    public static void main(String[] args) {
        JFrame frame = new JFrame("Tic-Tac-Toe Game"); //this creates a window witha a title
        TicTacToe gameUI = new TicTacToe(); // creates the game(this runs the constructer we created ealier )
        JLabel background = new JLabel(new ImageIcon("background.png"));
        background.setLayout(new BorderLayout());
        background.add(gameUI.panel1, BorderLayout.CENTER);
        frame.setContentPane(background);
        frame.setSize(500, 680);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true); //shows the window

        // Buttons only have their real positions once the window is showing,so we sort them now
        frame.validate();
        gameUI.arrangeButtonsByPosition();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        panel1.setBackground(new Color(-2565150));
        panel1.setOpaque(false);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.setBackground(new Color(-1185046));
        panel2.setOpaque(false);
        panel1.add(panel2, BorderLayout.CENTER);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(7, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel3.setBackground(new Color(-1186341));
        panel3.setOpaque(false);
        panel2.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel4.setBackground(new Color(-1186341));
        panel4.setOpaque(false);
        panel3.add(panel4, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 2, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel5.setOpaque(false);
        panel4.add(panel5, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder(null, "Game Mode", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.TOP, this.$$$getFont$$$("Bookshelf Symbol 7", -1, -1, panel5.getFont()), new Color(-14540254)));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel6.setOpaque(false);
        panel5.add(panel6, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        panel5.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(panel7, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setOpaque(false);
        label1.setText("AI Difficulty");
        panel7.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel8.setOpaque(false);
        panel5.add(panel8, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        VsComputer = new JRadioButton();
        VsComputer.setOpaque(false);
        VsComputer.setText("AI Mode");
        panel5.add(VsComputer, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        difficultycomboBox1 = new JComboBox();
        difficultycomboBox1.setOpaque(false);
        panel5.add(difficultycomboBox1, new com.intellij.uiDesigner.core.GridConstraints(2, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        twoPlayerRadio = new JRadioButton();
        twoPlayerRadio.setOpaque(false);
        twoPlayerRadio.setText("Two-Player Mode");
        panel5.add(twoPlayerRadio, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        panel4.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(2, 3, 2, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel9.setOpaque(false);
        panel4.add(panel9, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$(null, Font.BOLD, 36, label2.getFont());
        if (label2Font != null) label2.setFont(label2Font);
        label2.setOpaque(false);
        label2.setText("Tic-Tac-Toe Game");
        panel9.add(label2, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel10.setOpaque(false);
        panel4.add(panel10, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel10.setBorder(BorderFactory.createTitledBorder(null, "Scoreboard", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.TOP, this.$$$getFont$$$("Bookshelf Symbol 7", -1, -1, panel10.getFont()), new Color(-14540254)));
        resultsLabel = new JLabel();
        resultsLabel.setText("Results: Player X wins!"); //NON-NLS
        panel10.add(resultsLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(154, 21), null, 0, false));
        scoreLabel1 = new JLabel();
        scoreLabel1.setText("Score: ");
        panel10.add(scoreLabel1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        statusLabel = new JLabel();
        statusLabel.setText("Current Turn: Player X");
        panel4.add(statusLabel, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel11.setBackground(new Color(-1186341));
        panel11.setOpaque(false);
        panel3.add(panel11, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 4, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel12.setBackground(new Color(-1186341));
        panel12.setOpaque(false);
        panel3.add(panel12, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        button1 = new JButton();
        Font button1Font = this.$$$getFont$$$(null, Font.BOLD, 36, button1.getFont());
        if (button1Font != null) button1.setFont(button1Font);
        button1.setText("");
        panel12.add(button1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, 100), null, 0, false));
        button2 = new JButton();
        Font button2Font = this.$$$getFont$$$(null, Font.BOLD, 36, button2.getFont());
        if (button2Font != null) button2.setFont(button2Font);
        button2.setText("");
        panel12.add(button2, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, 100), null, 0, false));
        button3 = new JButton();
        Font button3Font = this.$$$getFont$$$(null, Font.BOLD, 36, button3.getFont());
        if (button3Font != null) button3.setFont(button3Font);
        button3.setText("");
        panel12.add(button3, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, 100), null, 0, false));
        button4 = new JButton();
        Font button4Font = this.$$$getFont$$$(null, Font.BOLD, 36, button4.getFont());
        if (button4Font != null) button4.setFont(button4Font);
        button4.setText("");
        panel12.add(button4, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, 100), null, 0, false));
        button5 = new JButton();
        Font button5Font = this.$$$getFont$$$(null, Font.BOLD, 36, button5.getFont());
        if (button5Font != null) button5.setFont(button5Font);
        button5.setText("");
        panel12.add(button5, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, 100), null, 0, false));
        button6 = new JButton();
        Font button6Font = this.$$$getFont$$$(null, Font.BOLD, 36, button6.getFont());
        if (button6Font != null) button6.setFont(button6Font);
        button6.setText("");
        panel12.add(button6, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, 100), null, 0, false));
        button7 = new JButton();
        Font button7Font = this.$$$getFont$$$(null, Font.BOLD, 36, button7.getFont());
        if (button7Font != null) button7.setFont(button7Font);
        button7.setText("");
        panel12.add(button7, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, 100), null, 0, false));
        button8 = new JButton();
        Font button8Font = this.$$$getFont$$$(null, Font.BOLD, 36, button8.getFont());
        if (button8Font != null) button8.setFont(button8Font);
        button8.setText("");
        panel12.add(button8, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, 100), null, 0, false));
        button9 = new JButton();
        Font button9Font = this.$$$getFont$$$(null, Font.BOLD, 36, button9.getFont());
        if (button9Font != null) button9.setFont(button9Font);
        button9.setText("");
        panel12.add(button9, new com.intellij.uiDesigner.core.GridConstraints(2, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, 100), null, 0, false));
        final JPanel panel13 = new JPanel();
        panel13.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel13.setOpaque(false);
        panel3.add(panel13, new com.intellij.uiDesigner.core.GridConstraints(6, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        newGameButton1 = new JButton();
        newGameButton1.setText("New Game");
        panel13.add(newGameButton1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        resetScoreButton1 = new JButton();
        resetScoreButton1.setText("Reset Score");
        panel13.add(resetScoreButton1, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel14 = new JPanel();
        panel14.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel14.setOpaque(false);
        panel14.setVisible(true);
        panel3.add(panel14, new com.intellij.uiDesigner.core.GridConstraints(6, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setNextFocusableComponent(panel8);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

}

