package Simulation.exported_data;

import Simulation.World;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExportFirstPatrolData extends AbstractExportData {

    private static final String CSV_DIRECTORY_PATH = "results";
    private static final String[] firstPatrolDataHeader = new String[]{
            "simulationTime",
            "patrolState",
            "timeInState"
    };

    private static ExportFirstPatrolData instance;
    private final World world = World.getInstance();
    private final File firstPatrolDataCsvFile;

    private ExportFirstPatrolData() {
        firstPatrolDataCsvFile = createExportFile(CSV_DIRECTORY_PATH, firstPatrolDataHeader, "--First Patrol Data.csv");
    }

    public static ExportFirstPatrolData getInstance() {
        // Result variable here may seem pointless, but it's needed for DCL (Double-checked locking).
        var result = instance;
        if (instance != null) {
            return result;
        }
        synchronized (ExportFirstPatrolData.class) {
            if (instance == null) {
                instance = new ExportFirstPatrolData();
            }
            return instance;
        }
    }

    public void writeToCsvFileFirstPatrolData(long simulationTimeLong, String patrolState, double timeInState) {
        try {
            writeToFiringsDetailsCsvFileFirstPatrolData(simulationTimeLong, patrolState, timeInState);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFiringsDetailsCsvFileFirstPatrolData(long simulationTimeLong, String patrolState, double timeInState) throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(firstPatrolDataCsvFile, true));
        csvWriter.writeNext(new String[]{
                String.valueOf(simulationTimeLong),
                String.valueOf(patrolState),
                String.valueOf(timeInState),
        }, false);
        csvWriter.close();
    }
}

