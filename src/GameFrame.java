import javax.swing.JFrame;

public class GameFrame extends JFrame {

    // Constructor to set up the game frame
    GameFrame(){
        this.add(new GamePanel()); // Add the game panel to the frame
        this.setTitle("Snake"); // Set the window title
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Set the close operation
        this.setResizable(false); // Disable resizing of the window
        this.pack(); // Adjust the window size to fit the preferred size of the panel
        this.setVisible(true); // Make the window visible
        this.setLocationRelativeTo(null); // Center the window
    }
}
