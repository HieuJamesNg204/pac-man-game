import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Pac Man");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        PacManPanel pacManPanel = new PacManPanel();
        frame.add(pacManPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        pacManPanel.requestFocus();
        frame.setVisible(true);
    }
}