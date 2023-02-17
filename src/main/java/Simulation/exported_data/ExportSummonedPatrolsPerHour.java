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

public class ExportSummonedPatrolsPerHour extends Thread {

    private static final String CSV_DIRECTORY_PATH = "results";
    private final DateTimeFormatter dateFormat = new DateTimeFormatterBuilder().appendPattern("dd-MM-yyyy_HH-mm-ss").toFormatter();

    private final String summonedPatrolsPerHourCsvFileName = dateFormat.format(LocalDateTime.now()) + "--Summoned Patrols Per Hour.csv";
    private static final String[] summonedPatrolsPerHourCsvFileNameHeader = new String[]{
            "simulationTime[s]",
            "summonedPatrols",
    };
    private final World world = World.getInstance();
    private final File summonedPatrolsPerHourHeaderCsvFile;
    public int previousSummonedPatrols = 0;
    private int exportCounter = 1;

    public String getSummonedPatrolsPerHourCsvFileName() {
        return summonedPatrolsPerHourCsvFileName;
    }


    public ExportSummonedPatrolsPerHour() {
        File csvDirectory = new File(CSV_DIRECTORY_PATH);
        if (!(csvDirectory.exists() && csvDirectory.isDirectory())) {
            csvDirectory.mkdir();
        }

        summonedPatrolsPerHourHeaderCsvFile = new File(CSV_DIRECTORY_PATH, getSummonedPatrolsPerHourCsvFileName());
        try {
            if (!summonedPatrolsPerHourHeaderCsvFile.createNewFile()) {
                throw new IOException("Unable to create file");
            }
            CSVWriter csvWriter1 = new CSVWriter(new FileWriter(summonedPatrolsPerHourHeaderCsvFile));
            csvWriter1.writeNext(summonedPatrolsPerHourCsvFileNameHeader);
            csvWriter1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!world.hasSimulationDurationElapsed() && !world.isSimulationFinished()) {
            if (!world.isSimulationPaused() && exportCounter <= (world.getSimulationTimeLong() / 3600)) {
                exportCounter++;
                var summonedPatrols = StatisticsCounter.getInstance().getSumonedPatrols();
                var simulationTimeLong = world.getSimulationTimeLong();
                try {
                    writeToSimulationDetailsCsvFile(simulationTimeLong, summonedPatrols - previousSummonedPatrols);
                    previousSummonedPatrols = StatisticsCounter.getInstance().getSumonedPatrols();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // sleep for next 'periodOfTimeToExportDetails' minutes in simulation time
                var sleepTime = ((3600 - (world.getSimulationTime() % 3600)) * 1000) / world.getConfig().getTimeRate();
                try {
                    sleep((long) sleepTime, (int) ((sleepTime - (long) sleepTime) * 1000000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void writeToSimulationDetailsCsvFile(long simulationTimeLong, int summonedPatrols) throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(summonedPatrolsPerHourHeaderCsvFile, true));
        csvWriter.writeNext(new String[]{
                String.valueOf(simulationTimeLong),
                String.valueOf(summonedPatrols),
        }, false);
        csvWriter.close();
    }

}

