import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class PacManFrame extends JFrame {
    public PacManFrame() {
        setTitle("Pac-Man");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        PacManPanel pacManPanel = new PacManPanel();
        add(pacManPanel);
        pack();
        setLocationRelativeTo(null);
        requestFocus();

        Image image = new ImageIcon(Objects.requireNonNull(getClass().getResource("/pacmanIcon.png")))
                .getImage();
        setIconImage(image);

        setVisible(true);
    }
}