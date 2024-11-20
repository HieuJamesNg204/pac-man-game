import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Objects;

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
            this.direction = direction;
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

    private Image pacmanUp;
    private Image pacmanDown;
    private Image pacmanLeft;
    private Image pacmanRight;

    //X = wall, O = skip, P = pac man, ' ' = food
    //Ghosts: b = blue, o = orange, p = pink, r = red
    private String[] tileMap = {
            "XXXXXXXXXXXXXXXXXXX",
            "X        X        X",
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
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX"
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    Block pacman;

    Timer gameLoop;

    PacManPanel() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        // Load ghost images
        wallImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/wall.png"))).getImage();
        blueGhost = new ImageIcon(Objects.requireNonNull(getClass().getResource("/blueGhost.png"))).getImage();
        orangeGhost = new ImageIcon(Objects.requireNonNull(getClass().getResource("/orangeGhost.png")))
                .getImage();
        pinkGhost = new ImageIcon(Objects.requireNonNull(getClass().getResource("/pinkGhost.png"))).getImage();
        redGhost = new ImageIcon(Objects.requireNonNull(getClass().getResource("/redGhost.png"))).getImage();

        // Load Pac Man images
        pacmanUp = new ImageIcon(Objects.requireNonNull(getClass().getResource("/pacmanUp.png"))).getImage();
        pacmanDown = new ImageIcon(Objects.requireNonNull(getClass().getResource("/pacmanDown.png"))).getImage();
        pacmanLeft = new ImageIcon(Objects.requireNonNull(getClass().getResource("/pacmanLeft.png"))).getImage();
        pacmanRight = new ImageIcon(Objects.requireNonNull(getClass().getResource("/pacmanRight.png")))
                .getImage();

        loadMap();
        gameLoop = new Timer(50, this);
        gameLoop.start();
    }

    public void loadMap() {
        walls = new HashSet<>();
        foods = new HashSet<>();
        ghosts = new HashSet<>();

        for (int r = 0; r < ROW_COUNT; r++) {
            for (int c = 0; c < COLUMN_COUNT; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c * TILE_SIZE;
                int y = r * TILE_SIZE;

                if (tileMapChar == 'X') {
                    Block wall = new Block(x, y, TILE_SIZE, TILE_SIZE, wallImage);
                    walls.add(wall);
                } else if (tileMapChar == 'b') {
                    Block ghost = new Block(x, y, TILE_SIZE, TILE_SIZE, blueGhost);
                    ghosts.add(ghost);
                } else if (tileMapChar == 'o') {
                    Block ghost = new Block(x, y, TILE_SIZE, TILE_SIZE, orangeGhost);
                    ghosts.add(ghost);
                } else if (tileMapChar == 'p') {
                    Block ghost = new Block(x, y, TILE_SIZE, TILE_SIZE, pinkGhost);
                    ghosts.add(ghost);
                } else if (tileMapChar == 'r') {
                    Block ghost = new Block(x, y, TILE_SIZE, TILE_SIZE, redGhost);
                    ghosts.add(ghost);
                } else if (tileMapChar == 'P') {
                    pacman = new Block(x, y, TILE_SIZE, TILE_SIZE, pacmanRight);
                } else if (tileMapChar == ' ') {
                    Block food = new Block(x + 14, y + 14, 4, 4, null);
                    foods.add(food);
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, null);
        }

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, null);
        }

        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }
    }

    public void move() {
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        for (Block wall : walls) {
            if (collides(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
            }
        }
    }

    public boolean collides(Block a, Block b) {
        return a.x < b.x + b.width
                && a.x + a.width > b.x
                && a.y < b.y + b.height
                && a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> pacman.setDirection('U');
            case KeyEvent.VK_DOWN -> pacman.setDirection('D');
            case KeyEvent.VK_LEFT -> pacman.setDirection('L');
            case KeyEvent.VK_RIGHT -> pacman.setDirection('R');
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}