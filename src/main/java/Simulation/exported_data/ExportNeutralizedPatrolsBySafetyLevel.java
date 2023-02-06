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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


public class ExportNeutralizedPatrolsBySafetyLevel extends AbstractExportData {
    private final World world = World.getInstance();
    private static final String CSV_DIRECTORY_PATH = "results";
    private List neutralizedPatrolsBySafetyLevelHeader = new ArrayList<String>(){
        {
            add("Safe");
            add("RatherSafe");
            add("NotSafe");
        }
    };

    private static ExportNeutralizedPatrolsBySafetyLevel instance;
    private final File neutralizedPatrolsBySafetyLevelHeaderCsvFile;

    public ExportNeutralizedPatrolsBySafetyLevel() {
        String[] array = new String[neutralizedPatrolsBySafetyLevelHeader.size()];
        neutralizedPatrolsBySafetyLevelHeader.toArray(array); // fill the array
        neutralizedPatrolsBySafetyLevelHeaderCsvFile = createExportFile(CSV_DIRECTORY_PATH, array, "--Neutralized Patrols By Safety Level.csv");
    }
    public static ExportNeutralizedPatrolsBySafetyLevel getInstance() {
        // Result variable here may seem pointless, but it's needed for DCL (Double-checked locking).
        var result = instance;
        if (instance != null) {
            return result;
        }
        synchronized (ExportNeutralizedPatrolsBySafetyLevel.class) {
            if (instance == null) {
                instance = new ExportNeutralizedPatrolsBySafetyLevel();
            }
            return instance;
        }
    }

    public void writeToCsvFile() {
        try {
            writeToFiringsDetailsCsvFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFiringsDetailsCsvFile() throws IOException {
        var csvWriter = new CSVWriter(new FileWriter(neutralizedPatrolsBySafetyLevelHeaderCsvFile, true));
        var valueList = new ArrayList<>();
        Map<String, Long> neutralizedPatrolsSafetyMap = StatisticsCounter.getInstance().getNeutralizedPatrolSafetyLevel()
                .stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        for (int i = 0; i < neutralizedPatrolsBySafetyLevelHeader.size(); i++) {
            if (neutralizedPatrolsSafetyMap.get(neutralizedPatrolsBySafetyLevelHeader.get(i)) != null) {
                valueList.add(neutralizedPatrolsSafetyMap.get(neutralizedPatrolsBySafetyLevelHeader.get(i)));
            } else {
                valueList.add(0);
            }
        }

        String myCsvValues = StringUtils.join(valueList, ",");

        csvWriter.writeNext(new String[]{
                myCsvValues
        }, false);
        csvWriter.close();
    }
}

