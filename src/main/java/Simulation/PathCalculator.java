package Simulation;

import Visualisation.SWAT;
import Visualisation.Ambulance;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmLatLon;
import Simulation.entities.Entity;
import Visualisation.Patrol;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AStarShortestPath;
import Support.ImportedEdge;
import utils.EntityTypes;
import utils.Haversine;

import java.util.ArrayList;
import java.util.List;

import static Visualisation.Map.getNearestNode;

public class PathCalculator extends Thread {

    private final AStarShortestPath<Node, ImportedEdge> pathCalc = World.getInstance().getMap().getPathCalculator();
    private final java.util.Map<Long, Node> myNodes = World.getInstance().getMap().getMyNodes();
    private final Entity source;
    private final Entity target;

    public PathCalculator(Entity source, Entity target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public void run() {
        var pathNodeList = getPathNodeList(source.getLatitude(), source.getLongitude(), target.getLatitude(), target.getLongitude());

        if (source.getType() == EntityTypes.PATROL) {
            if (pathNodeList.size() == 1) {
                var pathNodeList2 = new ArrayList<Node>();
                pathNodeList2.add(pathNodeList.get(0));
                ((Patrol.Transfer) ((Patrol) source).getAction()).setPathNodeList(pathNodeList2);
            } else {
                ((Patrol.Transfer) ((Patrol) source).getAction()).setPathNodeList(pathNodeList);
            }
        } else if (source.getType() == EntityTypes.SWAT) {
            if (pathNodeList.size() == 1) {
                var pathNodeList2 = new ArrayList<Node>();
                pathNodeList2.add(pathNodeList.get(0));
                ((SWAT.Transfer) ((SWAT) source).getAction()).setPathNodeList(pathNodeList2);
            } else {
                ((SWAT.Transfer) ((SWAT) source).getAction()).setPathNodeList(pathNodeList);
            }
        } else if (pathNodeList.size() == 1 && source.toString().contains("Ambulance")) {
            var pathNodeList2 = new ArrayList<Node>();
            pathNodeList2.add(pathNodeList.get(0));
            ((Ambulance.Transfer) ((Ambulance) source).getAction()).setPathNodeList(pathNodeList2);
        } else if (pathNodeList.size() != 1 && source.toString().contains("Ambulance")) {
            ((Ambulance.Transfer) ((Ambulance) source).getAction()).setPathNodeList(pathNodeList);

        } else if (pathNodeList.size() == 1 && source.toString().contains("Patrol")) {
            var pathNodeList2 = new ArrayList<Node>();
            pathNodeList2.add(pathNodeList.get(0));
            ((Patrol.Transfer) ((Patrol) source).getAction()).setPathNodeList(pathNodeList2);
        } else if (pathNodeList.size() != 1 && source.toString().contains("Patrol")) {
            ((Patrol.Transfer) ((Patrol) source).getAction()).setPathNodeList(pathNodeList);
        }
    }

    public List<Node> getPathNodeList(double sourceLatitude, double sourceLongitude, double targetLatitude,
                                      double targetLongitude) {
        Node nearSourceNode = findNearestNode(new OsmLatLon(sourceLatitude, sourceLongitude));
        Node nearTargetNode1 = findNearestNode(new OsmLatLon(targetLatitude, targetLongitude));
        GraphPath<Node, ImportedEdge> path = pathCalc.getPath(nearSourceNode, nearTargetNode1);

        // the case where the route between nodes does not exist
        if (path == null) {
            List<Node> forbiddenNodes = new ArrayList<>();
            while (path == null) {
                forbiddenNodes.add(nearSourceNode);
                forbiddenNodes.add(nearTargetNode1);
                nearSourceNode = findNearestNode(new OsmLatLon(sourceLatitude, sourceLongitude), forbiddenNodes);
                nearTargetNode1 = findNearestNode(new OsmLatLon(targetLatitude, targetLongitude), forbiddenNodes);

                // calculation of the route between two points in the case where initially there is no route between them, the simulation stops working smoothly
                while (nearSourceNode.equals(nearTargetNode1)) {
                    forbiddenNodes.add(nearSourceNode);
                    forbiddenNodes.add(nearTargetNode1);
                    nearSourceNode = findNearestNode(new OsmLatLon(sourceLatitude, sourceLongitude), forbiddenNodes);
                    nearTargetNode1 = findNearestNode(new OsmLatLon(targetLatitude, targetLongitude), forbiddenNodes);
                }
                path = pathCalc.getPath(nearSourceNode, nearTargetNode1);
            }
        }
        return path.getVertexList();
    }

    public Node findNearestNode(LatLon point) {
        double distance = Double.MAX_VALUE;
        Node nearestNode = null;
        for (java.util.Map.Entry<Long, Node> me : myNodes.entrySet()) {
            LatLon nodePosition = me.getValue().getPosition();
            double tmpDistance = Haversine.distance(point.getLatitude(), point.getLongitude(), nodePosition.getLatitude(), nodePosition.getLongitude());
            if (tmpDistance < distance) {
                distance = tmpDistance;
                nearestNode = me.getValue();
            }
        }
        return nearestNode;
    }

    public Node findNearestNode(LatLon point, List<Node> forbiddenNodes) {
        return getNearestNode(point, forbiddenNodes, myNodes);
    }
}
