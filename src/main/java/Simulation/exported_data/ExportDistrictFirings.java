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

public class ExportDistrictFirings extends AbstractExportData {
    private final World world = World.getInstance();
    private static final String CSV_DIRECTORY_PATH = "results";
    private List districtFiringsHeader = new ArrayList<String>();

    private static ExportDistrictFirings instance;
    private final File districtFiringsHeaderCsvFile;

    private ExportDistrictFirings() {
        var allDistricts = world.getDistricts();
        for (var district : allDistricts) {
            districtFiringsHeader.add(district.getName());
        }
        String[] array = new String[districtFiringsHeader.size()];
        districtFiringsHeader.toArray(array); // fill the array
        districtFiringsHeaderCsvFile = createExportFile(CSV_DIRECTORY_PATH, array, "--District Firings.csv");
    }

    public static ExportDistrictFirings getInstance() {
        // Result variable here may seem pointless, but it's needed for DCL (Double-checked locking).
        var result = instance;
        if (instance != null) {
            return result;
        }
        synchronized (ExportDistrictFirings.class) {
            if (instance == null) {
                instance = new ExportDistrictFirings();
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
        var csvWriter = new CSVWriter(new FileWriter(districtFiringsHeaderCsvFile, true));
        var valueList = new ArrayList<>();
        Map<String, Long> districtFiringsMap = StatisticsCounter.getInstance().getFiringDistricts()
                .stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        for (int i = 0; i < districtFiringsHeader.size(); i++) {
            if (districtFiringsMap.get(districtFiringsHeader.get(i)) != null) {
                valueList.add(districtFiringsMap.get(districtFiringsHeader.get(i)));
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

