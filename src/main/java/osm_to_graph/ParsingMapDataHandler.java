package osm_to_graph;

import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.osmapi.map.handler.DefaultMapDataHandler;
import de.westnordost.osmapi.map.handler.MapDataHandler;
import entities.District;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import utils.Haversine;

import java.awt.geom.Path2D;
import java.util.*;


public class ParsingMapDataHandler extends DefaultMapDataHandler implements MapDataHandler {

    private final Graph<Node, ImportedEdge> graph = new DefaultDirectedWeightedGraph<>(ImportedEdge.class);
    private final List<Relation> relations = new ArrayList<>();
    private final HashMap<Long, List<Long>> waysInRelation = new HashMap<>();
    private final HashMap<Long, Node> myNodes = new HashMap<>();
    private final List<ImportedEdge> myEdges = new ArrayList<>();
    private final HashMap<Long, Way> myWays = new HashMap<>();

    private Double minLatitude;
    private Double maxLatitude;
    private Double minLongitude;
    private Double maxLongitude;

    public static int findNearestNode(Node node, List<Node> nodes) {
        double distance = Double.MAX_VALUE;
        int id = 0;
        double latitude = node.getPosition().getLatitude();
        double longitude = node.getPosition().getLongitude();
        for (int i = 0; i < nodes.size(); i++) {
            double tmpDistance = Haversine.distance(latitude, longitude, nodes.get(i).getPosition().getLatitude(), nodes.get(i).getPosition().getLongitude());
            if (tmpDistance < distance) {
                distance = tmpDistance;
                id = i;
            }
        }
        return id;
    }

    public Map<Long, Node> getNodesMap() {
        return this.myNodes;
    }

    @Override
    public void handle(Node node) {
        super.handle(node);

        // counts the range of coordinates (will be useful for conversion):
        if (minLatitude == null) minLatitude = node.getPosition().getLatitude();
        if (maxLatitude == null) maxLatitude = node.getPosition().getLatitude();
        if (minLongitude == null) minLongitude = node.getPosition().getLongitude();
        if (maxLongitude == null) maxLongitude = node.getPosition().getLongitude();

        double latitude = node.getPosition().getLatitude();
        double longitude = node.getPosition().getLongitude();
        if (latitude > maxLatitude) maxLatitude = latitude;
        else if (latitude < minLatitude) minLatitude = latitude;
        if (longitude > maxLongitude) maxLongitude = longitude;
        else if (longitude < minLongitude) minLongitude = longitude;

        // add a node to the graph and myNodes:
        graph.addVertex(node);
        myNodes.put(node.getId(), node);
    }

    @Override
    public void handle(Way way) {
        super.handle(way);

        myWays.put(way.getId(), way);

        long previousNodeID = -1;
        boolean oneway = false;

        // uncomment if you want one-way roads to be treated as one-way

        //  check if road is oneway:
//        if (way.getTags().containsKey("oneway")) {if (way.getTags().get("oneway").equals("yes")) { oneway = true;} }

        // loop creating all edges in a given path:
        for (long nodeID : way.getNodeIds()) {
            if (previousNodeID == -1) {
                previousNodeID = nodeID;
                continue;
            }
            // previously unmet node found in a way:
            if (!myNodes.containsKey(previousNodeID)) {
                myNodes.put(previousNodeID, null);
            }

            // previously unmet node found in a way:
            if (!myNodes.containsKey(nodeID)) {
                myNodes.put(nodeID, null);
            }

            // Adding edge from node ID: previousNodeID to: nodeID
            ImportedEdge edge = new ImportedEdge(previousNodeID, nodeID);
            myEdges.add(edge);
            if (!oneway) {
                // Adding edge from node ID: nodeID to: previousNodeID
                edge = new ImportedEdge(nodeID, previousNodeID);
                myEdges.add(edge);
            }
            previousNodeID = nodeID;
        }
    }

    @Override
    public void handle(Relation relation) {
        List<RelationMember> members = relation.getMembers();
        List<Long> ways = new ArrayList<>();
        for (RelationMember r : members) {
            if (r.getType().name().equals("WAY") && r.getRole().equals("outer")) {
                ways.add(r.getRef());
            }
        }
        waysInRelation.put(relation.getId(), ways);
        relations.add(relation);
    }

    public Graph<Node, ImportedEdge> getGraph() {
        // adds all edges to the graph:
        for (ImportedEdge edge : myEdges) {

            Node nodeS = myNodes.get(edge.getSourceNodeID());
            Node nodeT = myNodes.get(edge.getTargetNodeID());

            // calculates the distance (edge weight):
            double dist = Haversine.distance(nodeS.getPosition().getLatitude(),
                    nodeS.getPosition().getLongitude(),
                    nodeT.getPosition().getLatitude(),
                    nodeT.getPosition().getLongitude());
            dist *= 100;
            dist = Math.round(dist);
            dist /= 100;

            edge.setDistance(dist);

            // adds an edge and its weight:
            boolean b = graph.addEdge(nodeS, nodeT, edge);
            if (b) {
                graph.setEdgeWeight(edge, dist);
            } else {
                if (!graph.containsEdge(nodeS, nodeT)) {
                    throw new IllegalStateException("edge has not been added to the graph " + nodeS.getId() + " " + nodeT.getId());
                }
            }
        }
        checkConnectivity();
        return graph;
    }

    private void checkConnectivity() {
        ConnectivityInspector<Node, ImportedEdge> connectivityInspector = new ConnectivityInspector<>(graph);
        List<Set<Node>> sets = connectivityInspector.connectedSets();
        int index = 0;
        int size = 0;
        for (int i = 0; i < sets.size(); i++) {
            int sizeTemp = sets.get(i).size();
            if (size < sizeTemp) {
                index = i;
                size = sizeTemp;
            }
        }
        sets.remove(index);
        for (Set<Node> set : sets) {
            for (Node n : set) {
                myNodes.remove(n.getId());
                graph.removeVertex(n);
            }
        }
    }

    public Double getMinLatitude() {
        return minLatitude;
    }

    public Double getMinLongitude() {
        return minLongitude;
    }

    public Double getMaxLatitude() {
        return maxLatitude;
    }

    public Double getMaxLongitude() {
        return maxLongitude;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public List<District> getDistricts() {

        List<District> districts = new ArrayList<>();

        for (Relation r : relations) {
            List<Node> unorderedNodes = new ArrayList<>();
            var ways = waysInRelation.get(r.getId());

            for (Long wayID : ways) {
                var way = myWays.get(wayID);
                var nodeIds = way.getNodeIds();
                for (Long node : nodeIds) {
                    unorderedNodes.add(myNodes.get(node));
                }
            }

            unorderedNodes = new ArrayList<>(new HashSet<>(unorderedNodes));
            var sortedNodes = sortNodes(unorderedNodes);

            var lats = new double[sortedNodes.size()];
            var lons = new double[sortedNodes.size()];
            for (int i = 0; i < sortedNodes.size(); ++i) {
                var position = sortedNodes.get(i).getPosition();
                lats[i] = position.getLatitude();
                lons[i] = position.getLongitude();
            }

            Path2D path = new Path2D.Double();
            path.moveTo(lats[0], lons[0]);
            for (int i = 1; i < lats.length; i++) {
                path.lineTo(lats[i], lons[i]);
                if (Haversine.distance(lats[0], lons[0], lats[i], lons[i]) < 25 && i > 100) {
                    path.lineTo(lats[0], lons[0]);
                    path.closePath();
                    break;
                }
            }
            path.lineTo(lats[0], lons[0]);
            path.closePath();
            districts.add(new District(r.getId(), r.getTags().get("name"), path));
        }
        return districts;
    }

    private List<Node> sortNodes(List<Node> unorderedNodes) {
        List<Node> sortedNodes = new ArrayList<>();
        Node removed1 = unorderedNodes.remove(0);
        sortedNodes.add(removed1);
        int size = unorderedNodes.size();
        for (int i = size - 1; i >= 0; i--) {
            Node node = sortedNodes.get(sortedNodes.size() - 1);
            int nearestNode = findNearestNode(node, unorderedNodes);
            Node removedTemp = unorderedNodes.remove(nearestNode);
            if (!(Haversine.distance(node.getPosition().getLatitude(), node.getPosition().getLongitude(), removedTemp.getPosition().getLatitude(), removedTemp.getPosition().getLongitude()) > 2 * Haversine.distance(node.getPosition().getLatitude(), node.getPosition().getLongitude(), removed1.getPosition().getLatitude(), removed1.getPosition().getLongitude()) && i < size - 100)) {
                sortedNodes.add(removedTemp);
            }
        }
        return sortedNodes;
    }
}
