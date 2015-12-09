package edu.msu.elhazzat.whirpool.routing;

import java.util.List;

public class DijkstraGraph {

    private final List<DijkstraVertex> vertexes;
    private final List<DijkstraEdge> edges;

    public DijkstraGraph(List<DijkstraVertex> vertexes, List<DijkstraEdge> edges) {
        this.vertexes = vertexes;
        this.edges = edges;
    }

    public List<DijkstraVertex> getVertexes() {
        return vertexes;
    }

    public List<DijkstraEdge> getEdges() {
        return edges;
    }

}
