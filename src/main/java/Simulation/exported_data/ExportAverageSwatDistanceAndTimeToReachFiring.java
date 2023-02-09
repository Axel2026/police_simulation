package Simulation.exported_data;

import Simulation.StatisticsCounter;
import Simulation.World;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class ExportAverageSwatDistanceAndTimeToReachFiring extends Thread {

    private static final String CSV_DIRECTORY_PATH = "results";
    private final DateTimeFormatter dateFormat = new DateTimeFormatterBuilder().appendPattern("dd-MM-yyyy_HH-mm-ss").toFormatter();
    public double previousCoveredDistanceBySwatSum = 0.0;
    public double previousElapsedTimeSum = 0.0;
    public double previousUsedSwats = 0.0;
    private final String averageSwatDistanceAndTimeToReachFiringCsvFileName = dateFormat.format(LocalDateTime.now()) + "--Average Swat Distance And Time To Reach Firing.csv";
    private static final String[] averageSwatDistanceAndTimeToReachFiringHeader = new String[]{
            "simulationTime",
            "averageDistanceToReach[m]",
            "averageTimeToReach[s]",
    };
    private final World world = World.getInstance();
    private final File averageSwatDistanceAndTimeToReachFiringHeaderCsvFile;

    private final int periodOfTimeToExportDetailsInSeconds = (60 * 60);
    private int exportCounter = 1;

    public String getAverageSwatDistanceAndTimeToReachFiringCsvFileName() {
        return averageSwatDistanceAndTimeToReachFiringCsvFileName;
    }


    public ExportAverageSwatDistanceAndTimeToReachFiring() {
        File csvDirectory = new File(CSV_DIRECTORY_PATH);
        if (!(csvDirectory.exists() && csvDirectory.isDirectory())) {
            csvDirectory.mkdir();
        }

        averageSwatDistanceAndTimeToReachFiringHeaderCsvFile = new File(CSV_DIRECTORY_PATH, getAverageSwatDistanceAndTimeToReachFiringCsvFileName());
        try {
            if (!averageSwatDistanceAndTimeToReachFiringHeaderCsvFile.createNewFile()) {
                throw new IOException("Unable to create file");
            }
            CSVWriter csvWriter1 = new CSVWriter(new FileWriter(averageSwatDistanceAndTimeToReachFiringHeaderCsvFile));
            csvWriter1.writeNext(averageSwatDistanceAndTimeToReachFiringHeader);
            csvWriter1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!world.hasSimulationDurationElapsed() && !world.isSimulationFinished()) {
            if (!world.isSimulationPaused() && exportCounter <= (world.getSimulationTimeLong() / periodOfTimeToExportDetailsInSeconds)) {
                exportCounter++;
                var simulationTimeLong = world.getSimulationTimeLong();
                var coveredDistanceBySwatSum = StatisticsCounter.getInstance().getCoveredDistanceBySWAT();
                var elapsedTimeSum = StatisticsCounter.getInstance().getElapsedTimeBySWAT();
                var usedSwats = StatisticsCounter.getInstance().getUsedSWAT();
                try {
                    writeToSimulationDetailsCsvFile(simulationTimeLong,
                            ((coveredDistanceBySwatSum - previousCoveredDistanceBySwatSum)) / (usedSwats - previousUsedSwats),
                            (elapsedTimeSum - previousElapsedTimeSum) / (usedSwats - previousUsedSwats));
                    previousCoveredDistanceBySwatSum = StatisticsCounter.getInstance().getCoveredDistanceBySWAT();
                    previousElapsedTimeSum = StatisticsCounter.getInstance().getElapsedTimeBySWAT();
                    previousUsedSwats = StatisticsCounter.getInstance().getUsedSWAT();
                    world.addSwatDistanceAndTimeCouter();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                var sleepTime = ((periodOfTimeToExportDetailsInSeconds - (world.getSimulationTime() % periodOfTimeToExportDetailsInSeconds)) * 1000) / world.getConfig().getTimeRate();
                try {
                    sleep((long) sleepTime, (int) ((sleepTime - (long) sleepTime) * 1000000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void writeToSimulationDetailsCsvFile(long simulationTimeLong, double coveredDistance, double elapsedTime) throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(averageSwatDistanceAndTimeToReachFiringHeaderCsvFile, true));
        csvWriter.writeNext(new String[]{
                String.valueOf(simulationTimeLong),
                String.valueOf(coveredDistance),
                String.valueOf(elapsedTime),
        }, false);
        csvWriter.close();
    }

}
