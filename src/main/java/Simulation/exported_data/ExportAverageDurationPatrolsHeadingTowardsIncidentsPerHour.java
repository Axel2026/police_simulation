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

public class ExportAverageDurationPatrolsHeadingTowardsIncidentsPerHour extends Thread {

    private static final String CSV_DIRECTORY_PATH = "results";
    private final DateTimeFormatter dateFormat = new DateTimeFormatterBuilder().appendPattern("dd-MM-yyyy_HH-mm-ss").toFormatter();

    private final String averageDurationPatrolsHeadingTowardsIncidentsPerHourCsvFileName = dateFormat.format(LocalDateTime.now()) + "--Average Duration Patrols Heading Towards Incidents Per Hour.csv";
    private static final String[] averageDurationPatrolsHeadingTowardsIncidentsPerHourHeader = new String[]{
            "simulationTime",
            "averageTransferToInterventionDuration",
            "averageTransferToFiringDuration",
    };
    private final World world = World.getInstance();
    private final File averageDurationOfIncidentsPerHourHeaderCsvFile;
    private int exportCounter = 1;
    public int previousAmountOfInterventions = 0;
    public int previousAmountOfFirings = 0;
    public int previousTransferToInterventionDuration = 0;
    public int previousTransferToFiringDuration = 0;

    public String getAverageDurationPatrolsHeadingTowardsIncidentsPerHourCsvFileName() {
        return averageDurationPatrolsHeadingTowardsIncidentsPerHourCsvFileName;
    }


    public ExportAverageDurationPatrolsHeadingTowardsIncidentsPerHour() {
        File csvDirectory = new File(CSV_DIRECTORY_PATH);
        if (!(csvDirectory.exists() && csvDirectory.isDirectory())) {
            csvDirectory.mkdir();
        }

        averageDurationOfIncidentsPerHourHeaderCsvFile = new File(CSV_DIRECTORY_PATH, getAverageDurationPatrolsHeadingTowardsIncidentsPerHourCsvFileName());
        try {
            if (!averageDurationOfIncidentsPerHourHeaderCsvFile.createNewFile()) {
                throw new IOException("Unable to create file");
            }
            CSVWriter csvWriter1 = new CSVWriter(new FileWriter(averageDurationOfIncidentsPerHourHeaderCsvFile));
            csvWriter1.writeNext(averageDurationPatrolsHeadingTowardsIncidentsPerHourHeader);
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
                var simulationTimeLong = world.getSimulationTimeLong();
                double transferToInterventionDuration = StatisticsCounter.getInstance().getTransferToInterventionTime();
                double transferToFiringDuration = StatisticsCounter.getInstance().getTransferToFiringTimeInMinutes();
                double amountOfInterventions = StatisticsCounter.getInstance().getNumberOfInterventions();
                double amountOfFirings = StatisticsCounter.getInstance().getNumberOfFirings();

                try {
                    writeToSimulationDetailsCsvFile(
                            simulationTimeLong,
                            (transferToInterventionDuration - previousTransferToInterventionDuration) / (amountOfInterventions - previousAmountOfInterventions),
                            (transferToFiringDuration - previousTransferToFiringDuration) / (amountOfFirings - previousAmountOfFirings)
                    );

                    previousAmountOfInterventions = StatisticsCounter.getInstance().getNumberOfInterventions();
                    previousTransferToInterventionDuration = StatisticsCounter.getInstance().getTransferToInterventionTime();
                    previousAmountOfFirings = StatisticsCounter.getInstance().getNumberOfFirings();
                    previousTransferToFiringDuration = StatisticsCounter.getInstance().getTransferToFiringTimeInMinutes();
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

    private void writeToSimulationDetailsCsvFile(
            long simulationTimeLong,
            double averageDurationOfIntervention,
            double averageDurationOfFiring
    ) throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(averageDurationOfIncidentsPerHourHeaderCsvFile, true));
        csvWriter.writeNext(new String[]{
                String.valueOf(simulationTimeLong),
                String.valueOf(averageDurationOfIntervention),
                String.valueOf(averageDurationOfFiring),
        }, false);
        csvWriter.close();
    }

}

