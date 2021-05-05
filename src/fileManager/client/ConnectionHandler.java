package fileManager.client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

/**
 * Klasa obsługująca połączenie klienta z serwerem.
 */
public class ConnectionHandler extends Thread {
    @Override
    public void run() {
        try {
            InetAddress ip = InetAddress.getByName("localhost");
            Client.s = new Socket(ip, 5056);
            Client.dis = new DataInputStream(Client.s.getInputStream());
            Client.dos = new DataOutputStream(Client.s.getOutputStream());

            System.out.println("Uzyskano połączenie z serwerem.");

            try {
                Client.dos.writeUTF(Client.login);
                Client.connection = Connection.CONNECTED;

                // Synchronizacja plików
                while (true) {
                    Client.sendFileButton.setDisable(true);
                    Client.dos.writeUTF("synchronize");

                    String allFilesString = "";

                    if (Client.userDir.list() != null) {
                        allFilesString = createDirectoryTreeString(Client.userDir, allFilesString);
                    }

                    if (allFilesString != null && allFilesString.length() > 1) {
                        allFilesString = allFilesString.replaceAll(Matcher.quoteReplacement(Client.userDir.getAbsolutePath() + File.separator), "");
                    }

                    Client.dos.writeUTF(allFilesString);

                    // Wysyłanie plików na serwer
                    while (true) {
                        try {
                            String filename = Client.dis.readUTF();

                            if (filename.equals("?done")) {
                                break;
                            }

                            System.out.println(Client.userDir + File.separator + filename);

                            if (new File(Client.userDir + File.separator + filename).isDirectory()) {
                                Client.dos.writeUTF("dir");
                                continue;
                            } else {
                                Client.dos.writeUTF("file");
                            }

                            Client.dos.writeLong(Files.size(Paths.get(Client.userDir.toString() + File.separator + filename)));
                            File myFile = new File(Client.userDir.toString() + File.separator + filename);
                            byte[] mybytearray = new byte[(int) myFile.length()];
                            FileInputStream fis = new FileInputStream(myFile);
                            BufferedInputStream bis = new BufferedInputStream(fis);

                            bis.read(mybytearray, 0, mybytearray.length);

                            System.out.println("Wysyłanie " + Client.userDir.toString() + File.separator + filename + " (" + mybytearray.length + "B)");
                            Client.dos.write(mybytearray, 0, mybytearray.length);
                            Client.dos.flush();
                        } catch (Exception e) {
                            return;
                        }
                    }

                    // Pobieranie plików z serwera
                    while (true) {
                        String filename = Client.dis.readUTF();

                        if (filename.equals("?done")) {
                            break;
                        }

                        String type = Client.dis.readUTF();

                        if (type.equals("dir")) {
                            Files.createDirectories(Paths.get(Client.userDir.toString() + File.separator + filename));
                            continue;
                        }

                        Long size = Client.dis.readLong();
                        int sendSize = Integer.parseInt(size.toString());
                        byte[] mybytearray = new byte[1024 * 1024 * 100];

                        FileOutputStream fos = new FileOutputStream(Paths.get(Client.userDir.toString() + File.separator + filename).toFile());
                        System.out.println("Zapis pliku " + Client.userDir.toString() + File.separator + filename);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        long bytesRead;
                        while (size > 0 && (bytesRead = Client.dis.read(mybytearray, 0, (int) Math.min(mybytearray.length, size))) != -1) {
                            size -= bytesRead;
                        }
                        bos.write(mybytearray, 0, sendSize);
                        bos.flush();

                        System.out.println("Pobrano.");
                        bos.close();
                        fos.close();
                    }

                    Client.users = Client.dis.readUTF();

                    ArrayList<String> usersList = new ArrayList<String>(Arrays.asList(Client.users.split("\n")));

                    for (String item : Client.usersList) {
                        if (!usersList.contains(item)) {
                            Client.usersList.remove(item);
                        }
                    }

                    for (String item : usersList) {
                        if (!Client.usersList.contains(item) && !item.equals(Client.login)) {
                            Client.usersList.add(item);
                        }
                    }

                    Client.sendFileButton.setDisable(false);
                    TimeUnit.SECONDS.sleep(1);
                    Client.sendFileButton.setDisable(true);
                }
            } catch (IOException | InterruptedException e) {
                Client.connection = Connection.REFUSED;
                System.out.println("Połączenie z serwerem zostało zerwane.");
                e.printStackTrace();
            }


        } catch (IOException e) {
            Client.connection = Connection.REFUSED;
            System.out.println("Serwer nieosiągalny.");
        }
    }

    /**
     * Metoda generująca listę ścieżek do wszystkich plików i katalogów użytkownika.
     *
     * @param root element, od którego rozpocznie się generacja listy
     * @param tree String przechowujący dotychczas zgromadzone ścieżki
     * @return ciąg znaków zawierający ścieżki do plików i katalogów oddzielone przecinkiem
     * @throws IOException
     */
    private String createDirectoryTreeString(File root, String tree) throws IOException {
        if (root.listFiles() == null || tree == null)
            return tree;
        try {
            for (int i = 0; i < Objects.requireNonNull(root.listFiles()).length; i++) {
                if (root.listFiles() == null)
                    return tree;
                File temp = Objects.requireNonNull(root.listFiles())[i];
                tree += temp.getCanonicalPath() + ',';
                if (temp.listFiles() != null)
                    tree += createDirectoryTreeString(temp, tree);

            }
        } catch (NullPointerException npe) {
            System.out.println("There was a problem while reading your local files.\nERROR: " + npe.getMessage());
            return null;
        }
        return tree;
    }
}
