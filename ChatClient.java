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
    
    // --- TAB LOGIC ---
    
    private void createChatTab(String tabName) {
        if (chatPanels.containsKey(tabName)) {
            // Tab exists, switch to it
            tabbedPane.setSelectedIndex(tabbedPane.indexOfTab(tabName));
            return;
        }

        // Create new Panel for this chat
        JPanel chatBox = new JPanel();
        chatBox.setLayout(new BoxLayout(chatBox, BoxLayout.Y_AXIS));
        chatBox.setBackground(new Color(245, 245, 245));
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(chatBox, BorderLayout.NORTH);
        
        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        
        chatPanels.put(tabName, chatBox);
        chatScrolls.put(tabName, scroll);
        
        tabbedPane.addTab(tabName, scroll);
        tabbedPane.setSelectedIndex(tabbedPane.indexOfTab(tabName)); // Switch to new tab
    }
    
    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (msg.isEmpty()) return;
        
        String currentTab = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
        
        if (currentTab.equals("Global")) {
            out.println(msg); // Send normally
        } else {
            // It's a private tab, send using /w
            out.println("/w " + currentTab + " " + msg);
        }
        inputField.setText("");
    }

    // --- NETWORK LOGIC ---

    private void run() {
        try {
            Socket socket = new Socket(serverAddress, 8080);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            while (in.hasNextLine()) {
                String line = in.nextLine();
                
                if (line.startsWith("SUBMITNAME")) {
                    String name = JOptionPane.showInputDialog(frame, "Enter Name:");
                    out.println(name);
                } 
                else if (line.startsWith("NAMEACCEPTED")) {
                    myName = line.substring(13);
                    frame.setTitle("Chat - " + myName);
                    frame.setVisible(true);
                } 
                else if (line.startsWith("USERLIST")) {
                    String[] users = line.substring(8).split(",");
                    SwingUtilities.invokeLater(() -> {
                        userListModel.clear();
                        for (String u : users) { if(!u.trim().isEmpty()) userListModel.addElement(u); }
                    });
                } 
                else if (line.startsWith("MESSAGE")) {
                    // PUBLIC MESSAGE
                    String content = line.substring(8); // "Name: Hello"
                    String senderName = content.split(":")[0];
                    String msgText = content.contains(":") ? content.substring(content.indexOf(":")+1).trim() : content;
                    
                    addMessageToTab("Global", senderName, msgText, senderName.equals(myName), false);
                } 
                else if (line.startsWith("PRIVATEMSG")) {
                    // PROTOCOL: PRIVATEMSG Sender Target Message
                    String[] parts = line.split(" ", 4);
                    String sender = parts[1];
                    String target = parts[2];
                    String msg = parts[3];
                    
                    if (sender.equals(myName)) {
                        // I sent it to Target -> Show in Target's tab
                        createChatTab(target); 
                        addMessageToTab(target, "Me", msg, true, true);
                    } else {
                        // Sender sent it to Me -> Show in Sender's tab
                        createChatTab(sender);
                        addMessageToTab(sender, sender, msg, false, true);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- UI HELPERS ---

    private void addMessageToTab(String tabName, String sender, String text, boolean isMe, boolean isPrivate) {
        if (!chatPanels.containsKey(tabName)) createChatTab(tabName);
        
        JPanel panel = chatPanels.get(tabName);
        
        // Bubble Logic
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        Color bubbleColor = isMe ? COLOR_MY_BUBBLE : (isPrivate ? COLOR_PRIVATE_THEIR : COLOR_THEIR_BUBBLE);
        
        RoundedPanel bubble = new RoundedPanel(15, bubbleColor);
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBorder(new EmptyBorder(8, 10, 8, 10));
        
        JLabel txt = new JLabel("<html><p style='width:200px; color:white'>" + text + "</p></html>");
        if (!isMe && !isPrivate) { // Global chat: show name
            JLabel nameLbl = new JLabel(sender);
            nameLbl.setForeground(Color.LIGHT_GRAY);
            nameLbl.setFont(new Font("Arial", Font.BOLD, 10));
            bubble.add(nameLbl);
        }
        bubble.add(txt);
        
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.add(bubble);
        
        if (isMe) row.add(wrapper, BorderLayout.EAST);
        else row.add(wrapper, BorderLayout.WEST);
        
        panel.add(row);
        panel.revalidate();
        panel.repaint();
        
        // Auto scroll
        JScrollPane scroll = chatScrolls.get(tabName);
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum()));
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception e){}
        new ChatClient("localhost").run();
    }
    
    // --- UTILS ---
    class UserListRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            lbl.setIcon(new Icon() {
                public void paintIcon(Component c, Graphics g, int x, int y) { g.setColor(Color.GREEN); g.fillOval(x, y+4, 8, 8); }
                public int getIconWidth() { return 12; }
                public int getIconHeight() { return 12; }
            });
            return lbl;
        }
    }
    
    class RoundedPanel extends JPanel {
        int r; Color c;
        RoundedPanel(int r, Color c) { this.r=r; this.c=c; setOpaque(false); }
        protected void paintComponent(Graphics g) {
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(c); g.fillRoundRect(0,0,getWidth(),getHeight(),r,r);
        }
    }
    
}