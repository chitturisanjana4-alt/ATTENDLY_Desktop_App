package storage;

import java.io.*;
import java.util.ArrayList;
import model.Subject;

public class SubjectFileManager {

    private static final String FILE_PATH = "data/subjects.txt";

    // Save subjects
    public static void saveSubjects(ArrayList<Subject> subjects) {

        File folder = new File("data");
        folder.mkdirs();

        try (BufferedWriter writer =
                new BufferedWriter(new FileWriter(FILE_PATH))) {

            for (Subject subject : subjects) {

                writer.write(
                    subject.getSubjectCode() + "," +
                    subject.getSubjectName() + "," +
                    subject.getFacultyName() + "," +
                    subject.getTotalClasses()
                );

                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error saving subjects: " + e.getMessage());
        }
    }

    // Load subjects
    public static ArrayList<Subject> loadSubjects() {

        ArrayList<Subject> subjects = new ArrayList<>();

        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return subjects;
        }

        try (BufferedReader reader =
                new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] data = line.split(",", -1);

                if (data.length == 4) {

                    try {
                        int totalClasses = Integer.parseInt(data[3]);

                        subjects.add(new Subject(
                            data[0],
                            data[1],
                            data[2],
                            totalClasses
                        ));

                    } catch (NumberFormatException e) {
                        System.out.println("Invalid subject record skipped.");
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error loading subjects: " + e.getMessage());
        }

        return subjects;
    }
}