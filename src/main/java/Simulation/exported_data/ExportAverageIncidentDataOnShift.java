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

public class ExportAverageIncidentDataOnShift extends Thread {

    private static final String CSV_DIRECTORY_PATH = "results";
    private final DateTimeFormatter dateFormat = new DateTimeFormatterBuilder().appendPattern("dd-MM-yyyy_HH-mm-ss").toFormatter();
    public int previousDurationOfInterventionsSum = 0;
    public int previousDurationOfFiringsSum = 0;
    public int previousAmountOfInterventions = 0;
    public int previousAmountOfFirings = 0;
    public int previousAmountOfPatrols = 0;
    private final String averageIncidentDataOnShiftCsvFileName = dateFormat.format(LocalDateTime.now()) + "--Export Average Incident Data On Shift.csv";
    private static final String[] averageIncidentDataOnShiftCsvFileNameFiringHeader = new String[]{
            "amountOfInterventions",
            "amountOfInterventionsPerPatrol",
            "durationOfInterventions[min]",
            "durationOfInterventionsPerPatrol[min]",
            "amountOfFirings",
            "amountOfFiringsPerPatrol",
            "durationOfFirings[min]",
            "durationOfFiringsPerPatrol[min]",
            "amountOfPatrols"
    };

    private final World world = World.getInstance();
    private final File averageIncidentDataOnShift;

    private final int periodOfTimeToExportDetailsInSeconds = (int) (world.getConfig().getPeriodOfTimeToExportDetails() * 480);
    private int exportCounter = 1;

    public String getAverageIncidentDataOnShiftCsvFileName() {
        return averageIncidentDataOnShiftCsvFileName;
    }


    public ExportAverageIncidentDataOnShift() {
        File csvDirectory = new File(CSV_DIRECTORY_PATH);
        if (!(csvDirectory.exists() && csvDirectory.isDirectory())) {
            csvDirectory.mkdir();
        }

        averageIncidentDataOnShift = new File(CSV_DIRECTORY_PATH, getAverageIncidentDataOnShiftCsvFileName());
        try {
            if (!averageIncidentDataOnShift.createNewFile()) {
                throw new IOException("Unable to create file");
            }
            CSVWriter csvWriter1 = new CSVWriter(new FileWriter(averageIncidentDataOnShift));
            csvWriter1.writeNext(averageIncidentDataOnShiftCsvFileNameFiringHeader);
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
                double amountOfInterventions = StatisticsCounter.getInstance().getNumberOfInterventions();
                double durationOfInterventionsSum = StatisticsCounter.getInstance().getDurationOfInterventions();
                double amountOfFirings = StatisticsCounter.getInstance().getNumberOfFirings();
                double durationOfFiringsSum = StatisticsCounter.getInstance().getDurationOfFirings();
                int amountOfPatrols = StatisticsCounter.getInstance().getNumberOfPatrols();
                try {
                    writeToSimulationDetailsCsvFile(
                            amountOfInterventions - previousAmountOfInterventions,
                            ((amountOfInterventions - previousAmountOfInterventions) / (amountOfPatrols - previousAmountOfPatrols)),
                            durationOfInterventionsSum - previousDurationOfInterventionsSum,
                            ((durationOfInterventionsSum - previousDurationOfInterventionsSum) / (amountOfPatrols - previousAmountOfPatrols)),
                            amountOfFirings - previousAmountOfFirings,
                            ((amountOfFirings - previousAmountOfFirings) / (amountOfPatrols - previousAmountOfPatrols)),
                            durationOfFiringsSum - previousDurationOfFiringsSum,
                            ((durationOfFiringsSum - previousDurationOfFiringsSum) / (amountOfPatrols - previousAmountOfPatrols)),
                            amountOfPatrols - previousAmountOfPatrols
                    );
                    previousAmountOfInterventions = StatisticsCounter.getInstance().getNumberOfInterventions();
                    previousDurationOfInterventionsSum = StatisticsCounter.getInstance().getDurationOfInterventions();
                    previousAmountOfFirings = StatisticsCounter.getInstance().getNumberOfFirings();
                    previousDurationOfFiringsSum = StatisticsCounter.getInstance().getDurationOfFirings();
                    previousAmountOfPatrols = StatisticsCounter.getInstance().getNumberOfPatrols();
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

    private void writeToSimulationDetailsCsvFile(double amountOfInterventions,
                                                 double amountOfInterventionsPerPatrol,
                                                 double durationOfInterventions,
                                                 double durationOfInterventionsPerPatrol,
                                                 double amountOfFirings,
                                                 double amountOfFiringsPerPatrol,
                                                 double durationOfFirings,
                                                 double durationOfFiringsPerPatrol,
                                                 double amountOfPatrols
    ) throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(averageIncidentDataOnShift, true));
        csvWriter.writeNext(new String[]{
                String.valueOf(amountOfInterventions),
                String.valueOf(amountOfInterventionsPerPatrol),
                String.valueOf(durationOfInterventions),
                String.valueOf(durationOfInterventionsPerPatrol),
                String.valueOf(amountOfFirings),
                String.valueOf(amountOfFiringsPerPatrol),
                String.valueOf(durationOfFirings),
                String.valueOf(durationOfFiringsPerPatrol),
                String.valueOf(amountOfPatrols),
        }, false);
        csvWriter.close();
    }

}
