package Simulation.exported_data;

import Simulation.StatisticsCounter;
import Simulation.World;
import Simulation.entities.Firing;
import Visualisation.Ambulance;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;
import utils.Haversine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


public class ExportFiringsPerAmbulance extends AbstractExportData {

    private static final String CSV_DIRECTORY_PATH = "results";
    private static final String[] firingsPerAmbulanceHeader = new String[]{
            "amountOfFirings",
            "amountOfAmbulances",
            "firingsPerAmbulance",
    };
    private static ExportFiringsPerAmbulance instance;
    private final World world = World.getInstance();
    private final File firingsPerAmbulanceHeaderCsvFile;

    private ExportFiringsPerAmbulance() {
        firingsPerAmbulanceHeaderCsvFile = createExportFile(CSV_DIRECTORY_PATH, firingsPerAmbulanceHeader, "--Firings Per Ambulance.csv");
    }

    public static ExportFiringsPerAmbulance getInstance() {
        // Result variable here may seem pointless, but it's needed for DCL (Double-checked locking).
        var result = instance;
        if (instance != null) {
            return result;
        }
        synchronized (ExportFiringsPerAmbulance.class) {
            if (instance == null) {
                instance = new ExportFiringsPerAmbulance();
            }
            return instance;
        }
    }

    public void writeToCsvFile() {
        try {
            writeToFiringsPerAmbulanceCsvFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFiringsPerAmbulanceCsvFile() throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(firingsPerAmbulanceHeaderCsvFile, true));
        double amountOfFirings = StatisticsCounter.getInstance().getNumberOfFirings();
        var amountOfAmbulances = world.getConfig().getNumberOfAmbulances();
        double firingsPerAmbulance = amountOfFirings / amountOfAmbulances;

        csvWriter.writeNext(new String[]{
                String.valueOf(amountOfFirings),
                String.valueOf(amountOfAmbulances),
                String.valueOf(firingsPerAmbulance),
        }, false);

        csvWriter.close();
    }
}

