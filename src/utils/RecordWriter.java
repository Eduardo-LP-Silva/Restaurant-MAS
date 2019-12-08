package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class RecordWriter {
    private static boolean emptyRecords = !(new File("records.csv").isFile());

    synchronized public static void writeHeaders() {
        if(emptyRecords) {
            try {
                FileWriter writer = new FileWriter("records.csv", true);
                writer.append("Customer," +
                        "Waiter," +
                        "Liar," +
                        "Initial Mood," +
                        "Final Mood," +
                        "Dish Availability," +
                        "Dish Cooking Time," +
                        "Dish Quality," +
                        "Tip\n");
                writer.flush();
                emptyRecords = false;
                writer.close();
            } catch (IOException e) {
                System.out.println("Can't write record...");
            }
        }
    }

    synchronized public static void write(String data) {
        try {
            FileWriter writer = new FileWriter("records.csv", true);
            writer.append(data);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("Can't write record...");
        }
    }
}
