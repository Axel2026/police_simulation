package entities;

import osm_to_graph.ImportedEdge;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmLatLon;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AStarShortestPath;
import utils.Haversine;

import java.util.ArrayList;
import java.util.List;

public class Map {

    private final Graph<Node, ImportedEdge> graph;
    private final java.util.Map<Long, Node> myNodes;
    private final BoundingBox boundingBox;
    private final List<District> districts;
    private final AStarShortestPath<Node, ImportedEdge> pathCalculator;

    public Map(Graph<Node, ImportedEdge> graph, java.util.Map<Long, Node> myNodes, BoundingBox boundingBox, List<District> districts) {
        this.graph = graph;
        this.myNodes = myNodes;
        this.boundingBox = boundingBox;
        this.districts = districts;
        this.pathCalculator = new AStarShortestPath<>(graph, new Haversine.OwnHeuristics());
        assignNodesToDistricts();
    }

    public AStarShortestPath<Node, ImportedEdge> getPathCalculator() {
        return new AStarShortestPath<>(graph, new Haversine.OwnHeuristics());
    }

    public java.util.Map<Long, Node> getMyNodes() {
        return myNodes;
    }

    public Graph<Node, ImportedEdge> getGraph() {
        return graph;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public List<District> getDistricts() {
        return new ArrayList<>(districts);
    }

    public void assignNodesToDistricts() {
        for (java.util.Map.Entry<Long, Node> me : myNodes.entrySet()) {
            for (var d : districts) {
                if (d.contains(me.getValue().getPosition())) {
                    d.addNodeToDistrict(me.getValue());
                    break;
                }
            }
        }
    }

    // patrols use nodeList to navigate the route
    public List<Node> getPathNodeList(double sourceLatitude, double sourceLongitude, double targetLatitude, double targetLongitude) {
        Node nearSourceNode = findNearestNode(new OsmLatLon(sourceLatitude, sourceLongitude), null);
        Node nearTargetNode1 = findNearestNode(new OsmLatLon(targetLatitude, targetLongitude), null);
        GraphPath<Node, ImportedEdge> path = pathCalculator.getPath(nearSourceNode, nearTargetNode1);

        // the case where the route between nodes does not exist
        if (path == null) {
            List<Node> forbiddenNodes = new ArrayList<>();
            while (path == null) {
                forbiddenNodes.add(nearSourceNode);
                forbiddenNodes.add(nearTargetNode1);
                nearSourceNode = findNearestNode(new OsmLatLon(sourceLatitude, sourceLongitude), forbiddenNodes);
                nearTargetNode1 = findNearestNode(new OsmLatLon(targetLatitude, targetLongitude), forbiddenNodes);

                // calculation of the route between two points in the case where initially there is no route between them, the simulation stops working smoothly
                while (nearSourceNode == null || nearSourceNode.equals(nearTargetNode1)) {
                    forbiddenNodes.add(nearSourceNode);
                    forbiddenNodes.add(nearTargetNode1);
                    nearSourceNode = findNearestNode(new OsmLatLon(sourceLatitude, sourceLongitude), forbiddenNodes);
                    nearTargetNode1 = findNearestNode(new OsmLatLon(targetLatitude, targetLongitude), forbiddenNodes);
                }
                path = pathCalculator.getPath(nearSourceNode, nearTargetNode1);
            }
        }
        return path.getVertexList();
    }

    private Node findNearestNode(LatLon point, List<Node> forbiddenNodes) {
        return getNearestNode(point, forbiddenNodes, myNodes);
    }

    public static Node getNearestNode(LatLon point, List<Node> forbiddenNodes, java.util.Map<Long, Node> myNodes) {
        var distance = Double.MAX_VALUE;
        Node nearestNode = null;
        for (java.util.Map.Entry<Long, Node> me : myNodes.entrySet()) {
            LatLon nodePosition = me.getValue().getPosition();
            var tmpDistance = Haversine.distance(point.getLatitude(), point.getLongitude(), nodePosition.getLatitude(), nodePosition.getLongitude());
            if (tmpDistance < distance && !forbiddenNodes.contains(me.getValue())) {
                distance = tmpDistance;
                nearestNode = me.getValue();
            }
        }
        return nearestNode;
    }
}
