package Simulation.entities;

import Simulation.World;
import Simulation.exported_data.*;
import Visualisation.Ambulance;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import Visualisation.IDrawable;

public class Hospital extends Entity implements IDrawable {
    private final World world = World.getInstance();
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

        var mark = new Rectangle2D.Double((int) (point.getX() - size / 2.0), (int) (point.getY() - size / 2.0), size, size);
        g.fill(mark);

        g.setColor(oldColor);
    }

    public void assignTasksHospital() {
        updateListOfIncidents();
        var allFirings = incidents.stream().filter(Firing.class::isInstance).sorted(Comparator.comparingLong(Incident::getStartTime)).collect(Collectors.toList());
        checkAllFirings(allFirings);
    }

    private void updateListOfIncidents() {
        var allEntities = World.getInstance().getAllEntities();
        incidents = allEntities.stream().filter(Incident.class::isInstance).map(Incident.class::cast).collect(Collectors.toList());
    }

    private void checkAllFirings(List<Incident> allFirings) {
        for (var firing : allFirings) {
            if(((Firing) firing).getAmbulancesSolving().size() == 1 && ((Firing) firing).getAmbulancesReaching().size() < 1) {
                summonSupportForFiring((Firing) firing);
            }
        }
    }

    public void summonSupportForFiring(Firing firing) {
            var availableAmbulance = World.getInstance().getAllEntities()
                    .stream()
                    .filter(x -> x instanceof Ambulance && ((Ambulance) x).getStateAmbulance() == Ambulance.State.AVAILABLE).findFirst()
                    .map(Ambulance.class::cast).orElse(null);
            if (availableAmbulance != null) {
                giveOrdersToFoundAmbulance(firing, availableAmbulance);
            }
    }

    private void giveOrdersToFoundAmbulance(Incident firing, Ambulance foundAmbulance) {
        ExportAmbulanceDistanceAndTimeToReachFiring.getInstance().writeToCsvFile((Firing) firing, foundAmbulance);
        foundAmbulance.setState(Ambulance.State.TRANSFER_TO_ACCIDENT);
        foundAmbulance.takeOrderAmbulance((
                foundAmbulance.new Transfer(World.getInstance().getSimulationTimeLong(),
                        firing, Ambulance.State.TRANSFER_TO_ACCIDENT)));
        ((Firing) firing).addReachingAmbulance(foundAmbulance);
    }
}
