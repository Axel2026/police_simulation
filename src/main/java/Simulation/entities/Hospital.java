package Simulation.entities;

import Simulation.World;
import Simulation.exported_data.ExportFiringDetails;
import Simulation.exported_data.ExportRevokingPatrolsDetails;
import Simulation.exported_data.ExportSupportSummonDetails;
import Visualisation.IDrawable;
import Visualisation.Patrol;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import utils.Logger;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import Simulation.exported_data.ExportFiringDetails;
import Simulation.exported_data.ExportRevokingPatrolsDetails;
import Simulation.exported_data.ExportSupportSummonDetails;
import Visualisation.IDrawable;
import Visualisation.Patrol;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import utils.Logger;
import Simulation.World;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Hospital extends Entity implements IDrawable {

    private final double durationOfTheShift;
    private List<Incident> incidents = new ArrayList<>();
    private double endOfCurrentShift;

    public Hospital(double latitude, double longitude) {
        super(latitude, longitude);
        this.durationOfTheShift = World.getInstance().getDurationOfTheShift();
        this.endOfCurrentShift = World.getInstance().getSimulationTime() + durationOfTheShift;
    }

    @Override
    public void drawSelf(Graphics2D g, JXMapViewer mapViewer) {
        var oldColor = g.getColor();
        g.setColor(Color.CYAN);

        final var size = 10;
        var point = mapViewer.convertGeoPositionToPoint(new GeoPosition(getLatitude(), getLongitude()));

        var mark = new Ellipse2D.Double((int) (point.getX() - size / 2.0), (int) (point.getY() - size / 2.0), size, size);
        g.fill(mark);

        g.setColor(oldColor);
    }

    public void assignTasks() {
        var allInterventions = incidents.stream().filter(Intervention.class::isInstance).sorted(Comparator.comparingLong(Incident::getStartTime)).collect(Collectors.toList());
        var allFirings = incidents.stream().filter(Firing.class::isInstance).sorted(Comparator.comparingLong(Incident::getStartTime)).collect(Collectors.toList());

        checkAllFirings(allFirings);
    }

    private void checkAllFirings(List<Incident> allFirings) {
        for (var firing : allFirings) {
            var requiredPatrols = ((Firing) firing).getRequiredPatrols();
            var patrolsSolving = ((Firing) firing).getPatrolsSolving();
            var patrolsReaching = ((Firing) firing).getPatrolsReaching();
        }
    }


}
