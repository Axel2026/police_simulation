package Simulation.entities;

import Simulation.World;
import Simulation.exported_data.ExportFiringDetails;
import Simulation.exported_data.ExportSupportSummonDetails;
import Visualisation.*;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import Visualisation.IDrawable;
import utils.Logger;

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
            System.out.println("im in ");
            var ambulancesSolving = ((Firing) firing).getAmbulancesSolving();
            var ambulancesReaching = ((Firing) firing).getAmbulancesReaching();
//            revokeRedundantAmbulances((Firing) firing, ambulancesSolving, ambulancesReaching);
            summonSupportForFiring((Firing) firing);
        }
    }

    private void summonSupportForFiring(Firing firing) {
        System.out.println("summoned");
        var availableAmbulance = World.getInstance().getAllEntities()
                .stream()
                .filter(x -> x instanceof Ambulance && ((Ambulance) x).getStateAmbulance() == Ambulance.State.AVAILABLE)
                .map(Ambulance.class::cast)
                .collect(Collectors.toList());
        System.out.println("availableAmbulancexdd  " + availableAmbulance);
        giveOrdersToFoundAmbulance(firing, availableAmbulance);
    }

    private void revokeRedundantAmbulances(Firing firing, List<Ambulance> patrolsSolving, List<Ambulance> ambulancesReaching) {
        System.out.println("ambulancesReaching " + ambulancesReaching);
        System.out.println("solv pat " + patrolsSolving);
        if (patrolsSolving.size() >= 1) {
            for (int i = ambulancesReaching.size() - 1; i >= 0; i--) {
                ambulancesReaching.get(i).setState(Ambulance.State.RETURNING_TO_HOSPITAL);
                firing.removeReachingAmbulance(ambulancesReaching.get(i));
            }
        }
    }

    private void giveOrdersToFoundAmbulance(Incident firing, List<Ambulance> foundAmbulance) {
        System.out.println("ambulance jest");
        for (var ambulance : foundAmbulance) {
            System.out.println("amb " + ambulance);
            ambulance.setState(Ambulance.State.TRANSFER_TO_ACCIDENT);
            ambulance.takeOrderAmbulance((
                    ambulance.new Transfer(World.getInstance().getSimulationTimeLong(),
                            firing, Ambulance.State.TRANSFER_TO_ACCIDENT)));
            ((Firing) firing).addReachingAmbulance(ambulance);
            System.out.println("firing " + firing);
        }
        if (!foundAmbulance.isEmpty()) {
            System.out.println("lista pusta ");
        }
    }
}
