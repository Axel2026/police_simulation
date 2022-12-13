package entities;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import simulation.PathCalculator;
import utils.Haversine;
import utils.Logger;
import world.World;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Patrol extends Entity implements IAgent, IDrawable {

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

    public Patrol() {
        this.basePatrollingSpeed = World.getInstance().getConfig().getBasePatrollingSpeed();
        this.baseTransferSpeed = World.getInstance().getConfig().getBaseTransferSpeed();
        this.basePrivilegedSpeed = World.getInstance().getConfig().getBasePrivilegedSpeed();
        this.timeOfLastMove = World.getInstance().getSimulationTime();
        this.durationOfTheShift = World.getInstance().getDurationOfTheShift();
        this.shiftEndTime = World.getInstance().getSimulationTime() + durationOfTheShift;
        this.timeBetweenDrawNeutralization = ThreadLocalRandom.current().nextInt(1000) + 3000.0;
        this.timeOfLastDrawNeutralization = World.getInstance().getSimulationTime();
    }

    public Patrol(double latitude, double longitude) {
        this();
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }

    public Patrol(LatLon position) {
        this(position.getLatitude(), position.getLongitude());
    }

    public Patrol(double x, double y, double baseTransferSpeed, double basePatrollingSpeed, double basePrivilegedSpeed) {
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
        if (state == State.PATROLLING) {
            updateStateIfPatrolling();
        } else if (state == State.TRANSFER_TO_INTERVENTION) {
            updateStateIfTransferToIntervention();
        } else if (state == State.INTERVENTION) {
            updateStateIfIntervention();
        } else if (state == State.TRANSFER_TO_FIRING) {
            updateStateIfTransferToFiring();
        } else if (state == State.FIRING) {
            updateStateIfFiring();
        } else if (state == State.CALCULATING_PATH) {
            updateStateIfCalculatingPath();
        } else if (state == State.RETURNING_TO_HQ) {
            updateStateIfReturningToHQ();
        }
    }

    private void updateStateIfPatrolling() {
        String currentStateToLog = "PATROLLING";
        if (isShiftOver()) {
            setState(State.RETURNING_TO_HQ);
            var hq = World.getInstance().getAllEntities().stream().filter(Headquarters.class::isInstance).findFirst().orElse(null);
            setAction(new Transfer(World.getInstance().getSimulationTimeLong(), hq, this.state));
        } else if (action == null) {
            drawNewTarget(currentStateToLog);
        } else if (action instanceof Transfer) {
            // if pathNodeList is empty, it draws a new patrol target
            if (((Transfer) action).pathNodeList != null && ((Transfer) action).pathNodeList.isEmpty()) {
                drawNewTarget(currentStateToLog);
            }
        } else {
            drawNewTarget(currentStateToLog);
        }
    }

    private void updateStateIfTransferToIntervention() {
        // if patrol has reached his destination, patrol changes state to INTERVENTION
        if (action instanceof Transfer) {
            if (((Transfer) action).pathNodeList.isEmpty()) {
                setState(State.INTERVENTION);
                action = new IncidentParticipation(World.getInstance().getSimulationTimeLong(), (Incident) action.target);
            }
        } else {
            throw new IllegalTransferStateException();
        }
    }

    private void updateStateIfIntervention() {
        if (action.target instanceof Firing) {
            setState(State.FIRING);
            action = new IncidentParticipation(World.getInstance().getSimulationTimeLong(), (Incident) action.target);
        }
        // if the duration of the intervention is over, patrol changes state to PATROLLING
        else if (action instanceof IncidentParticipation) {
            if (action.target == null) {
                setState(State.PATROLLING);
                drawNewTarget(null);
            } else if (!(((Intervention) (action).target).isActive())) {
                World.getInstance().removeEntity((action.target));
                setState(State.PATROLLING);
                drawNewTarget(null);
            }
        } else {
            throw new IllegalStateException("Action should be 'IncidentParticipation' and it is not");
        }
    }

    private void updateStateIfTransferToFiring() {
        // if patrol has reached his destination, patrol changes state to FIRING
        if (action instanceof Transfer) {
            if (((Transfer) action).pathNodeList != null && ((Transfer) action).pathNodeList.isEmpty()) {
                setState(State.FIRING);
                ((Firing) action.target).removeReachingPatrol(this);
                ((Firing) action.target).addSolvingPatrol(this);
                action = new IncidentParticipation(World.getInstance().getSimulationTimeLong(), (Incident) action.target);
            }
        } else {
            throw new IllegalTransferStateException();
        }
    }

    private void updateStateIfFiring() {
        // when the firing strength drops to zero, patrol changes state to PATROLLING
        if (action instanceof IncidentParticipation) {
            if (action.target == null || !((Firing) action.target).isActive() || !(action.target instanceof Firing)) {
                setState(State.PATROLLING);
                drawNewTarget(null);
            } else if (World.getInstance().getSimulationTime() > timeOfLastDrawNeutralization + timeBetweenDrawNeutralization) {
                if (ThreadLocalRandom.current().nextDouble() < 0.001) {
                    ((Firing) this.action.target).removeSolvingPatrol(this);
                    setState(State.NEUTRALIZED);
                }
                timeOfLastDrawNeutralization = World.getInstance().getSimulationTime();
            }
        } else {
            throw new IllegalStateException("Action should be 'IncidentParticipation' and it is not");
        }
    }

    private void updateStateIfCalculatingPath() {
        if (((Transfer) getAction()).pathNodeList != null) {
            setState(this.previousState);
        }
    }

    private void updateStateIfReturningToHQ() {
        if (action == null) {
            World.getInstance().getAllEntities()
                    .stream()
                    .filter(Headquarters.class::isInstance)
                    .findFirst()
                    .ifPresent(hq -> action = new Transfer(World.getInstance().getSimulationTimeLong(), hq, this.state));
        } else if (!(action instanceof Transfer)) {
            throw new IllegalTransferStateException();
        }
    }

    private void drawNewTarget(String previousState) {
        var world = World.getInstance();
        var node = (Node) world.getMap().getMyNodes().values().toArray()[ThreadLocalRandom.current().nextInt(world.getMap().getMyNodes().size())];
        this.action = new Transfer(World.getInstance().getSimulationTimeLong(), new Point(node.getPosition().getLatitude(), node.getPosition().getLongitude()), this.state);
        if (previousState != null) {
            logChangingState(previousState, this.state.toString());
        }
    }

    public void performAction() {
        double simulationTime = World.getInstance().getSimulationTime();
        switch (state) {
            case PATROLLING:
                if (action instanceof Transfer && ((Transfer) this.action).pathNodeList != null) {
                    move(simulationTime);
                }
                break;
            case RETURNING_TO_HQ:
                if (action instanceof Transfer && ((Transfer) this.action).pathNodeList != null) {
                    if (((Transfer) action).pathNodeList.isEmpty()) {
                        World.getInstance().removeEntity(this);
                        Logger.getInstance().logNewOtherMessage(this + " removed itself after ending shift and coming back to HQ");

                    } else {
                        move(simulationTime);
                    }
                }
                break;
            case TRANSFER_TO_INTERVENTION, TRANSFER_TO_FIRING:
                move(simulationTime);
                break;
            case INTERVENTION, CALCULATING_PATH, FIRING, NEUTRALIZED:
                // empty
                break;
            default:
                throw new IllegalStateException("Illegal state");
        }
        timeOfLastMove = simulationTime;
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

    private void logChangingState(String previousState, String currentState) {
        Logger.getInstance().logNewMessageChangingState(this, previousState, currentState);
    }

    @Override
    public void takeOrder(Action action) {
        this.action = action;
    }

    private double getDistanceToNearestNode() {
        if (((Transfer) action).pathNodeList.isEmpty()) throw new IllegalStateException("pathNodeList is empty!");

        LatLon sourceNodePosition = ((Transfer) action).pathNodeList.get(0).getPosition();
        return Haversine.distance(getLatitude(), getLongitude(), sourceNodePosition.getLatitude(), sourceNodePosition.getLongitude());
    }

    public double getSpeed() {
        switch (state) {
            case PATROLLING, RETURNING_TO_HQ:
                return basePatrollingSpeed - (ThreadLocalRandom.current().nextBoolean() ? ThreadLocalRandom.current().nextDouble(basePatrollingSpeed * 10 / 100) : 0);
            case TRANSFER_TO_INTERVENTION:
                return baseTransferSpeed;
            case TRANSFER_TO_FIRING:
                return basePrivilegedSpeed + (ThreadLocalRandom.current().nextBoolean() ? ThreadLocalRandom.current().nextDouble(basePrivilegedSpeed * 10 / 100) : 0);
            default:
                Logger.getInstance().logNewOtherMessage("The patrol is currently not moving");
                return basePatrollingSpeed;
        }
    }

    public boolean isShiftOver() {
        return World.getInstance().getSimulationTime() > shiftEndTime;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        var previousStateToLog = this.state;
        this.state = state;
        logChangingState(previousStateToLog != null ? previousStateToLog.toString() : " ", this.state.toString());
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public double getTimeSinceLastActive() {
        return World.getInstance().getSimulationTime() - timeOfLastMove;
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

    public enum State {
        PATROLLING,
        TRANSFER_TO_INTERVENTION,
        TRANSFER_TO_FIRING,
        INTERVENTION,
        FIRING,
        NEUTRALIZED,
        CALCULATING_PATH,
        RETURNING_TO_HQ
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
            new PathCalculator(Patrol.this, target).start();
            Patrol.this.previousState = nextState;
            if (nextState == State.TRANSFER_TO_FIRING || nextState == State.TRANSFER_TO_INTERVENTION) {
                Logger.getInstance().logNewMessageChangingState(Patrol.this, nextState.toString(), State.CALCULATING_PATH.toString());
            }
            Patrol.this.state = State.CALCULATING_PATH;
        }

        public List<Node> getPathNodeList() {
            return pathNodeList;
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
