package Simulation.exported_data;

import Simulation.StatisticsCounter;
import Simulation.World;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExportFiringsPerSwat extends AbstractExportData {

    private static final String CSV_DIRECTORY_PATH = "results";
    private static final String[] firingsPerSwatHeader = new String[]{
            "amountOfFirings",
            "amountOfSwatSquads",
            "firingsPerSwatSquad",
    };
    private static ExportFiringsPerSwat instance;
    private final World world = World.getInstance();
    private final File firingsPerSwatHeaderCsvFile;

    private ExportFiringsPerSwat() {
        firingsPerSwatHeaderCsvFile = createExportFile(CSV_DIRECTORY_PATH, firingsPerSwatHeader, "--Firings Per Swat.csv");
    }

    public static ExportFiringsPerSwat getInstance() {
        // Result variable here may seem pointless, but it's needed for DCL (Double-checked locking).
        var result = instance;
        if (instance != null) {
            return result;
        }
        synchronized (ExportFiringsPerSwat.class) {
            if (instance == null) {
                instance = new ExportFiringsPerSwat();
            }
            return instance;
        }
    }

    public void writeToCsvFile() {
        try {
            writeToFiringsPerSwatCsvFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFiringsPerSwatCsvFile() throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(firingsPerSwatHeaderCsvFile, true));
        double amountOfFirings = StatisticsCounter.getInstance().getNumberOfFirings();
        var amountOfSwatSquads = world.getDistricts().size() * world.getConfig().getSWATSquadsPerDistrict();
        double firingsPerSwat = amountOfFirings / amountOfSwatSquads;

        csvWriter.writeNext(new String[]{
                String.valueOf(amountOfFirings),
                String.valueOf(amountOfSwatSquads),
                String.valueOf(firingsPerSwat),
        }, false);

        csvWriter.close();
    }
}

