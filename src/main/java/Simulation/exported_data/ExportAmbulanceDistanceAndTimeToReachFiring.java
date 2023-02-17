package Simulation.exported_data;

import Simulation.World;
import Simulation.entities.Firing;
import Visualisation.Ambulance;
import com.opencsv.CSVWriter;
import utils.Haversine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExportAmbulanceDistanceAndTimeToReachFiring extends AbstractExportData {

    private static final String CSV_DIRECTORY_PATH = "results";
    private static final String[] averageAmbulanceTimeToReachHeader = new String[]{
            "simulationTime[s]",
            "firingID",
            "districtName",
            "districtSafetyLevel",
            "distanceOfSummonedAmbulance[m]",
            "timeToReachFiring[s]",
    };
    private static ExportAmbulanceDistanceAndTimeToReachFiring instance;
    private final World world = World.getInstance();
    private final File averageAmbulanceTimeToReachCsvFile;

    private ExportAmbulanceDistanceAndTimeToReachFiring() {
        averageAmbulanceTimeToReachCsvFile = createExportFile(CSV_DIRECTORY_PATH, averageAmbulanceTimeToReachHeader, "--Ambulance Distance And Time To Reach Firing.csv");
    }

    public static ExportAmbulanceDistanceAndTimeToReachFiring getInstance() {
        // Result variable here may seem pointless, but it's needed for DCL (Double-checked locking).
        var result = instance;
        if (instance != null) {
            return result;
        }
        synchronized (ExportAmbulanceDistanceAndTimeToReachFiring.class) {
            if (instance == null) {
                instance = new ExportAmbulanceDistanceAndTimeToReachFiring();
            }
            return instance;
        }
    }

    public void writeToCsvFile(Firing firing, Ambulance summonedAmbulance) {
        var simulationTimeLong = world.getSimulationTimeLong();
        try {
            writeToFiringsDetailsCsvFile(simulationTimeLong, firing, summonedAmbulance);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFiringsDetailsCsvFile(long simulationTimeLong, Firing firing, Ambulance summonedAmbulance) throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(averageAmbulanceTimeToReachCsvFile, true));
        csvWriter.writeNext(new String[]{
                String.valueOf(simulationTimeLong),
                String.valueOf(firing.getUniqueID()),
                firing.getDistrict().getName(),
                String.valueOf(firing.getDistrict().getThreatLevel()),
                String.valueOf(distanceOfSummonedAmbulance(firing, summonedAmbulance)).replace(".", ","),
                String.valueOf(((distanceOfSummonedAmbulance(firing, summonedAmbulance)) / ((World.getInstance().getConfig().getBasePrivilegedSpeed() * 1000) / 3600.0)))
        }, false);

        world.addCoveredDistance(distanceOfSummonedAmbulance(firing, summonedAmbulance));
        world.addElapsedTime((distanceOfSummonedAmbulance(firing, summonedAmbulance)) / ((World.getInstance().getConfig().getBasePrivilegedSpeed() * 1000) / 3600.0));
        world.addDistanceAndTimeCounter();

        csvWriter.close();
    }

    private double distanceOfSummonedAmbulance(Firing firing, Ambulance summonedAmbulance) {
        return Haversine.distance(firing.getLatitude(), firing.getLongitude(), summonedAmbulance.getLatitude(), summonedAmbulance.getLongitude());
    }
}

