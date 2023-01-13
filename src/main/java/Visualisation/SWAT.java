package Visualisation;

import Simulation.PathCalculator;
import Simulation.World;
import Simulation.entities.*;
import Simulation.entities.Point;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import utils.EntityTypes;
import utils.Haversine;
import utils.Logger;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SWAT extends Entity implements IDrawable {

    private final double basePrivilegedSpeed;
    private final double timeBetweenDrawNeutralization;
    private double timeOfLastMove;
    private double timeOfLastDrawNeutralization;
    private State state;
    private State previousState;
    private Action action;
    static AtomicInteger nextId = new AtomicInteger();
    private final int id = nextId.incrementAndGet();
    private District district;

    public SWAT() {
        this.basePrivilegedSpeed = World.getInstance().getConfig().getBasePrivilegedSpeed();
        this.timeOfLastMove = World.getInstance().getSimulationTime();
        this.timeBetweenDrawNeutralization = ThreadLocalRandom.current().nextInt(1000) + 3000.0;
        this.timeOfLastDrawNeutralization = World.getInstance().getSimulationTime();
        this.state = State.WAITING_FOR_ORDERS;
    }

    public SWAT(double latitude, double longitude, District district) {
        this();
        this.setLatitude(latitude);
        this.setLongitude(longitude);
        this.setType(EntityTypes.SWAT);
        this.state = State.WAITING_FOR_ORDERS;
        this.district = district;
    }

    private void drawNewTarget(String previousState) {
        var world = World.getInstance();
        var node = (Node) world.getMap().getMyNodes().values().toArray()[ThreadLocalRandom.current().nextInt(world.getMap().getMyNodes().size())];
        this.action = new Transfer(World.getInstance().getSimulationTimeLong(), new Point(node.getPosition().getLatitude(), node.getPosition().getLongitude()), this.state);
        if (previousState != null) {
            logChangingState(previousState, this.state.toString());
        }
    }

    public void updateStateSelf() throws IllegalStateException {
        if (state == State.WAITING_FOR_ORDERS) {
        } else if (state == State.INTERVENTION) {
            updateStateIfIntervention();
        } else if (state == State.TRANSFER_TO_FIRING) {
            updateStateIfTransferToFiring();
        } else if (state == State.CALCULATING_PATH) {
            updateStateIfCalculatingPath();
        } else if (state == State.RETURNING_TO_HQ) {
            updateStateIfReturningToHQ();
        }
    }

    private void updateStateIfIntervention() {
        if (action instanceof IncidentParticipation) {
            if (action.target == null || !((Firing) action.target).isActive() || !(action.target instanceof Firing)) {
                System.out.println("RETURNING_TO_HQ from updateStateIfIntervention()");
                setState(State.RETURNING_TO_HQ);
                var swatHQs = World.getInstance().getAllEntities().stream().filter(SWATHeadquarters.class::isInstance).map(SWATHeadquarters.class::cast).collect(Collectors.toList());

                for (SWATHeadquarters hq : swatHQs) {
                    if (hq.getDistrict().getName().equals(this.getDistrict().getName())) {
                        setState(State.RETURNING_TO_HQ);
                        System.out.println("Latitude: " + hq.getLatitude());
                        System.out.println("Longitude: " + hq.getLongitude());
                        setAction(new Transfer(World.getInstance().getSimulationTimeLong(), hq, this.state));
                        hq.addAvailableSWATSquad(this);
                    }
                }
            } else if (World.getInstance().getSimulationTime() > timeOfLastDrawNeutralization + timeBetweenDrawNeutralization) {
                if (ThreadLocalRandom.current().nextDouble() < 0.001) {
                    ((Firing) this.action.target).removeSolvingSWATSquad(this);
                }
                timeOfLastDrawNeutralization = World.getInstance().getSimulationTime();
            }
        } else {
            throw new IllegalStateException("Action should be 'IncidentParticipation' and it is not");
        }
    }

    private void updateStateIfTransferToFiring() {
        // if patrol has reached his destination, patrol changes state to FIRING
        if (action instanceof Transfer) {
            if (((Transfer) action).pathNodeList != null && ((Transfer) action).pathNodeList.isEmpty()) {
                setState(State.INTERVENTION);
                ((Firing) action.target).removeReachingSWATSquad(this);
                ((Firing) action.target).addSolvingSWATSquad(this);
                action = new IncidentParticipation(World.getInstance().getSimulationTimeLong(), (Incident) action.target);
            }
        } else {
            throw new IllegalTransferStateException();
        }
    }

    private void updateStateIfCalculatingPath() {
        if (((Transfer) getAction()).pathNodeList != null) {
            setState(this.previousState);
        }
    }

    private void updateStateIfReturningToHQ() {
        var swatHQs = World.getInstance().getAllEntities().stream().filter(SWATHeadquarters.class::isInstance).map(SWATHeadquarters.class::cast).collect(Collectors.toList());

        if (action == null) {
            System.out.println("Test69");
            for (SWATHeadquarters hq : swatHQs) {
                if (hq.getDistrict().getName().equals(this.getDistrict().getName())) {
                    setState(State.RETURNING_TO_HQ);
                    setAction(new Transfer(World.getInstance().getSimulationTimeLong(), hq, this.state));
                    hq.addAvailableSWATSquad(this);
                }
            }
        } else if (!(action instanceof Transfer)) {
            throw new IllegalTransferStateException();
        }
    }

    private double getDistanceToNearestNode() {
        if (((SWAT.Transfer) action).pathNodeList.isEmpty()) throw new IllegalStateException("pathNodeList is empty!");

        LatLon sourceNodePosition = ((SWAT.Transfer) action).pathNodeList.get(0).getPosition();
        return Haversine.distance(getLatitude(), getLongitude(), sourceNodePosition.getLatitude(), sourceNodePosition.getLongitude());
    }

    private void logChangingState(String previousState, String currentState) {
        Logger.getInstance().logNewMessageChangingState(this, previousState, currentState);
    }

    public void setState(State state) {
        var previousStateToLog = this.state;
        this.state = state;
        logChangingState(previousStateToLog != null ? previousStateToLog.toString() : " ", this.state.toString());
    }

    private void move(double simulationTime) {
        // speed changed from km/h to m/s
        double traveledDistance = this.basePrivilegedSpeed * 1000 / 3600 * Math.abs(simulationTime - timeOfLastMove);
        if (action instanceof SWAT.Transfer) {

            double distanceToNearestNode = getDistanceToNearestNode();
            while (distanceToNearestNode < traveledDistance) {
                if (((SWAT.Transfer) action).pathNodeList.size() == 1) break;

                traveledDistance -= distanceToNearestNode;
                Node removedNode = ((SWAT.Transfer) action).pathNodeList.remove(0);
                setPosition(removedNode.getPosition());
                distanceToNearestNode = getDistanceToNearestNode();
            }
            LatLon nearestNodePosition = ((SWAT.Transfer) action).pathNodeList.get(0).getPosition();
            if (distanceToNearestNode > traveledDistance) {
                double distanceFactor = traveledDistance / distanceToNearestNode;
                setLatitude((getLatitude() + (nearestNodePosition.getLatitude() - getLatitude()) * distanceFactor));
                setLongitude((getLongitude() + (nearestNodePosition.getLongitude() - getLongitude()) * distanceFactor));
            } else {
                setPosition(nearestNodePosition);
                ((SWAT.Transfer) action).pathNodeList.remove(0);
            }
        } else {
            throw new IllegalTransferStateException();
        }
    }

    public District getDistrict() {
        return district;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    private static class IllegalTransferStateException extends IllegalStateException {
        public IllegalTransferStateException() {
            super("Action should be 'Transfer' and it is not");
        }
    }

    public int getId() {
        return id;
    }

    public void performAction() throws IllegalStateException {
        double simulationTime = World.getInstance().getSimulationTime();
        switch (state) {
            case WAITING_FOR_ORDERS:
                break;
            case RETURNING_TO_HQ:
                if (action instanceof SWAT.Transfer && ((SWAT.Transfer) this.action).pathNodeList != null) {
                    if (((SWAT.Transfer) action).pathNodeList.isEmpty()) {
                        World.getInstance().removeEntity(this);

                        Logger.getInstance().logNewOtherMessage(this + "Swat squad returned to HQ");
                    } else {
                        move(simulationTime);
                    }
                }
                break;
            case TRANSFER_TO_FIRING:
                move(simulationTime);
                break;
            case INTERVENTION, CALCULATING_PATH:
                // empty
                break;
            default:
                throw new IllegalStateException("Illegal state");
        }
        timeOfLastMove = simulationTime;
    }

    public void takeOrder(Action action) {
        this.action = action;
    }

    public State getState() {
        return state;
    }

    public Action getAction() {
        return action;
    }

    public enum State {
        WAITING_FOR_ORDERS,
        TRANSFER_TO_FIRING,
        INTERVENTION,
        RETURNING_TO_HQ,
        CALCULATING_PATH
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
            new PathCalculator(SWAT.this, target).start();
            SWAT.this.previousState = nextState;
            if (nextState == State.TRANSFER_TO_FIRING) {
                Logger.getInstance().logNewMessageChangingState(SWAT.this, nextState.toString(), State.CALCULATING_PATH.toString());
            }
            SWAT.this.state = State.CALCULATING_PATH;
        }

        public List<Node> getPathNodeList() {
            return pathNodeList;
        }

        public void setPathNodeList(java.util.List<Node> pathNodeList) {
            this.pathNodeList = pathNodeList;
        }
    }

    @Override
    public void drawSelf(Graphics2D g, JXMapViewer mapViewer) {
        var oldColor = g.getColor();

        switch (this.state) {
            case WAITING_FOR_ORDERS -> g.setColor(new Color(255, 131, 54, 255)); // transparent
            case TRANSFER_TO_FIRING -> g.setColor(new Color(255, 131, 54)); // orangeish
            case INTERVENTION -> g.setColor(new Color(0, 92, 230)); // blue
            case RETURNING_TO_HQ -> g.setColor(new Color(153, 0, 204)); // purple
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

    public class IncidentParticipation extends Action {

        public IncidentParticipation(Long startTime, Incident incident) {
            super(startTime);
            this.target = incident;
        }
    }
}
