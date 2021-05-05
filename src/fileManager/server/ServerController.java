package fileManager.server;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Główny kontroler serwera.
 * Zawiera definicje elementów interfejsu aplikacji oraz podstawowe funkcjonalności.
 */
public class ServerController implements Initializable {
    @FXML
    ContextMenu contextMenu;

    @FXML
    private TreeView<String> usersTreeView;
    private TreeItem<String> root;
    public Thread t = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Node rootIcon = new ImageView(new Image(getClass().getResourceAsStream("../icons/server_16.png")));

        File usersDir = new File("users");

        if(!usersDir.exists()) {
            usersDir.mkdir();
        }

        root = new TreeItem<>("Użytkownicy");
        root.setGraphic(rootIcon);
        root.setExpanded(true);

        Thread treeViewRefresher = new ServerTreeViewRefresher(usersTreeView, root, usersDir);
        treeViewRefresher.start();

        Thread serverInit = null;
        try {
            serverInit = new ServerInit(usersDir, root);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert serverInit != null;
        serverInit.start();
    }
}
