package fileManager.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Klasa przechowująca informacje na temat połączenia klienta z serwerem.
 */
public class Connection {
    final Socket s;
    final DataInputStream dis;
    final DataOutputStream dos;

    public Connection(Socket s, DataInputStream dis, DataOutputStream dos) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
    }

    void close() throws IOException {
        this.dis.close();
        this.dos.close();
        this.s.close();
    }
}
