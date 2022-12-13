package csv_export;

import com.opencsv.CSVWriter;
import entities.Firing;
import entities.Patrol;
import utils.Haversine;
import world.World;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ExportFiringDetails extends AbstractExportData {

    private static final String CSV_DIRECTORY_PATH = "results";
    private static final String[] firingDetailsHeader = new String[]{
            "simulationTime",
            "firingID",
            "districtName",
            "districtSafetyLevel",
            "generallyRequiredPatrols",
            "solvingPatrols",
            "reachingPatrols(including 'called')",
            "calledPatrols",
            "totalDistanceOfCalledPatrols",
            "isNight"
    };
    private static ExportFiringDetails instance;
    private final World world = World.getInstance();
    private final File firingsDetailsCsvFile;

    private ExportFiringDetails() {
        firingsDetailsCsvFile = createExportFile(CSV_DIRECTORY_PATH, firingDetailsHeader, "--Firings Details.csv");
    }

    public static ExportFiringDetails getInstance() {
        // Result variable here may seem pointless, but it's needed for DCL (Double-checked locking).
        var result = instance;
        if (instance != null) {
            return result;
        }
        synchronized (ExportFiringDetails.class) {
            if (instance == null) {
                instance = new ExportFiringDetails();
            }
            return instance;
        }
    }

    public void writeToCsvFileCalledPatrols(Firing firing, List<Patrol> calledPatrols) {
        var simulationTimeLong = world.getSimulationTimeLong();
        var isNight = world.isNight();
        try {
            writeToFiringsDetailsCsvFileCalledPatrols(simulationTimeLong, firing, calledPatrols, isNight);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFiringsDetailsCsvFileCalledPatrols(long simulationTimeLong, Firing firing, List<Patrol> calledPatrols, boolean isNight) throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(firingsDetailsCsvFile, true));
        csvWriter.writeNext(new String[]{
                String.valueOf(simulationTimeLong),
                String.valueOf(firing.getUniqueID()),
                firing.getDistrict().getName(),
                String.valueOf(firing.getDistrict().getThreatLevel()),
                String.valueOf(firing.getRequiredPatrols()),
                String.valueOf(firing.getPatrolsSolving().size()),
                String.valueOf(firing.getPatrolsReaching().size()),
                String.valueOf(calledPatrols.size()),
                String.valueOf(totalDistanceOfPatrols(firing, calledPatrols)).replace(".", ","),
                isNight ? "1" : "0"
        }, false);
        csvWriter.close();
    }

    private double totalDistanceOfPatrols(Firing firing, List<Patrol> calledPatrols) {
        double distance = 0;
        for (var patrol : calledPatrols
        ) {
            distance += Haversine.distance(firing.getLatitude(), firing.getLongitude(), patrol.getLatitude(), patrol.getLongitude());
        }
        return distance;
    }
}

