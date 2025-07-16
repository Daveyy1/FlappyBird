import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;
    
    // Game state
    boolean gameOver = false;
    double score = 0;
    
    // Time management
    private long lastUpdateTime;

    // Game constants - now in pixels per second instead of pixels per frame
    private final double PIPE_SPEED = 240.0; // 4 pixels/frame * 60 frames = 240 pixels/second
    private final double GRAVITY = 630.0;    // Pixels per second squared
    private final double JUMP_VELOCITY = -170.0;
    
    // Images and Font
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;
    private Font gameFont;

    // Bird properties
    int birdX = boardWidth/8;
    int birdY = boardHeight/2;
    int birdWidth = 34;
    int birdHeight = 24;
    double velocityY = 0;
    
    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;
        
        Bird(Image img) {
            this.img = img;
        }
    }
    
    // Pipes
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;
    
    class Pipe {
        double x = pipeX; // Using double for smoother movement
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;
        
        Pipe(Image img) {
            this.img = img;
        }
    }
    
    // Game objects
    Bird bird;
    ArrayList<Pipe> pipes = new ArrayList<>();
    Random random = new Random();
    Timer pipeSpawner;
    
    // Game loop thread
    private Thread gameThread;
    
    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);
        
        // Load Images
        backgroundImg = new ImageIcon(getClass().getResource("flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("bottompipe.png")).getImage();
        
        // Load custom font
        loadGameFont();
        
        bird = new Bird(birdImg);
        
        // Pipe spawner timer - still use Swing timer for this as it's not timing-critical
        pipeSpawner = new Timer(1200, e -> placePipes());
        
        // Start the game
        startGame();
    }
    
    private void startGame() {
        if (gameThread != null && gameThread.isAlive()) return;

        gameOver = false;
        score = 0;
        velocityY = 0;
        bird.y = birdY;
        pipes.clear();
        
        pipeSpawner.start();
        
        gameThread = new Thread(() -> {
            lastUpdateTime = System.nanoTime();
            
            while (!gameOver) {
                long currentTime = System.nanoTime();
                double deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0; // Convert to seconds
                lastUpdateTime = currentTime;
                
                // Cap delta time to avoid huge jumps if the game freezes temporarily
                if (deltaTime > 0.1) deltaTime = 0.1;
                
                // Update game state
                updateGame(deltaTime);
                
                // Render
                repaint();
                
                // Simple frame limiting to avoid excessive CPU usage
                try {
                    Thread.sleep(1000 / 60); // Aim for approximately 60 FPS
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        gameThread.start();
    }
    
    private void stopGame() {
        gameOver = true;
        pipeSpawner.stop();
        
        try {
            if (gameThread != null) {
                // Set gameThread to null after it stops to allow a new game to start
                Thread threadToJoin = gameThread;
                gameThread = null;
                threadToJoin.join(500); // Reduced timeout to make restart more responsive
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void loadGameFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/Fonts/BmCube.ttf");
            if (is != null) {
                gameFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(32f);
                is.close();
            } else {
                gameFont = new Font("Arial", Font.BOLD, 24);
                System.out.println("Custom font not found, using default font");
            }
        } catch (IOException | FontFormatException e) {
            gameFont = new Font("Arial", Font.BOLD, 24);
            System.out.println("Error loading custom font: " + e.getMessage());
        }
    }
    
    public void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        int openingSpace = boardHeight / 4;
        
        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);
        
        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }
    
    private void updateGame(double deltaTime) {
        if (gameOver) {
            // When game is over, we should stop the game completely
            stopGame();
            return;
        }
        
        // Update bird
        velocityY += GRAVITY * deltaTime;
        bird.y += velocityY * deltaTime;
        bird.y = Math.max(bird.y, 0);
        
        // Update pipes
        for (int i = pipes.size() - 1; i >= 0; i--) {
            Pipe pipe = pipes.get(i);
            pipe.x -= PIPE_SPEED * deltaTime;
            
            // Score when passing pipes
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5; // 0.5 because we have two pipes per gap
            }
            
            // Check collision
            if (collision(bird, pipe)) {
                gameOver = true;
            }
            
            // Remove pipes that are off-screen
            if (pipe.x + pipe.width < 0) {
                pipes.remove(i);
            }
        }
        
        // Check if bird hits the ground
        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }
    
    public void draw(Graphics g) {
        // Draw background
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);
        
        // Draw bird
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);
        
        // Draw pipes
        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, (int)pipe.x, pipe.y, pipe.width, pipe.height, null);
        }
        
        // Score and game over text
        g.setColor(Color.black);
        g.setFont(gameFont);
        
        FontMetrics metrics = g.getFontMetrics(gameFont);
        
        if (gameOver) {
            String gameOverText = "Game Over: " + (int) score;
            String instructionText = "Press SPACE";
            
            int gameOverWidth = metrics.stringWidth(gameOverText);
            int instructionWidth = metrics.stringWidth(instructionText);
            
            int gameOverX = (boardWidth - gameOverWidth) / 2;
            g.drawString(gameOverText, gameOverX, boardHeight/2 - 30);
            
            int instructionX = (boardWidth - instructionWidth) / 2;
            g.drawString(instructionText, instructionX, boardHeight/2 + 20);
        } else {
            String scoreText = String.valueOf((int) score);
            int scoreWidth = metrics.stringWidth(scoreText);
            int scoreX = (boardWidth - scoreWidth) / 2;
            g.drawString(scoreText, scoreX, 45);
        }
    }
    
    private boolean collision(Bird b, Pipe p) {
        return b.x < (int)p.x + p.width &&
               b.x + b.width > (int)p.x &&
               b.y < p.y + p.height &&
               b.y + b.height > p.y;
    }
    
    @Override // not needed
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (gameOver) {
                // Explicitly stop the previous game before starting a new one
                stopGame();
                startGame();
            } else {
                velocityY = JUMP_VELOCITY;
            }
        }
    }
    
    @Override // not needed
    public void keyReleased(KeyEvent e) {
    }
}