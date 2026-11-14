package storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import model.Student;

public class FileManager {

    private static final String FILE_PATH = "data/students.txt";

    // SAVE STUDENTS
    public static void saveStudents(ArrayList<Student> students) {

        try {

            File file = new File(FILE_PATH);

            System.out.println(
                "STUDENT FILE LOCATION: " + file.getAbsolutePath()
            );

            File parent = file.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (BufferedWriter writer =
                    new BufferedWriter(new FileWriter(file))) {

                for (Student student : students) {

                    writer.write(
                        student.getRollNumber() + "," +
                        student.getName() + "," +
                        student.getDepartment() + "," +
                        student.getYear()
                    );

                    writer.newLine();
                }
            }

            System.out.println("Student data saved successfully.");

        } catch (Exception e) {

            System.out.println(
                "Error saving students: " + e.getMessage()
            );
        }
    }

    // LOAD STUDENTS
    public static ArrayList<Student> loadStudents() {

        ArrayList<Student> students = new ArrayList<>();

        File file = new File(FILE_PATH);

        System.out.println(
            "LOADING STUDENT FILE: " + file.getAbsolutePath()
        );

        if (!file.exists()) {

            System.out.println("Student file does not exist yet.");

            return students;
        }

        try (BufferedReader reader =
                new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] data = line.split(",", -1);

                if (data.length == 4) {

                    try {

                        int year = Integer.parseInt(data[3].trim());

                        Student student = new Student(
                            data[0].trim(),
                            data[1].trim(),
                            data[2].trim(),
                            year
                        );

                        students.add(student);

                    } catch (NumberFormatException e) {

                        System.out.println(
                            "Skipping invalid student year: " + line
                        );
                    }
                }
            }

            System.out.println(
                "Total students loaded: " + students.size()
            );

        } catch (Exception e) {

            System.out.println(
                "Error loading students: " + e.getMessage()
            );
        }

        return students;
    }
}