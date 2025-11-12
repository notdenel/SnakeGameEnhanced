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

import javax.swing.*;

public class App {
    public static void main(String[] args) throws Exception {
        int boardWidth = 600; // sets the border width and height (can be changed to user's preference)
        int boardHeight = boardWidth;

        // displays the start menu with relevant options
        int option = JOptionPane.showOptionDialog(
            null,
            "<html><h2>Snake Game...Enhanced!</h2><br><br><b>Rules:</b><br>" +
            "1. Eat apples to grow the snake's length<br>" +
            "2. The snake must eat an apple of the matching color<br>" +
            "3. The game ends if the snake eats an apple of the wrong color or collides with the environment<br>" +
            "4. Try to get the highest score :D</html>",
            "Welcome to Snake Game...Enhanced!",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            new String[]{"Endless", "Timed", "Quit"}, // options for user, with "endless" as default
            "Endless"
        );

        if (option == JOptionPane.YES_OPTION || option == JOptionPane.NO_OPTION) {
            int timeLimit = 0;
            boolean isTimedMode = (option == JOptionPane.NO_OPTION);

            // if "timed" mode is selected, show another menu for time selection
            if (isTimedMode) {
                String[] timeOptions = {"30 seconds", "60 seconds", "120 seconds"};
                int timeChoice = JOptionPane.showOptionDialog(
                    null,
                    "Select your time limit:",
                    "Choose Timer Duration",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    timeOptions,
                    timeOptions[0]
                );

                switch (timeChoice) {
                    case 0: timeLimit = 30; break;
                    case 1: timeLimit = 60; break;
                    case 2: timeLimit = 120; break;
                    default: System.exit(0); 
                }
            }

            JFrame frame = new JFrame("Snake Game...Enhanced!");
            frame.setSize(boardWidth, boardHeight); // sets the frame size to the conditions specified
            frame.setLocationRelativeTo(null); // centers the window
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // the following exists so the the contents of the snake game (literally the entire program) runs inside this window
            SnakeGame snakeGame = new SnakeGame(boardWidth, boardHeight, isTimedMode, timeLimit);
            frame.add(snakeGame);
            frame.pack();
            frame.setVisible(true);
            snakeGame.requestFocus();
        } else {
            System.exit(0);
        }
    }
}

//READ ME: the following section was created from a simple snake game tutorial. i followed the idea of adding
//elements to an existing game rather than combine two existing games. while this was the base code for my project,
//there was actually a lot of implementation that went into it, which transformed it to a greater extent than i
//had originally thought.

//import javax.swing.*;
//
//public class App {
//    public static void main(String[] args) throws Exception {
//        int boardWidth = 600;
//        int boardHeight = boardWidth;
//
//        JFrame frame = new JFrame("Snake");
//        frame.setVisible(true);
//        frame.setSize(boardWidth, boardHeight);
//        frame.setLocationRelativeTo(null);
//        frame.setResizable(false);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//        SnakeGame snakeGame = new SnakeGame(boardWidth, boardHeight);
//        frame.add(snakeGame);
//        frame.pack();
//        snakeGame.requestFocus();
//    }
//}