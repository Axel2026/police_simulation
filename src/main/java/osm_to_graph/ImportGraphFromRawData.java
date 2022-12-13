package osm_to_graph;

import de.westnordost.osmapi.map.MapDataParser;
import de.westnordost.osmapi.map.OsmMapDataFactory;
import de.westnordost.osmapi.map.data.BoundingBox;
import org.apache.commons.io.FileUtils;
import utils.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class ImportGraphFromRawData {

    private static final String RAW_DATA_FILE_1 = "export";
    private static final String RAW_DATA_FILE_2 = "Raw.osm";
    private static final String RAW_DATA_DISTRICT_FILE_1 = "export";
    private static final String RAW_DATA_DISTRICT_FILE_2 = "DistrictsRaw.osm";
    private static final String RAW_DATA_PATH = "OsmRawData/";
    private static final String QUERY_1 = "area[\"admin_level\"=";
    private static final String QUERY_2 = "][name=\"";
    private static final String QUERY_3 = "\"]->.a;(way(area.a)[\"highway\"~\"^(motorway|trunk|primary|secondary|tertiary|unclassified|residential|motorway_link|trunk_link|primary_link|secondary_link|tertiary_link|living_street|service|pedestrian|track|road)$\"][\"crossing\"!~\".\"][\"name\"];);out meta;>;out meta qt;";
    private static final String API_URL = "https://overpass-api.de/api/interpreter";
    private static final String QUERY_DISTRICT_1 = "area[\"admin_level\"=";
    private static final String QUERY_DISTRICT_2 = "][name=\"";
    private static final String QUERY_DISTRICT_3 = "\"]->.a;(relation(area.a)[\"admin_level\"=";
    private static final String QUERY_DISTRICT_4 = "][boundary=administrative][\"name\"];);out meta;>;out meta qt;";

    private ImportGraphFromRawData() {
    }

    public static entities.Map createMap(String cityName, int cityAdminLevel, int districtAdminLevel) throws IOException {
        ParsingMapDataHandler dataHandler = handleRawData(RAW_DATA_PATH + RAW_DATA_FILE_1 + cityName + RAW_DATA_FILE_2, QUERY_1 + cityAdminLevel + QUERY_2 + cityName + QUERY_3, cityName, false);
        ParsingMapDataHandler districtDataHandler = handleRawData(RAW_DATA_PATH + RAW_DATA_DISTRICT_FILE_1 + cityName + RAW_DATA_DISTRICT_FILE_2, QUERY_DISTRICT_1 + cityAdminLevel + QUERY_DISTRICT_2 + cityName + QUERY_DISTRICT_3 + districtAdminLevel + QUERY_DISTRICT_4, cityName, true);
        Logger.getInstance().logNewOtherMessage("Loaded map data.");

        BoundingBox boundingBox = new BoundingBox(dataHandler.getMinLatitude(), dataHandler.getMinLongitude(), dataHandler.getMaxLatitude(), dataHandler.getMaxLongitude());
        return new entities.Map(dataHandler.getGraph(), dataHandler.getNodesMap(), boundingBox, districtDataHandler.getDistricts());
    }

    public static ParsingMapDataHandler handleRawData(String rawDataFilePath, String query, String cityName, boolean districtData) throws IOException {
        ParsingMapDataHandler dataHandler;
        try {
            InputStream fin = new FileInputStream(rawDataFilePath);
            dataHandler = handleInputStream(cityName, fin, true, districtData);
            // close the file:
            fin.close();
        } catch (IOException e) {
            dataHandler = handleRawDataFromRequest(query, cityName, districtData);
        }
        return dataHandler;
    }

    public static ParsingMapDataHandler handleRawDataFromRequest(String query, String cityName, boolean districtData) throws IOException {
        HttpURLConnection urlConn = makeRequest(API_URL, query);
        InputStream inputStream = urlConn.getInputStream();
        return handleInputStream(cityName, inputStream, false, districtData);
    }

    public static ParsingMapDataHandler handleInputStream(String cityName, InputStream inputStream, boolean doesFileExist, boolean districtData) throws IOException {
        BufferedInputStream bin;

        if (!doesFileExist) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            inputStream.transferTo(baos);
            InputStream firstClone = new ByteArrayInputStream(baos.toByteArray());
            InputStream secondClone = new ByteArrayInputStream(baos.toByteArray());

            writeRawDataToFile(cityName, firstClone, districtData);

            bin = new BufferedInputStream(secondClone);
        } else {
            bin = new BufferedInputStream(inputStream);
        }

        ParsingMapDataHandler dataHandler = new ParsingMapDataHandler();
        OsmMapDataFactory factory = new OsmMapDataFactory();
        MapDataParser mapDataParser = new MapDataParser(dataHandler, factory);
        mapDataParser.parse(bin);
        return dataHandler;
    }

    public static void writeRawDataToFile(String cityName, InputStream inputStream, boolean districtData) throws IOException {
        File myObj;
        if (districtData) {
            myObj = new File(RAW_DATA_PATH + RAW_DATA_DISTRICT_FILE_1 + cityName + RAW_DATA_DISTRICT_FILE_2);
        } else {
            myObj = new File(RAW_DATA_PATH + RAW_DATA_FILE_1 + cityName + RAW_DATA_FILE_2);
        }
        FileUtils.copyInputStreamToFile(inputStream, myObj);
    }

    public static HttpURLConnection makeRequest(String apiURL, String query) throws IOException {
        URL url = new URL(apiURL);
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        urlConn.setRequestMethod("POST");
        urlConn.setDoOutput(true);
        urlConn.setRequestProperty("data", query);
        urlConn.setRequestProperty("Content-Length", Integer.toString(query.length()));
        urlConn.getOutputStream().write(query.getBytes(StandardCharsets.UTF_8));
        return urlConn;
    }
}
