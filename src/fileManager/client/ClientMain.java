package fileManager.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Główna klasa aplikacji klienckiej.
 */
public class ClientMain extends Application {
    private static Stage stage;
    static boolean isAlive = true;
    static List<String> mainParameters;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        mainParameters = getParameters().getRaw();

        Parent root = FXMLLoader.load(getClass().getResource("clientGUI.fxml"));
        primaryStage.setTitle("Menedżer plików");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("../icons/icon_16.png")));
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

    public static Stage getStage() {
        return stage;
    }
}
