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

public class ExportDistrictIterventions extends AbstractExportData {
    private final World world = World.getInstance();
    private static final String CSV_DIRECTORY_PATH = "results";
    private List districtInterventionsHeader = new ArrayList<String>();

    private static ExportDistrictIterventions instance;
    private final File districtIterventionsHeaderCsvFile;

    private ExportDistrictIterventions() {
        var allDistricts = world.getDistricts();
        for (var district : allDistricts) {
            districtInterventionsHeader.add(district.getName());
        }
        String[] array = new String[districtInterventionsHeader.size()];
        districtInterventionsHeader.toArray(array); // fill the array
        districtIterventionsHeaderCsvFile = createExportFile(CSV_DIRECTORY_PATH, array, "--District Interventions.csv");
    }

    public static ExportDistrictIterventions getInstance() {
        // Result variable here may seem pointless, but it's needed for DCL (Double-checked locking).
        var result = instance;
        if (instance != null) {
            return result;
        }
        synchronized (ExportDistrictIterventions.class) {
            if (instance == null) {
                instance = new ExportDistrictIterventions();
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
        var csvWriter = new CSVWriter(new FileWriter(districtIterventionsHeaderCsvFile, true));
        var valueList = new ArrayList<>();
        Map<String, Long> districtInterventionsMap = StatisticsCounter.getInstance().getInterventionsDistricts()
                .stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        for (int i = 0; i < districtInterventionsHeader.size(); i++) {
            if (districtInterventionsMap.get(districtInterventionsHeader.get(i)) != null) {
                valueList.add(districtInterventionsMap.get(districtInterventionsHeader.get(i)));
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

