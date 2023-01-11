package Visualisation;

import Simulation.World;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.stream.Collectors;

public class PatrolPanelUnitComponent extends JPanel {

    private JLabel idLabel;
    private JLabel stateLabel;
    private JLabel speedLabel;
    private Patrol patrol;

    public PatrolPanelUnitComponent(Patrol patrol) {
        setPreferredSize(new Dimension(800, 50));
        setMaximumSize(new Dimension(2000, 50));
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        idLabel = new JLabel(String.valueOf(patrol.getId()));
        stateLabel = new JLabel(String.valueOf(patrol.getState()));
        speedLabel = new JLabel(String.valueOf((int)patrol.getSpeed()));
        this.patrol = patrol;

        add(idLabel);
        add(stateLabel);
        add(speedLabel);
        revalidate();
        repaint();
        setVisible(true);
    }

    public void updateSelf() {
        var allPatrols = World.getInstance().getAllEntities().stream().filter(Patrol.class::isInstance).map(Patrol.class::cast).collect(Collectors.toList());
        System.out.println("Test5 " + World.getInstance().getConfig().getNumberOfPolicePatrols());
        System.out.println("Test6 " + allPatrols.size());
        if(patrol.getId() < World.getInstance().getConfig().getNumberOfPolicePatrols()) {
            System.out.println("Test4 " + patrol.getId());
            this.patrol = allPatrols.get(patrol.getId());
            idLabel = new JLabel(String.valueOf(this.patrol.getId()));
            stateLabel = new JLabel(String.valueOf(this.patrol.getState()));
            speedLabel = new JLabel(String.valueOf((int)this.patrol.getSpeed()));
            removeAll();
            add(idLabel);
            add(stateLabel);
            add(speedLabel);
            revalidate();
            repaint();
            setVisible(true);
        }
    }

    public int getId() {
        return patrol.getId();
    }
}
