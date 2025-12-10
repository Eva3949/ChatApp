import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ChatClient {

    String serverAddress;
    Scanner in;
    PrintWriter out;
    String myName;
    
    JFrame frame = new JFrame();
    JTextField inputField = new JTextField();
    
    // TABS SYSTEM
    JTabbedPane tabbedPane = new JTabbedPane();
    
    // Store Chat Panels: "Global" -> Panel, "Bob" -> Panel
    Map<String, JPanel> chatPanels = new HashMap<>(); 
    Map<String, JScrollPane> chatScrolls = new HashMap<>();

    // User List
    DefaultListModel<String> userListModel = new DefaultListModel<>();
    JList<String> userListUI = new JList<>(userListModel);

    // Colors
    Color COLOR_HEADER_1 = new Color(0, 120, 255);
    Color COLOR_HEADER_2 = new Color(0, 180, 255);
    Color COLOR_MY_BUBBLE = new Color(0, 132, 255);
    Color COLOR_THEIR_BUBBLE = new Color(100, 100, 100);
    Color COLOR_PRIVATE_THEIR = new Color(255, 100, 150); // Pink for them

    public ChatClient(String serverAddress) {
        this.serverAddress = serverAddress;
        setupUI();
    }

    
}