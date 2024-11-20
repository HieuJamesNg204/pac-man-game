import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;

public class PacManPanel extends JPanel implements ActionListener, KeyListener {
    public class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U';
        char queuedDirection = ' ';
        int velocityX = 0;
        int velocityY = 0;

        public Block(int x, int y, int width, int height, Image image) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.image = image;
            this.startX = x;
            this.startY = y;
        }

        public void setDirection(char direction) {
            // Store the requested direction
            this.queuedDirection = direction;

            // Try to turn immediately if possible
            tryTurn();
        }

        public void tryTurn() {
            // If no queued direction, nothing to do
            if (queuedDirection == ' ') {
                return;
            }

            // Save current position and direction
            int originalX = this.x;
            int originalY = this.y;
            char originalDirection = this.direction;

            // Try to move in the queued direction
            this.direction = queuedDirection;
            setVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;

            boolean canTurn = true;
            // Check if the turn would cause a collision
            for (Block wall : walls) {
                if (collides(this, wall)) {
                    canTurn = false;
                    break;
                }
            }

            // Restore position
            this.x = originalX;
            this.y = originalY;

            if (canTurn) {
                // If we can turn, make the turn and clear the queued direction
                this.direction = queuedDirection;
                this.queuedDirection = ' ';
                if (this == pacman) {
                    updatePacmanImage();
                }
            } else {
                // If we can't turn, keep the original direction but maintain the queued direction
                this.direction = originalDirection;
            }
            setVelocity();
        }

        public void setVelocity() {
            switch (this.direction) {
                case 'U' -> {
                    this.velocityX = 0;
                    this.velocityY = -TILE_SIZE / 4;
                }

                case 'D' -> {
                    this.velocityX = 0;
                    this.velocityY = TILE_SIZE / 4;
                }

                case 'L' -> {
                    this.velocityX = -TILE_SIZE / 4;
                    this.velocityY = 0;
                }

                case 'R' -> {
                    this.velocityX = TILE_SIZE / 4;
                    this.velocityY = 0;
                }
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
            this.queuedDirection = ' ';
            if (this == pacman) {
                this.direction = 'R';
                updatePacmanImage();
            }
        }
    }

    private final int ROW_COUNT = 21;
    private final int COLUMN_COUNT = 19;
    private final int TILE_SIZE = 32;
    private final int BOARD_WIDTH = COLUMN_COUNT * TILE_SIZE;
    private final int BOARD_HEIGHT = ROW_COUNT * TILE_SIZE;

    private Image wallImage;
    private Image blueGhost;
    private Image orangeGhost;
    private Image pinkGhost;
    private Image redGhost;

    private Image scaredGhost;
    private boolean powerPelletActive = false;
    private int powerPelletTimer = 0;
    private final int POWER_PELLET_DURATION = 200; // 10 seconds (50ms * 200)
    private final int GHOST_POINTS = 200;
    private HashSet<Block> eatenGhosts; // Track which ghosts have been eaten during power pellet

    private Image pacmanUp;
    private Image pacmanDown;
    private Image pacmanLeft;
    private Image pacmanRight;

    //X = wall, O = skip, P = pac man, ' ' = food, @ = power pellet
    //Ghosts: b = blue, o = orange, p = pink, r = red
    private String[] tileMap = {
            "XXXXXXXXXXXXXXXXXXX",
            "X@       X       @X",
            "X XX XXX X XXX XX X",
            "X                 X",
            "X XX X XXXXX X XX X",
            "X    X       X    X",
            "XXXX XXXX XXXX XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXrXX X XXXX",
            "O       bpo       O",
            "XXXX X XXXXX X XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "X@               @X",
            "XXXXXXXXXXXXXXXXXXX"
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> powerPellets;
    HashSet<Block> ghosts;
    Block pacman;

    Timer gameLoop;
    char[] ghostDirections = {'U', 'D', 'L', 'R'}; // Up Down Left Right
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean isGameOver = false;

    PacManPanel() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        // Load ghost images
        wallImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/wall.png"))).getImage();
        blueGhost = new ImageIcon(Objects.requireNonNull(getClass().getResource("/blueGhost.png")))
                .getImage();
        orangeGhost = new ImageIcon(Objects.requireNonNull(getClass().getResource("/orangeGhost.png")))
                .getImage();
        pinkGhost = new ImageIcon(Objects.requireNonNull(getClass().getResource("/pinkGhost.png")))
                .getImage();
        redGhost = new ImageIcon(Objects.requireNonNull(getClass().getResource("/redGhost.png"))).getImage();
        scaredGhost = new ImageIcon(Objects.requireNonNull(getClass().getResource("/scaredGhost.png")))
                .getImage();

        eatenGhosts = new HashSet<>();

        // Load Pac Man images
        pacmanUp = new ImageIcon(Objects.requireNonNull(getClass().getResource("/pacmanUp.png"))).getImage();
        pacmanDown = new ImageIcon(Objects.requireNonNull(getClass().getResource("/pacmanDown.png"))).getImage();
        pacmanLeft = new ImageIcon(Objects.requireNonNull(getClass().getResource("/pacmanLeft.png"))).getImage();
        pacmanRight = new ImageIcon(Objects.requireNonNull(getClass().getResource("/pacmanRight.png")))
                .getImage();

        loadMap();
        for (Block ghost : ghosts) {
            char ghostDirection = ghostDirections[random.nextInt(4)];
            ghost.setDirection(ghostDirection);
        }
        gameLoop = new Timer(50, this);
        gameLoop.start();
    }

    public void loadMap() {
        walls = new HashSet<>();
        foods = new HashSet<>();
        powerPellets = new HashSet<>();
        ghosts = new HashSet<>();

        for (int r = 0; r < ROW_COUNT; r++) {
            for (int c = 0; c < COLUMN_COUNT; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c * TILE_SIZE;
                int y = r * TILE_SIZE;

                switch (tileMapChar) {
                    case 'X' -> {
                        Block wall = new Block(x, y, TILE_SIZE, TILE_SIZE, wallImage);
                        walls.add(wall);
                    }

                    case 'b' -> {
                        Block ghost = new Block(x, y, TILE_SIZE, TILE_SIZE, blueGhost);
                        ghosts.add(ghost);
                    }

                    case 'o' -> {
                        Block ghost = new Block(x, y, TILE_SIZE, TILE_SIZE, orangeGhost);
                        ghosts.add(ghost);
                    }

                    case 'p' -> {
                        Block ghost = new Block(x, y, TILE_SIZE, TILE_SIZE, pinkGhost);
                        ghosts.add(ghost);
                    }

                    case 'r' -> {
                        Block ghost = new Block(x, y, TILE_SIZE, TILE_SIZE, redGhost);
                        ghosts.add(ghost);
                    }

                    case 'P' -> pacman = new Block(x, y, TILE_SIZE, TILE_SIZE, pacmanRight);
                    case ' ' -> {
                        Block food = new Block(x + 14, y + 14, 4, 4, null);
                        foods.add(food);
                    }

                    case '@' -> {
                        Block powerPellet = new Block(x + 14, y + 14, 12, 12, null);
                        powerPellets.add(powerPellet);
                    }
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (!isGameOver) {
            g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

            for (Block ghost : ghosts) {
                Image ghostImage;
                if (powerPelletActive && !eatenGhosts.contains(ghost)) {
                    ghostImage = scaredGhost;
                } else {
                    ghostImage = ghost.image;
                }
                g.drawImage(ghostImage, ghost.x, ghost.y, ghost.width, ghost.height, null);
            }

            for (Block wall : walls) {
                g.drawImage(wallImage, wall.x, wall.y, wall.width, wall.height, null);
            }

            g.setColor(Color.WHITE);
            for (Block food : foods) {
                g.fillRect(food.x, food.y, food.width, food.height);
            }

            for (Block powerPellet : powerPellets) {
                g.fillOval(powerPellet.x, powerPellet.y, powerPellet.width, powerPellet.height);
            }

            // Score
            g.setFont(new Font("Comic Sans MS", Font.PLAIN, 18));
            g.drawString("x" + lives + " Score: " + score, TILE_SIZE / 2, TILE_SIZE / 2);
        } else {
            gameOver(g);
        }
    }

    public void move() {
        pacman.tryTurn();

        // Teleport Pac Man to the other border when it reaches one
        if (pacman.x < 0) {
            pacman.x = BOARD_WIDTH;
        } else if (pacman.x > BOARD_WIDTH) {
            pacman.x = 0;
        }

        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        // Check wall collisions
        for (Block wall : walls) {
            if (collides(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
            }
        }

        // Handle power pellet collision
        Block pelletEaten = null;
        for (Block pellet : powerPellets) {
            if (collides(pacman, pellet)) {
                pelletEaten = pellet;
                powerPelletActive = true;
                powerPelletTimer = POWER_PELLET_DURATION;
                eatenGhosts.clear(); // Reset eaten ghosts when a new power pellet is consumed
                score += 50; // Score for eating a power pellet
            }
        }
        powerPellets.remove(pelletEaten);

        if (powerPelletActive) {
            powerPelletTimer--;
            if (powerPelletTimer <= 0) {
                powerPelletActive = false;
                eatenGhosts.clear();
            }
        }

        // Ghost movement and collision logic
        for (Block ghost : ghosts) {
            // Check if the ghost is at an intersection
            if (isIntersection(ghost)) {
                // 25% chance of a ghost changing its direction at the intersection
                if (random.nextInt(100) < 25) {
                    char newDirection = ghostDirections[random.nextInt(4)];
                    ghost.setDirection(newDirection);

                    // Ensure that the direction changed is not blocked
                    while (isBlocked(ghost.x + ghost.velocityX, ghost.y + ghost.velocityY)) {
                        newDirection = ghostDirections[random.nextInt(4)];
                        ghost.setDirection(newDirection);
                    }
                }
            }

            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            for (Block wall : walls) {
                if (collides(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= BOARD_WIDTH) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = ghostDirections[random.nextInt(4)];
                    ghost.setDirection(newDirection);
                }
            }

            // Ghost-Pac Man interaction
            if (collides(ghost, pacman)) {
                if (powerPelletActive && !eatenGhosts.contains(ghost)) {
                    // Ghost gets eaten
                    ghost.reset();
                    eatenGhosts.add(ghost);
                    score += GHOST_POINTS;
                    char newDirection = ghostDirections[random.nextInt(4)];
                    ghost.setDirection(newDirection);
                } else {
                    // Pac Man gets eaten
                    lives -= 1;
                    if (lives == 0) {
                        isGameOver = true;
                        return;
                    }
                    resetPosition();
                }
            }
        }

        // Check food collision
        Block foodEaten = null;
        for (Block food : foods) {
            if (collides(pacman, food)) {
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten);

        if (foods.isEmpty()) {
            loadMap();
            resetPosition();
        }
    }

    public void gameOver(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Comic Sans MS", Font.PLAIN, 18));
        g.drawString("Score: " + score, TILE_SIZE / 2, TILE_SIZE / 2);

        String gameOverMessage = "Game over :(";
        g.setColor(Color.RED);
        g.setFont(new Font("Comic Sans MS", Font.PLAIN, 75));
        FontMetrics messageMetrics = getFontMetrics(g.getFont());
        g.drawString(
                gameOverMessage,
                (BOARD_WIDTH - messageMetrics.stringWidth(gameOverMessage)) / 2,
                BOARD_HEIGHT / 2
        );

        String restartMessage = "Press Any Key to Restart";
        g.setFont(new Font("Comic Sans MS", Font.PLAIN, 25));
        FontMetrics restartMetrics = getFontMetrics(g.getFont());
        g.drawString(
                restartMessage,
                (BOARD_WIDTH - restartMetrics.stringWidth(restartMessage)) / 2,
                BOARD_HEIGHT / 2 + 50
        );
    }

    public boolean collides(Block a, Block b) {
        return a.x < b.x + b.width
                && a.x + a.width > b.x
                && a.y < b.y + b.height
                && a.y + a.height > b.y;
    }

    public void resetPosition() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        powerPelletActive = false;
        powerPelletTimer = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = ghostDirections[random.nextInt(4)];
            ghost.setDirection(newDirection);
        }
    }

    public boolean isIntersection(Block ghost) {
        int possibleDirections = 0;

        if (!isBlocked(ghost.x, ghost.y - TILE_SIZE)) {
            possibleDirections++; // Possible up direction
        }

        if (!isBlocked(ghost.x, ghost.y + TILE_SIZE)) {
            possibleDirections++; // Possible down direction
        }

        if (!isBlocked(ghost.x - TILE_SIZE, ghost.y)) {
            possibleDirections++; // Possible left direction
        }

        if (!isBlocked(ghost.x + TILE_SIZE, ghost.y)) {
            possibleDirections++; // Possible right direction
        }

        // If there are more than 2 possible directions, an intersection is detected
        return possibleDirections > 2;
    }

    public boolean isBlocked(int x, int y) {
        for (Block wall : walls) {
            if (wall.x == x && wall.y == y) {
                return true; // Blocked
            }
        }
        return x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT; // Out of bounds
    }

    public void updatePacmanImage() {
        switch (pacman.direction) {
            case 'U' -> pacman.image = pacmanUp;
            case 'D' -> pacman.image = pacmanDown;
            case 'L' -> pacman.image = pacmanLeft;
            case 'R' -> pacman.image = pacmanRight;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (isGameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (isGameOver) {
            loadMap();
            resetPosition();
            lives = 3;
            score = 0;
            isGameOver = false;
            gameLoop.start();
            return;
        }

        char requestedDirection = switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> 'U';
            case KeyEvent.VK_DOWN -> 'D';
            case KeyEvent.VK_LEFT -> 'L';
            case KeyEvent.VK_RIGHT -> 'R';
            default -> ' ';
        };

        if (requestedDirection != ' ') {
            pacman.setDirection(requestedDirection);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}