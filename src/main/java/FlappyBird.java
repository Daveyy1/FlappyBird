import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

// JPanel is the "Canvas" of the game
public class FlappyBird extends JPanel implements ActionListener{
    int boardWidth = 360;
    int boardHeight = 640;

    // Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // Bird
    // 1/8 to the right, in the middle of the height
    int birdX = boardWidth/8;
    int birdY = boardHeight/2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird{
        int x = birdX;
        int y = birdY;
        int width =  birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img){
            this.img = img;
        }
    }

    // Game Logic
    Bird bird;
    int velocityY = -6; // move 6 pixels upwards per frame


    Timer gameLoop;

    FlappyBird(){
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        // setBackground(Color.blue);

        // Load Images
        backgroundImg = new ImageIcon(getClass().getResource("flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("bottompipe.png")).getImage();

        bird = new Bird(birdImg);

        // Game Timer, 1000/60 = 60 times per second (60fps), this refers to the implemented ActionListener
        gameLoop = new Timer(1000/60, this);
        gameLoop.start();
    }

    public void paintComponent(Graphics g){
        // inherits the paintComponent method from JPanel
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g){
        // background, 0 and 0 is the top left
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);
        // bird
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);
    }

    public void move(){
        // bird
        bird.y += velocityY;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        // calls paintComponent
        repaint();
    }
}
