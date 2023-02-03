package Simulation.exported_data;

import Simulation.StatisticsCounter;
import Simulation.World;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;

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

public class ExportDistrictsFiringsPerHour extends Thread {
    private static final String CSV_DIRECTORY_PATH = "results";
    private final DateTimeFormatter dateFormat = new DateTimeFormatterBuilder().appendPattern("dd-MM-yyyy_HH-mm-ss").toFormatter();
    private final World world = World.getInstance();
    private List districtFiringsHeader = new ArrayList<String>();
    private List oldList = new ArrayList<Integer>(Collections.nCopies(world.getDistricts().size(), 0));

    private final File districtsFiringsPerHourHeaderCsvFile;
    private int exportCounter = 1;


    public ExportDistrictsFiringsPerHour() {
        var allDistricts = world.getDistricts();
        for (var district : allDistricts) {
            districtFiringsHeader.add(district.getName());
        }
        String[] array = new String[districtFiringsHeader.size()];
        districtFiringsHeader.toArray(array); // fill the array
        districtsFiringsPerHourHeaderCsvFile = createExportFile(CSV_DIRECTORY_PATH, array, "--District Firings Per Hour.csv");
    }

    @Override
    public void run() {
        while (!world.hasSimulationDurationElapsed() && !world.isSimulationFinished()) {
            if (!world.isSimulationPaused() && exportCounter <= (world.getSimulationTimeLong() / 3600)) {
                exportCounter++;
                try {
                    var csvWriter = new CSVWriter(new FileWriter(districtsFiringsPerHourHeaderCsvFile, true));
                    var result = new ArrayList<>();
                    var valueList = new ArrayList<>();
                    Map<String, Long> districtFiringsMap = StatisticsCounter.getInstance().getFiringDistricts()
                            .stream()
                            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

                    for (int i = 0; i < districtFiringsHeader.size(); i++) {
                        if (districtFiringsMap.get(districtFiringsHeader.get(i)) != null) {
                            var value = (districtFiringsMap.get(districtFiringsHeader.get(i)).intValue()) - ((Integer) oldList.get(i));
                            valueList.add(value);
                        } else {
                            valueList.add(0);
                        }
                    }

                    for (int i = 0; i < oldList.size(); i++) {
                        result.add((Integer) oldList.get(i) + (Integer) valueList.get(i));
                    }

                    oldList = result;

                    String myCsvValues = StringUtils.join(valueList, ",");
                    csvWriter.writeNext(new String[]{
                            myCsvValues
                    }, false);
                    csvWriter.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                // sleep for next 'periodOfTimeToExportDetails' minutes in simulation time
                var sleepTime = ((3600 - (world.getSimulationTime() % 3600)) * 1000) / world.getConfig().getTimeRate();
                try {
                    sleep((long) sleepTime, (int) ((sleepTime - (long) sleepTime) * 1000000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    File createExportFile(String csvDirectoryPath, String[] header, String csvFileName) {
        File csvDirectory = new File(csvDirectoryPath);
        if (!(csvDirectory.exists() && csvDirectory.isDirectory())) {
            csvDirectory.mkdir();
        }

        var csvFile = new File(csvDirectoryPath, dateFormat.format(LocalDateTime.now()) + csvFileName);
        try {
            if (!csvFile.createNewFile()) {
                throw new IOException("Unable to create file");
            }
            var csvWriter1 = new CSVWriter(new FileWriter(csvFile));
            csvWriter1.writeNext(header);
            csvWriter1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvFile;
    }
}
