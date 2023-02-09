package Simulation.exported_data;

import Simulation.World;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class ExportAverageAmbulanceDistanceAndTimeToReachFiring extends Thread {

    private static final String CSV_DIRECTORY_PATH = "results";
    private final DateTimeFormatter dateFormat = new DateTimeFormatterBuilder().appendPattern("dd-MM-yyyy_HH-mm-ss").toFormatter();
    public double coveredDistanceSum = 0.0;
    public double elapsedTimeSum = 0.0;
    private final String averageAmbulanceDistanceAndTimeToReachFiringCsvFileName = dateFormat.format(LocalDateTime.now()) + "--Average Ambulance Distance And Time To Reach Firing.csv";
    private static final String[] averageAmbulanceDistanceAndTimeToReachFiringHeader = new String[]{
            "simulationTime",
            "averageDistanceToReach",
            "averageTimeToReach",
    };
    private final World world = World.getInstance();
    private final File averageAmbulanceDistanceAndTimeToReachFiringHeaderCsvFile;

    private final int periodOfTimeToExportDetailsInSeconds = (int) (world.getConfig().getPeriodOfTimeToExportDetails() * 60);
    private int exportCounter = 1;

    public String getAverageAmbulanceDistanceAndTimeToReachFiringCsvFileName() {
        return averageAmbulanceDistanceAndTimeToReachFiringCsvFileName;
    }


    public ExportAverageAmbulanceDistanceAndTimeToReachFiring() {
        File csvDirectory = new File(CSV_DIRECTORY_PATH);
        if (!(csvDirectory.exists() && csvDirectory.isDirectory())) {
            csvDirectory.mkdir();
        }

        averageAmbulanceDistanceAndTimeToReachFiringHeaderCsvFile = new File(CSV_DIRECTORY_PATH, getAverageAmbulanceDistanceAndTimeToReachFiringCsvFileName());
        try {
            if (!averageAmbulanceDistanceAndTimeToReachFiringHeaderCsvFile.createNewFile()) {
                throw new IOException("Unable to create file");
            }
            CSVWriter csvWriter1 = new CSVWriter(new FileWriter(averageAmbulanceDistanceAndTimeToReachFiringHeaderCsvFile));
            csvWriter1.writeNext(averageAmbulanceDistanceAndTimeToReachFiringHeader);
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
                coveredDistanceSum = world.getCoveredDistance();
                elapsedTimeSum = world.getElapsedTime();
                try {
                    writeToSimulationDetailsCsvFile(simulationTimeLong, coveredDistanceSum / world.getDistanceAndTimeCounter(), elapsedTimeSum / world.getDistanceAndTimeCounter());
                    world.removeCoveredDistance();
                    world.removeElapsedTime();
                    world.removeDistanceAndTimeCounter();
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
        var csvWriter = new CSVWriter(new FileWriter(averageAmbulanceDistanceAndTimeToReachFiringHeaderCsvFile, true));
        csvWriter.writeNext(new String[]{
                String.valueOf(simulationTimeLong),
                String.valueOf(coveredDistance),
                String.valueOf(elapsedTime),
        }, false);
        csvWriter.close();
    }

}
