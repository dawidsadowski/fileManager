package fileManager.server;

import javafx.scene.control.TreeItem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Wątek inicjalizujący połączenie z klientami.
 */
public class ServerInit extends Thread {
    final File usersDir;

    ServerSocket ss = new ServerSocket(5056);
    ArrayList<Connection> clients = new ArrayList<>();
    TreeItem<String> root;

    ServerInit(File usersDir, TreeItem<String> root) throws IOException {
        this.usersDir = usersDir;
        this.root = root;
    }

    @Override
    public void run() {
        while(ServerMain.isAlive) {
            try {
                Socket s = ss.accept();
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                clients.add(new Connection(s, dis, dos));

                Thread clientHandler = new ClientHandler(clients.get(clients.size() - 1), root);
                clientHandler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
