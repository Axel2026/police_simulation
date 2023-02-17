package Simulation.exported_data;

import Simulation.World;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class ExportAmbulancesInUsePerHour extends Thread {

    private static final String CSV_DIRECTORY_PATH = "results";
    private final DateTimeFormatter dateFormat = new DateTimeFormatterBuilder().appendPattern("dd-MM-yyyy_HH-mm-ss").toFormatter();

    private final String ambulancesInUsePerHourCsvFileName = dateFormat.format(LocalDateTime.now()) + "--Ambulances In Use Per Hour.csv";
    private static final String[] ambulancesInUsePerHourHeader = new String[]{
            "simulationTime[s]",
            "amountOfSolvingAmbulances",
    };
    private final World world = World.getInstance();
    private final File ambulancesInUsePerHourHeaderCsvFile;
    public int previousAmountOfAmbulances = 0;
    private int exportCounter = 1;

    public String getAmbulancesInUsePerHourCsvFileName() {
        return ambulancesInUsePerHourCsvFileName;
    }


    public ExportAmbulancesInUsePerHour() {
        File csvDirectory = new File(CSV_DIRECTORY_PATH);
        if (!(csvDirectory.exists() && csvDirectory.isDirectory())) {
            csvDirectory.mkdir();
        }

        ambulancesInUsePerHourHeaderCsvFile = new File(CSV_DIRECTORY_PATH, getAmbulancesInUsePerHourCsvFileName());
        try {
            if (!ambulancesInUsePerHourHeaderCsvFile.createNewFile()) {
                throw new IOException("Unable to create file");
            }
            CSVWriter csvWriter1 = new CSVWriter(new FileWriter(ambulancesInUsePerHourHeaderCsvFile));
            csvWriter1.writeNext(ambulancesInUsePerHourHeader);
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
                var usedAmbulances = world.getAmbulancesInUse();
                var simulationTimeLong = world.getSimulationTimeLong();
                try {
                    writeToSimulationDetailsCsvFile(simulationTimeLong, usedAmbulances - previousAmountOfAmbulances);
                    previousAmountOfAmbulances = world.getAmbulancesInUse();
                } catch (IOException e) {
                    e.printStackTrace();
                }

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

    private void writeToSimulationDetailsCsvFile(long simulationTimeLong, int usedAmbulances) throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(ambulancesInUsePerHourHeaderCsvFile, true));
        csvWriter.writeNext(new String[]{
                String.valueOf(simulationTimeLong),
                String.valueOf(usedAmbulances),
        }, false);
        csvWriter.close();
    }

}
