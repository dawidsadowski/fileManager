package fileManager.client;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Główny kontroler aplikacji klienckiej.
 * Zawiera definicje elementów interfejsu aplikacji oraz podstawowe funkcjonalności.
 */
public class ClientController {
    static boolean isLoggedIn = false;

    @FXML
    private TreeView<String> userTreeView;
    @FXML
    private TextField directoryTextField, loginTextField;
    @FXML
    private Button directoryButton, loginButton, sendFileButton;
    @FXML
    private ListView<String> usersListView;

    public Thread treeViewRefresher = null;
    public Thread connectionHandler = null;

    @FXML
    public void initialize() {
        if(directoryTextField.getText().equals("")) {
            directoryTextField.setText(ClientMain.mainParameters.get(0));
            loginTextField.setText(ClientMain.mainParameters.get(1));
        }

        Client.sendFileButton = sendFileButton;

        Client.usersList = FXCollections.observableArrayList();
        usersListView.setItems(Client.usersList);

        Client.root = new TreeItem<>("[Zaloguj się, aby ujrzeć swoje pliki]");
        userTreeView.setRoot(Client.root);
    }

    /**
     * Akcja dla przycisku chooseDirectoryButton.
     */
    @FXML
    void chooseDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File directory = directoryChooser.showDialog(userTreeView.getScene().getWindow());
        directoryTextField.setText(directory.getPath());
    }

    /**
     * Akcje dla przycisku loginButton.
     * @throws IOException
     * @throws InterruptedException
     */
    @FXML
    void login() throws IOException, InterruptedException {
        if(!isLoggedIn) {
            String path = directoryTextField.getText();
            Client.login = loginTextField.getText();

            if (path.equals("") || Client.login.equals("")) {
                return;
            }

            Node rootIcon = new ImageView(new Image(getClass().getResourceAsStream("../icons/user_16.png")));
            Client.userDir = new File(path);

            if(!Client.userDir.exists()) {
                Client.userDir.mkdir();
            }

            Client.root.setValue(Client.login);
            Client.root.setGraphic(rootIcon);
            Client.root.setExpanded(true);

            connectionHandler = new ConnectionHandler();
            connectionHandler.start();

            System.out.println("Próba łączenia z serwerem...");
            setTitle("łączenie z serwerem...");
            TimeUnit.SECONDS.sleep(1);

            while(Client.connection == Connection.PENDING) {
                System.out.println("Ponowna próba łączenia z serwerem...");
                TimeUnit.SECONDS.sleep(1);
            }

            if(Client.connection == Connection.REFUSED) {
                System.out.println("Wystąpił problem podczas próby połączenia z serwerem.");
                setTitle("wystąpił problem podczas łączenia z serwerem");
                initialize();
                return;
            }

            setTitle("połączono");

            treeViewRefresher = new ClientTreeViewRefresher(Client.root, Client.userDir, Client.login);
            treeViewRefresher.start();

            isLoggedIn = true;
            directoryTextField.setDisable(true);
            loginTextField.setDisable(true);
            directoryButton.setDisable(true);
            sendFileButton.setDisable(false);
            loginButton.setText("Wyloguj się");
        } else {
            setTitle("rozłączono");
            isLoggedIn = false;

            while(treeViewRefresher.isAlive());

            Client.dos.close();
            Client.dis.close();
            Client.s.close();

            connectionHandler.stop();
            initialize();

            directoryTextField.setDisable(false);
            loginTextField.setDisable(false);
            directoryButton.setDisable(false);
            sendFileButton.setDisable(true);
            loginButton.setText("Zaloguj się");
        }
    }

    /**
     * Akcja dla przycisku sendFileButton.
     * @throws IOException
     */
    @FXML
    void sendFile() throws IOException {
        if(userTreeView.getSelectionModel().getSelectedItem() == null || usersListView.getSelectionModel().getSelectedItem() == null) {
            return;
        }

        Client.dos.writeUTF("send_to");
        Client.dos.writeUTF(usersListView.getSelectionModel().getSelectedItem());
        Client.dos.writeUTF(getPath(userTreeView.getSelectionModel().getSelectedItem()));
        Client.dos.writeUTF(userTreeView.getSelectionModel().getSelectedItem().getValue());
    }

    /**
     * Funkcja generująca ścieżkę do korzenia elementu TreeView.
     * @param item referencja do obiektu TreeItem
     * @return
     */
    String getPath(TreeItem<String> item) {
        String path = item.getValue();

        while(item.getParent() != null) {
            item = item.getParent();
            if(item.getParent() != null) {
                path = item.getValue() + File.separator + path;
            }
        }

        return path;
    }

    public static void setTitle(String newTitle) {
        ClientMain.getStage().setTitle("Menedżer plików - " + newTitle);
    }
}
