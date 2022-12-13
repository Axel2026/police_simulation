package csv_export;

import com.opencsv.CSVWriter;
import entities.Firing;
import entities.Patrol;
import utils.Haversine;
import world.World;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExportSupportSummonDetails extends AbstractExportData {

    private static final String CSV_DIRECTORY_PATH = "results";
    private static final String[] firingDetailsHeader = new String[]{
            "simulationTime",
            "firingID",
            "districtName",
            "districtSafetyLevel",
            "previousPatrolState",
            "numberOfIteration",
            "distanceOfSummonedPatrol",
            "isNight"
    };
    private static ExportSupportSummonDetails instance;
    private final World world = World.getInstance();
    private final File firingsDetailsCsvFile;

    private ExportSupportSummonDetails() {
        firingsDetailsCsvFile = createExportFile(CSV_DIRECTORY_PATH, firingDetailsHeader, "--Support Summon Details.csv");
    }

    public static ExportSupportSummonDetails getInstance() {
        // Result variable here may seem pointless, but it's needed for DCL (Double-checked locking).
        var result = instance;
        if (instance != null) {
            return result;
        }
        synchronized (ExportSupportSummonDetails.class) {
            if (instance == null) {
                instance = new ExportSupportSummonDetails();
            }
            return instance;
        }
    }

    public void writeToCsvFile(Firing firing, Patrol summonedPatrol, String previousPatrolState, int numberOfIteration) {
        var simulationTimeLong = world.getSimulationTimeLong();
        var isNight = world.isNight();
        try {
            writeToFiringsDetailsCsvFile(simulationTimeLong, firing, summonedPatrol, previousPatrolState, numberOfIteration, isNight);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFiringsDetailsCsvFile(long simulationTimeLong, Firing firing, Patrol summonedPatrol, String previousPatrolState, int numberOfIteration, boolean isNight) throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(firingsDetailsCsvFile, true));
        csvWriter.writeNext(new String[]{
                String.valueOf(simulationTimeLong),
                String.valueOf(firing.getUniqueID()),
                firing.getDistrict().getName(),
                String.valueOf(firing.getDistrict().getThreatLevel()),
                previousPatrolState,
                String.valueOf(numberOfIteration),
                String.valueOf(distanceOfSummonedPatrol(firing, summonedPatrol)).replace(".", ","),
                isNight ? "1" : "0"
        }, false);
        csvWriter.close();
    }

    private double distanceOfSummonedPatrol(Firing firing, Patrol summonedPatrol) {
        return Haversine.distance(firing.getLatitude(), firing.getLongitude(), summonedPatrol.getLatitude(), summonedPatrol.getLongitude());
    }
}

