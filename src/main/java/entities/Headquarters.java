package entities;

import csv_export.ExportFiringDetails;
import csv_export.ExportRevokingPatrolsDetails;
import csv_export.ExportSupportSummonDetails;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import utils.Logger;
import world.World;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Headquarters extends Entity implements IDrawable {

    private final double searchRange;
    private final double durationOfTheShift;
    private List<Incident> incidents = new ArrayList<>();
    private double endOfCurrentShift;

    public Headquarters(double latitude, double longitude) {
        super(latitude, longitude);
        this.durationOfTheShift = World.getInstance().getDurationOfTheShift();
        this.endOfCurrentShift = World.getInstance().getSimulationTime() + durationOfTheShift;
        this.searchRange = World.getInstance().getConfig().getBasicSearchDistance();
    }

    @Override
    public void drawSelf(Graphics2D g, JXMapViewer mapViewer) {
        var oldColor = g.getColor();
        g.setColor(Color.BLUE);

        final var size = 10;
        var point = mapViewer.convertGeoPositionToPoint(new GeoPosition(getLatitude(), getLongitude()));

        var mark = new Ellipse2D.Double((int) (point.getX() - size / 2.0), (int) (point.getY() - size / 2.0), size, size);
        g.fill(mark);

        g.setColor(oldColor);
    }

    public void assignTasks() {
        checkIfTheShiftIsOver();

        updateListOfIncidents();
        var allInterventions = incidents.stream().filter(Intervention.class::isInstance).sorted(Comparator.comparingLong(Incident::getStartTime)).collect(Collectors.toList());
        var allFirings = incidents.stream().filter(Firing.class::isInstance).sorted(Comparator.comparingLong(Incident::getStartTime)).collect(Collectors.toList());

        checkAllFirings(allFirings);
        checkAllInterventions(allInterventions);
    }

    private void checkAllFirings(List<Incident> allFirings) {
        for (var firing : allFirings) {
            var requiredPatrols = ((Firing) firing).getRequiredPatrols();
            var patrolsSolving = ((Firing) firing).getPatrolsSolving();
            var patrolsReaching = ((Firing) firing).getPatrolsReaching();
            revokeRedundantPatrols((Firing) firing, patrolsSolving, patrolsReaching, requiredPatrols);
            summonSupportForFiring((Firing) firing, patrolsSolving, patrolsReaching, requiredPatrols);
        }
    }

    private void revokeRedundantPatrols(Firing firing, List<Patrol> patrolsSolving, List<Patrol> patrolsReaching, int requiredPatrols) {
        if (requiredPatrols <= patrolsSolving.size()) {
            if (!patrolsReaching.isEmpty()) {
                ExportRevokingPatrolsDetails.getInstance().writeToCsvFileRevokedPatrols(firing, patrolsReaching.size());
            }
            for (int i = patrolsReaching.size() - 1; i >= 0; i--) {
                patrolsReaching.get(i).setState(Patrol.State.PATROLLING);
                firing.removeReachingPatrol(patrolsReaching.get(i));
            }
        }
    }

    private void summonSupportForFiring(Firing firing, List<Patrol> patrolsSolving, List<Patrol> patrolsReaching, int requiredPatrols) {
        if (patrolsSolving.size() + patrolsReaching.size() < requiredPatrols) {
            for (int i = 1; i < 4; i++) {
                var foundPatrols = World.getInstance().getEntitiesNear(firing, searchRange * i)
                        .stream()
                        .filter(x -> x instanceof Patrol && ((Patrol) x).getState() == Patrol.State.PATROLLING)
                        .map(Patrol.class::cast)
                        .collect(Collectors.toList());
                giveOrdersToFoundPatrols(firing, foundPatrols, i);
                if (foundPatrols.size() + patrolsSolving.size() + patrolsReaching.size() >= requiredPatrols) {
                    break;
                }
            }
            if (patrolsSolving.size() + patrolsReaching.size() < requiredPatrols) {
                var foundTransferringToInterventionPatrols = World.getInstance().getEntitiesNear(firing, searchRange * 3)
                        .stream()
                        .filter(x -> x instanceof Patrol && ((Patrol) x).getState() == Patrol.State.TRANSFER_TO_INTERVENTION)
                        .map(Patrol.class::cast)
                        .collect(Collectors.toList());
                giveOrdersToFoundPatrols(firing, foundTransferringToInterventionPatrols, 4);
            }
        }
    }

    private void checkAllInterventions(List<Incident> allInterventions) {
        for (var intervention : allInterventions) {
            if (((Intervention) intervention).getPatrolSolving() == null) {
                Patrol availablePatrol;
                int i = 0;
                while (true) {
                    availablePatrol = World.getInstance().getEntitiesNear(intervention, searchRange * i)
                            .stream()
                            .filter(x -> x instanceof Patrol && ((Patrol) x).getState() == Patrol.State.PATROLLING)
                            .map(Patrol.class::cast).findFirst()
                            .orElse(null);
                    if (availablePatrol != null || i > 10) {
                        break;
                    }
                    i++;
                }
                if (availablePatrol != null) {
                    Logger.getInstance().logNewOtherMessage(availablePatrol + " took order from HQ.");
                    Logger.getInstance().logNewMessageChangingState(availablePatrol, availablePatrol.getState().toString(), "TRANSFER_TO_INTERVENTION");
                    availablePatrol.takeOrder(
                            availablePatrol.new Transfer(World.getInstance().getSimulationTimeLong(),
                                    intervention, Patrol.State.TRANSFER_TO_INTERVENTION));
                    ((Intervention) intervention).setPatrolSolving(availablePatrol);
                }
            }
        }
    }

    private void giveOrdersToFoundPatrols(Incident firing, List<Patrol> foundPatrols, int numberOfIteration) {
        for (var p : foundPatrols) {
            Logger.getInstance().logNewOtherMessage(p + " took order from HQ.");
            Logger.getInstance().logNewMessageChangingState(p, p.getState().toString(), "TRANSFER_TO_FIRING");
            ExportSupportSummonDetails.getInstance().writeToCsvFile((Firing) firing, p, p.getState().name(), numberOfIteration);
            p.takeOrder(p.new Transfer(World.getInstance().getSimulationTimeLong(), firing, Patrol.State.TRANSFER_TO_FIRING));
            ((Firing) firing).addReachingPatrol(p);
        }
        if (!foundPatrols.isEmpty()) {
            ExportFiringDetails.getInstance().writeToCsvFileCalledPatrols((Firing) firing, foundPatrols);
        }
    }

    private void updateListOfIncidents() {
        var allEntities = World.getInstance().getAllEntities();
        incidents = allEntities.stream().filter(Incident.class::isInstance).map(Incident.class::cast).collect(Collectors.toList());
    }

    private void checkIfTheShiftIsOver() {
        var world = World.getInstance();
        if (world.getSimulationTime() > endOfCurrentShift) {
            for (int i = 0; i < world.getConfig().getNumberOfPolicePatrols(); i++) {
                var newPatrol = new Patrol(this.getPosition());
                newPatrol.setState(Patrol.State.PATROLLING);
                world.addEntity(newPatrol);
            }
            endOfCurrentShift += durationOfTheShift;
            Logger.getInstance().logNewOtherMessage("New shift has started");
        }
    }
}
