import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Objects;

public class ChromeDinosaur extends JPanel implements ActionListener, KeyListener {
    private final int BOARD_WIDTH;
    private final int BOARD_HEIGHT;

    // -------- Images --------
    // Dinosaur images
    public final Image DINOSAUR_IMG;
    public final Image DINOSAUR_DEAD_IMG;
    public final Image DINOSAUR_JUMP_IMG;
    // Cactus images
    public final Image CACTUS_SMALL_1_IMG;
    public final Image CACTUS_SMALL_2_IMG;
    public final Image CACTUS_SMALL_3_IMG;
    // Ground image
    public final Image GROUND_IMG;
    private final int GROUND_HEIGHT; // Height of the ground image
    private final int GROUND_WIDTH; // Width of the ground image

    // Dinosaur
    // Set dinosaur dimensions and position
    private final int DINOSAUR_WIDTH;
    private final int DINOSAUR_HEIGHT;
    private final int DINOSAUR_X;
    private final int DINOSAUR_Y;
    private final Block DINOSAUR;

    // Cactus
    private final int CACTUS_1_WIDTH;
    private final int CACTUS_2_WIDTH;
    private final int CACTUS_3_WIDTH;
    private final int CACTUS_HEIGHT; // Height of the cactus images
    private final int CACTUS_X;
    private final int CACTUS_Y;
    private final ArrayList<Block> cactusArray;

    // Physics
    private int velocityX = -12; // Horizontal velocity, for the cactus movement
    private int velocityY = 0; // Vertical velocity for jumping
    private final int GRAVITY = 1; // Gravity effect on the dinosaur
    private int groundOffsetX = 0; // Offset for the ground movement

    private boolean gameOver = false; // Flag to check if the game is over
    private int score = 0;
    private int highScore = 0;

    // Game loop timer
    private final Timer GAMELOOP;
    private final Timer PLACE_CACTUS_TIMER;

    /**
     * Constructor for ChromeDinosaur class.
     * @param boardWidth The width of the game board.
     * @param boardHeight The height of the game board.
     */
    ChromeDinosaur(int boardWidth, int boardHeight) {
        this.BOARD_WIDTH = boardWidth;
        this.BOARD_HEIGHT = boardHeight;

        this.setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        this.setFocusable(true);
        this.addKeyListener(this); // Add key listener
        this.setBackground(Color.LIGHT_GRAY);

        // Load images
        DINOSAUR_IMG = new ImageIcon(Objects.requireNonNull(getClass().getResource("images/dino-run.gif"))).getImage();
        DINOSAUR_DEAD_IMG = new ImageIcon(Objects.requireNonNull(getClass().getResource("images/dino-dead.png"))).getImage();
        DINOSAUR_JUMP_IMG = new ImageIcon(Objects.requireNonNull(getClass().getResource("images/dino-jump.png"))).getImage();
        CACTUS_SMALL_1_IMG = new ImageIcon(Objects.requireNonNull(getClass().getResource("images/cactus1.png"))).getImage();
        CACTUS_SMALL_2_IMG = new ImageIcon(Objects.requireNonNull(getClass().getResource("images/cactus2.png"))).getImage();
        CACTUS_SMALL_3_IMG = new ImageIcon(Objects.requireNonNull(getClass().getResource("images/cactus3.png"))).getImage();
        GROUND_IMG = new ImageIcon(Objects.requireNonNull(getClass().getResource("images/track.png"))).getImage();

        // Set ground dimensions
        this.GROUND_HEIGHT = GROUND_IMG.getHeight(null); // Height of the ground image
        this.GROUND_WIDTH = GROUND_IMG.getWidth(null); // Width of the ground image

        // Set dinosaur dimensions and position
        this.DINOSAUR_WIDTH = 88; // Width of the dinosaur image
        this.DINOSAUR_HEIGHT = 94; // Height of the dinosaur image
        this.DINOSAUR_X = 50; // X position of the dinosaur
        this.DINOSAUR_Y = BOARD_HEIGHT - DINOSAUR_HEIGHT - 10; // Y position of the dinosaur, 10 pixels above the bottom

        // Set cactus dimensions and position
        this.CACTUS_1_WIDTH = 34;
        this.CACTUS_2_WIDTH = 69;
        this.CACTUS_3_WIDTH = 102;
        this.CACTUS_HEIGHT = 70; // Height of the cactus images
        this.CACTUS_X = 700;
        this.CACTUS_Y = BOARD_HEIGHT - CACTUS_HEIGHT; // Y position of the cactus

        // Initialize the dinosaur block
        this.DINOSAUR = new Block(DINOSAUR_X, DINOSAUR_Y, DINOSAUR_WIDTH, DINOSAUR_HEIGHT, DINOSAUR_IMG);

        // Initialize the cactus array
        this.cactusArray = new ArrayList<>();

        // Set the game loop timer
        GAMELOOP = new Timer(1_000 / 60, this); // 60 FPS
        GAMELOOP.start();

        // Set the cactus placement timer
        PLACE_CACTUS_TIMER = new Timer(1_500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeCactus();
            }
        });
        PLACE_CACTUS_TIMER.start();
    }

    /**
     * Places a cactus on the game board with a random chance.
     * The cactus can be of three different sizes, each with a different probability.
     */
    public void placeCactus() {
        if (gameOver)
            return;

        Block cactus = null;
        double placeCactusChance = Math.random();
        if (placeCactusChance > .90) { // 10% chance
            cactus = new Block(CACTUS_X, CACTUS_Y, CACTUS_3_WIDTH, CACTUS_HEIGHT, CACTUS_SMALL_3_IMG);
        } else if (placeCactusChance > .70) { // 20% chance
            cactus = new Block(CACTUS_X, CACTUS_Y, CACTUS_2_WIDTH, CACTUS_HEIGHT, CACTUS_SMALL_2_IMG);
        } else if (placeCactusChance > .50) { // 20% chance
            cactus = new Block(CACTUS_X, CACTUS_Y, CACTUS_1_WIDTH, CACTUS_HEIGHT, CACTUS_SMALL_1_IMG);
        }
        
        if (cactus != null)
            cactusArray.add(cactus);
    }

    /**
     * Paints the game components.
     * @param g The Graphics object used for painting.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    /**
     * Draws the game components.
     * @param g The Graphics object used for drawing.
     */
    public void draw(Graphics g) {
        // Draw the ground
        g.drawImage(GROUND_IMG, groundOffsetX, BOARD_HEIGHT - GROUND_HEIGHT, GROUND_WIDTH, GROUND_HEIGHT, null);
        // Draw the ground again to create a seamless effect
        g.drawImage(GROUND_IMG, groundOffsetX + GROUND_WIDTH, BOARD_HEIGHT - GROUND_HEIGHT, GROUND_WIDTH, GROUND_HEIGHT, null);

        // Draw the dinosaur
        g.drawImage(DINOSAUR.image, DINOSAUR.x, DINOSAUR.y, DINOSAUR.width, DINOSAUR.height, null);

        // Draw the cacti
        for (Block cactus : cactusArray) {
            g.drawImage(cactus.image, cactus.x, cactus.y, cactus.width, cactus.height, null);
        }

        // Draw the score
        g.setColor(Color.BLACK);
        g.setFont(new Font("Courrier", Font.PLAIN, 23));
        if (gameOver) {
            g.drawString("Game Over: " + score, 10, 30);
        } else {
            g.drawString(String.valueOf(score), 10, 30);
        }

        // Draw the high score
        g.setFont(new Font("Courrier", Font.PLAIN, 19));
        g.drawString("High Score: " + highScore, BOARD_WIDTH / 2, 30);
    }

    /**
     * Moves the dinosaur and cacti, applies gravity, and checks for collisions.
     */
    public void move() {
        // Apply gravity to the dinosaur
        velocityY += GRAVITY;
        DINOSAUR.y += velocityY;

        if (DINOSAUR.y > DINOSAUR_Y) {
            DINOSAUR.y = DINOSAUR_Y;
            velocityY = 0; // Reset velocity when dinosaur lands
            DINOSAUR.image = DINOSAUR_IMG; // Change back to run image
        }

        // Move the cacti
        for (Block cactus : cactusArray) {
            cactus.x += velocityX;

            if (collision(DINOSAUR, cactus)) {
                gameOver = true; // Set game over flag
                DINOSAUR.image = DINOSAUR_DEAD_IMG; // Change dinosaur image to dead
            }
        }

        // Remove cacti that are off-screen
        cactusArray.removeIf(cactus -> cactus.x + cactus.width < 0);

        // Move the ground
        groundOffsetX += velocityX;
        if (groundOffsetX <= -GROUND_WIDTH) {
            groundOffsetX += GROUND_WIDTH; // Reset ground offset when it goes off-screen
        }

        // Score calculation
        score++;
        if (score > highScore)
            highScore = score; // Update high score

        // Update cactus speed based on score
        if (score % 1_000 == 0 && velocityX > -20) // Increase speed every 1'000 points
            velocityX -= 1; // Make the game harder by increasing cactus speed
    }

    /**
     * Checks for collision between two blocks.
     * @param a The first block (Dinosaur).
     * @param b The second block (Cactus).
     * @return true if there is a collision, false otherwise.
     */
    private boolean collision(Block a, Block b) {
        return a.x < b.x + b.width && a.x + a.width > b.x &&
               a.y < b.y + b.height && a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        this.repaint();
        if (gameOver) {
            PLACE_CACTUS_TIMER.stop();
            GAMELOOP.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (DINOSAUR.y == DINOSAUR_Y) {
                velocityY = -17;
                DINOSAUR.image = DINOSAUR_JUMP_IMG; // Change to jump image
            }

            if (gameOver) {
                // Reset the game
                DINOSAUR.y = DINOSAUR_Y;
                DINOSAUR.image = DINOSAUR_IMG;
                velocityY = 0;
                cactusArray.clear();
                gameOver = false;
                score = 0;
                GAMELOOP.start();
                PLACE_CACTUS_TIMER.start();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}