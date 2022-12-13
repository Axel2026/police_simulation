package entities;

import world.World;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

public class Firing extends Incident implements IDrawable {

    private final int requiredPatrols;
    private double strength;
    private List<Patrol> patrolsSolving = new ArrayList<>();
    private List<Patrol> patrolsReaching = new ArrayList<>();
    private District district;

    public Firing(double latitude, double longitude) {
        super(latitude, longitude);

        this.requiredPatrols = 3;
        this.strength = requiredPatrols * 15 * 60.0;
    }

    public Firing(double latitude, double longitude, int requiredPatrols, double initialStrength, District district) {
        super(latitude, longitude);
        this.requiredPatrols = requiredPatrols;
        this.strength = initialStrength;
        this.district = district;
    }

    public int getRequiredPatrols() {
        return requiredPatrols;
    }

    public List<Patrol> getPatrolsSolving() {
        return patrolsSolving;
    }

    public List<Patrol> getPatrolsReaching() {
        return patrolsReaching;
    }

    public void addReachingPatrol(Patrol patrol) {
        patrolsReaching.add(patrol);
    }

    public void removeReachingPatrol(Patrol patrol) {
        patrolsReaching.remove(patrol);
    }

    public void addSolvingPatrol(Patrol patrol) {
        patrolsSolving.add(patrol);
    }

    public void removeSolvingPatrol(Patrol patrol) {
        patrolsSolving.remove(patrol);
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

        var mark = new Ellipse2D.Double((int) (point.getX() - size / 2.0), (int) (point.getY() - size / 2.0), size, size);
        g.fill(mark);

        if (World.getInstance().getConfig().isDrawFiringDetails()) {
            drawString(g,(int) point.getX() + 5, (int) point.getY(), String.format("Time left: %.2f [minutes]", strength / 60 / patrolsSolving.size()));
            drawString(g, (int) point.getX() + 5, (int) point.getY() - 15, String.format("Patrols Required: %d", requiredPatrols));
            drawString(g, (int) point.getX() + 5, (int) point.getY() - 30, String.format("Patrols Reaching: %d", patrolsReaching.size()));
            drawString(g, (int) point.getX() + 5, (int) point.getY() - 45, String.format("Patrols Solving :%d", patrolsSolving.size()));
        }

        g.setColor(oldColor);
    }

    @Override
    public void updateState() {
        this.strength -= patrolsSolving.size() * (World.getInstance().getSimulationTime() - timeOfLastUpdate);
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
