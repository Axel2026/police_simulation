package Simulation.exported_data;

import Simulation.StatisticsCounter;
import Simulation.World;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExportNeutralizedPatrolsPerDistrict extends AbstractExportData {
    private final World world = World.getInstance();
    private static final String CSV_DIRECTORY_PATH = "results";
    private List neutralizedPatrolsPerDistrictHeader = new ArrayList<String>();

    private static ExportNeutralizedPatrolsPerDistrict instance;
    private final File neutralizedPatrolsPerDistrictHeaderCsvFile;

    private ExportNeutralizedPatrolsPerDistrict() {
        var allDistricts = world.getDistricts();
        for (var district : allDistricts) {
            neutralizedPatrolsPerDistrictHeader.add(district.getName());
        }
        String[] array = new String[neutralizedPatrolsPerDistrictHeader.size()];
        neutralizedPatrolsPerDistrictHeader.toArray(array); // fill the array
        neutralizedPatrolsPerDistrictHeaderCsvFile = createExportFile(CSV_DIRECTORY_PATH, array, "--Neutralized Patrols Per District.csv");
    }

    public static ExportNeutralizedPatrolsPerDistrict getInstance() {
        // Result variable here may seem pointless, but it's needed for DCL (Double-checked locking).
        var result = instance;
        if (instance != null) {
            return result;
        }
        synchronized (ExportNeutralizedPatrolsPerDistrict.class) {
            if (instance == null) {
                instance = new ExportNeutralizedPatrolsPerDistrict();
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
        var csvWriter = new CSVWriter(new FileWriter(neutralizedPatrolsPerDistrictHeaderCsvFile, true));
        var valueList = new ArrayList<>();
        Map<String, Long> neutralizedPatrolsDistrictMap = StatisticsCounter.getInstance().getNeutralizedPatrolDistrict()
                .stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        for (int i = 0; i < neutralizedPatrolsPerDistrictHeader.size(); i++) {
            if (neutralizedPatrolsDistrictMap.get(neutralizedPatrolsPerDistrictHeader.get(i)) != null) {
                valueList.add(neutralizedPatrolsDistrictMap.get(neutralizedPatrolsPerDistrictHeader.get(i)));
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

