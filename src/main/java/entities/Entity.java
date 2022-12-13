package entities;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;

import java.util.UUID;

public abstract class Entity {

    private final UUID uniqueID = UUID.randomUUID();
    private double latitude = 0;
    private double longitude = 0;

    protected Entity() {
    }

    protected Entity(double latitude, double longitude) {
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }

    protected Entity(LatLon position) {
        this(position.getLatitude(), position.getLongitude());
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LatLon getPosition() {
        return new OsmLatLon(latitude, longitude);
    }

    public void setPosition(LatLon position) {
        this.latitude = position.getLatitude();
        this.longitude = position.getLongitude();
    }

    public UUID getUniqueID() {
        return uniqueID;
    }

    @Override
    public String toString() {
        return getClass().toString() + ": " + getUniqueID();
    }
}
