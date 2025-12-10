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
   private void setupUI() {
        frame.setTitle("Tabbed Chat App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // 1. HEADER
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, COLOR_HEADER_1, getWidth(), 0, COLOR_HEADER_2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(400, 50));
        header.add(new JLabel("<html><span style='color:white; font-size:16px; font-weight:bold'>Chat Application</span></html>"));
        frame.add(header, BorderLayout.NORTH);

        // 2. CENTER - TABBED PANE
        // Create the default Global Chat tab
        createChatTab("Global"); 
        frame.add(tabbedPane, BorderLayout.CENTER);

        // 3. SIDEBAR - USER LIST
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(180, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY));
        
        JLabel sidebarTitle = new JLabel(" ONLINE USERS");
        sidebarTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        sidebarTitle.setBorder(new EmptyBorder(10, 5, 10, 5));
        sidebar.add(sidebarTitle, BorderLayout.NORTH);

        userListUI.setCellRenderer(new UserListRenderer());
        
        // DOUBLE CLICK TO OPEN TAB
        userListUI.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    String target = userListUI.getSelectedValue();
                    if (target != null && !target.equals(myName)) {
                        createChatTab(target); // Open tab for this user
                    }
                }
            }
        });
        
        sidebar.add(new JScrollPane(userListUI), BorderLayout.CENTER);
        frame.add(sidebar, BorderLayout.EAST);

        // 4. BOTTOM - INPUT
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JButton sendBtn = new JButton("Send");
        
        ActionListener sendAction = e -> sendMessage();
        inputField.addActionListener(sendAction);
        sendBtn.addActionListener(sendAction);
        
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendBtn, BorderLayout.EAST);
        frame.add(bottomPanel, BorderLayout.SOUTH);
    }
    
    
    
}