package fileManager.server;

import fileManager.Tree;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Wątek służący do aktualizowania listy plików w elemencie TreeView aplikacji serwerowej.
 */
class ServerTreeViewRefresher extends Thread {
    final TreeView<String> treeView;
    final TreeItem<String> root;
    final File dir;

    /**
     * Konstruktor wątku.
     * @param treeView element TreeView przechowujący drzewo plików użytkowników
     * @param root korzeń drzewa TreeView
     * @param dir katalog użytkowników
     */
    ServerTreeViewRefresher(TreeView<String> treeView, TreeItem<String> root, File dir) {
        this.treeView = treeView;
        this.root = root;
        this.dir = dir;
    }

    @Override
    public void run() {
        root.getChildren().clear();
        treeView.setRoot(getUsersTreeView(root, dir, 0));

        while (ServerMain.isAlive) {
            Tree<String> itemsTree = new Tree<>(dir.getName());
            Tree<String> filesTree = new Tree<>(dir.getName());

            getFileNames(dir, filesTree);

            filesTree.setData("Użytkownicy");

            checkIntegrity(filesTree, root);

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Metoda wykrywająca zmiany w folderze i aktualizująca element TreeView aplikacji serwerowej.
     * @param left pliki na dysku
     * @param right pliki w TreeView
     */
    void checkIntegrity(Tree<String> left, TreeItem<String> right) {
        if(!left.getData().equals(right.getValue())) {
//            System.out.println("Niezgodność plików (nazwy):");
//            System.out.println("Ścieżka: " + left.getRootPath());
//            System.out.println("Element: " + right.getValue());

            File changedDir = new File(dir.getName() + File.separator + left.getRootPath());
            TreeItem<String> parent = right.getParent();
            parent.getChildren().clear();
            if(left.getRootPath().equals("")) {
                System.out.println(getUsersTreeView(parent, changedDir, 0).getValue());
            } else {
                System.out.println(getUsersTreeView(parent, changedDir, 1).getValue());
            }
            return;
        }

        if(left.getChildren().size() != right.getChildren().size()) {
//            System.out.println("Niezgodność plików");
//            System.out.println("Ścieżka: " + left.getRootPath());
//            System.out.println("Element: " + right.getValue());

            String directoryName = (right.getValue().equals(root.getValue()) ? "" : right.getValue());

            File changedDir = new File(dir.getName() + File.separator + left.getRootPath() + directoryName);
            right.getChildren().clear();

            if(directoryName.equals("")) {
                getUsersTreeView(right, changedDir, 0).getValue();
            } else {
                getUsersTreeView(right, changedDir, 1).getValue();
            }

            return;
        }

        for(int i = 0; i < left.getChildren().size(); i++) {
            checkIntegrity(left.getChildren().get(i), right.getChildren().get(i));
        }
    }

    /**
     * Metoda generująca strukturę dla instancji klasy Tree dla listy plików.
     * @param file plik, od którego ma być generowane drzewo plików
     * @param node obiekt klasy Tree, na której ma zostać zbudowana struktura
     */
    void getFileNames(File file, Tree<String> node) {
        node.setData(file.getName());

        File[] files = file.listFiles();

        if(files == null) {
            return;
        }

        for(File f : files) {
            if(f.isDirectory()) {
                getFileNames(f, node.addChild(f.getName()));
            }
        }

        for(File f : files) {
            if(f.isFile()) {
                getFileNames(f, node.addChild(f.getName()));
            }
        }
    }

    /**
     * Metoda generująca strukturę elementu TreeView (lista plików użytkowników)
     * @param root korzeń obiektu TreeView, do którego będą dołączane kolejne pliki i foldery
     * @param file folder, od którego ma być generowane drzewo plików
     * @param n poziom zagnieżdżenia (do rozróżniania ikonek klientów/folderów)
     * @return referencja do korzenia drzewa
     */
    TreeItem<String> getUsersTreeView(TreeItem<String> root, File file, int n) {
//        for(int i = 0; i < n; ++i) {
//            System.out.print("\t");
//        }
//
//        System.out.println(file.getName());

        File[] files = file.listFiles();

        if(files == null) {
            return root;
        }

        // Adding directories first
        for(File f : files) {
            if(f.isDirectory()) {
                Image img;

                if(n == 0) {
                    img = new Image(getClass().getResourceAsStream("../icons/user_16.png"));
                } else {
                    img = new Image(getClass().getResourceAsStream("../icons/folder_16.png"));
                }

                Node icon = new ImageView(img);

                TreeItem<String> current = new TreeItem<>(f.getName());
                current.setGraphic(icon);

                root.getChildren().add(current);
                getUsersTreeView(current, f, n + 1);
            }
        }

        // Adding files
        for(File f : files) {
            if(f.isFile()) {
                Node icon = new ImageView(new Image(getClass().getResourceAsStream("../icons/file_16.png")));

                TreeItem<String> current = new TreeItem<>(f.getName());
                current.setGraphic(icon);

                root.getChildren().add(current);
            }
        }

        return root;
    }
}