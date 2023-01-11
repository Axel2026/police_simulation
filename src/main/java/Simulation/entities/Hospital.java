package Simulation.entities;

import Simulation.World;
import Visualisation.*;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import Visualisation.IDrawable;

public class Hospital extends Entity implements IDrawable {
    World world = World.getInstance();
    private List<Incident> incidents = new ArrayList<>();

    public Hospital(double latitude, double longitude) {
        super(latitude, longitude);
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

    public void sendAmbulances(Firing target) {
        var hospital = world.getAllEntities().stream().filter(Hospital.class::isInstance).findFirst().orElse(null);
        assert hospital != null;
        Ambulance ambulance = new Ambulance(hospital.getLatitude(), hospital.getLongitude());
        ambulance.setState(Ambulance.State.TRANSFER_TO_ACCIDENT);
        World.getInstance().addEntity(ambulance);
        ambulance.takeOrderAmbulance((
                ambulance.new Transfer(World.getInstance().getSimulationTimeLong(),
                        target, Ambulance.State.TRANSFER_TO_ACCIDENT)));
        System.out.println("-----ambulance sent------");
    }
}
