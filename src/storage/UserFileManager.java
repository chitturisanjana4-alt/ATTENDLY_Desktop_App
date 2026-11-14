package storage;

import java.io.*;
import java.util.ArrayList;

import model.UserAccount;

public class UserFileManager {

    private static final String FILE_PATH =
            "data/users.txt";

    public static void saveUsers(ArrayList<UserAccount> users) {

        File directory = new File("data");

        if (!directory.exists()) {
            directory.mkdirs();
        }

        try (PrintWriter writer =
                new PrintWriter(new FileWriter(FILE_PATH))) {

            for (UserAccount user : users) {

                writer.println(
                        user.getUsername() + "," +
                        user.getPassword() + "," +
                        user.getRole()
                );
            }

        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    public static ArrayList<UserAccount> loadUsers() {

        ArrayList<UserAccount> users = new ArrayList<>();

        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return users;
        }

        try (BufferedReader reader =
                new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] data = line.split(",", -1);

                if (data.length == 3) {

                    users.add(
                            new UserAccount(
                                    data[0],
                                    data[1],
                                    data[2]
                            )
                    );
                }
            }

        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }

        return users;
    }
}