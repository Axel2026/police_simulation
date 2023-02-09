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

public class ExportUsedSwatPerHour extends Thread {

    private static final String CSV_DIRECTORY_PATH = "results";
    private final DateTimeFormatter dateFormat = new DateTimeFormatterBuilder().appendPattern("dd-MM-yyyy_HH-mm-ss").toFormatter();
    public double previousAmountOfUsedSwat = 0.0;
    private final String usedSwatPerHourCsvFileName = dateFormat.format(LocalDateTime.now()) + "--Used Swat Per Hour.csv";
    private static final String[] usedSwatPerHourHeader = new String[]{
            "simulationTime",
            "amountOfUsedSwat"
    };
    private final World world = World.getInstance();
    private final File usedSwatPerHourHeaderCsvFile;

    private final int periodOfTimeToExportDetailsInSeconds = (60 * 60);
    private int exportCounter = 1;

    public String getUsedSwatPerHourCsvFileName() {
        return usedSwatPerHourCsvFileName;
    }


    public ExportUsedSwatPerHour() {
        File csvDirectory = new File(CSV_DIRECTORY_PATH);
        if (!(csvDirectory.exists() && csvDirectory.isDirectory())) {
            csvDirectory.mkdir();
        }

        usedSwatPerHourHeaderCsvFile = new File(CSV_DIRECTORY_PATH, getUsedSwatPerHourCsvFileName());
        try {
            if (!usedSwatPerHourHeaderCsvFile.createNewFile()) {
                throw new IOException("Unable to create file");
            }
            CSVWriter csvWriter1 = new CSVWriter(new FileWriter(usedSwatPerHourHeaderCsvFile));
            csvWriter1.writeNext(usedSwatPerHourHeader);
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
                var usedSwats = StatisticsCounter.getInstance().getUsedSWAT();
                try {
                    writeToSimulationDetailsCsvFile(simulationTimeLong,
                            ((usedSwats - previousAmountOfUsedSwat)));

                    previousAmountOfUsedSwat = StatisticsCounter.getInstance().getUsedSWAT();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // sleep for next 'periodOfTimeToExportDetails' minutes in simulation time
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

    private void writeToSimulationDetailsCsvFile(long simulationTimeLong, double coveredDistance) throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(usedSwatPerHourHeaderCsvFile, true));
        csvWriter.writeNext(new String[]{
                String.valueOf(simulationTimeLong),
                String.valueOf(coveredDistance),
        }, false);
        csvWriter.close();
    }

}
