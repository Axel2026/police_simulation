package Simulation.exported_data;

import Simulation.StatisticsCounter;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExportAverageSwatDistanceAndTime extends AbstractExportData {

    private static final String CSV_DIRECTORY_PATH = "results";
    private static final String[] averageSwatDistanceAndTimeHeader = new String[]{
            "averageDistanceCoveredBySwat[m]",
            "averageTimeElapsedReachingFiring[s]",
    };
    private static ExportAverageSwatDistanceAndTime instance;
    private final File averageSwatDistanceAndTimeHeaderCsvFile;

    private ExportAverageSwatDistanceAndTime() {
        averageSwatDistanceAndTimeHeaderCsvFile = createExportFile(CSV_DIRECTORY_PATH, averageSwatDistanceAndTimeHeader, "--Average Swat Distance And Time.csv");
    }

    public static ExportAverageSwatDistanceAndTime getInstance() {
        // Result variable here may seem pointless, but it's needed for DCL (Double-checked locking).
        var result = instance;
        if (instance != null) {
            return result;
        }
        synchronized (ExportAverageSwatDistanceAndTime.class) {
            if (instance == null) {
                instance = new ExportAverageSwatDistanceAndTime();
            }
            return instance;
        }
    }

    public void writeToCsvFile() {
        try {
            writeToAverageSwatDistanceAndTimeCsvFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToAverageSwatDistanceAndTimeCsvFile() throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(averageSwatDistanceAndTimeHeaderCsvFile, true));
        var coveredDistanceBySWAT = (StatisticsCounter.getInstance().getCoveredDistanceBySWAT()) / (StatisticsCounter.getInstance().getUsedSWAT());
        var elapsedTimeBySWAT = (StatisticsCounter.getInstance().getElapsedTimeBySWAT()) / (StatisticsCounter.getInstance().getUsedSWAT());

        csvWriter.writeNext(new String[]{
                String.valueOf(coveredDistanceBySWAT),
                String.valueOf(elapsedTimeBySWAT),
        }, false);

        csvWriter.close();
    }
}

