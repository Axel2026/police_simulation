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
    private final double durationOfTheShift;
    private final double basePatrollingSpeed;
    private final double baseTransferSpeed;
    private final double basePrivilegedSpeed;
    private final double shiftEndTime;
    private final double timeBetweenDrawNeutralization;
    private double timeOfLastMove;
    private State state;
    private State previousState;
    private Action action;
    private double timeOfLastDrawNeutralization;

    public Ambulance() {
        this.basePatrollingSpeed = World.getInstance().getConfig().getBasePatrollingSpeed();
        this.baseTransferSpeed = World.getInstance().getConfig().getBaseTransferSpeed();
        this.basePrivilegedSpeed = World.getInstance().getConfig().getBasePrivilegedSpeed();
        this.timeOfLastMove = World.getInstance().getSimulationTime();
        this.durationOfTheShift = World.getInstance().getDurationOfTheShift();
        this.shiftEndTime = World.getInstance().getSimulationTime() + durationOfTheShift;
        this.timeBetweenDrawNeutralization = ThreadLocalRandom.current().nextInt(1000) + 3000.0;
        this.timeOfLastDrawNeutralization = World.getInstance().getSimulationTime();
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
        this.baseTransferSpeed = baseTransferSpeed;
        this.basePrivilegedSpeed = basePrivilegedSpeed;
        this.timeOfLastMove = World.getInstance().getSimulationTime();
        this.durationOfTheShift = World.getInstance().getDurationOfTheShift();
        this.shiftEndTime = World.getInstance().getSimulationTime() + durationOfTheShift;
        this.timeBetweenDrawNeutralization = ThreadLocalRandom.current().nextInt(1000) + 3000.0;
        this.timeOfLastDrawNeutralization = World.getInstance().getSimulationTime();
    }

    public void updateStateSelf() {
        if (state == State.WAIT_IN_HOSPITAL) {
            updateStateIfPatrolling();
        } else if (state == State.TRANSFER_TO_ACCIDENT) {
            updateStateIfTransferToFiring();
        } else if (state == State.ACCIDENT) {
            updateStateIfFiring();
        } else if (state == State.RETURNING_TO_HOSPITAL) {
            updateStateIfReturningToHospital();
        }
    }

    private void updateStateIfPatrolling() {
        setState(State.RETURNING_TO_HOSPITAL);
        var hq = World.getInstance().getAllEntities().stream().filter(Hospital.class::isInstance).findFirst().orElse(null);
        setAction(new Ambulance.Transfer(World.getInstance().getSimulationTimeLong(), hq, this.state));
    }

    private void updateStateIfTransferToFiring() {
        // if patrol has reached his destination, patrol changes state to INTERVENTION
        if (action instanceof Ambulance.Transfer) {
            if (((Ambulance.Transfer) action).pathNodeList.isEmpty()) {
                setState(State.ACCIDENT);
                System.out.println("elo dotarlem na strzelanine");
                action = new Ambulance.IncidentParticipation(World.getInstance().getSimulationTimeLong(), (Incident) action.target);
            }
        } else {
            throw new Ambulance.IllegalTransferStateException();
        }
    }

    private void updateStateIfFiring() {
        // when the firing strength drops to zero, patrol changes state to PATROLLING
//        System.out.println(action.target);
        if (action instanceof Ambulance.IncidentParticipation) {
            if (action.target == null || !((Firing) action.target).isActive() || !(action.target instanceof Firing)) {
                System.out.println("koniec 3");
                setState(State.RETURNING_TO_HOSPITAL);
                drawNewTarget(null);
            } else if (World.getInstance().getSimulationTime() > timeOfLastDrawNeutralization + timeBetweenDrawNeutralization) {
                System.out.println("koniec 4");
                setState(State.RETURNING_TO_HOSPITAL);
                timeOfLastDrawNeutralization = World.getInstance().getSimulationTime();
            }
        } else {
            throw new IllegalStateException("Action should be 'IncidentParticipation' and it is not");
        }
    }


    private void updateStateIfReturningToHospital() {
        var entities = World.getInstance().getEntitiesNear(this, 10);
//        System.out.println(entities.get(0));
        System.out.println("karetka powinna wracac");
        World.getInstance().getAllEntities()
                .stream()
                .filter(Headquarters.class::isInstance)
                .findFirst()
                .ifPresent(hqs -> action = new Ambulance.Transfer(World.getInstance().getSimulationTimeLong(), hqs, this.state));
        drawNewTarget(null);
    }

    private void drawNewTarget(String previousState) {
        var world = World.getInstance();
        var node = (Node) world.getMap().getMyNodes().values().toArray()[ThreadLocalRandom.current().nextInt(world.getMap().getMyNodes().size())];
        this.action = new Transfer(World.getInstance().getSimulationTimeLong(), new Point(node.getPosition().getLatitude(), node.getPosition().getLongitude()), State.WAIT_IN_HOSPITAL);
    }

    public void performAction() {
        double simulationTime = World.getInstance().getSimulationTime();
        switch (state) {
            case WAIT_IN_HOSPITAL:
//                if (action instanceof Transfer && ((Transfer) this.action).pathNodeList != null) {
//                    move(simulationTime);
//                }
                break;
            case RETURNING_TO_HOSPITAL:
                if (action instanceof Transfer && ((Transfer) this.action).pathNodeList != null) {
                    if (((Transfer) action).pathNodeList.isEmpty()) {
                        World.getInstance().removeEntity(this);
                        Logger.getInstance().logNewOtherMessage(this + " removed itself after ending shift and coming back to HQ");

                    } else {
                        move(simulationTime);
                    }
                }
                break;
            case TRANSFER_TO_ACCIDENT:
                if (action instanceof Ambulance.Transfer && ((Ambulance.Transfer) this.action).pathNodeList != null) {
                    move(simulationTime);
                }
                break;
            case ACCIDENT:
                // empty
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
            case WAIT_IN_HOSPITAL:
                return 0;
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
            case WAIT_IN_HOSPITAL -> g.setColor(new Color(70, 100, 200)); // green
            case TRANSFER_TO_ACCIDENT -> g.setColor(new Color(200, 90, 255)); // dark green
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
        WAIT_IN_HOSPITAL,
        TRANSFER_TO_ACCIDENT,
        ACCIDENT,
        RETURNING_TO_HOSPITAL,
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
