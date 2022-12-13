package csv_export;

import com.opencsv.CSVWriter;
import entities.Firing;
import world.World;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExportRevokingPatrolsDetails extends AbstractExportData {

    private static final String CSV_DIRECTORY_PATH = "results";
    private static final String[] firingDetailsHeader = new String[]{
            "simulationTime",
            "firingID",
            "districtName",
            "districtSafetyLevel",
            "revokedPatrols",
            "isNight"
    };
    private static ExportRevokingPatrolsDetails instance;
    private final World world = World.getInstance();
    private final File firingsDetailsCsvFile;

    private ExportRevokingPatrolsDetails() {
        firingsDetailsCsvFile = createExportFile(CSV_DIRECTORY_PATH, firingDetailsHeader, "--Revoking Patrols Details.csv");
    }

    public static ExportRevokingPatrolsDetails getInstance() {
        // Result variable here may seem pointless, but it's needed for DCL (Double-checked locking).
        var result = instance;
        if (instance != null) {
            return result;
        }
        synchronized (ExportRevokingPatrolsDetails.class) {
            if (instance == null) {
                instance = new ExportRevokingPatrolsDetails();
            }
            return instance;
        }
    }

    public void writeToCsvFileRevokedPatrols(Firing firing, int revokedPatrols) {
        var simulationTimeLong = world.getSimulationTimeLong();
        var isNight = world.isNight();
        try {
            writeToFiringsDetailsCsvFileRevokedPatrols(simulationTimeLong, firing, revokedPatrols, isNight);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFiringsDetailsCsvFileRevokedPatrols(long simulationTimeLong, Firing firing, int revokedPatrols, boolean isNight) throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(firingsDetailsCsvFile, true));
        csvWriter.writeNext(new String[]{
                String.valueOf(simulationTimeLong),
                String.valueOf(firing.getUniqueID()),
                firing.getDistrict().getName(),
                String.valueOf(firing.getDistrict().getThreatLevel()),
                String.valueOf(revokedPatrols),
                isNight ? "1" : "0"
        }, false);
        csvWriter.close();
    }
}

