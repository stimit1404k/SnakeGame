import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class GamePanel extends JPanel implements ActionListener {

    static final int SCREEN_WIDTH = 750;
    static final int SCREEN_HEIGHT = 750;
    static final int UNIT_SIZE = 15;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / UNIT_SIZE;
    static final int INITIAL_DELAY = 200;
    
    int currDelay = INITIAL_DELAY;

    final int x[] = new int[GAME_UNITS];
    final int y[] = new int[GAME_UNITS];
    
    int bodyParts = 6;
    int applesEaten = 0;
    int appleX;
    int appleY;
    char direction = 'R';
    boolean running = false;
    Timer timer;
    Random random;

    boolean paused = false;
    JLabel pauseLabel;

    Clip backgroundClip;

    // Constructor to set up the game panel
    GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.DARK_GRAY);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        startGame(); // Start the game
        
        pauseLabel = new JLabel("Press SPACEBAR to Pause/Resume");
        pauseLabel.setFont(new Font("Arial", Font.BOLD, 20));
        pauseLabel.setForeground(Color.CYAN);
        pauseLabel.setHorizontalAlignment(SwingConstants.CENTER);
        pauseLabel.setBounds(0, 45, SCREEN_WIDTH, 30);
        
        this.setLayout(null);
        this.add(pauseLabel);
    }
    
    // Start the game by initializing variables and starting the game loop
    public void startGame() {  
        x[0] = 0;
        y[0] = 75;
        for (int i = 1; i < bodyParts; i++) {
            x[i] = x[i - 1] - UNIT_SIZE;
            y[i] = y[0];
        }
        
        newApple(); // Generate the first apple
        running = true;
        timer = new Timer(currDelay, this);
        timer.start();

        backgroundClip = playSound("src/resources/music.wav"); // Start background music
    }
    
    // Override paintComponent to draw the game components
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (paused) {
            showPausedScreen(g); // Show paused screen if the game is paused
        } else {
            draw(g); // Draw the game components if the game is running
        }

        pauseLabel.repaint(); // Repaint the pause label
    }
    
    // Show the pause screen when the game is paused
    public void showPausedScreen(Graphics g) {
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 20));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Paused", ((SCREEN_WIDTH - metrics1.stringWidth("Paused")) / 2), SCREEN_HEIGHT / 2);
    }

    // Pause the game by stopping the timer and music
    public void pauseGame() {
        paused = true;
        timer.stop();
        pauseMusic(); // Pause the background music
        pauseLabel.setText("Game Paused - Press SPACEBAR to resume");
    }

    // Resume the game by starting the timer and music
    public void resumeGame() {
        paused = false;
        timer.start();
        resumeMusic(); // Resume the background music
        pauseLabel.setText("Press SPACEBAR to Pause");
    }
    
    // Pause the background music
    public void pauseMusic() {
        if (backgroundClip != null && backgroundClip.isRunning())
            backgroundClip.stop();
    }

    // Resume the background music
    public void resumeMusic() {
        if (backgroundClip != null)
            backgroundClip.start();
    }

    // Stop the background music
    public void stopMusic() {
        if (backgroundClip != null) {
            backgroundClip.stop();
            backgroundClip.close();
        }
    }

    // Draw the game components: snake, apple, and score
    public void draw(Graphics g) {
        if (running) {
            g.setColor(Color.red);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE); // Draw apple

            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g.setColor(Color.green);
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE); // Draw snake head
                } else {
                    g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE); // Draw snake body
                }
            }

            g.setColor(Color.red);
            g.setFont(new Font("Ink Free", Font.BOLD, 40)); // Draw score
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + applesEaten, ((SCREEN_WIDTH - metrics.stringWidth("Score: " + applesEaten)) / 2), g.getFont().getSize());
        } else {
            gameOver(g); // Show game over screen if the game is not running
        }
    }

    // Generate a new apple at a random position
    public void newApple() {
        appleX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        appleY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
    }

    // Move the snake by updating the position of each body part
    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        // Move the snake based on the current direction
        switch (direction) {
            case 'U': y[0] -= UNIT_SIZE; break;
            case 'D': y[0] += UNIT_SIZE; break;
            case 'L': x[0] -= UNIT_SIZE; break;
            case 'R': x[0] += UNIT_SIZE; break;
        }
    }

    // Check if the snake eats an apple, and update the score and body size
    public void checkApple() {
        if (x[0] == appleX && y[0] == appleY) {
            bodyParts++; // Increase snake's body size
            applesEaten++; // Increment score
            newApple(); // Generate a new apple

            if (currDelay > 30) {
                currDelay -= 10;
                timer.setDelay(currDelay); // Increase game speed
            }
        }
    }

    // Check if the snake collides with itself or the borders
    public void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) {
                running = false; // Game over if snake hits itself
            }
        }

        if (x[0] < 0 || x[0] > SCREEN_WIDTH || y[0] < 0 || y[0] > SCREEN_HEIGHT) {
            running = false; // Game over if snake hits the border
        }

        if (!running) {
            timer.stop(); // Stop the game timer
        }
    }

    // Display the game over screen
    public void gameOver(Graphics g) {
        stopMusic(); // Stop background music

        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 75)); // Display "Game Over" text
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Game Over", ((SCREEN_WIDTH - metrics1.stringWidth("Game Over")) / 2), 100 + SCREEN_HEIGHT / 2);

        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 40)); // Display score
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten, ((SCREEN_WIDTH - metrics2.stringWidth("Score: " + applesEaten)) / 2), 100 + g.getFont().getSize());
        
        // Display encouragement based on score
        if (applesEaten < 15) {
            g.setColor(Color.red);
            g.setFont(new Font("Ink Free", Font.BOLD, 30));
            FontMetrics metrics3 = getFontMetrics(g.getFont());
            g.drawString("Come On, You can do better", ((SCREEN_WIDTH - metrics3.stringWidth("Come On, You can do better")) / 2), g.getFont().getSize());
        } else {
            g.setColor(Color.red);
            g.setFont(new Font("Ink Free", Font.BOLD, 30));
            FontMetrics metrics4 = getFontMetrics(g.getFont());
            g.drawString("Congrats, you're score is: " + applesEaten, ((SCREEN_WIDTH - metrics4.stringWidth("Congrats, you're score is: " + applesEaten)) / 2), g.getFont().getSize());
        }
    }

    // Play background music
    public Clip playSound(String soundFile) {
        try {
            File file = new File(soundFile);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start(); // Start music
            return clip; // Return clip to control it later
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Handle user inputs for controlling the snake and pausing/resuming the game
    @Override
    public void actionPerformed(ActionEvent e) {
        if (running && !paused) {
            move(); // Move the snake
            checkApple(); // Check if snake eats an apple
            checkCollisions(); // Check for collisions
        }
        repaint(); // Repaint the screen
    }

    // KeyAdapter for controlling snake and pausing/resuming the game
    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT: // Left arrow
                    if (direction != 'R') direction = 'L'; break;
                case KeyEvent.VK_RIGHT: // Right arrow
                    if (direction != 'L') direction = 'R'; break;
                case KeyEvent.VK_UP: // Up arrow
                    if (direction != 'D') direction = 'U'; break;
                case KeyEvent.VK_DOWN: // Down arrow
                    if (direction != 'U') direction = 'D'; break;
                case KeyEvent.VK_SPACE: // Spacebar for pause/resume
                    if (paused) resumeGame(); else pauseGame(); break;
            }
        }
    }
}
