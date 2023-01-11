package Visualisation;

import Simulation.World;
import Simulation.entities.Entity;
import de.westnordost.osmapi.map.data.LatLon;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.concurrent.ThreadLocalRandom;

public class Ambulance extends Entity implements IDrawable{
    private double ambulanceSpeed = World.getInstance().getConfig().getBasePrivilegedSpeed();
    private Patrol.State state;
    public Ambulance() {
    }

    public Ambulance(double latitude, double longitude) {
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }

    public Ambulance(LatLon position) {
        this(position.getLatitude(), position.getLongitude());
    }

    public void setState(Patrol.State state) {
        this.state = state;
    }

    @Override
    public void drawSelf(Graphics2D g, JXMapViewer mapViewer) {
        var oldColor = g.getColor();

        switch (this.state) {
            case PATROLLING -> g.setColor(new Color(0, 153, 0)); // green
            case RETURNING_TO_HQ -> g.setColor(new Color(0, 100, 0)); // dark green
            case TRANSFER_TO_INTERVENTION -> g.setColor(new Color(255, 87, 36)); // yellowish
            case TRANSFER_TO_FIRING -> g.setColor(new Color(255, 131, 54)); // orangeish
            case INTERVENTION -> g.setColor(new Color(0, 92, 230)); // blue
            case FIRING -> g.setColor(new Color(153, 0, 204)); // purple
            case NEUTRALIZED -> g.setColor(new Color(255, 255, 255)); // white
            case CALCULATING_PATH -> g.setColor(new Color(255, 123, 255)); // pink
            default -> {
                g.setColor(Color.BLACK); // black
                throw new IllegalStateException("the patrol has no State");
            }
        }

        final var size = 10;
        var point = mapViewer.convertGeoPositionToPoint(new GeoPosition(getLatitude(), getLongitude()));

        var mark = new Ellipse2D.Double((int) (point.getX() - size / 2.0), (int) (point.getY() - size / 2.0), size, size);
        g.fill(mark);
        g.setColor(oldColor);
    }
}


