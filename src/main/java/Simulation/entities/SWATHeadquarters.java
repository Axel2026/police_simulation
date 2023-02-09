package Simulation.entities;

import Simulation.StatisticsCounter;
import Simulation.World;
import Simulation.exported_data.*;
import Visualisation.District;
import Visualisation.IDrawable;
import Visualisation.Patrol;
import Visualisation.SWAT;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import utils.EntityTypes;
import utils.Logger;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SWATHeadquarters extends Entity implements IDrawable {

    private final double searchRange;
    private final double durationOfTheShift;
    private List<Incident> incidents = new ArrayList<>();
    private double endOfCurrentShift;
    private ArrayList<SWAT> availableSWATSquads = new ArrayList<>();
    private ArrayList<SWAT> interveningSWATSquads = new ArrayList<>();
    private District district;

    public SWATHeadquarters(double latitude, double longitude, District district) {
        super(latitude, longitude);
        this.setType(EntityTypes.SWAT_HEADQUARTERS);
        this.durationOfTheShift = World.getInstance().getDurationOfTheShift();
        this.endOfCurrentShift = World.getInstance().getSimulationTime() + durationOfTheShift;
        this.searchRange = World.getInstance().getConfig().getBasicSearchDistance();
        this.district = district;

        for (int i = 0; i < World.getInstance().getConfig().getSWATSquadsPerDistrict(); i++) {
            SWAT newSWAT = new SWAT(this.getLatitude(), this.getLongitude(), this.district);
            addAvailableSWATSquad(newSWAT);
            World.getInstance().addEntity(newSWAT);
        }
    }

    public void addAvailableSWATSquad() {
        availableSWATSquads.add(interveningSWATSquads.get(0));
        System.out.println("Added available SWAT squad. Current number: " + availableSWATSquads.size());
    }

    public void addAvailableSWATSquad(SWAT swatSquad) {
        availableSWATSquads.add(swatSquad);
        System.out.println("Added available SWAT squad. Current number: " + availableSWATSquads.size());
    }

    public SWAT removeAvailableSWATSquad() {
        if (availableSWATSquads.size() > 0) {
            interveningSWATSquads.add(availableSWATSquads.remove(availableSWATSquads.size() - 1));
            System.out.println("Removed available SWAT squad in district " + this.district.getName() + ". Current number: " + availableSWATSquads.size());
            return interveningSWATSquads.get(interveningSWATSquads.size() - 1);
        }
        System.out.println("Not enough SWAT squads for this district");
        return null;
    }

    @Override
    public void drawSelf(Graphics2D g, JXMapViewer mapViewer) {
        var oldColor = g.getColor();
        g.setColor(Color.PINK);

        final var size = 10;
        var point = mapViewer.convertGeoPositionToPoint(new GeoPosition(getLatitude(), getLongitude()));

        var mark = new Rectangle2D.Double((int) (point.getX() - size / 2.0), (int) (point.getY() - size / 2.0), size, size);
        g.fill(mark);

        g.setColor(oldColor);
    }

    private void revokeRedundantPatrols(Firing firing, List<Patrol> patrolsSolving, List<Patrol> patrolsReaching, int requiredPatrols) {
        if (requiredPatrols <= patrolsSolving.size()) {
            for (int i = patrolsReaching.size() - 1; i >= 0; i--) {
                patrolsReaching.get(i).setState(Patrol.State.PATROLLING);
                firing.removeReachingPatrol(patrolsReaching.get(i));
            }
        }
    }

    public SWAT summonSWATSquad(Firing firing) {
        System.out.println("availableSWATSquads " + availableSWATSquads);
        SWAT summonedSWATSquad = removeAvailableSWATSquad();
        StatisticsCounter.getInstance().increaseUsedSWAT();
        if (summonedSWATSquad != null) {
            giveOrdersToFoundSQUAD(firing, summonedSWATSquad);
        }

        return summonedSWATSquad;
    }

    private void giveOrdersToFoundSQUAD(Incident firing, SWAT swatSquad) {
        Logger.getInstance().logNewOtherMessage(swatSquad + " took order from HQ.");
        Logger.getInstance().logNewMessageChangingState(swatSquad, swatSquad.getState().toString(), "TRANSFER_TO_FIRING");
        swatSquad.takeOrder(swatSquad.new Transfer(World.getInstance().getSimulationTimeLong(), firing, SWAT.State.TRANSFER_TO_FIRING));
        ((Firing) firing).addReachingSWATSquad(swatSquad);
    }

    public District getDistrict() {
        return district;
    }
}
