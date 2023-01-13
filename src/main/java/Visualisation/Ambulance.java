package Visualisation;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import Simulation.entities.*;
import Simulation.entities.Point;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import Simulation.PathCalculator;
import utils.Haversine;
import utils.Logger;
import Simulation.World;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Ambulance extends Entity implements IAgent, IDrawable {
    private final World world = World.getInstance();
    private final double basePatrollingSpeed;
    private double timeOfLastMove;
    private State state;
    private Action action;
    Entity hospital = world.getAllEntities().stream().filter(Hospital.class::isInstance).findFirst().orElse(null);

    public Ambulance() {
        this.basePatrollingSpeed = World.getInstance().getConfig().getBasePatrollingSpeed();
        this.timeOfLastMove = World.getInstance().getSimulationTime();
    }

    public Ambulance(double latitude, double longitude) {
        this();
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }

    public Ambulance(LatLon position) {
        this(position.getLatitude(), position.getLongitude());
    }

    public Ambulance(double x, double y, double baseTransferSpeed, double basePatrollingSpeed, double basePrivilegedSpeed) {
        this.setLatitude(x);
        this.setLongitude(y);
        this.basePatrollingSpeed = basePatrollingSpeed;
        this.timeOfLastMove = World.getInstance().getSimulationTime();
    }

    public void updateStateSelf() {
        if (state == State.AVAILABLE) {
            updateStateIfAvailable();
        } else if (state == State.TRANSFER_TO_ACCIDENT) {
            updateStateIfTransferToAccident();
        } else if (state == State.ACCIDENT) {
            updateStateIfFiring();
        } else if (state == State.RETURNING_TO_HOSPITAL) {
            updateStateIfReturningToHospital();
        }
    }

    private void updateStateIfAvailable() {
//        System.out.println("ambulance is available");
    }

    private void updateStateIfTransferToAccident() {
        if (action instanceof Ambulance.Transfer) {
            if (((Ambulance.Transfer) action).pathNodeList.isEmpty() || ((Ambulance.Transfer) action).pathNodeList == null) {
                setState(State.ACCIDENT);
                action = new Ambulance.IncidentParticipation(World.getInstance().getSimulationTimeLong(), (Incident) action.target);
            }
        } else {
            throw new Ambulance.IllegalTransferStateException();
        }
    }

    private void updateStateIfFiring() {
        if (action instanceof Ambulance.IncidentParticipation) {
            if (action.target == null || !((Firing) action.target).isActive() || !(action.target instanceof Firing)) {
                this.setState(State.RETURNING_TO_HOSPITAL);
                this.takeOrderAmbulance(new Transfer(World.getInstance().getSimulationTimeLong(),
                        hospital, State.RETURNING_TO_HOSPITAL));
                System.out.println("koniec ");
            }
        } else {
            throw new IllegalStateException("Action should be 'IncidentParticipation' and it is not");
        }
    }


    private void updateStateIfReturningToHospital() {

        if (action instanceof Ambulance.Transfer) {
            if (((Ambulance.Transfer) action).pathNodeList.isEmpty()) {
                System.out.println("elo wrocilem do szpitala");
                setState(State.AVAILABLE);
            }
        }
    }

    public void performAction() {
        double simulationTime = World.getInstance().getSimulationTime();
        switch (state) {
            case AVAILABLE,ACCIDENT:
                //empty
                break;
            case RETURNING_TO_HOSPITAL:
                if (action instanceof Ambulance.Transfer && ((Ambulance.Transfer) this.action).pathNodeList != null) {
                    move(simulationTime);
                }
                break;
            case TRANSFER_TO_ACCIDENT:
                //if (action instanceof Ambulance.Transfer && ((Ambulance.Transfer) this.action).pathNodeList != null) {
                move(simulationTime);
                //}
                break;
            default:
                throw new IllegalStateException("Illegal state");
        }
        timeOfLastMove = simulationTime;
    }

    @Override
    public void takeOrder(Patrol.Action action) {

    }

    private void move(double simulationTime) {
        // speed changed from km/h to m/s
        double traveledDistance = getSpeed() * 1000 / 3600 * Math.abs(simulationTime - timeOfLastMove);
        if (action instanceof Transfer) {

            double distanceToNearestNode = getDistanceToNearestNode();
            while (distanceToNearestNode < traveledDistance) {
                if (((Transfer) action).pathNodeList.size() == 1) break;

                traveledDistance -= distanceToNearestNode;
                Node removedNode = ((Transfer) action).pathNodeList.remove(0);
                setPosition(removedNode.getPosition());
                distanceToNearestNode = getDistanceToNearestNode();
            }
            LatLon nearestNodePosition = ((Transfer) action).pathNodeList.get(0).getPosition();
            if (distanceToNearestNode > traveledDistance) {
                double distanceFactor = traveledDistance / distanceToNearestNode;
                setLatitude((getLatitude() + (nearestNodePosition.getLatitude() - getLatitude()) * distanceFactor));
                setLongitude((getLongitude() + (nearestNodePosition.getLongitude() - getLongitude()) * distanceFactor));
            } else {
                setPosition(nearestNodePosition);
                ((Transfer) action).pathNodeList.remove(0);
            }
        } else {
            throw new IllegalTransferStateException();
        }
    }

    @Override
    public void takeOrderAmbulance(Ambulance.Action action) {
        this.action = action;
    }

    @Override
    public Patrol.State getState() {
        return null;
    }

    private double getDistanceToNearestNode() {
        if (((Transfer) action).pathNodeList.isEmpty()) throw new IllegalStateException("pathNodeList is empty!");

        LatLon sourceNodePosition = ((Transfer) action).pathNodeList.get(0).getPosition();
        return Haversine.distance(getLatitude(), getLongitude(), sourceNodePosition.getLatitude(), sourceNodePosition.getLongitude());
    }

    public double getSpeed() {
        switch (state) {
            case TRANSFER_TO_ACCIDENT, RETURNING_TO_HOSPITAL:
                return basePatrollingSpeed - (ThreadLocalRandom.current().nextBoolean() ? ThreadLocalRandom.current().nextDouble(basePatrollingSpeed * 10 / 100) : 0);
            case AVAILABLE:
                return basePatrollingSpeed - (ThreadLocalRandom.current().nextBoolean() ? ThreadLocalRandom.current().nextDouble(basePatrollingSpeed * 10 / 100) : 0);
            default:
                Logger.getInstance().logNewOtherMessage("The patrol is currently not moving");
                return basePatrollingSpeed;
        }
    }

    @Override
    public State getStateAmbulance() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public void drawSelf(Graphics2D g, JXMapViewer mapViewer) {
        var oldColor = g.getColor();

        switch (this.state) {
            case AVAILABLE -> g.setColor(new Color(70, 100, 200)); // green
            case TRANSFER_TO_ACCIDENT -> g.setColor(new Color(30, 180, 200)); // green
            case ACCIDENT -> g.setColor(new Color(255, 87, 36)); // yellowish
            case RETURNING_TO_HOSPITAL -> g.setColor(new Color(255, 255, 0)); // orangeish
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

    public enum State {
        AVAILABLE,
        TRANSFER_TO_ACCIDENT,
        ACCIDENT,
        RETURNING_TO_HOSPITAL
    }

    private static class IllegalTransferStateException extends IllegalStateException {
        public IllegalTransferStateException() {
            super("Action should be 'Transfer' and it is not");
        }
    }

    public class Action {
        protected Long startTime;
        protected Entity target;

        public Action(Long startTime) {
            this.startTime = startTime;
        }

        public Long getStartTime() {
            return startTime;
        }

        public void setStartTime(Long startTime) {
            this.startTime = startTime;
        }

        public Entity getTarget() {
            return target;
        }

        public void setTarget(Entity target) {
            this.target = target;
        }
    }

    public class Transfer extends Action {
        private java.util.List<Node> pathNodeList;

        public Transfer(Long startTime, Entity target, State nextState) {
            super(startTime);
            this.target = target;
            new PathCalculator(Ambulance.this, target).start();
        }

        public void setPathNodeList(java.util.List<Node> pathNodeList) {
            this.pathNodeList = pathNodeList;
        }
    }

    public class IncidentParticipation extends Action {

        public IncidentParticipation(Long startTime, Incident incident) {
            super(startTime);
            this.target = incident;
        }
    }
}
