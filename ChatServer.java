import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    // Map to store Name -> Output Stream
    private static Map<String, PrintWriter> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        System.out.println("Tabbed Chat Server running on port 59001...");
        ServerSocket listener = new ServerSocket(8080);
        ExecutorService pool = Executors.newFixedThreadPool(20);

        try {
            while (true) {
                pool.execute(new Handler(listener.accept()));
            }
        } finally {
            listener.close();
        }
    }

    private static class Handler implements Runnable {
        private String name;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    out.println("SUBMITNAME");
                    name = in.nextLine();
                    if (name == null) return;
                    synchronized (clients) {
                        if (!name.isEmpty() && !clients.containsKey(name)) {
                            clients.put(name, out);
                            break;
                        }
                    }
                }

                out.println("NAMEACCEPTED " + name);
                broadcast("MESSAGE Server: " + name + " has joined.");
                broadcastUserList();

                while (true) {
                    String input = in.nextLine();
                    if (input.toLowerCase().startsWith("/quit")) return;

                    if (input.startsWith("/w ")) {
                        // NEW PROTOCOL: /w target msg
                        String[] parts = input.split(" ", 3);
                        if (parts.length == 3) {
                            String target = parts[1];
                            String msg = parts[2];
                            sendPrivateMessage(target, msg);
                        }
                    } else {
                        broadcast("MESSAGE " + name + ": " + input);
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                if (name != null) {
                    clients.remove(name);
                    broadcast("MESSAGE Server: " + name + " has left.");
                    broadcastUserList();
                }
                try { socket.close(); } catch (IOException e) {}
            }
        }

        private void sendPrivateMessage(String targetName, String msg) {
            PrintWriter targetOut = clients.get(targetName);
            if (targetOut != null) {
                // SEND NEW PROTOCOL: PRIVATEMSG Sender Target MessageContent
                String protocolMsg = "PRIVATEMSG " + name + " " + targetName + " " + msg;
                
                // Send to Target
                targetOut.println(protocolMsg);
                // Send to Sender (so it appears in their tab too)
                out.println(protocolMsg);
            } else {
                out.println("MESSAGE Server: User " + targetName + " not found.");
            }
        }

        
    }
}