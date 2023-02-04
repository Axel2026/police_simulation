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


public class ExportAverageDurationOfIncidentsPerHour extends Thread {

    private static final String CSV_DIRECTORY_PATH = "results";
    private final DateTimeFormatter dateFormat = new DateTimeFormatterBuilder().appendPattern("dd-MM-yyyy_HH-mm-ss").toFormatter();

    private final String averageDurationOfIncidentsPerHourCsvFileName = dateFormat.format(LocalDateTime.now()) + "--Average Duration Of Incidents Per Hour.csv";
    private static final String[] averageDurationOfIncidentsPerHourHeader = new String[]{
            "simulationTime",
            "amountOfInterventions",
            "interventionsDuration",
            "averageInterventionDuration",
            "amountOfFirings",
            "firingsDuration",
            "averageFiringDuration",
    };
    private final World world = World.getInstance();
    private final File averageDurationOfIncidentsPerHourHeaderCsvFile;
    public int previousAmountOfAmbulances = 0;
    private int exportCounter = 1;
    public int previousDurationOfInterventionsSum = 0;
    public int previousDurationOfFiringsSum = 0;
    public int previousAmountOfInterventions = 0;
    public int previousAmountOfFirings = 0;

    public String getAverageDurationOfIncidentsPerHourCsvFileName() {
        return averageDurationOfIncidentsPerHourCsvFileName;
    }


    public ExportAverageDurationOfIncidentsPerHour() {
        File csvDirectory = new File(CSV_DIRECTORY_PATH);
        if (!(csvDirectory.exists() && csvDirectory.isDirectory())) {
            csvDirectory.mkdir();
        }

        averageDurationOfIncidentsPerHourHeaderCsvFile = new File(CSV_DIRECTORY_PATH, getAverageDurationOfIncidentsPerHourCsvFileName());
        try {
            if (!averageDurationOfIncidentsPerHourHeaderCsvFile.createNewFile()) {
                throw new IOException("Unable to create file");
            }
            CSVWriter csvWriter1 = new CSVWriter(new FileWriter(averageDurationOfIncidentsPerHourHeaderCsvFile));
            csvWriter1.writeNext(averageDurationOfIncidentsPerHourHeader);
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
                double amountOfInterventions = StatisticsCounter.getInstance().getNumberOfInterventions();
                double durationOfInterventionsSum = StatisticsCounter.getInstance().getDurationOfInterventions();
                double amountOfFirings = StatisticsCounter.getInstance().getNumberOfFirings();
                double durationOfFiringsSum = StatisticsCounter.getInstance().getDurationOfFirings();

                try {
                    writeToSimulationDetailsCsvFile(
                            simulationTimeLong,
                            amountOfInterventions - previousAmountOfInterventions,
                            durationOfInterventionsSum - previousDurationOfInterventionsSum,
                            (durationOfInterventionsSum - previousDurationOfInterventionsSum) / (amountOfInterventions - previousAmountOfInterventions),
                            amountOfFirings - previousAmountOfFirings,
                            durationOfFiringsSum - previousDurationOfFiringsSum,
                            (durationOfFiringsSum - previousDurationOfFiringsSum) / (amountOfFirings - previousAmountOfFirings)
                    );

                    previousAmountOfInterventions = StatisticsCounter.getInstance().getNumberOfInterventions();
                    previousDurationOfInterventionsSum = StatisticsCounter.getInstance().getDurationOfInterventions();
                    previousAmountOfFirings = StatisticsCounter.getInstance().getNumberOfFirings();
                    previousDurationOfFiringsSum = StatisticsCounter.getInstance().getDurationOfFirings();
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
            double amountOfInterventions,
            double durationOfInterventionsSum,
            double averageDurationOfIntervention,
            double amountOfFirings,
            double durationOfFiringsSum,
            double averageDurationOfFiring
    ) throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(averageDurationOfIncidentsPerHourHeaderCsvFile, true));
        csvWriter.writeNext(new String[]{
                String.valueOf(simulationTimeLong),
                String.valueOf(amountOfInterventions),
                String.valueOf(durationOfInterventionsSum),
                String.valueOf(averageDurationOfIntervention),
                String.valueOf(amountOfFirings),
                String.valueOf(durationOfFiringsSum),
                String.valueOf(averageDurationOfFiring),
        }, false);
        csvWriter.close();
    }

}
