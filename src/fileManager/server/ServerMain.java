package fileManager.server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Główna klasa aplikacji serwerowej.
 */
public class ServerMain extends Application {
    static boolean isAlive = true;
    static ArrayList<String> usersList = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("serverGUI.fxml"));
        primaryStage.setTitle("Serwer menedżera plików");
        primaryStage.setScene(new Scene(root, 330, 400));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("../icons/icon_server_16.png")));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() {
        System.out.println("Zamykam za 3, 2, 1...");
        isAlive = false;

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
