package csv_export;

import com.opencsv.CSVWriter;
import entities.*;
import world.World;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.stream.Collectors;

public class ExportSimulationAndDistrictDetails extends Thread {

    private static final String CSV_DIRECTORY_PATH = "results";
    private static final String[] simulationDetailsHeader = new String[]{
            "simulationTime",
            "amountOfPatrols",
            "amountOfPatrollingPatrols",
            "amountOfCalculatingPathPatrols",
            "amountOfTransferToInterventionPatrols",
            "amountOfTransferToFiringPatrols",
            "amountOfInterventionPatrols",
            "amountOfFiringPatrols",
            "amountOfNeutralizedPatrols",
            "amountOfReturningToHqPatrols",
            "amountOfIncidents",
            "amountOfInterventions",
            "amountOfInterventionsBeingSolved",
            "amountOfFirings",
            "amountOfFiringBeingSolved",
            "isNight"
    };
    private final World world = World.getInstance();
    private final File simulationDetailsCsvFile;
    private final File districtsDetailsCsvFile;
    private final String[] districtsDetailsHeader = new String[]{
            "simulationTime",
            "districtName",
            "districtSafetyLevel",
            "amountOfPatrols",
            "amountOfPatrollingPatrols",
            "amountOfCalculatingPathPatrols",
            "amountOfTransferToInterventionPatrols",
            "amountOfTransferToFiringPatrols",
            "amountOfInterventionPatrols",
            "amountOfFiringPatrols",
            "amountOfReturningToHqPatrols",
            "amountOfIncidents",
            "isNight"
    };

    private final DateTimeFormatter dateFormat = new DateTimeFormatterBuilder().appendPattern("dd-MM-yyyy_HH-mm-ss").toFormatter();
    private final int periodOfTimeToExportDetailsInSeconds = (int) (world.getConfig().getPeriodOfTimeToExportDetails() * 60);
    private int exportCounter = 1;

    public ExportSimulationAndDistrictDetails() {
        File csvDirectory = new File(CSV_DIRECTORY_PATH);
        if (!(csvDirectory.exists() && csvDirectory.isDirectory())) {
            csvDirectory.mkdir();
        }

        simulationDetailsCsvFile = new File(CSV_DIRECTORY_PATH, dateFormat.format(LocalDateTime.now()) + "--Simulation Details.csv");
        districtsDetailsCsvFile = new File(CSV_DIRECTORY_PATH, dateFormat.format(LocalDateTime.now()) + "--Districts Details.csv");
        try {
            if (!simulationDetailsCsvFile.createNewFile()) {
                throw new IOException("Unable to create file");
            }
            CSVWriter csvWriter1 = new CSVWriter(new FileWriter(simulationDetailsCsvFile));
            csvWriter1.writeNext(simulationDetailsHeader);
            csvWriter1.close();

            if (!districtsDetailsCsvFile.createNewFile()) {
                throw new IOException("Unable to create file");
            }
            CSVWriter csvWriter2 = new CSVWriter(new FileWriter(districtsDetailsCsvFile));
            csvWriter2.writeNext(districtsDetailsHeader);
            csvWriter2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!world.hasSimulationDurationElapsed()) {
            if (!world.isSimulationPaused() && exportCounter <= (world.getSimulationTimeLong() / periodOfTimeToExportDetailsInSeconds)) {
                exportCounter++;
                var allEntities = world.getAllEntities();
                var allPatrols = allEntities.stream()
                        .filter(Patrol.class::isInstance)
                        .map(Patrol.class::cast)
                        .collect(Collectors.toList());
                var allIncidents = allEntities.stream()
                        .filter(x -> x instanceof Incident && ((Incident) x).isActive())
                        .map(Incident.class::cast)
                        .collect(Collectors.toList());
                var simulationTimeLong = world.getSimulationTimeLong();
                var isNight = world.isNight();
                try {
                    writeToSimulationDetailsCsvFile(simulationTimeLong, allPatrols, allIncidents, isNight);
                    writeToDistrictsDetailsCsvFile(simulationTimeLong, allPatrols, allIncidents, isNight);
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

    private void writeToSimulationDetailsCsvFile(long simulationTimeLong, List<Patrol> allPatrols, List<Incident> allIncidents, boolean isNight) throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(simulationDetailsCsvFile, true));
        csvWriter.writeNext(new String[]{
                String.valueOf(simulationTimeLong),
                String.valueOf(allPatrols.size()),
                String.valueOf(allPatrols.stream().filter(x -> x.getState() == Patrol.State.PATROLLING).count()),
                String.valueOf(allPatrols.stream().filter(x -> x.getState() == Patrol.State.CALCULATING_PATH).count()),
                String.valueOf(allPatrols.stream().filter(x -> x.getState() == Patrol.State.TRANSFER_TO_INTERVENTION).count()),
                String.valueOf(allPatrols.stream().filter(x -> x.getState() == Patrol.State.TRANSFER_TO_FIRING).count()),
                String.valueOf(allPatrols.stream().filter(x -> x.getState() == Patrol.State.INTERVENTION).count()),
                String.valueOf(allPatrols.stream().filter(x -> x.getState() == Patrol.State.FIRING).count()),
                String.valueOf(world.getNeutralizedPatrolsTotal() + allPatrols.stream().filter(x -> x.getState() == Patrol.State.NEUTRALIZED).count()),
                String.valueOf(allPatrols.stream().filter(x -> x.getState() == Patrol.State.RETURNING_TO_HQ).count()),
                String.valueOf(allIncidents.size()),
                String.valueOf(allIncidents.stream().filter(Intervention.class::isInstance).count()),
                String.valueOf(allIncidents.stream().filter(x -> x instanceof Intervention && ((Intervention) x).getPatrolSolving() != null).count()),
                String.valueOf(allIncidents.stream().filter(Firing.class::isInstance).count()),
                String.valueOf(allIncidents.stream().filter(x -> x instanceof Firing && !((Firing) x).getPatrolsSolving().isEmpty()).count()),
                isNight ? "1" : "0"
        }, false);
        csvWriter.close();
    }

    private void writeToDistrictsDetailsCsvFile(long simulationTimeLong, List<Patrol> allPatrols, List<Incident> allIncidents, boolean isNight) throws IOException {
        var districts = world.getDistricts();
        var csvWriter = new CSVWriter(new FileWriter(districtsDetailsCsvFile, true));
        for (District d : districts) {
            var allPatrolsInDistrict = allPatrols.stream().filter(x -> d.contains(x.getPosition())).collect(Collectors.toList());
            csvWriter.writeNext(new String[]{
                    String.valueOf(simulationTimeLong),
                    d.getName(),
                    String.valueOf(d.getThreatLevel()),
                    String.valueOf(allPatrolsInDistrict.size()),
                    String.valueOf(allPatrolsInDistrict.stream().filter(x -> x.getState() == Patrol.State.PATROLLING).count()),
                    String.valueOf(allPatrolsInDistrict.stream().filter(x -> x.getState() == Patrol.State.CALCULATING_PATH).count()),
                    String.valueOf(allPatrolsInDistrict.stream().filter(x -> x.getState() == Patrol.State.TRANSFER_TO_INTERVENTION).count()),
                    String.valueOf(allPatrolsInDistrict.stream().filter(x -> x.getState() == Patrol.State.TRANSFER_TO_FIRING).count()),
                    String.valueOf(allPatrolsInDistrict.stream().filter(x -> x.getState() == Patrol.State.INTERVENTION).count()),
                    String.valueOf(allPatrolsInDistrict.stream().filter(x -> x.getState() == Patrol.State.FIRING).count()),
                    String.valueOf(allPatrolsInDistrict.stream().filter(x -> x.getState() == Patrol.State.RETURNING_TO_HQ).count()),
                    String.valueOf(allIncidents.stream().filter(x -> d.contains(x.getPosition())).count()),
                    isNight ? "1" : "0"
            }, false);
        }
        csvWriter.close();
    }
}
