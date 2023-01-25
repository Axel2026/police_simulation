package Simulation.entities;

import Simulation.StatisticsCounter;
import Visualisation.Ambulance;
import Visualisation.District;
import Visualisation.IDrawable;
import Visualisation.Patrol;
import Simulation.World;
import Visualisation.SWAT;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

public class Firing extends Incident implements IDrawable {

    private final int requiredPatrols;
    private double strength;
    private int duration;
    private List<Patrol> patrolsSolving = new ArrayList<>();
    private List<Ambulance> ambulancesSolving = new ArrayList<>();
    private List<Patrol> patrolsReaching = new ArrayList<>();
    private List<SWAT> swatSolving = new ArrayList<>();
    private List<SWAT> swatReaching = new ArrayList<>();
    private List<Ambulance> ambulancesReaching = new ArrayList<>();
    private int neutralized = 0;
    private District district;
    private int durationCounter = 0;

    public Firing(double latitude, double longitude) {
        super(latitude, longitude);

        this.requiredPatrols = 3;
        this.strength = requiredPatrols * 15 * 60.0;
    }

    public Firing(double latitude, double longitude, int requiredPatrols, double initialStrength, int duration, District district, int neutralized) {
        super(latitude, longitude);
        this.requiredPatrols = requiredPatrols;
        this.duration = duration;
        this.strength = initialStrength;
        this.district = district;
        this.neutralized = neutralized;
    }

    public int getRequiredPatrols() {
        return requiredPatrols;
    }

    public List<Patrol> getPatrolsSolving() {
        return patrolsSolving;
    }

    public List<Ambulance> getAmbulancesSolving() {
        return ambulancesSolving;
    }

    public List<Patrol> getPatrolsReaching() {
        return patrolsReaching;
    }

    public List<Ambulance> getAmbulancesReaching() {
        return ambulancesReaching;
    }

    public boolean getIsSWATSolving() {
        return swatSolving.size() > 0;
    }

    public boolean getIsSWATReaching() {
        return swatReaching.size() > 0;
    }

    public void addReachingPatrol(Patrol patrol) {
        patrolsReaching.add(patrol);
    }

    public void addReachingAmbulance(Ambulance ambulance) {
        ambulancesReaching.add(ambulance);
    }

    public void removeReachingPatrol(Patrol patrol) {
        patrolsReaching.remove(patrol);
    }

    public void addSolvingAmbulance(Ambulance ambulance) {
        ambulancesSolving.add(ambulance);
    }

    public void removeSolvingAmbulance(Ambulance ambulance) {
        ambulancesSolving.remove(ambulance);
    }

    public void removeReachingAmbulance(Ambulance ambulance) {
        ambulancesReaching.remove(ambulance);
    }

    public void addSolvingPatrol(Patrol patrol) {
        patrolsSolving.add(patrol);
    }

    public void removeSolvingPatrol(Patrol patrol) {
        patrolsSolving.remove(patrol);
    }

    public void addReachingSWATSquad(SWAT swat) {
        swatReaching.add(swat);
    }

    public void removeReachingSWATSquad(SWAT swat) {
        swatReaching.remove(swat);
    }

    public void addSolvingSWATSquad(SWAT swat) {
        swatSolving.add(swat);
    }

    public void removeSolvingSWATSquad(SWAT swat) {
        swatSolving.remove(swat);
    }

    public void addNeutralizedPatrol() {
        neutralized++;
    }

    public int getNeutralizedPatrol() {
        return neutralized;
    }

    public void removeNeutralizedPatrol() {
        neutralized = 0;
    }

    public double getStrength() {
        return strength;
    }

    public District getDistrict() {
        return district;
    }

    @Override
    public void drawSelf(Graphics2D g, JXMapViewer mapViewer) {
        var oldColor = g.getColor();

        g.setColor(Color.BLACK);

        final var size = 10;
        var point = mapViewer.convertGeoPositionToPoint(new GeoPosition(getLatitude(), getLongitude()));
        if (durationCounter < 1) {
            StatisticsCounter.getInstance().increaseDurationOfFirings(duration / 60);
            durationCounter = 1;
        }
        var mark = new Ellipse2D.Double((int) (point.getX() - size / 2.0), (int) (point.getY() - size / 2.0), size, size);
        g.fill(mark);

        if (World.getInstance().getConfig().isDrawFiringDetails()) {
            drawString(g, (int) point.getX() + 5, (int) point.getY(), String.format("Time left: %.2f [minutes]", strength / 60 / patrolsSolving.size()));
            drawString(g, (int) point.getX() + 5, (int) point.getY() - 15, String.format("Patrols Required: %d", requiredPatrols));
            drawString(g, (int) point.getX() + 5, (int) point.getY() - 30, String.format("Patrols Reaching: %d", patrolsReaching.size()));
            drawString(g, (int) point.getX() + 5, (int) point.getY() - 45, String.format("Patrols Solving :%d", patrolsSolving.size()));
            drawString(g, (int) point.getX() + 5, (int) point.getY() - 60, "SWAT reaching: " + getIsSWATReaching());
            drawString(g, (int) point.getX() + 5, (int) point.getY() - 75, "SWAT solving: " + getIsSWATSolving());
        }

        g.setColor(oldColor);
    }

    @Override
    public void updateState() {
        this.strength -= swatSolving.size() * World.getInstance().getConfig().getSWATSoldiersPerSquad() + patrolsSolving.size() * (World.getInstance().getSimulationTime() - timeOfLastUpdate);
        timeOfLastUpdate = World.getInstance().getSimulationTime();
        if (this.strength <= 0) {
            setActive(false);
            World.getInstance().removeEntity(this);
            for (var p : patrolsSolving) {
                p.setState(Patrol.State.PATROLLING);
            }
        }
    }
}
