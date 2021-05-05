package fileManager.client;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Typ wyliczeniowy służący do sprawdzania statusu połączenia klienta serwerem.
 */
enum Connection {
     PENDING, REFUSED, CONNECTED
}

/**
 * Klasa przechowuje dane o połączeniu z serwerem, a także referencje do obiektów globalnych.
 */
public class Client {
     @FXML
     public static Button sendFileButton;

     public static Socket s = null;
     public static DataInputStream dis = null;
     public static DataOutputStream dos = null;
     public static String login = null;
     public static Connection connection = Connection.PENDING;

     public static TreeItem<String> root;
     public static File userDir;
     public static ObservableList<String> usersList;

     public static String users;
}

