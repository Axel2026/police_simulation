package Simulation.exported_data;

import Simulation.StatisticsCounter;
import Simulation.World;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class ExportIncidentsHourDuration extends AbstractExportData {

    private static final String CSV_DIRECTORY_PATH = "results";
    private static final String[] incidentsHourDurationHeader = new String[]{
            "incidentDurationLessThanHour",
            "incidentDurationMoreThanHour",
    };
    private static ExportIncidentsHourDuration instance;
    private final World world = World.getInstance();
    private final File incidentsHourDurationHeaderCsvFile;

    private ExportIncidentsHourDuration() {
        incidentsHourDurationHeaderCsvFile = createExportFile(CSV_DIRECTORY_PATH, incidentsHourDurationHeader, "--Incidents Hour Duration.csv");
    }

    public static ExportIncidentsHourDuration getInstance() {
        // Result variable here may seem pointless, but it's needed for DCL (Double-checked locking).
        var result = instance;
        if (instance != null) {
            return result;
        }
        synchronized (ExportIncidentsHourDuration.class) {
            if (instance == null) {
                instance = new ExportIncidentsHourDuration();
            }
            return instance;
        }
    }

    public void writeToCsvFile() {
        try {
            writeToIncidentHourDurationCsvFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToIncidentHourDurationCsvFile() throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(incidentsHourDurationHeaderCsvFile, true));
        int durationLessThanHour = StatisticsCounter.getInstance().getAmountOfDurationLessThanHour();
        int durationMoreThanHour = StatisticsCounter.getInstance().getAmountOfDurationMoreThanHour();

        csvWriter.writeNext(new String[]{
                String.valueOf(durationLessThanHour),
                String.valueOf(durationMoreThanHour),
        }, false);

        csvWriter.close();
    }
}

