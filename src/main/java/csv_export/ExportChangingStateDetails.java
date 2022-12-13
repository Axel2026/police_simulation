package csv_export;

import com.opencsv.CSVWriter;
import entities.District;
import entities.Patrol;
import world.World;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExportChangingStateDetails extends AbstractExportData {

    private static final String CSV_DIRECTORY_PATH = "results";
    private static final String[] firingDetailsHeader = new String[]{
            "simulationTime",
            "districtName",
            "districtSafetyLevel",
            "patrolID",
            "previousPatrolState",
            "currentPatrolState",
            "isNight"
    };
    private static ExportChangingStateDetails instance;
    private final World world = World.getInstance();
    private final File firingsDetailsCsvFile;

    private ExportChangingStateDetails() {
        firingsDetailsCsvFile = createExportFile(CSV_DIRECTORY_PATH, firingDetailsHeader, "--Changing State Details.csv");
    }

    public static ExportChangingStateDetails getInstance() {
        // Result variable here may seem pointless, but it's needed for DCL (Double-checked locking).
        var result = instance;
        if (instance != null) {
            return result;
        }
        synchronized (ExportChangingStateDetails.class) {
            if (instance == null) {
                instance = new ExportChangingStateDetails();
            }
            return instance;
        }
    }

    public void writeToCsvFile(Patrol patrol, String previousPatrolState, String currentPatrolState) {
        var simulationTimeLong = world.getSimulationTimeLong();
        var isNight = world.isNight();
        try {
            writeToFiringsDetailsCsvFile(simulationTimeLong, patrol, previousPatrolState, currentPatrolState, isNight);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFiringsDetailsCsvFile(long simulationTimeLong, Patrol patrol, String previousPatrolState, String currentPatrolState, boolean isNight) throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(firingsDetailsCsvFile, true));
        var district = getDistrict(patrol);
        csvWriter.writeNext(new String[]{
                String.valueOf(simulationTimeLong),
                district != null ? district.getName() : "",
                district != null ? String.valueOf(district.getThreatLevel()) : "",
                String.valueOf(patrol.getUniqueID()),
                previousPatrolState,
                currentPatrolState,
                isNight ? "1" : "0"
        }, false);
        csvWriter.close();
    }

    private District getDistrict(Patrol patrol) {
        for (var district : world.getDistricts()) {
            if (district.contains(patrol.getPosition())) {
                return district;
            }
        }
        return null;
    }
}

