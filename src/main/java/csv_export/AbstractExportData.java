package csv_export;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public abstract class AbstractExportData {

    private final DateTimeFormatter dateFormat = new DateTimeFormatterBuilder().appendPattern("dd-MM-yyyy_HH-mm-ss").toFormatter();

    File createExportFile(String csvDirectoryPath, String[] header, String csvFileName) {
        File csvDirectory = new File(csvDirectoryPath);
        if (!(csvDirectory.exists() && csvDirectory.isDirectory())) {
            csvDirectory.mkdir();
        }

        var csvFile = new File(csvDirectoryPath, dateFormat.format(LocalDateTime.now()) + csvFileName);
        try {
            if (!csvFile.createNewFile()) {
                throw new IOException("Unable to create file");
            }
            var csvWriter1 = new CSVWriter(new FileWriter(csvFile));
            csvWriter1.writeNext(header);
            csvWriter1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvFile;
    }
}
