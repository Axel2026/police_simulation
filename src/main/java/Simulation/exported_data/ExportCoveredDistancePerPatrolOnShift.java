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

public class ExportCoveredDistancePerPatrolOnShift extends Thread {

    private static final String CSV_DIRECTORY_PATH = "results";
    private final DateTimeFormatter dateFormat = new DateTimeFormatterBuilder().appendPattern("dd-MM-yyyy_HH-mm-ss").toFormatter();
    public int previousDurationOfInterventionsSum = 0;
    public int previousDurationOfFiringsSum = 0;
    public int previousCoveredDistance = 0;
    public int previousAmountOfFirings = 0;
    public int previousAmountOfPatrols = 0;
    private final String coveredDistancePerPatrolOnShiftCsvFileName = dateFormat.format(LocalDateTime.now()) + "--Covered Distance Per Patrol On Shift.csv";
    private static final String[] coveredDistancePerPatrolOnShiftCsvFileNameFiringHeader = new String[]{
            "distanceCoveredPerPatrolOnShift[km]",
            "distanceCoveredPerPatrolOnShiftPerHour[km]",
    };

    private final World world = World.getInstance();
    private final File coveredDistancePerPatrolOnShift;

    private final int periodOfTimeToExportDetailsInSeconds = (int) (60 * 480);
    private int exportCounter = 1;

    public String getCoveredDistancePerPatrolOnShiftCsvFileName() {
        return coveredDistancePerPatrolOnShiftCsvFileName;
    }


    public ExportCoveredDistancePerPatrolOnShift() {
        File csvDirectory = new File(CSV_DIRECTORY_PATH);
        if (!(csvDirectory.exists() && csvDirectory.isDirectory())) {
            csvDirectory.mkdir();
        }

        coveredDistancePerPatrolOnShift = new File(CSV_DIRECTORY_PATH, getCoveredDistancePerPatrolOnShiftCsvFileName());
        try {
            if (!coveredDistancePerPatrolOnShift.createNewFile()) {
                throw new IOException("Unable to create file");
            }
            CSVWriter csvWriter1 = new CSVWriter(new FileWriter(coveredDistancePerPatrolOnShift));
            csvWriter1.writeNext(coveredDistancePerPatrolOnShiftCsvFileNameFiringHeader);
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
                double coveredDistance = StatisticsCounter.getInstance().getCoveredDistanceByPatrol();
                int amountOfPatrols = StatisticsCounter.getInstance().getNumberOfPatrols();
                try {
                    writeToSimulationDetailsCsvFile(
                            (((coveredDistance - previousCoveredDistance) / 1000) / (amountOfPatrols - previousAmountOfPatrols)),
                    ((((coveredDistance - previousCoveredDistance) / 1000) / (amountOfPatrols - previousAmountOfPatrols))/8)
                    );
                    previousCoveredDistance = StatisticsCounter.getInstance().getCoveredDistanceByPatrol();
                    previousAmountOfPatrols = StatisticsCounter.getInstance().getNumberOfPatrols();
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

    private void writeToSimulationDetailsCsvFile(double coveredDistancePerPatrol, double coveredDistancePerPatrolPerHour) throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(coveredDistancePerPatrolOnShift, true));
        csvWriter.writeNext(new String[]{
                String.valueOf(coveredDistancePerPatrol),
                String.valueOf(coveredDistancePerPatrolPerHour),
        }, false);
        csvWriter.close();
    }

}
