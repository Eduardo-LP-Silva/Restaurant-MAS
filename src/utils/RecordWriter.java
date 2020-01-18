package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class RecordWriter {
    private static boolean emptyRecords = !(new File("records.csv").isFile());

    synchronized public static void writeHeaders(String filename, String headers) {
        if(!(new File(filename).isFile())) {
            try {
                FileWriter writer = new FileWriter(filename, true);
                writer.append(headers);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                System.out.println("Can't write record...");
            }
        }
    }

    synchronized public static void write(String filename, String data) {
        try {
            FileWriter writer = new FileWriter(filename, true);
            writer.append(data);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("Can't write record...");
        }
    }
}
