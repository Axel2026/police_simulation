package Simulation.exported_data;

import Simulation.World;
import Simulation.entities.Firing;
import Simulation.entities.SWATHeadquarters;
import Visualisation.District;
import Visualisation.Patrol;
import Visualisation.SWAT;
import com.opencsv.CSVWriter;
import utils.Haversine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExportSWATDistanceToShootings extends AbstractExportData {

    private static final String CSV_DIRECTORY_PATH = "results";
    private static final String[] swatDistanceToShootingsHeader = new String[]{
            "simulationTime",
            "districtId",
            "districtName",
            "districtSafetyLevel",
            "squadId",
            "distanceToShooting",
    };
    private static ExportSWATDistanceToShootings instance;
    private final World world = World.getInstance();
    private final File swatDistanceToShootingsCsvFile;

    private ExportSWATDistanceToShootings() {
        swatDistanceToShootingsCsvFile = createExportFile(CSV_DIRECTORY_PATH, swatDistanceToShootingsHeader, "--SWAT distance to shooting.csv");
    }

    public static ExportSWATDistanceToShootings getInstance() {
        // Result variable here may seem pointless, but it's needed for DCL (Double-checked locking).
        var result = instance;
        if (instance != null) {
            return result;
        }
        synchronized (ExportSWATDistanceToShootings.class) {
            if (instance == null) {
                instance = new ExportSWATDistanceToShootings();
            }
            return instance;
        }
    }

    public void writeToCsvFile(Firing firing, SWAT squad, District district, SWATHeadquarters swatHQ) {
        var simulationTimeLong = world.getSimulationTimeLong();
        var isNight = world.isNight();
        try {
            writeToSwatDistanceToShootingsCsvFile(simulationTimeLong, firing, squad, district, swatHQ, isNight);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToSwatDistanceToShootingsCsvFile(long simulationTimeLong, Firing firing, SWAT squad, District district, SWATHeadquarters swatHQ, boolean isNight) throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(swatDistanceToShootingsCsvFile, true));
        csvWriter.writeNext(new String[]{
                String.valueOf(simulationTimeLong),
                String.valueOf(district.getId()),
                firing.getDistrict().getName(),
                String.valueOf(firing.getDistrict().getThreatLevel()),
                String.valueOf(squad.getUniqueID()),
                String.valueOf(distanceOfSummonedPatrol(firing, squad)).replace(".", ","),
                isNight ? "1" : "0"
        }, false);
        csvWriter.close();
    }

    private double distanceOfSummonedPatrol(Firing firing, SWAT summonedPatrol) {
        return Haversine.distance(firing.getLatitude(), firing.getLongitude(), summonedPatrol.getLatitude(), summonedPatrol.getLongitude());
    }
}

