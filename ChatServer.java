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

    
}