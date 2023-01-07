package main;

import Configuration.ConfigurationPanel;
import Visualisation.LoggerPanel;
import utils.Logger;

import javax.swing.*;
import java.awt.*;

public class Main {

    /**
     * Entry point of the application.
     *
     * @param args params passed to the application.
     */

    public static void main(String[] args) {
        JFrame frame = new JFrame("City Police Simulation");
        frame.setResizable(false);
        frame.setSize(1680, 950);
        frame.setVisible(true);
        frame.setLayout(new GridLayout(2, 1));

        JPanel mapPanel = new JPanel();
        mapPanel.setLayout(new GridLayout(1, 1));

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

        JPanel configurationPanelTab = new JPanel();
        configurationPanelTab.setLayout(new FlowLayout());
        configurationPanelTab.setPreferredSize(new Dimension( 1620,900));
        JScrollPane scrollFrame = new JScrollPane(configurationPanelTab);
        configurationPanelTab.setAutoscrolls(true);
        scrollFrame.setPreferredSize(new Dimension( 800,300));

        JPanel LoggerPanelTab = new JPanel();
        LoggerPanelTab.setLayout(new GridLayout(1, 1));

        tabbedPane.addTab("Configuration", scrollFrame);
        tabbedPane.addTab("Logger", LoggerPanelTab);

        frame.getContentPane().add(mapPanel);
        frame.getContentPane().add(tabbedPane);

        var panel = new ConfigurationPanel(configurationPanelTab, mapPanel);
        panel.createWindow();

        var logPan = new LoggerPanel(LoggerPanelTab);
        logPan.createWindow();
        Logger.getInstance().addLoggingPanel(logPan);

        tabbedPane.setSelectedIndex(1);
        tabbedPane.setSelectedIndex(0);
    }
}
