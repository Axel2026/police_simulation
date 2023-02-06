package Visualisation;

import Simulation.World;
import Simulation.entities.*;
import Simulation.exported_data.*;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import main.LineChart;
import main.Main;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import Simulation.SimulationThread;
import Simulation.StatisticsCounter;
import utils.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class MapPanel {

    private final JPanel panel;
    private final JFrame frameCharts = new JFrame();
    private final JXMapViewer mapViewer = new JXMapViewer();
    private final JButton simulationPauseButton = new JButton("Pause");
    private final JButton simulationFinishButton = new JButton("Finish");
    private final JButton hqButton = new JButton("HQ");
    private final JCheckBox willChangeIntoFiringCheckbox = new JCheckBox();
    private final World world = World.getInstance();
    private GeoPosition hqPosition;
    private GeoPosition hospitalPosition;

    public MapPanel(JPanel panel) {
        var info = new OSMTileFactoryInfo();
        var tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        mapViewer.addMouseMotionListener(new PanMouseInputListener(mapViewer));
        JScrollPane scrollPane = new JScrollPane();
        frameCharts.setLayout(new GridLayout(0, 2));
        mapViewer.setOverlayPainter(new MapPainter());
        this.panel = panel;
    }

    public void createMapWindow() {
        panel.setSize(1000, 1000);

        var position = World.getInstance().getPosition();

        mapViewer.setAddressLocation(new GeoPosition(position.getLatitude(), position.getLongitude()));

        mapViewer.setZoom(7);

        simulationPauseButton.setMaximumSize(new Dimension(50, 50));

        simulationPauseButton.addActionListener(new ActionListener() {

            private boolean showingPause = !World.getInstance().isSimulationPaused();

            @Override
            public void actionPerformed(ActionEvent e) {
                if (showingPause) {
                    World.getInstance().pauseSimulation();
                    JButton button = (JButton) e.getSource();
                    button.setText("Resume");
                    showingPause = false;
                } else {
                    World.getInstance().resumeSimulation(Main.getPanel());
                    JButton button = (JButton) e.getSource();
                    button.setText("Pause");
                    showingPause = true;
                }
            }

        });

        simulationFinishButton.setMaximumSize(new Dimension(50, 50));
        hqButton.setMaximumSize(new Dimension(50, 50));

        simulationFinishButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                World.getInstance().finishSimulation();
                System.out.println("Simulation finished!");
            }

        });

        hqButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                addPatrolWindow(hqPosition);
            }

        });
        mapViewer.add(simulationPauseButton);
        mapViewer.add(simulationFinishButton);
        mapViewer.add(hqButton);

        panel.add(mapViewer);
        panel.setVisible(true);
    }

    public void selectHQLocation() {
        mapViewer.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                var position = mapViewer.convertPointToGeoPosition(e.getPoint());
                Logger.getInstance().logNewOtherMessage("HQ position has been selected.");
                hqPosition = mapViewer.convertPointToGeoPosition(e.getPoint());
                var hq = new Headquarters(position.getLatitude(), position.getLongitude());
                World.getInstance().addEntity(hq);
                selectInterventionLocation();
                selectHospitalLocation();
                // GUI Drawing thread
//                new Thread(() -> {
//                    while (!World.getInstance().hasSimulationDurationElapsed() && !World.getInstance().isSimulationFinished()) {
//                        mapViewer.repaint();
//                        try {
//                            Thread.sleep(1000 / 30);
//                        } catch (Exception exception) {
//                            // Ignore
//                            exception.printStackTrace();
//                            Thread.currentThread().interrupt();
//                        }
//                    }
//
//                    try {
//                        showSummary();
//                    } catch (IOException ioException) {
//                        ioException.printStackTrace();
//                    }
//                }).start();
//
//                // Simulation thread
//                new SimulationThread().start();

                mapViewer.removeMouseListener(this);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // nothing should be happening here
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // nothing should be happening here
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // nothing should be happening here
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // nothing should be happening here
            }
        });
        JOptionPane.showMessageDialog(panel, "Please select HQ location.");
    }

    public void addAmbulances(GeoPosition position, int amountOfAmbulances) {
        for (int i = 0; i < amountOfAmbulances; i++) {
            Ambulance ambulance = new Ambulance(position.getLatitude(), position.getLongitude());
            World.getInstance().addEntity(ambulance);
        }
    }

    public void selectHospitalLocation() {
        mapViewer.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                var position = mapViewer.convertPointToGeoPosition(e.getPoint());
                Logger.getInstance().logNewOtherMessage("HQ position has been selected.");
                hospitalPosition = mapViewer.convertPointToGeoPosition(e.getPoint());
                var hospital = new Hospital(position.getLatitude(), position.getLongitude());
                World.getInstance().addEntity(hospital);
//                addAmbulances(hospitalPosition, 5);
                // GUI Drawing thread
                new Thread(() -> {
                    while (!World.getInstance().hasSimulationDurationElapsed() && !World.getInstance().isSimulationFinished()) {
                        mapViewer.repaint();
                        try {
                            Thread.sleep(1000 / 30);
                        } catch (Exception exception) {
                            // Ignore
                            exception.printStackTrace();
                            Thread.currentThread().interrupt();
                        }
                    }

                    try {
                        showSummary();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }).start();

                // Simulation thread
                new SimulationThread().start();

                mapViewer.removeMouseListener(this);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // nothing should be happening here
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // nothing should be happening here
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // nothing should be happening here
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // nothing should be happening here
            }
        });
        JOptionPane.showMessageDialog(panel, "Please select hospital location.");
    }

    public void selectInterventionLocation() {
        mapViewer.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {

                    var position = mapViewer.convertPointToGeoPosition(e.getPoint());
                    addInterventionWindow(position);

                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // nothing should be happening here
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // nothing should be happening here
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // nothing should be happening here
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // nothing should be happening here
            }
        });
    }

    private void addInterventionWindow(GeoPosition position) {
        JFrame frame = new JFrame("Insert values");
        frame.setVisible(true);
        frame.setSize(300, 250);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);

        JLabel interventionDurationLabel = new JLabel("Intervention duration [min]");
        interventionDurationLabel.setPreferredSize(new Dimension(180, 40));
        JPanel addInterventionPanel = new JPanel();
        frame.add(addInterventionPanel);
        addInterventionPanel.add(interventionDurationLabel);

        final JTextField interventionDurationInput = new JTextField(4);
        interventionDurationInput.setText(String.valueOf(30));
        addInterventionPanel.add(interventionDurationInput);

        JLabel willChangeIntoFiringLabel = new JLabel("will change into firing");
        willChangeIntoFiringLabel.setPreferredSize(new Dimension(180, 40));
        addInterventionPanel.add(willChangeIntoFiringLabel);
        willChangeIntoFiringCheckbox.setPreferredSize(new Dimension(50, 20));
        addInterventionPanel.add(willChangeIntoFiringCheckbox);

        JLabel timeToChangeIntoFiringLabel = new JLabel("time to change into firing [min]");
        timeToChangeIntoFiringLabel.setPreferredSize(new Dimension(180, 40));
        timeToChangeIntoFiringLabel.setVisible(false);
        addInterventionPanel.add(timeToChangeIntoFiringLabel);

        final JTextField timeToChangeIntoFiringInput = new JTextField(4);
        timeToChangeIntoFiringInput.setVisible(false);
        timeToChangeIntoFiringInput.setText(String.valueOf(10));

        addInterventionPanel.add(timeToChangeIntoFiringInput);

        var jSeparator = new JSeparator();
        jSeparator.setOrientation(SwingConstants.HORIZONTAL);
        jSeparator.setPreferredSize(new Dimension(240, 10));
        addInterventionPanel.add(jSeparator);

        JButton generateIntervention = new JButton("Generate Intervention");
        generateIntervention.setPreferredSize(new Dimension(240, 40));
        addInterventionPanel.add(generateIntervention);

        final JLabel output = new JLabel(); // A label for your output
        addInterventionPanel.add(output);

        willChangeIntoFiringCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (willChangeIntoFiringCheckbox.isSelected()) {
                    timeToChangeIntoFiringInput.setVisible(true);
                    timeToChangeIntoFiringLabel.setVisible(true);
                } else {
                    timeToChangeIntoFiringInput.setVisible(false);
                    timeToChangeIntoFiringLabel.setVisible(false);
                }
            }
        });

        generateIntervention.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                var allDistricts = world.getDistricts();
                Intervention intervention = null;

                for (var district : allDistricts) {
                    if (district.contains(position.getLatitude(), position.getLongitude())) {
                        intervention = new Intervention(
                                position.getLatitude(),
                                position.getLongitude(),
                                (long) (Integer.parseInt(interventionDurationInput.getText()) * (60.0)),
                                willChangeIntoFiringCheckbox.isSelected(),
                                (long) (Integer.parseInt(timeToChangeIntoFiringInput.getText()) * (60.0)),
                                district
                        );
                    }
                }

                World.getInstance().addEntity(intervention);
                willChangeIntoFiringCheckbox.setSelected(false);
                frame.setVisible(false);
            }
        });
    }

    private int amountOfPatrols() {
        String allEntites = World.getInstance().getAllEntities().toString();
        int index = allEntites.indexOf("Visualisation.Patrol");
        int patrolsCounter = -1;
        while (index != -1) {
            patrolsCounter++;
            allEntites = allEntites.substring(index + 1);
            index = allEntites.indexOf("Patrol");
        }
        return patrolsCounter;
    }

    private void addPatrolWindow(GeoPosition position) {
        JFrame frame = new JFrame("Headquarters");
        frame.setVisible(true);
        frame.setSize(330, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);

        JPanel addPatrolsPanel = new JPanel();
        frame.add(addPatrolsPanel);

        JLabel numberOfPatrolsLabel = new JLabel("current number of patrols: " + amountOfPatrols());
        numberOfPatrolsLabel.setPreferredSize(new Dimension(240, 30));

        JLabel neutralizedPatrolsLabel = new JLabel("neutralized patrols: " + StatisticsCounter.getInstance().getNumberOfNeutralizedPatrols());
        neutralizedPatrolsLabel.setPreferredSize(new Dimension(240, 30));

        JLabel numberOfInterventionsLabel = new JLabel("number of interventions [solved]: " + StatisticsCounter.getInstance().getNumberOfInterventions() + " [" + StatisticsCounter.getInstance().getNumberOfSolvedInterventions() + "]");
        numberOfInterventionsLabel.setPreferredSize(new Dimension(240, 30));

        JLabel numberOfFiringsLabel = new JLabel("number of firings [solved]: " + StatisticsCounter.getInstance().getNumberOfFirings() + " [" + StatisticsCounter.getInstance().getNumberOfSolvedFirings() + "]");
        numberOfFiringsLabel.setPreferredSize(new Dimension(240, 30));

        JLabel addNumberOfPatrolsLabel = new JLabel("add patrols");
        addNumberOfPatrolsLabel.setPreferredSize(new Dimension(180, 30));

        addPatrolsPanel.add(numberOfPatrolsLabel);
        addPatrolsPanel.add(neutralizedPatrolsLabel);
        addPatrolsPanel.add(numberOfInterventionsLabel);
        addPatrolsPanel.add(numberOfFiringsLabel);
        addPatrolsPanel.add(addNumberOfPatrolsLabel);

        final JTextField addNumberOfPatrols = new JTextField(4);
        addNumberOfPatrols.setText(String.valueOf(1));
        addPatrolsPanel.add(addNumberOfPatrols);

        var jSeparator = new JSeparator();
        jSeparator.setOrientation(SwingConstants.HORIZONTAL);
        jSeparator.setPreferredSize(new Dimension(240, 10));
        addPatrolsPanel.add(jSeparator);

        JButton addPatrols = new JButton("add patrols");
        addPatrols.setPreferredSize(new Dimension(240, 40));
        addPatrolsPanel.add(addPatrols);

        final JLabel output = new JLabel(); // A label for your output
        addPatrolsPanel.add(output);

        addPatrols.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < (Integer.parseInt(addNumberOfPatrols.getText())); i++) {
                    Patrol patrol = new Patrol(position.getLatitude(), position.getLongitude());
                    patrol.setState(Patrol.State.PATROLLING);
                    World.getInstance().addEntity(patrol);
                }
                frame.setVisible(false);
            }
        });
    }

    private void showSimulationCharts() throws IOException {
        CSVReader reader = new CSVReaderBuilder(new FileReader("results/" + World.getInstance().getExportSimulationAndDistrictDetails().getSimulationDetailsCsvFileName())).build();

        List<String> timeList = new ArrayList<String>();
        List<String> incidentsList = new ArrayList<String>();
        List<String> interventionsList = new ArrayList<String>();
        List<String> firingsList = new ArrayList<String>();
        List<String> notSafeList = new ArrayList<String>();
        List<String> ratherSafeList = new ArrayList<String>();
        List<String> safeList = new ArrayList<String>();
        List<String> patrollingList = new ArrayList<String>();
        List<String> interventionList = new ArrayList<String>();
        List<String> firingList = new ArrayList<String>();
        DefaultCategoryDataset datasetIncidents = new DefaultCategoryDataset();
        DefaultCategoryDataset datasetInterventionsAndFirings = new DefaultCategoryDataset();
        DefaultCategoryDataset datasetIncidentsPerSafetyLevel = new DefaultCategoryDataset();
        DefaultCategoryDataset datasetPatrolsStates = new DefaultCategoryDataset();
        String[] record = null;
        String currentSimulationTime = "";

        //Incidents per 10 minutes of simulation

        while ((record = reader.readNext()) != null) {
            if (!record[0].equals(currentSimulationTime) && reader.getLinesRead() > 1) {
                timeList.add(Integer.toString(Integer.parseInt(record[0]) / 3600));
                incidentsList.add(record[10]);
                interventionsList.add(record[11]);
                firingsList.add(record[13]);
                currentSimulationTime = record[0];
                notSafeList.add("0");
                ratherSafeList.add("0");
                safeList.add("0");
                patrollingList.add(record[2]);
                interventionList.add(record[6]);
                firingList.add(record[7]);

            } else if (record[0].equals(currentSimulationTime) && reader.getLinesRead() > 1) {
                int lastIndex = notSafeList.size() - 1;
                switch (record[2]) {
                    case "NotSafe":
                        int lastValueNotSafe = Integer.parseInt(notSafeList.get(lastIndex));
                        notSafeList.remove(lastIndex);
                        notSafeList.add(Integer.toString(lastValueNotSafe + Integer.parseInt(record[11])));
                        break;
                    case "RatherSafe":
                        int lastValueRatherSafe = Integer.parseInt(ratherSafeList.get(lastIndex));
                        ratherSafeList.remove(lastIndex);
                        ratherSafeList.add(Integer.toString(lastValueRatherSafe + Integer.parseInt(record[11])));
                        break;
                    case "Safe":
                        int lastValueSafe = Integer.parseInt(safeList.get(lastIndex));
                        safeList.remove(lastIndex);
                        safeList.add(Integer.toString(lastValueSafe + Integer.parseInt(record[11])));
                        break;
                    default:
                        System.out.println("None of those were correct levels: " + record[2]);
                }
            }
        }

        for (int i = 0; i < timeList.size(); i++) {
            datasetIncidents.addValue(Integer.parseInt(incidentsList.get(i)), "Number of incidents", timeList.get(i));
        }

        SwingUtilities.invokeLater(() -> {
            new LineChart("Number of incidents", "Simulation time[h]", "Incidents", datasetIncidents, frameCharts);
        });

        //Interventions and firings per 10 minutes of simulation

        for (int i = 0; i < timeList.size(); i++) {
            datasetInterventionsAndFirings.addValue(Integer.parseInt(interventionsList.get(i)), "Number of interventions", timeList.get(i));
            datasetInterventionsAndFirings.addValue(Integer.parseInt(firingsList.get(i)), "Number of firings", timeList.get(i));
        }

        SwingUtilities.invokeLater(() -> {
            new LineChart("Number of interventions and firings", "Simulation time[h]", "Incidents", datasetInterventionsAndFirings, frameCharts);
        });

        //Incidents per safety level

        for (int i = 0; i < timeList.size(); i++) {
            if (notSafeList.size() > i) {
                datasetIncidentsPerSafetyLevel.addValue(Integer.parseInt(notSafeList.get(i)), "Not Safe", timeList.get(i));
            }

            if (ratherSafeList.size() > i) {
                datasetIncidentsPerSafetyLevel.addValue(Integer.parseInt(ratherSafeList.get(i)), "Rather Safe", timeList.get(i));
            }

            if (safeList.size() > i) {
                datasetIncidentsPerSafetyLevel.addValue(Integer.parseInt(safeList.get(i)), "Safe", timeList.get(i));
            }
        }

        SwingUtilities.invokeLater(() -> {
            new LineChart("Number of incidents per safety level", "Simulation time[h]", "Incidents", datasetIncidentsPerSafetyLevel, frameCharts);
        });

        //Patrols states per hour

        for (int i = 0; i < timeList.size(); i++) {
            datasetPatrolsStates.addValue(Integer.parseInt(patrollingList.get(i)), "Number of patrolling patrols", timeList.get(i));
            datasetPatrolsStates.addValue(Integer.parseInt(interventionList.get(i)), "Number of intervening patrols", timeList.get(i));
            datasetPatrolsStates.addValue(Integer.parseInt(firingList.get(i)), "Number of firing patrols", timeList.get(i));
        }

        SwingUtilities.invokeLater(() -> {
            new LineChart("Number of patrols in different states", "Simulation time[h]", "Patrols", datasetPatrolsStates, frameCharts);
        });

        reader.close();
    }

    private void showSummary() throws IOException {
        showSimulationCharts();
        ExportDistrictIterventions.getInstance().writeToCsvFile();
        ExportNeutralizedPatrolsBySafetyLevel.getInstance().writeToCsvFile();
        ExportNeutralizedPatrolsPerDistrict.getInstance().writeToCsvFile();
        ExportDistrictFirings.getInstance().writeToCsvFile();
        ExportFiringsPerAmbulance.getInstance().writeToCsvFile();
        ExportIncidentsHourDuration.getInstance().writeToCsvFile();
        String simulationSummaryMessage = "Simulation has finished.\n\n" +
                "Simulated Patrols: " + StatisticsCounter.getInstance().getNumberOfPatrols() + "\n" +
                "Simulated Interventions: " + StatisticsCounter.getInstance().getNumberOfInterventions() + "\n" +
                "Simulated Firings: " + StatisticsCounter.getInstance().getNumberOfFirings() + "\n" +
                "Neutralized Patrols: " + StatisticsCounter.getInstance().getNeutralizedPatrolDistrict().size() + "\n" +
                "Solved Interventions: " + StatisticsCounter.getInstance().getNumberOfSolvedInterventions() + "\n" +
                "Solved Firings: " + StatisticsCounter.getInstance().getNumberOfSolvedFirings() + "\n";
        JOptionPane.showMessageDialog(panel, simulationSummaryMessage);
    }

    class MapPainter implements Painter<JXMapViewer> {

        @Override
        public void paint(Graphics2D g, JXMapViewer mapViewer, int width, int height) {
            if (World.getInstance().getConfig().isDrawDistrictsBorders()) {
                World.getInstance().getMap().getDistricts().forEach(x -> x.drawSelf(g, mapViewer));
            }

            World.getInstance().getAllEntities().stream().filter(IDrawable.class::isInstance).forEach(x -> ((IDrawable) x).drawSelf(g, mapViewer));
            drawSimulationClock(g);
            if (World.getInstance().getConfig().isDrawLegend()) {
                drawLegend(g);
            }
        }

        private void drawSimulationClock(Graphics2D g) {
            var time = World.getInstance().getSimulationTimeLong();

            var days = (int) (time / 86400);
            var hours = (int) ((time % 86400) / 3600);
            var minutes = (int) ((time % 3600) / 60);
            var seconds = (int) (time % 60);

            // Draw background
            var oldColor = g.getColor();
            g.setColor(new Color(244, 226, 198, 175));
            g.fillRect(5, 5, 150, 20);
            g.setColor(oldColor);

            // Draw date
            var oldFont = g.getFont();
            g.setFont(new Font("TimesRoman", Font.BOLD, 15));
            g.drawString(String.format("Day: %03d, %02d:%02d:%02d", days, hours, minutes, seconds), 10, 20);
            g.setFont(oldFont);
        }


        private void drawLegend(Graphics2D g) {

            var topLeftCornerX = 1800;
            final var size = 10;
            final String newFont = "TimesRoman";

            // Draw background
            var oldColor = g.getColor();
            g.setColor(new Color(244, 226, 198, 225));
            g.fillRect(topLeftCornerX, 0, 1465 - topLeftCornerX, 470);
            g.setColor(oldColor);

            var swatStates = new HashMap<String, Color>();
            swatStates.put("CALCULATING_PATH", new Color(255, 123, 255));
            swatStates.put("RETURNING_TO_HQ", new Color(222, 205, 0));
            swatStates.put("INTERVENTION", new Color(0, 92, 230));
            swatStates.put("TRANSFER_TO_FIRING", new Color(102, 0, 224 ));
            swatStates.put("WAITING_FOR_ORDERS", new Color(224, 0, 180));

            int i = 0;

            for (Map.Entry<String, Color> entry : swatStates.entrySet()) {
                g.setColor(entry.getValue());
                var mark = new Ellipse2D.Double((int) 1475, 470 - 30 - i * 15.0, size, size);
                g.fill(mark);

                g.setColor(oldColor);
                g.drawString(entry.getKey(), 1475 + 18, 470 - 20 - i * 15);
                i++;
            }

            var oldFont = g.getFont();
            g.setFont(new Font(newFont, Font.BOLD, 13));
            g.drawString("SWAT's states:", 1475, 470 - 20 - i * 15);
            g.setFont(oldFont);
            i++;

            var ambulanceStates = new HashMap<String, Color>();
            ambulanceStates.put("CALCULATING_PATH", new Color(255, 123, 255));
            ambulanceStates.put("SAVING_HURT_PATROL", new Color(155, 55, 55));
            ambulanceStates.put("RETURNING_TO_HOSPITAL", new Color(222, 161, 0));
            ambulanceStates.put("ACCIDENT", new Color(255, 87, 36));
            ambulanceStates.put("TRANSFER_TO_ACCIDENT", new Color(30, 180, 200));
            ambulanceStates.put("AVAILABLE", new Color(70, 100, 200));

            for (Map.Entry<String, Color> entry : ambulanceStates.entrySet()) {
                g.setColor(entry.getValue());
                var mark = new Ellipse2D.Double((int) 1475, 470 - 40 - i * 15.0, size, size);
                g.fill(mark);

                g.setColor(oldColor);
                g.drawString(entry.getKey(), 1475 + 18, 470 - 30 - i * 15);
                i++;
            }

            oldFont = g.getFont();
            g.setFont(new Font(newFont, Font.BOLD, 13));
            g.drawString("Ambulance's states:", 1475, 470 - 30 - i * 15);
            g.setFont(oldFont);
            i++;

            var patrolStates = new HashMap<String, Color>();
            patrolStates.put("CALCULATING_PATH", new Color(255, 123, 255));
            patrolStates.put("NEUTRALIZED", new Color(255, 255, 255));
            patrolStates.put("RETURNING_TO_HQ", new Color(0, 100, 0));
            patrolStates.put("FIRING", new Color(153, 0, 204));
            patrolStates.put("TRANSFER_TO_FIRING", new Color(255, 131, 54));
            patrolStates.put("INTERVENTION", new Color(0, 92, 230));
            patrolStates.put("TXFR_TO_INTERVENTION", new Color(255, 87, 36));
            patrolStates.put("PATROLLING", new Color(0, 153, 0));

            for (Map.Entry<String, Color> entry : patrolStates.entrySet()) {
                g.setColor(entry.getValue());
                var mark = new Ellipse2D.Double((int) 1475, 470 - 50 - i * 15.0, size, size);
                g.fill(mark);

                g.setColor(oldColor);
                g.drawString(entry.getKey(), 1475 + 18, 470 - 40 - i * 15);
                i++;
            }


            oldFont = g.getFont();
            g.setFont(new Font(newFont, Font.BOLD, 13));
            g.drawString("Patrol's states:", 1475, 470 - 40 - i * 15);
            g.setFont(oldFont);
            i++;
            var places = new HashMap<String, Color>();
            var places2 = new HashMap<String, Color>();
            places.put("SWAT", Color.PINK);
            places.put("HOSPITAL", Color.CYAN);
            places.put("HQ", Color.BLUE);
            places2.put("INTERVENTION", Color.RED);
            places2.put("FIRING", Color.BLACK);

            for (Map.Entry<String, Color> entry : places2.entrySet()) {
                g.setColor(entry.getValue());
                var mark = new Ellipse2D.Double((int) 1475, 470 - 60 - i * 15.0, size, size);
                g.fill(mark);

                g.setColor(oldColor);
                g.drawString(entry.getKey(), 1475 + 18, 470 - 50 - i * 15);
                i++;
            }

            for (Map.Entry<String, Color> entry : places.entrySet()) {
                g.setColor(entry.getValue());
                var mark = new Rectangle2D.Double((int) 1475, 470 - 60 - i * 15.0, size, size);
                g.fill(mark);

                g.setColor(oldColor);
                g.drawString(entry.getKey(), 1475 + 18, 470 - 50 - i * 15);
                i++;
            }

            oldFont = g.getFont();
            g.setFont(new Font(newFont, Font.BOLD, 13));
            g.drawString("Places:", 1475, 470 - 50 - i * 15);
            g.setFont(oldFont);
        }
    }

}
