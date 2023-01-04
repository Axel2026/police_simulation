package Visualisation;

import Simulation.World;
import Simulation.entities.Headquarters;
import Simulation.entities.Intervention;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import main.LineChart;
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
import java.util.List;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class MapPanel {

    private final JPanel panel;
    private final JFrame frameCharts = new JFrame();
    private final JXMapViewer mapViewer = new JXMapViewer();
    private final JButton simulationPauseButton = new JButton("Pause");
    private final JButton simulationFinishButton = new JButton("Finish");
    private final JCheckBox willChangeIntoFiringCheckbox = new JCheckBox();
    private final World world = World.getInstance();

    public MapPanel(JPanel panel) {
        var info = new OSMTileFactoryInfo();
        var tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        mapViewer.addMouseMotionListener(new PanMouseInputListener(mapViewer));
        frameCharts.setLayout(new FlowLayout());
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
                    World.getInstance().resumeSimulation();
                    JButton button = (JButton) e.getSource();
                    button.setText("Pause");
                    showingPause = true;
                }
            }

        });

        simulationFinishButton.setMaximumSize(new Dimension(50, 50));

        simulationFinishButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                World.getInstance().finishSimulation();
                System.out.println("Simulation finished!");
            }

        });
        mapViewer.add(simulationPauseButton);
        mapViewer.add(simulationFinishButton);

        panel.add(mapViewer);
        panel.setVisible(true);
    }

    public void selectHQLocation() {
        mapViewer.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                var position = mapViewer.convertPointToGeoPosition(e.getPoint());
                Logger.getInstance().logNewOtherMessage("HQ position has been selected.");

                var hq = new Headquarters(position.getLatitude(), position.getLongitude());
                World.getInstance().addEntity(hq);
                selectInterventionLocation();
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
        JOptionPane.showMessageDialog(panel, "Please select HQ location.");
    }

    public void selectInterventionLocation() {
        mapViewer.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {

                    var position = mapViewer.convertPointToGeoPosition(e.getPoint());
//                    addInterventionWindow();
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
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
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
                var districtAmount = world.getDistricts().size();
                var district = ThreadLocalRandom.current().nextInt(0, districtAmount + 1);
                var intervention = new Intervention(
                        position.getLatitude(),
                        position.getLongitude(),
                        (long) (Integer.parseInt(interventionDurationInput.getText()) * (60.0)),
                        willChangeIntoFiringCheckbox.isSelected(),
                        (long) (Integer.parseInt(timeToChangeIntoFiringInput.getText()) * (60.0)),
                        world.getDistricts().get(district)
                );
                World.getInstance().addEntity(intervention);
                willChangeIntoFiringCheckbox.setSelected(false);
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
        DefaultCategoryDataset datasetIncidents = new DefaultCategoryDataset();
        DefaultCategoryDataset datasetInterventionsAndFirings = new DefaultCategoryDataset();
        String[] record = null;
        String currentSimulationTime = "";

        //Incidents per 10 minutes of simulation

        while ((record = reader.readNext()) != null) {
            if (!record[0].equals(currentSimulationTime) && reader.getLinesRead() > 1) {
                timeList.add(Integer.toString(Integer.parseInt(record[0]) / 60));
                incidentsList.add(record[10]);
                interventionsList.add(record[11]);
                firingsList.add(record[13]);
                currentSimulationTime = record[0];
            }
        }
        System.out.println(Arrays.toString(timeList.toArray()));
        System.out.println(Arrays.toString(incidentsList.toArray()));

        for (int i = 0; i < timeList.size(); i++) {
            datasetIncidents.addValue(Integer.parseInt(incidentsList.get(i)), "Number of incidents", timeList.get(i));
        }

        SwingUtilities.invokeLater(() -> {
            new LineChart("Number of incidents", "Minutes of simulation", "Incidents", datasetIncidents, frameCharts);
        });

        //Interventions and firings per 10 minutes of simulation

        System.out.println("---------------");
        System.out.println(Arrays.toString(timeList.toArray()));
        System.out.println(Arrays.toString(interventionsList.toArray()));
        System.out.println(Arrays.toString(firingsList.toArray()));

        for (int i = 0; i < timeList.size(); i++) {
            datasetInterventionsAndFirings.addValue(Integer.parseInt(interventionsList.get(i)), "Number of interventions", timeList.get(i));
            datasetInterventionsAndFirings.addValue(Integer.parseInt(firingsList.get(i)), "Number of firings", timeList.get(i));
        }

        SwingUtilities.invokeLater(() -> {
            new LineChart("Number of interventions and firings", "Minutes of simulation", "Incidents", datasetInterventionsAndFirings, frameCharts);
        });

        reader.close();
    }

    private void showSummary() throws IOException {
        showSimulationCharts();

        var simulationSummaryMessage = new StringBuilder();

        simulationSummaryMessage.append("Simulation has finished.\n\n");

        simulationSummaryMessage.append("Simulated Patrols: ").append(StatisticsCounter.getInstance().getNumberOfPatrols()).append("\n");
        simulationSummaryMessage.append("Simulated Interventions: ").append(StatisticsCounter.getInstance().getNumberOfInterventions()).append("\n");
        simulationSummaryMessage.append("Simulated Firings: ").append(StatisticsCounter.getInstance().getNumberOfFirings()).append("\n");
        simulationSummaryMessage.append("Neutralized Patrols: ").append(StatisticsCounter.getInstance().getNumberOfNeutralizedPatrols()).append("\n");
        simulationSummaryMessage.append("Solved Interventions: ").append(StatisticsCounter.getInstance().getNumberOfSolvedInterventions()).append("\n");
        simulationSummaryMessage.append("Solved Firings: ").append(StatisticsCounter.getInstance().getNumberOfSolvedFirings()).append("\n");

        JOptionPane.showMessageDialog(panel, simulationSummaryMessage.toString());
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

            var topLeftCornerX = 800;
            var topLeftCornerY = 750;
            final var size = 10;
            final String newFont = "TimesRoman";

            // Draw background
            var oldColor = g.getColor();
            g.setColor(new Color(244, 226, 198, 225));
            g.fillRect(topLeftCornerX, topLeftCornerY, 1000 - topLeftCornerX, 1000 - topLeftCornerY);
            g.setColor(oldColor);

            var patrolStates = new HashMap<String, Color>();
            patrolStates.put("PATROLLING", new Color(0, 153, 0));
            patrolStates.put("RETURNING_TO_HQ", new Color(0, 100, 0));
            patrolStates.put("TXFR_TO_INTERVENTION", new Color(255, 87, 36));
            patrolStates.put("TRANSFER_TO_FIRING", new Color(255, 131, 54));
            patrolStates.put("INTERVENTION", new Color(0, 92, 230));
            patrolStates.put("FIRING", new Color(153, 0, 204));
            patrolStates.put("NEUTRALIZED", new Color(255, 255, 255));
            patrolStates.put("CALCULATING_PATH", new Color(255, 123, 255));

            int i = 0;
            for (Map.Entry<String, Color> entry : patrolStates.entrySet()) {
                g.setColor(entry.getValue());
                var mark = new Ellipse2D.Double((int) (topLeftCornerX + 10 - size / 2.0), 1000 - 60 - i * 15.0, size, size);
                g.fill(mark);

                g.setColor(oldColor);
                g.drawString(entry.getKey(), topLeftCornerX + 25, 1000 - 50 - i * 15);
                i++;
            }
            var oldFont = g.getFont();
            g.setFont(new Font(newFont, Font.BOLD, 13));
            g.drawString("Patrol's states:", topLeftCornerX + 5, 1000 - 50 - i * 15);
            g.setFont(oldFont);
            i++;
            var places = new HashMap<String, Color>();
            places.put("HQ", Color.BLUE);
            places.put("INTERVENTION", Color.RED);
            places.put("FIRING", Color.BLACK);

            for (Map.Entry<String, Color> entry : places.entrySet()) {
                g.setColor(entry.getValue());
                var mark = new Ellipse2D.Double((int) (topLeftCornerX + 10 - size / 2.0), 1000 - 60 - i * 15.0, size, size);
                g.fill(mark);

                g.setColor(oldColor);
                g.drawString(entry.getKey(), topLeftCornerX + 25, 1000 - 50 - i * 15);
                i++;
            }
            oldFont = g.getFont();
            g.setFont(new Font(newFont, Font.BOLD, 13));
            g.drawString("Places:", topLeftCornerX + 5, 1000 - 50 - i * 15);
            g.setFont(oldFont);
        }
    }

}
