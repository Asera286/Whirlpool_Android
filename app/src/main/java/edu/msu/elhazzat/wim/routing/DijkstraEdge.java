package edu.msu.elhazzat.wim.routing;

public class DijkstraEdge {

    private final String id;
    private final DijkstraVertex source;
    private final DijkstraVertex destination;
    private final double weight;

    public DijkstraEdge(String id, DijkstraVertex source, DijkstraVertex destination) {
        this.id = id;
        this.source = source;
        this.destination = destination;
        weight = Math.sqrt((destination.coords.latitude - source.coords.latitude)*(destination.coords.latitude - source.coords.latitude)
                + (destination.coords.longitude - source.coords.longitude)*(destination.coords.longitude - source.coords.longitude));
    }

    public String getId() {
        return id;
    }
    public DijkstraVertex getDestination() {
        return destination;
    }

    public DijkstraVertex getSource() {
        return source;
    }
    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return source + " " + destination;
    }
}
