package service;

import java.util.ArrayList;

import model.UserAccount;
import storage.UserFileManager;

public class UserService {

    private ArrayList<UserAccount> users;

    public UserService() {

        users = UserFileManager.loadUsers();

        if (users == null) {
            users = new ArrayList<>();
        }

        if (users.isEmpty()) {

            users.add(new UserAccount(
                    "admin", "admin123", "ADMIN"
            ));

            users.add(new UserAccount(
                    "faculty", "faculty123", "FACULTY"
            ));

            users.add(new UserAccount(
                    "student", "student123", "STUDENT"
            ));

            saveData();
        }
    }

    // LOGIN
    public UserAccount login(String username, String password) {

        if (username == null || password == null) {
            return null;
        }

        for (UserAccount user : users) {

            if (user.getUsername().equalsIgnoreCase(username.trim())
                    && user.getPassword().equals(password)) {

                return user;
            }
        }

        return null;
    }

    // ADD USER
    public boolean addUser(UserAccount user) {

        if (user == null) {
            return false;
        }

        String username = user.getUsername();
        String password = user.getPassword();
        String role = user.getRole();

        if (username == null || username.trim().isEmpty()
                || password == null || password.length() < 8
                || !isValidRole(role)) {

            return false;
        }

        if (findUser(username) != null) {
            return false;
        }

        users.add(user);
        saveData();

        return true;
    }

    // UPDATE USER ROLE
    public boolean updateUserRole(String username, String newRole) {

        if (username == null || !isValidRole(newRole)) {
            return false;
        }

        UserAccount user = findUser(username);

        if (user == null) {
            return false;
        }

        if (user.getUsername().equalsIgnoreCase("admin")) {
            return false;
        }

        user.setRole(newRole.toUpperCase());

        saveData();

        return true;
    }

    // DELETE USER
    public boolean deleteUser(String username) {

        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        UserAccount user = findUser(username);

        if (user == null) {
            return false;
        }

        if (user.getUsername().equalsIgnoreCase("admin")) {
            return false;
        }

        users.remove(user);

        saveData();

        return true;
    }

    // CHANGE PASSWORD
    public boolean changePassword(
            String username,
            String oldPassword,
            String newPassword) {

        if (username == null
                || oldPassword == null
                || newPassword == null
                || newPassword.length() < 8) {

            return false;
        }

        UserAccount user = findUser(username);

        if (user == null) {
            return false;
        }

        if (!user.getPassword().equals(oldPassword)) {
            return false;
        }

        user.setPassword(newPassword);

        saveData();

        return true;
    }

    // FIND USER
    private UserAccount findUser(String username) {

        for (UserAccount user : users) {

            if (user.getUsername().equalsIgnoreCase(username.trim())) {
                return user;
            }
        }

        return null;
    }

    // VALIDATE ROLE
    private boolean isValidRole(String role) {

        if (role == null) {
            return false;
        }

        return role.equalsIgnoreCase("ADMIN")
                || role.equalsIgnoreCase("FACULTY")
                || role.equalsIgnoreCase("STUDENT");
    }

    // TOTAL USERS
    public int getTotalUsers() {
        return users.size();
    }

    // ADMIN COUNT
    public int getAdminCount() {

        int count = 0;

        for (UserAccount user : users) {

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                count++;
            }
        }

        return count;
    }

    // FACULTY COUNT
    public int getFacultyCount() {

        int count = 0;

        for (UserAccount user : users) {

            if ("FACULTY".equalsIgnoreCase(user.getRole())) {
                count++;
            }
        }

        return count;
    }

    // STUDENT COUNT
    public int getStudentCount() {

        int count = 0;

        for (UserAccount user : users) {

            if ("STUDENT".equalsIgnoreCase(user.getRole())) {
                count++;
            }
        }

        return count;
    }

    // GET ALL USERS
    public ArrayList<UserAccount> getAllUsers() {

        return new ArrayList<>(users);
    }

    // SAVE DATA
    public void saveData() {

        UserFileManager.saveUsers(users);
    }
}