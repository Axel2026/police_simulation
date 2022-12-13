package entities;

import entities.factories.IncidentFactory;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import world.World;

import java.awt.*;

public class Intervention extends Incident implements IDrawable {

    private final long duration;
    private final boolean willChangeIntoFiring;
    private final long timeToChange;
    private District district;

    private Patrol patrolSolving;

    public Intervention(double latitude, double longitude) {
        super(latitude, longitude);

        // the default intervention duration is the middle of the values entered by the user in the configuration
        var config = World.getInstance().getConfig();
        this.duration = config.getMinimumInterventionDuration() + (config.getMaximumInterventionDuration() - config.getMinimumInterventionDuration()) / 2L;
        this.willChangeIntoFiring = false;
        this.timeToChange = -1;
    }

    public Intervention(double latitude, double longitude, long duration, District district) {
        super(latitude, longitude);
        this.duration = duration;
        this.district = district;

        this.willChangeIntoFiring = false;
        this.timeToChange = -1;
    }

    public Intervention(double latitude, double longitude, long duration, boolean willChangeIntoFiring, long timeToChange, District district) {
        super(latitude, longitude);
        this.duration = duration;
        this.willChangeIntoFiring = willChangeIntoFiring;
        this.district = district;
        if (timeToChange < 0) {
            throw new IllegalArgumentException("timeToChange must be greater than or equal to zero");
        }
        this.timeToChange = timeToChange;
    }

    @Override
    public void updateState() {
        super.updateState();
        if (this.patrolSolving != null) {
            if (willChangeIntoFiring && patrolSolving.getAction() instanceof Patrol.IncidentParticipation && patrolSolving.getAction().getStartTime() + this.timeToChange < World.getInstance().getSimulationTime()) {
                var firing = IncidentFactory.createRandomFiringFromIntervention(this);
                this.patrolSolving.getAction().setTarget(firing);
                firing.addSolvingPatrol(this.patrolSolving);
                World.getInstance().removeEntity(this);
                World.getInstance().addEntity(firing);
            } else if (patrolSolving.getAction() instanceof Patrol.IncidentParticipation && patrolSolving.getAction().getStartTime() + this.getDuration() < World.getInstance().getSimulationTime()) {
                setActive(false);
                World.getInstance().removeEntity(this);
            }
        }
    }

    @Override
    public void drawSelf(Graphics2D g, JXMapViewer mapViewer) {
        super.drawSelf(g, mapViewer);
        var point = mapViewer.convertGeoPositionToPoint(new GeoPosition(getLatitude(), getLongitude()));
        if (World.getInstance().isSimulationPaused() && World.getInstance().getConfig().isDrawInterventionDetails()) {
            drawString(g, (int) point.getX() + 5, (int) point.getY(), "Duration:" + duration / 60 + " [minutes]");
            drawString(g, (int) point.getX() + 5, (int) point.getY() - 15, "Will change into firing: " + willChangeIntoFiring);
            if (this.patrolSolving != null) {
                if (patrolSolving.getAction() instanceof Patrol.IncidentParticipation) {
                    var timeLeft = duration - (World.getInstance().getSimulationTime() - patrolSolving.getAction().getStartTime());

                    drawString(g, (int) point.getX() + 5, (int) point.getY() - 30, String.format("Time left: %.2f [minutes]", timeLeft / 60));
                } else {
                    drawString(g, (int) point.getX() + 5, (int) point.getY() - 30, "Patrol is on its way to the intervention.");
                }
            }
        }
    }

    public Patrol getPatrolSolving() {
        return patrolSolving;
    }

    public void setPatrolSolving(Patrol patrolSolving) {
        this.patrolSolving = patrolSolving;
    }

    public long getDuration() {
        return duration;
    }

    public District getDistrict() {
        return district;
    }
}
