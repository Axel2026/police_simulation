package entities;

import world.World;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public abstract class Incident extends Entity implements IEvent, IDrawable {

    protected double timeOfLastUpdate;
    private long startTime;
    private boolean isActive = true;

    protected Incident() {
        startTime = World.getInstance().getSimulationTimeLong();
        timeOfLastUpdate = startTime;
    }

    protected Incident(double latitude, double longitude) {
        super(latitude, longitude);
        startTime = World.getInstance().getSimulationTimeLong();
        timeOfLastUpdate = startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    protected void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public void drawSelf(Graphics2D g, JXMapViewer mapViewer) {
        if (isActive()) {
            var oldColor = g.getColor();

            g.setColor(Color.RED);

            final var size = 10;
            var point = mapViewer.convertGeoPositionToPoint(new GeoPosition(getLatitude(), getLongitude()));

            var mark = new Ellipse2D.Double((int) (point.getX() - size / 2.0), (int) (point.getY() - size / 2.0), size, size);

            g.fill(mark);

            g.setColor(oldColor);
        }
    }

    @Override
    public void updateState() {
        timeOfLastUpdate = World.getInstance().getSimulationTime();
    }

    void drawString(Graphics2D g, int x, int y, String str1){

        var oldColor = g.getColor();

        Color bgColor = new Color(1,1,1,0.8f );
        FontMetrics fm = g.getFontMetrics();
        Rectangle2D rect = fm.getStringBounds(str1, g);
        g.setColor(bgColor);
        g.fillRect(x,
                y - fm.getAscent() + 2,
                (int) rect.getWidth(),
                (int) rect.getHeight());
        g.setColor(oldColor);
        g.drawString(str1, x, y);
    }
}
