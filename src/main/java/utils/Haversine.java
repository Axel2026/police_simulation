package utils;

import de.westnordost.osmapi.map.data.Node;
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;

// class counting the distance between two nodes
public class Haversine {
    private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

    private Haversine() {
    }

    public static double distance(double startLat, double startLong, double endLat, double endLong) {
        double latDistance = Math.toRadians(endLat - startLat);
        double lonDistance = Math.toRadians(endLong - startLong);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(startLat)) * Math.cos(Math.toRadians(endLat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c * 1000; // convert to meters

        distance = Math.pow(distance, 2);
        return Math.sqrt(distance);
    }

    public static class OwnHeuristics implements AStarAdmissibleHeuristic<Node> {
        @Override
        public double getCostEstimate(Node sourceVertex, Node targetVertex) {
            return Haversine.distance(sourceVertex.getPosition().getLatitude(),
                    sourceVertex.getPosition().getLongitude(),
                    targetVertex.getPosition().getLatitude(),
                    targetVertex.getPosition().getLongitude());
        }
    }
}