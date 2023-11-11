import swiftbot.*;

import java.util.Random;
import java.util.Scanner;

public class SimonSwiftBotGame {
    static SwiftBotAPI swiftBot;
    static final int NUM_ROUNDS_TO_PROMPT = 5;

    public static void main(String[] args) throws InterruptedException {
        try {
            swiftBot = new SwiftBotAPI();
        } catch (Exception e) {
            System.out.println("\nI2C disabled!");
            System.out.println("Run the following command:");
            System.out.println("sudo raspi-config nonint do_i2c 0\n");
            System.exit(5);
        }

        int round = 1;
        int score = 0;

        System.out.println("Welcome to Simon SwiftBot Game!");

        // Game loop
        while (true) {
            System.out.println("Round: " + round + " | Score: " + score);

            // Generate and display sequence
            int[] sequence = generateSequence(round);
            displaySequence(sequence);

            // User input using buttons
            if (!getUserInputWithButtons(sequence)) {
                gameOver(score);
                break;
            }

            // Check if it's time to prompt user to continue or quit
            if (round % NUM_ROUNDS_TO_PROMPT == 0) {
                if (!promptToContinue()) {
                    endGame(score);
                    break;
                }
            }

            // Increment round and score
            round++;
            score++;

            // Celebrate if the score is 5 or more
            if (score >= 5) {
                celebrate();
                endGame(score);
                break;
            }
        }
    }

    // Generate a random sequence of colors (represented by integers)
    private static int[] generateSequence(int length) {
        Random rand = new Random();
        int[] sequence = new int[length];
        for (int i = 0; i < length; i++) {
            sequence[i] = rand.nextInt(4);  // 0: Red, 1: Green, 2: Blue, 3: Yellow
        }
        return sequence;
    }

    // Display the sequence by blinking corresponding LEDs
    private static void displaySequence(int[] sequence) throws InterruptedException {
        for (int color : sequence) {
            blinkLEDForColor(color);
            Thread.sleep(500);  // Adjust delay as needed
        }
    }

    // Get user input using buttons
    private static boolean getUserInputWithButtons(int[] sequence) {
        System.out.println("Your turn! Enter the sequence using buttons (A, B, X, Y):");

        for (int color : sequence) {
            Button button = waitForButtonPress();
            int buttonColor = mapButtonToColor(button);

            if (buttonColor != color) {
                return false;  // Incorrect input
            }
        }
        return true;  // Correct input
    }

    // Wait for a button press and return the pressed button
    private static Button waitForButtonPress() {
        while (true) {
            if (swiftBot.isButtonPressed(Button.A)) {
                return Button.A;
            } else if (swiftBot.isButtonPressed(Button.B)) {
                return Button.B;
            } else if (swiftBot.isButtonPressed(Button.X)) {
                return Button.X;
            } else if (swiftBot.isButtonPressed(Button.Y)) {
                return Button.Y;
            }
        }
    }

    // Map button to color
    private static int mapButtonToColor(Button button) {
        switch (button) {
            case A:
                return 0;  // Red
            case B:
                return 1;  // Green
            case X:
                return 2;  // Blue
            case Y:
                return 3;  // Yellow
            default:
                return -1; // Invalid color
        }
    }

    // Blink the LED corresponding to the color
    private static void blinkLEDForColor(int color) {
        switch (color) {
            case 0:
                swiftBot.setUnderlight(Underlight.FRONT_LEFT, new int[]{255, 0, 0});
                break;
            case 1:
                swiftBot.setUnderlight(Underlight.FRONT_RIGHT, new int[]{0, 255, 0});
                break;
            case 2:
                swiftBot.setUnderlight(Underlight.MIDDLE_LEFT, new int[]{0, 0, 255});
                break;
            case 3:
                swiftBot.setUnderlight(Underlight.MIDDLE_RIGHT, new int[]{255, 255, 0});
                break;
        }
        // Add a delay for the LED to blink
        try {
            Thread.sleep(500);  // Adjust delay as needed
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Turn off the LED
        swiftBot.disableUnderlights();
    }

    // Prompt the user to continue or quit
    private static boolean promptToContinue() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Do you want to continue? (yes/no)");
        String response = scanner.next().toLowerCase();
        return response.equals("yes");
    }

    // Game over message
    private static void gameOver(int score) {
        System.out.println("Game Over! Your final score: " + score);
    }

    // End game message
    private static void endGame(int score) {
        System.out.println("See you again champ! Your final score: " + score);
    }

    // Celebrate with a random V-shaped dive
    private static void celebrate() throws InterruptedException {
        Random rand = new Random();
        int[] colors = {0, 1, 2, 3};  // Red, Green, Blue, Yellow
        for (int i = 0; i < 4; i++) {
            int color = colors[rand.nextInt(colors.length)];
            blinkLEDForColor(color);
            swiftBot.move(50, 50, 500);  // Move in a V-shape, adjust velocity and duration as needed
        }
        swiftBot.disableUnderlights();
    }
}
