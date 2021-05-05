package fileManager.server;

import fileManager.client.Client;
import javafx.scene.control.TreeItem;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * Wątek obsługujący połączenie klientów z serwerem.
 */
public class ClientHandler extends Thread {
    final Connection connection;
    final TreeItem<String> root;

    private String username;
    private String offlinePath;
    private String path;
    private File offlineDir;
    private File dir;

    /**
     * Konstruktor klasy.
     * @param connection instancja klasy Connection zawierająca informacje na temat połączenia
     * @param root dostęp do pierwszego elementu TreeView
     */
    ClientHandler(Connection connection, TreeItem<String> root) {
        this.connection = connection;
        this.root = root;
    }

    @Override
    public void run() {
        try {
            username = connection.dis.readUTF();

            System.out.println("Klient " + username + " połączony.");
            ServerMain.usersList.add(username);

            String offlinePath = "users_offline" + File.separator + username;
            String path = "users" + File.separator + username;

            offlineDir = new File(offlinePath);
            dir = new File(path);

            // System użytkowników online (przenoszenie folderów)
//            if(offlineDir.exists()) {
//                Files.move(offlineDir.toPath(), dir.toPath());
//            } else {
//                 dir.mkdir();
//            }

            if(!dir.exists()) {
                dir.mkdir();
            }

            while (ServerMain.isAlive) {
                if (connection.dis.readUTF().equals("send_to")) {
                    System.out.println("IT WORKS!");
                    String recipient = connection.dis.readUTF();
                    String filePath = connection.dis.readUTF();
                    String fileName = connection.dis.readUTF();

                    System.out.println(username + " -> " + filePath + " -> " + recipient);

                    System.out.println(dir + File.separator + filePath);
                    System.out.println("users" + File.separator + recipient);

                    Files.copy(Paths.get(dir + File.separator + filePath), Paths.get("users" + File.separator + recipient + File.separator + fileName), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    String clientFileListString = connection.dis.readUTF();
                    String serverFileListString = "";

                    if (dir.list() != null) {
                        serverFileListString = createDirectoryTreeString(dir, serverFileListString);
                    }

                    if (serverFileListString != null && serverFileListString.length() > 1) {
                        serverFileListString = serverFileListString.replaceAll(Matcher.quoteReplacement(dir.getAbsolutePath() + File.separator), "");
                    }

                    assert serverFileListString != null;
                    ArrayList<String> serverFileList = new ArrayList<>(Arrays.asList(serverFileListString.split(",")));
                    ArrayList<String> clientFileList = new ArrayList<>(Arrays.asList(clientFileListString.split(",")));

                    // Pobieranie plików od klienta
                    for (String filename : clientFileList) {
                        if (!serverFileList.contains(filename)) {
                            connection.dos.writeUTF(filename);

                            String type = connection.dis.readUTF();

                            if (type.equals("dir")) {
                                Files.createDirectories(Paths.get(dir.toString() + File.separator + filename));
                                continue;
                            }

                            Long size = connection.dis.readLong();
                            int sendSize = Integer.parseInt(size.toString());
                            byte[] mybytearray = new byte[1024 * 1024 * 100];

                            FileOutputStream fos = new FileOutputStream(Paths.get(dir.toString() + File.separator + filename).toFile());
                            System.out.println("Pobieram plik " + dir.toString() + File.separator + filename);
                            BufferedOutputStream bos = new BufferedOutputStream(fos);
                            long bytesRead;
                            while (size > 0 && (bytesRead = connection.dis.read(mybytearray, 0, (int) Math.min(mybytearray.length, size))) != -1) {
                                size -= bytesRead;
                            }
                            bos.write(mybytearray, 0, sendSize);
                            bos.flush();

                            System.out.println("Pobrano plik.");
                            bos.close();
                            fos.close();
                        }
                    }
                    connection.dos.writeUTF("?done");

                    // Wysyłanie plików do klienta
                    for (String filename : serverFileList) {
                        if (!clientFileList.contains(filename)) {
                            System.out.println("Wysyłam plik: " + filename);

                            connection.dos.writeUTF(filename);

                            if (new File(dir + File.separator + filename).isDirectory()) {
                                connection.dos.writeUTF("dir");
                                continue;
                            } else {
                                connection.dos.writeUTF("file");
                            }

                            connection.dos.writeLong(Files.size(Paths.get(dir.toString() + File.separator + filename)));
                            File myFile = new File(dir.toString() + File.separator + filename);
                            byte[] mybytearray = new byte[(int) myFile.length()];
                            FileInputStream fis = new FileInputStream(myFile);
                            BufferedInputStream bis = new BufferedInputStream(fis);
                            bis.read(mybytearray, 0, mybytearray.length);
                            System.out.println("Wysyłanie " + dir.toString() + File.separator + filename + " (" + mybytearray.length + "B)");
                            connection.dos.write(mybytearray, 0, mybytearray.length);
                            connection.dos.flush();
                        }
                    }
                    connection.dos.writeUTF("?done");

                    String users = "";

                    // Lista wszystkich użytkowników
                    //  for (TreeItem<String> item : root.getChildren()) {
                    //      users += item.getValue() + "\n";
                    //  }

                    // Lista zalogowanych użytkowników
                    for (String item : ServerMain.usersList) {
                        users += item + "\n";
                    }

                    connection.dos.writeUTF(users);
                }
            }
        } catch (IOException e) {
//            try {
//                // System użytkowników online (przenoszenie folderów)
//                Files.move(dir.toPath(), offlineDir.toPath());
//            } catch (IOException ioException) {
//                ioException.printStackTrace();
//            }

            ServerMain.usersList.remove(username);
            System.out.println("Klient " + username + " rozłączony.");
        }
    }

    /**
     * Metoda generująca listę ścieżek do wszystkich plików i katalogów użytkownika.
     * @param root element, od którego rozpocznie się generacja listy
     * @param tree String przechowujący dotychczas zgromadzone ścieżki
     * @return ciąg znaków zawierający ścieżki do plików i katalogów oddzielone przecinkiem
     * @throws IOException
     */
    private String createDirectoryTreeString(File root, String tree) throws IOException {
        if(root.listFiles() == null || tree == null)
            return tree;
        try {
            for (int i = 0; i < Objects.requireNonNull(root.listFiles()).length; i++) {
                if(root.listFiles()==null)
                    return tree;
                File temp = Objects.requireNonNull(root.listFiles())[i];
                tree += temp.getCanonicalPath() + ',';
                if (temp.listFiles() != null)
                    tree+= createDirectoryTreeString(temp, tree);

            }
        }catch (NullPointerException npe)
        {
            System.out.println("There was a problem while reading your local files.\nERROR: " + npe.getMessage());
            return null;
        }
        return tree;
    }
}

