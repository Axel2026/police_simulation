package Visualisation;

import Simulation.entities.Incident;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PatrolPanel {

    private final JPanel frame;
    private JPanel panel = new JPanel();
    private JScrollPane scrollPane = new JScrollPane(panel);
    private static List<PatrolPanelUnitComponent> patrolUnitsList = new ArrayList<>();

    public PatrolPanel(JPanel panel) {
        this.frame = panel;
    }

    public void createWindow() {
        frame.add(scrollPane);
        BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(layout);
        frame.setVisible(true);
    }

    public void addNewPatrolUnit(Patrol patrol) {
        System.out.println("Test2 " + patrol.getId());
        System.out.println("Test2 " + patrolUnitsList.size());
        var patrolUnitComponent = new PatrolPanelUnitComponent(patrol);
        patrolUnitsList.add(patrolUnitComponent);
        panel.add(patrolUnitComponent);
        var scrollBar = scrollPane.getVerticalScrollBar();
        scrollBar.setValue(scrollBar.getMaximum());
    }

    public void updatePatrolUnit(Patrol patrol) {
        int id = patrol.getId();
        if(id < patrolUnitsList.size()) {
            System.out.println("Test8 " + id);
            System.out.println("Test8 " + patrolUnitsList.size());
            var patrolPanelUnitComponentToUpdate = patrolUnitsList.stream().filter(it -> it.getId() == id).findFirst().orElse(null);
            patrolPanelUnitComponentToUpdate.updateSelf();
            panel.revalidate();
            panel.repaint();
            var scrollBar = scrollPane.getVerticalScrollBar();
            scrollBar.setValue(scrollBar.getMaximum());
        }
    }
}
