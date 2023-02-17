package Simulation.exported_data;

import Simulation.World;
import Simulation.entities.Firing;
import Simulation.entities.Intervention;
import Visualisation.Patrol;
import com.opencsv.CSVWriter;
import utils.Haversine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExportPatrolDistanceToReachIncident extends AbstractExportData {

    private static final String CSV_DIRECTORY_PATH = "results";
    private static final String[] patrolDistanceToReachIncidentHeader = new String[]{
            "simulationTime[s]",
            "incidentID",
            "districtName",
            "districtSafetyLevel",
            "distanceOfSummonedPatrol[m]",
    };
    private static ExportPatrolDistanceToReachIncident instance;
    private final World world = World.getInstance();
    private final File patrolDistanceToReachIncidentCsvFile;

    private ExportPatrolDistanceToReachIncident() {
        patrolDistanceToReachIncidentCsvFile = createExportFile(CSV_DIRECTORY_PATH, patrolDistanceToReachIncidentHeader, "--Patrol Distance To Reach Incident.csv");
    }

    public static ExportPatrolDistanceToReachIncident getInstance() {
        // Result variable here may seem pointless, but it's needed for DCL (Double-checked locking).
        var result = instance;
        if (instance != null) {
            return result;
        }
        synchronized (ExportPatrolDistanceToReachIncident.class) {
            if (instance == null) {
                instance = new ExportPatrolDistanceToReachIncident();
            }
            return instance;
        }
    }

    public void writeToCsvFileFiring(Firing firing, Patrol summonedPatrol) {
        var simulationTimeLong = world.getSimulationTimeLong();
        try {
            writeToFiringsDetailsCsvFileFiring(simulationTimeLong, firing, summonedPatrol);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFiringsDetailsCsvFileFiring(long simulationTimeLong, Firing firing, Patrol summonedPatrol) throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(patrolDistanceToReachIncidentCsvFile, true));
        csvWriter.writeNext(new String[]{
                String.valueOf(simulationTimeLong),
                String.valueOf(firing.getUniqueID()),
                firing.getDistrict().getName(),
                String.valueOf(firing.getDistrict().getThreatLevel()),
                String.valueOf(distanceOfSummonedPatrolFiring(firing, summonedPatrol)).replace(".", ","),
        }, false);

        csvWriter.close();
    }

    public void writeToCsvFileIntervention(Intervention intervention, Patrol summonedPatrol) {
        var simulationTimeLong = world.getSimulationTimeLong();
        try {
            writeToFiringsDetailsCsvFileIntervention(simulationTimeLong, intervention, summonedPatrol);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFiringsDetailsCsvFileIntervention(long simulationTimeLong, Intervention intervention, Patrol summonedPatrol) throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(patrolDistanceToReachIncidentCsvFile, true));
        csvWriter.writeNext(new String[]{
                String.valueOf(simulationTimeLong),
                String.valueOf(intervention.getUniqueID()),
                intervention.getDistrict().getName(),
                String.valueOf(intervention.getDistrict().getThreatLevel()),
                String.valueOf(distanceOfSummonedPatrolIntervention(intervention, summonedPatrol)).replace(".", ","),
        }, false);

        csvWriter.close();
    }

    private double distanceOfSummonedPatrolFiring(Firing firing, Patrol summonedPatrol) {
        return Haversine.distance(firing.getLatitude(), firing.getLongitude(), summonedPatrol.getLatitude(), summonedPatrol.getLongitude());
    }

    private double distanceOfSummonedPatrolIntervention(Intervention intervention, Patrol summonedPatrol) {
        return Haversine.distance(intervention.getLatitude(), intervention.getLongitude(), summonedPatrol.getLatitude(), summonedPatrol.getLongitude());
    }
}

