package fileManager.client;

import fileManager.Tree;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Wątek służący do aktualizowania listy plików w elemencie TreeView aplikacji klienckiej.
 */
class ClientTreeViewRefresher extends Thread {
    final TreeItem<String> root;
    final File dir;
    final String login;

    /**
     * Konstruktor wątku.
     * @param root korzeń drzewa TreeView
     * @param dir katalog użytkownika
     * @param login login użytkownika
     */
    ClientTreeViewRefresher(TreeItem<String> root, File dir, String login) {
        this.root = root;
        this.dir = dir;
        this.login = login;
    }

    @Override
    public void run() {
        root.getChildren().clear();
        getUsersTreeView(root, dir, 0);

        while (ClientMain.isAlive && ClientController.isLoggedIn) {
            Tree<String> filesTree = new Tree<>(dir.getName());

//            getTreeViewItems(root, itemsTree);
            getFileNames(dir, filesTree);
            filesTree.setData(login);
            checkIntegrity(filesTree, root);

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Metoda wykrywająca zmiany w folderze i aktualizująca element TreeView aplikacji klienckiej.
     * @param left pliki na dysku
     * @param right pliki w TreeView
     */
    void checkIntegrity(Tree<String> left, TreeItem<String> right) {
        if(!left.getData().equals(right.getValue())) {
//            System.out.println("Niezgodność plików (nazwy):");
//            System.out.println("Ścieżka: " + dir.getPath() + File.separator + left.getRootPath());
//            System.out.println("Element: " + right.getValue());

            File changedDir = new File(dir.getPath() + File.separator + left.getRootPath());
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
            String directoryName = (right.getValue().equals(root.getValue()) ? "" : right.getValue());

            File changedDir = new File(dir.getPath() + File.separator + left.getRootPath() + directoryName);
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
     * Metoda generująca strukturę dla instancji klasy Tree dla elementów drzewa TreeView.
     * @param root referencja do obiektu TreeItem (korzeń, od którego ma być generowane drzewo plików)
     * @param node obiekt klasy Tree, na której ma zostać zbudowana struktura
     */
    void getTreeViewItems(TreeItem<String> root, Tree<String> node) {
        node.setData(root.getValue());

        root.getChildren().forEach(item -> {
            getTreeViewItems(item, node.addChild(item.getValue()));
        });
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
     * Metoda generująca strukturę elementu TreeView (lista plików użytkownika)
     * @param root korzeń obiektu TreeView, do którego będą dołączane kolejne pliki i foldery
     * @param file folder, od którego ma być generowane drzewo plików
     * @param n poziom zagnieżdżenia (do rozróżniania ikonek klientów/folderów)
     * @return referencja do korzenia drzewa
     */
    TreeItem<String> getUsersTreeView(TreeItem<String> root, File file, int n) {

        File[] files = file.listFiles();

        if(files == null) {
            return root;
        }

        // Adding directories first
        for(File f : files) {
            if(f.isDirectory()) {
                Image img;

                img = new Image(getClass().getResourceAsStream("../icons/folder_16.png"));

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