// Daniel Sims
// CSEN 160
// Java Game Project

// description: this project serves as an addition to the original snake game. added to it is a unique feature of
// color variation within the game. the player must match the correct color to one of the many different apples on
// the board in order to grow the snake in length. if it eats the wrong color or collides with itself or its
// environment, then the game ends. the implementation of not only different colored apples, but multiple apples on
// the board adds complexity, strategy, and difficulty to the game with different paths and more natural barriers.
// also implemented is a nicer UI and different menus for the player to navigate through in order for a better
// playing experience.

package snakegame;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
	// the tile class represents the squares on the grid, in which apples and segments of the snake will be positioned
	// this is done so by specifying x and y coordinates, with respect to pixels on the screen
    private class Tile {
        int x;
        int y;
        Color color;
        
        // tiles are also given color in addition to their coordinates to create the food
        Tile(int x, int y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }
    
    private Tile lastEatenFood = null; // specifies the color of the last apple eaten

    int boardWidth;
    int boardHeight;
    int tileSize = 25; // in pixels

    // a single tile for the head, and an ArrayList for both the body and food, because there are multiple placed throughout the grid
    Tile snakeHead;
    ArrayList<Tile> snakeBody;
    ArrayList<Tile> apples;
    
    // colors for each can be changed to the user's liking, but must match in order when comparing the two during collisions
    Color[] foodColors = {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA};
    Color[] snakeColors = {Color.GREEN, Color.RED, Color.YELLOW, Color.ORANGE, Color.BLUE, Color.MAGENTA};
    int maxFoodCount = 50; // can change the amount of food that spawns on the grid at will
    Random random;

    int velocityX;
    int velocityY;
    Timer gameLoop; // timer for the main game loop, defines how the game is run
    
    // dictates different variables in the game environment
    boolean gameOver = false;
    boolean gameStarted = false;
    boolean firstAppleEaten = false;
    boolean gamePaused = false;

    boolean isTimedMode = false;  // determines if the game is in timed mode depending on the selection from the menu
    int timeLimit = 30;           // default time limit
    int remainingTime;            
    Timer countdownTimer;         

    // this is the main constructor to initialize the game's parameters
    public SnakeGame(int boardWidth, int boardHeight, boolean isTimedMode, int timeLimit) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.isTimedMode = isTimedMode;
        this.timeLimit = timeLimit;  // set the time limit from the parameter
        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
        setBackground(Color.black);
        addKeyListener(this); // this is the keyboard listener, used to track inputs to move the snake
        setFocusable(true);

        snakeHead = new Tile(5, 5, Color.WHITE); // the position of the snake's head will always start on (5, 5), but this can be changed
        snakeBody = new ArrayList<>();
        apples = new ArrayList<>();
        random = new Random(); // variable to randomize the placement of the apples at initialization

        // loop to place the amount of apples until it reaches its max specified
        for (int i = 0; i < maxFoodCount; i++) {
            placeFood();
        }

        // velocity can be specified in both left and right directions, as well as up and down (depending on the sign of the value)
        velocityX = 1; // velocity is set to the right on button press (unless started with arrow press)
        velocityY = 0;
        gameLoop = new Timer(100, this); // sets the time for the game loop (100ms), meaning the game will update this quickly
        							     // you can change this value, but i personally haven't wanted to mess with it
        
        // starts the countdown for "timed" mode
        if (isTimedMode) {
            remainingTime = timeLimit;  // sets remaining time to the selected time limit
            countdownTimer = new Timer(1000, e -> updateCountdown()); // needed to update the timer every second
        }
    }

    // function to update the timer each second
    private void updateCountdown() {
        if (!gamePaused && remainingTime > 0) { // if the pause menu is opened, pauses the timer
            remainingTime--;
            repaint(); // needed to visualize the timer stopping
        }

        if (remainingTime <= 0) { // triggers the game over screen once it reaches zero
            gameOver = true; // sets the game's state to be "game over"
            countdownTimer.stop();
            showGameOverPopup();
        }
    }
    
    // the purpose of this function is to draw whatever visuals need to be on screen at the moment, meaning that this function will continuously be called throughout runtime
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g); // draws the game elements (all of them)

        g.setFont(new Font("Arial", Font.PLAIN, 16));

        if (gameOver) {
            g.setColor(Color.red);
            //g.drawString("Game Over. Score: " + snakeBody.size(), boardWidth / 2 - 60, boardHeight / 2);
        } else if (gameStarted) {
            g.setColor(Color.white); // displays the time remaining and score if the game is not over
            
            // only applies to timed mode
            if (isTimedMode) {
                g.drawString("Time Remaining: " + remainingTime + "s", boardWidth - 155, tileSize);
            }
            
            // displays the score
            g.drawString("Score: " + snakeBody.size(), tileSize - 16, tileSize);
        } else {
            // the start prompt when the game has initialized, and requires a key press to begin
            g.setColor(Color.white);
            g.drawString("Press any key to start", boardWidth / 2 - 80, boardHeight / 2);
        }
    }
    
    // the actual method to fill in the grid lines, as well as draw other elements on the screen during the game
    public void draw(Graphics g) {
        // loop to draw the grid lines, which will always match the window size
        for (int i = 0; i < boardWidth / tileSize; i++) {
            g.drawLine(i * tileSize, 0, i * tileSize, boardHeight);
            g.drawLine(0, i * tileSize, boardWidth, i * tileSize);
        }

        // draws the apples, given a random color
        for (Tile food : apples) {
            g.setColor(food.color);
            g.fill3DRect(food.x * tileSize, food.y * tileSize, tileSize, tileSize, true);
        }

        // snake head
        g.setColor(snakeHead.color);
        g.fill3DRect(snakeHead.x * tileSize, snakeHead.y * tileSize, tileSize, tileSize, true);

        // snake body
        for (Tile snakePart : snakeBody) {
            g.setColor(snakePart.color);
            g.fill3DRect(snakePart.x * tileSize, snakePart.y * tileSize, tileSize, tileSize, true);
        }

        // updates the drawing on the score/game over message
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        if (gameOver) {
            g.setColor(Color.red);
            g.drawString("Game Over: " + snakeBody.size(), tileSize - 16, tileSize);
        } else {
            g.setColor(Color.white);
            if (!gameStarted) {
                g.drawString("Press any key to start", boardWidth / 2 - 80, boardHeight / 2);
            } else {
                g.drawString("Score: " + snakeBody.size(), tileSize - 16, tileSize);
            }
        }
    }

    // places new apples on the grid
    public void placeFood() {
        ArrayList<Color> colorsOnBoard = new ArrayList<>();
        
        // checks to see if there are any missing apple colors on the board, then accounts for that
        // this ensures that there's always at least one of each color, and that the game cannot end by a missing color
        for (Tile food : apples) {
            if (!colorsOnBoard.contains(food.color)) {
                colorsOnBoard.add(food.color);
            }
        }

        // if it's the first apple, make sure the food is the same color as the snake
        if (!firstAppleEaten) {
            // randomizes the color of the snake after eating
            Color snakeColor = snakeColors[random.nextInt(snakeColors.length)];
            Tile newFood = generateUniqueFood(snakeColor, -1); 
            if (newFood != null) {
                apples.add(newFood);
                lastEatenFood = newFood;  // checks the position of the last eaten food (helps with food generation)
            }
        } else {
            // food generation if not the first apple
            for (Color color : foodColors) {
                if (!colorsOnBoard.contains(color)) {
                    Tile newFood = generateUniqueFood(color, snakeHead.y); 
                    if (newFood != null) {
                        apples.add(newFood);
                        lastEatenFood = newFood;
                    }
                }
            }
            
            // generation of random apple colors and positions until the max number of apples is reached
            while (apples.size() < maxFoodCount) {
                Color randomColor = foodColors[random.nextInt(foodColors.length)];
                Tile newFood = generateUniqueFood(randomColor, -1);
                if (newFood != null) {
                    apples.add(newFood);
                    lastEatenFood = newFood;
                }
            }
        }
    }

    // method generates new apples at positions that don't overlap with other apples, as well as spawn on top of apples that were just eaten
    // this is where lastEatenFood is useful, to ensure that an apple doesn't spawn right on the snake's head after it eats one
    public Tile generateUniqueFood(Color color, int restrictedRow) {
        int x, y;

        do {
            x = random.nextInt(boardWidth / tileSize);
            y = random.nextInt(boardHeight / tileSize);
        } while ((lastEatenFood != null && x == lastEatenFood.x && y == lastEatenFood.y) || y == restrictedRow);

        return new Tile(x, y, color);
    }

    // checks for collisions within the game environment, as well as change variables based on interactions during the game
    // there are multiple debug statements within this method specifically, as there were issues with color generation, game timing, and incorrect collisions as a result
    public void move() {
        for (int i = 0; i < apples.size(); i++) {
            Tile food = apples.get(i);

            // if there is a collision between snake head and apple (eaten)
            if (collision(snakeHead, food)) {
                //System.out.println("snake head at (" + snakeHead.x + ", " + snakeHead.y + ") collided with food at (" + food.x + ", " + food.y + ")");
                
            	if (!firstAppleEaten || snakeHead.color.getRGB() == food.color.getRGB()) { // compares the actual values of the two colors in the collision
                // if (!firstAppleEaten || snakeHead.color.equals(food.color)) {
                    if (!firstAppleEaten) {
                        firstAppleEaten = true;  // first apple eaten, changes collisions from now on
                    }

                    // add to the snake's body length
                    snakeBody.add(new Tile(food.x, food.y, snakeHead.color));

                    // remove eaten apple and place another
                    apples.remove(i);
                    placeFood();

                    // immediately change the snake's color after eating
                    if (firstAppleEaten) {
                        // set new color from the specified colors
                        Color newColor = snakeColors[random.nextInt(snakeColors.length)];
                        snakeHead.color = newColor;
                        // updates all snake body parts with the new color
                        for (Tile snakePart : snakeBody) {
                            snakePart.color = newColor;
                        }

                        //System.out.println("Snake color changed to: " + newColor);
                    }
                    return;  // Exit after processing the food
                } else {
                    //System.out.println("Game Over! Snake head color: " + snakeHead.color + ", Food color: " + food.color);
                    gameOver = true;
                    return;
                }
            }
        }

        // snake's body follows based on the head movements
        for (int i = snakeBody.size() - 1; i > 0; i--) {
            snakeBody.get(i).x = snakeBody.get(i - 1).x;
            snakeBody.get(i).y = snakeBody.get(i - 1).y;
        }

        if (!snakeBody.isEmpty()) {
            snakeBody.get(0).x = snakeHead.x;
            snakeBody.get(0).y = snakeHead.y;
        }

        // move the snake's head at the velocity (direction) specified by key input
        snakeHead.x += velocityX;
        snakeHead.y += velocityY;

        // check if snake collides with itself or goes out of bounds
        checkGameOver();
    }
    
    // checks to see if there is a collision between the snake's head and its environment
    public boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    // steps specified given game over conditions
    public void checkGameOver() {
        for (Tile snakePart : snakeBody) {
            if (collision(snakeHead, snakePart)) { // if the snake head collides with any part of its body
                gameOver = true;
                if (isTimedMode) countdownTimer.stop(); // stops countdown upon reaching game over
                return;
            }
        }

        // checks to see if the snake head collides with any part of the border
        if (snakeHead.x < 0 || snakeHead.x >= boardWidth / tileSize || 
            snakeHead.y < 0 || snakeHead.y >= boardHeight / tileSize) {
            gameOver = true;
            if (isTimedMode) countdownTimer.stop();
        }
    }

    // method to take in actions and "repaint" the board as a result
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver && gameStarted && !gamePaused) {
            move();
            repaint();
        } else {
            if (!gameStarted) {
                repaint();
            }
            gameLoop.stop(); // necessary to stop movement of snake, take in key inputs, and stops the timer
            if (gameOver) {
                if (isTimedMode) countdownTimer.stop();
                showGameOverPopup(); // shows the game over menu
            }
        }
    }

    // checks for key presses to start the game (game will initialize beforehand but game timer will not start)
    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameStarted && !gameOver) {
            gameStarted = true;
            gameLoop.start();

            // if in "timed" mode, the countdown timer will begin
            if (isTimedMode && countdownTimer != null) {
                countdownTimer.start();
            }
        }

        // takes in arrow key inputs to move the snake
        // must check for edge cases (like you can't move in the opposite direction as you're currently moving)
        if (e.getKeyCode() == KeyEvent.VK_UP && velocityY != 1) {
            velocityX = 0;
            velocityY = -1;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN && velocityY != -1) {
            velocityX = 0;
            velocityY = 1;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT && velocityX != 1) {
            velocityX = -1;
            velocityY = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && velocityX != -1) {
            velocityX = 1;
            velocityY = 0;
        }

        // steps taken when game is paused (escape key)
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && !gameOver) {
            gameLoop.stop(); // must occur to stop the game, as well as the timer
            if (isTimedMode && countdownTimer != null) {  // checks to see if the countdown timer exists, then pauses if it does
                countdownTimer.stop();
            }
            gameStarted = false;  // must be set to false to that in case of a restart, the game will work correctly upon key press
            showPauseMenu();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    // when "main menu" button is pressed, program starts in App.java again
    public void returnToMenu() {
        // will close the game window before heading back to the menu
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this); // specifies the opening of the window beforehand, which was the menu
        if (topFrame != null) {
            topFrame.dispose();  // closes the current game window
        }

        // return to the menu
        try {
            String[] args = {};  // no arguments needed
            App.main(args);  // relaunch app.java (main)
        } catch (Exception e) {
            e.printStackTrace();  // print the stack trace in case of an error (gets exception otherwise)
        }
    }
    
    // steps after the pause menu is open through key input
    public void showPauseMenu() {
        gamePaused = true; // variable that determines the state of the game timer
        int choice = JOptionPane.showOptionDialog(
        	// creates the button and button labels
            this,
            "Game paused.",
            "Pause Menu",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            new String[]{"Continue", "Restart", "Main Menu", "Quit"},
            "Continue"
        );
        
        // determines the function of each of the button in the pause menu
        if (choice == JOptionPane.YES_OPTION) { // this serves as the default option in the menu
            gamePaused = false;
            gameLoop.start();
            if (isTimedMode && countdownTimer != null) countdownTimer.start();
        } else if (choice == JOptionPane.NO_OPTION) { // Restart
            restartGame();
        } else if (choice == 2) {
            returnToMenu();	// returns to the main menu
        } else if (choice == 3) {
            System.exit(0);	// closes the window and terminates the program
        }
    }

    // must reset all game variables and reinitialize the game when this option is selected
    public void restartGame() {
        snakeHead = new Tile(5, 5, Color.WHITE);
        snakeBody.clear();
        apples.clear();
        gameOver = false;
        velocityX = 1;
        velocityY = 0;
        firstAppleEaten = false;
        gamePaused = false;  // Ensure the game is not paused after restart, resulting in key presses not working

        // places the apples on the grid again
        for (int i = 0; i < maxFoodCount; i++) {
            placeFood();
        }

        // resets the countdown timer (if in "timed" mode)
        if (isTimedMode) {
            remainingTime = timeLimit;
            if (countdownTimer != null) {
                countdownTimer.stop();  // must stop the countdown timer again, as the game is reset
            }
        }

        gameStarted = false;  // set to false to pause the environment and wait for a key press
        repaint();
    }

    // menu when a player's game ends
    public void showGameOverPopup() {
        int choice = JOptionPane.showOptionDialog(
            this,
            "Game Over :( Score: " + snakeBody.size() + "\nPlay again?",
            "Game Over",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            new String[]{"Continue", "Main Menu", "Quit"},
            "Continue"  // sets "continue" as the default option
        );

        if (choice == JOptionPane.YES_OPTION) {
            restartGame();  // restarts the game and all of its environment variables
        } else if (choice == JOptionPane.NO_OPTION) {
            returnToMenu();  // goes back to the main menu
        } else {
            System.exit(0);  // closes the window and terminates the program
        }
    }
}

// READ ME: the following section was created from a simple snake game tutorial. i followed the idea of adding
// elements to an existing game rather than combine two existing games. while this was the base code for my project,
// there was actually a lot of implementation that went into it, which transformed it to a greater extent than i
// had originally thought.

//package snakegame;
//
//import java.awt.*;
//import java.awt.event.*;
//import java.util.ArrayList;
//import java.util.Random;
//import javax.swing.*;
//
//public class SnakeGame extends JPanel implements ActionListener, KeyListener {
//	// this class creates the tile size to be used with all elements in the game, given as coordinates
//    private class Tile {
//        int x;
//        int y;
//
//        Tile(int x, int y) {
//            this.x = x;
//            this.y = y;
//        }
//    }  
//
//    int boardWidth;
//    int boardHeight;
//    int tileSize = 25;
//    
//    //snake
//    Tile snakeHead;
//    ArrayList<Tile> snakeBody;
//
//    //food
//    Tile food;
//    Random random;
//
//    //game logic
//    int velocityX;
//    int velocityY;
//    Timer gameLoop;
//
//    boolean gameOver = false;
//
//    SnakeGame(int boardWidth, int boardHeight) {
//        this.boardWidth = boardWidth;
//        this.boardHeight = boardHeight;
//        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
//        setBackground(Color.black);
//        addKeyListener(this);
//        setFocusable(true);
//
//        snakeHead = new Tile(5, 5);
//        snakeBody = new ArrayList<Tile>();
//
//        food = new Tile(10, 10);
//        random = new Random();
//        placeFood();
//      
//
//        velocityX = 1;
//        velocityY = 0;
//        
//		//game timer
//		gameLoop = new Timer(100, this); //how long it takes to start timer, milliseconds gone between frames 
//        gameLoop.start();
//	}	
//    
//    public void paintComponent(Graphics g) {
//		super.paintComponent(g);
//		draw(g);
//	}
// 
//	public void draw(Graphics g) {
//        //Grid Lines
//        for(int i = 0; i < boardWidth/tileSize; i++) {
//            //(x1, y1, x2, y2)
//            g.drawLine(i*tileSize, 0, i*tileSize, boardHeight);
//            g.drawLine(0, i*tileSize, boardWidth, i*tileSize); 
//        }
//
//        //Food
//        g.setColor(Color.red);
//        // g.fillRect(food.x*tileSize, food.y*tileSize, tileSize, tileSize);
//        g.fill3DRect(food.x*tileSize, food.y*tileSize, tileSize, tileSize, true);
//
//        //Snake Head
//        g.setColor(Color.green);
//        // g.fillRect(snakeHead.x, snakeHead.y, tileSize, tileSize);
//        // g.fillRect(snakeHead.x*tileSize, snakeHead.y*tileSize, tileSize, tileSize);
//        g.fill3DRect(snakeHead.x*tileSize, snakeHead.y*tileSize, tileSize, tileSize, true);
//        
//        //Snake Body
//        for (int i = 0; i < snakeBody.size(); i++) {
//            Tile snakePart = snakeBody.get(i);
//            // g.fillRect(snakePart.x*tileSize, snakePart.y*tileSize, tileSize, tileSize);
//            g.fill3DRect(snakePart.x*tileSize, snakePart.y*tileSize, tileSize, tileSize, true);
//		}
//
//        //Score
//        g.setFont(new Font("Arial", Font.PLAIN, 16));
//        if (gameOver) {
//            g.setColor(Color.red);
//            g.drawString("Game Over: " + String.valueOf(snakeBody.size()), tileSize - 16, tileSize);
//        }
//        else {
//            g.drawString("Score: " + String.valueOf(snakeBody.size()), tileSize - 16, tileSize);
//        }
//	}
//
//    public void placeFood(){
//        food.x = random.nextInt(boardWidth/tileSize);
//		food.y = random.nextInt(boardHeight/tileSize);
//	}
//
//    public void move() {
//        //eat food
//        if (collision(snakeHead, food)) {
//            snakeBody.add(new Tile(food.x, food.y));
//            placeFood();
//        }
//
//        //move snake body
//        for (int i = snakeBody.size()-1; i >= 0; i--) {
//            Tile snakePart = snakeBody.get(i);
//            if (i == 0) { //right before the head
//                snakePart.x = snakeHead.x;
//                snakePart.y = snakeHead.y;
//            }
//            else {
//                Tile prevSnakePart = snakeBody.get(i-1);
//                snakePart.x = prevSnakePart.x;
//                snakePart.y = prevSnakePart.y;
//            }
//        }
//        //move snake head
//        snakeHead.x += velocityX;
//        snakeHead.y += velocityY;
//
//        //game over conditions
//        for (int i = 0; i < snakeBody.size(); i++) {
//            Tile snakePart = snakeBody.get(i);
//
//            //collide with snake head
//            if (collision(snakeHead, snakePart)) {
//                gameOver = true;
//            }
//        }
//
//        if (snakeHead.x*tileSize < 0 || snakeHead.x*tileSize > boardWidth || //passed left border or right border
//            snakeHead.y*tileSize < 0 || snakeHead.y*tileSize > boardHeight ) { //passed top border or bottom border
//            gameOver = true;
//        }
//    }
//
//    public boolean collision(Tile tile1, Tile tile2) {
//        return tile1.x == tile2.x && tile1.y == tile2.y;
//    }
//
//    @Override
//    public void actionPerformed(ActionEvent e) { //called every x milliseconds by gameLoop timer
//        move();
//        repaint();
//        if (gameOver) {
//            gameLoop.stop();
//        }
//    }  
//
//    @Override
//    public void keyPressed(KeyEvent e) {
//        // System.out.println("KeyEvent: " + e.getKeyCode());
//        if (e.getKeyCode() == KeyEvent.VK_UP && velocityY != 1) {
//            velocityX = 0;
//            velocityY = -1;
//        }
//        else if (e.getKeyCode() == KeyEvent.VK_DOWN && velocityY != -1) {
//            velocityX = 0;
//            velocityY = 1;
//        }
//        else if (e.getKeyCode() == KeyEvent.VK_LEFT && velocityX != 1) {
//            velocityX = -1;
//            velocityY = 0;
//        }
//        else if (e.getKeyCode() == KeyEvent.VK_RIGHT && velocityX != -1) {
//            velocityX = 1;
//            velocityY = 0;
//        }
//    }
//
//    //not needed
//    @Override
//    public void keyTyped(KeyEvent e) {}
//
//    @Override
//    public void keyReleased(KeyEvent e) {}
//}