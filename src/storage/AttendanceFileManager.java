package storage;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;

import model.Attendance;

public class AttendanceFileManager {

    private static final String FILE_PATH = "data/attendance.txt";

    // Save attendance records
    public static void saveAttendance(ArrayList<Attendance> records) {

        File folder = new File("data");
        folder.mkdirs();

        try (BufferedWriter writer =
                new BufferedWriter(new FileWriter(FILE_PATH))) {

            for (Attendance record : records) {

                writer.write(
                    record.getRollNumber() + "," +
                    record.getSubjectCode() + "," +
                    record.getDate() + "," +
                    record.isPresent()
                );

                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println(
                "Error saving attendance: " + e.getMessage());
        }
    }

    // Load attendance records
    public static ArrayList<Attendance> loadAttendance() {

        ArrayList<Attendance> records = new ArrayList<>();

        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return records;
        }

        try (BufferedReader reader =
                new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] data = line.split(",", -1);

                if (data.length == 4) {

                    try {
                        Attendance attendance = new Attendance(
                            data[0],
                            data[1],
                            LocalDate.parse(data[2]),
                            Boolean.parseBoolean(data[3])
                        );

                        records.add(attendance);

                    } catch (Exception e) {
                        System.out.println(
                            "Invalid attendance record skipped.");
                    }
                }
            }

        } catch (IOException e) {
            System.out.println(
                "Error loading attendance: " + e.getMessage());
        }

        return records;
    }
}