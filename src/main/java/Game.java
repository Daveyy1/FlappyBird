import javax.swing.*;

public class Game {
    public static void main(String[] args) {
        //dim of background img
        int boardWidth = 360;
        int boardHeight = 640;

        JFrame frame = new JFrame("Flappy Bird");
        //frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        //sets default location to the center of the screen
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        FlappyBird flappyBird = new FlappyBird();
        frame.add(flappyBird);
        frame.pack();
        // normally, you only make the frame visible after initializing everything
        frame.setVisible(true);
    }
}
