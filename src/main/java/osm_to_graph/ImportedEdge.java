package osm_to_graph;

import org.jgrapht.graph.DefaultEdge;

// the class of our "personalized" edge
public class ImportedEdge extends DefaultEdge {

    private final Long sourceNodeID;
    private final Long targetNodeID;
    private double distance;

    public ImportedEdge(Long sourceID, Long targetID) {
        this.sourceNodeID = sourceID;
        this.targetNodeID = targetID;
    }

    public ImportedEdge(Long source, Long target, double distance) {
        this(source, target);
        this.distance = distance;
    }

    public Long getSourceNodeID() {
        return this.sourceNodeID;
    }

    public Long getTargetNodeID() {
        return this.targetNodeID;
    }

    public double getDistance() {
        return this.distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return this.sourceNodeID + "->" + this.targetNodeID;
    }
}