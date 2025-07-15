import java.awt.*;
import java.awt.event.*;
import java.nio.channels.Pipe;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

// JPanel is the "Canvas" of the game
public class FlappyBird extends JPanel implements ActionListener, KeyListener{
    int boardWidth = 360;
    int boardHeight = 640;

    Timer gameLoop;
    Timer placePipesTimer;
    boolean gameOver = false;
    double score = 0;

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

    // Pipes
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64; // scaled by 1/6
    int pipeHeight = 512;

    class Pipe{
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        // has the bird passed yet
        boolean passed = false;

        Pipe(Image img){
            this.img = img;
        }
    }

    // Game Logic
    Bird bird;
    int velocityX = -4; // move pipes to the left to simulate the bird moving left
    int velocityY = 0; // move 6 pixels upwards per frame
    int gravity = 1;

    ArrayList<Pipe> pipes = new ArrayList<>();
    Random random = new Random();

    FlappyBird(){
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        // setBackground(Color.blue);
        setFocusable(true); // makes sure that this class takes in the key presses
        addKeyListener(this);

        // Load Images
        backgroundImg = new ImageIcon(getClass().getResource("flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("bottompipe.png")).getImage();

        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        // places a new pipe every 1.5s
        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipesTimer.start();

        // Game Timer, 1000/60 = 60 times per second (60fps), this refers to the implemented ActionListener
        gameLoop = new Timer(1000/60, this);
        gameLoop.start();
    }

    public void placePipes(){
        // (0-1) * pipeheight/2 --> (0-256)
        // 128
        // 0 - 128 - (0-256) --> 1/4 pipeHeight to 3/4 pipeHeight

        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        // topPipe.y is the top of the canvas, then add the height of said pipe to get down and add the openingSpace
        // to get the height of the bottompipe
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
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
        // pipes
        for (Pipe pipe : pipes){
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        if (gameOver){
            g.drawString("Game Over: " + String.valueOf((int) score), 10, 35);
        } else {
            g.drawString("Score: " + String.valueOf((int) score), 10, 35);
        }
    }

    public void move(){
        // bird
        velocityY += gravity;
        bird.y += velocityY;
        // y value is never higher than the top of the screen (0)
        bird.y = Math.max(bird.y, 0);

        // pipes
        for (Pipe pipe : pipes){
            pipe.x += velocityX;

            if (!(pipe.passed) && bird.x > pipe.x + pipe.width){
                pipe.passed = true;
                // 0.5 because the player passes two pipes at a time
                score += 0.5;
            }

            if (collision(bird, pipe)){
                gameOver = true;
            }
        }

        if (bird.y > boardHeight){
            gameOver = true;
        }
    }

    public boolean collision(Bird b, Pipe p){
        return b.x < p.x + p.width &&    // b's top left corner doesnt reach p's top right corner
                b.x + b.width > p.x &&   // b's top right corner passes b's top left corner
                b.y < p.y + p.height &&  // b's top left corner doesnt reach b's bottom left corner
                b.y + b.height > p.y;    // b's bottom left corner passes p's top left corner
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        // calls paintComponent
        repaint();
        if (gameOver){
            placePipesTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    // for every key that has a value, so no F5 or such
    public void keyTyped(KeyEvent e) {

    }

    // for every key
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;
            if (gameOver){
                // reset the game by resetting the values to their default values
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                placePipesTimer.start();
            }
        }
    }

    // what happens when you release a key
    @Override
    public void keyReleased(KeyEvent e) {

    }
}
