package edu.msu.elhazzat.whirpool.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Original author:
 * Vogella
 * http://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html
 * Code partially modified from source
 */
public class DijkstraAlgorithm {

    private final List<DijkstraVertex> nodes;
    private final List<DijkstraEdge> edges;
    private Set<DijkstraVertex> settledNodes;
    private Set<DijkstraVertex> unSettledNodes;
    private Map<DijkstraVertex, DijkstraVertex> predecessors;
    private Map<DijkstraVertex, Double> distance;

    public DijkstraAlgorithm(DijkstraGraph graph) {
        // create a copy of the array so that we can operate on this array
        this.nodes = new ArrayList<DijkstraVertex>(graph.getVertexes());
        this.edges = new ArrayList<DijkstraEdge>(graph.getEdges());
    }

    public void execute(DijkstraVertex source) {
        settledNodes = new HashSet<DijkstraVertex>();
        unSettledNodes = new HashSet<DijkstraVertex>();
        distance = new HashMap<DijkstraVertex, Double>();
        predecessors = new HashMap<DijkstraVertex, DijkstraVertex>();
        distance.put(source, 0.0);
        unSettledNodes.add(source);

        //make the starting node the source vertex
        while (unSettledNodes.size() > 0) {
            DijkstraVertex node = getMinimum(unSettledNodes);
            settledNodes.add(node);
            unSettledNodes.remove(node);
            findMinimalDistances(node);
        }
    }

    private void findMinimalDistances(DijkstraVertex node) {
        List<DijkstraVertex> adjacentNodes = getNeighbors(node);
        for (DijkstraVertex target : adjacentNodes) {
            if (getShortestDistance(target) > getShortestDistance(node)
                    + getDistance(node, target)) {
                distance.put(target, getShortestDistance(node)
                        + getDistance(node, target));
                predecessors.put(target, node);
                unSettledNodes.add(target);
            }
        }

    }

    private double getDistance(DijkstraVertex node, DijkstraVertex target) {
        for (DijkstraEdge edge : edges) {
            if (edge.getSource().equals(node)
                    && edge.getDestination().equals(target)) {
                return edge.getWeight();
            }
        }
        throw new RuntimeException("Should not happen");
    }

    private List<DijkstraVertex> getNeighbors(DijkstraVertex node) {
        List<DijkstraVertex> neighbors = new ArrayList<DijkstraVertex>();
        for (DijkstraEdge edge : edges) {
            if (edge.getSource().equals(node)
                    && !isSettled(edge.getDestination())) {
                neighbors.add(edge.getDestination());
            }
        }
        return neighbors;
    }

    private DijkstraVertex getMinimum(Set<DijkstraVertex> vertexes) {
        DijkstraVertex minimum = null;
        for (DijkstraVertex vertex : vertexes) {
            if (minimum == null) {
                minimum = vertex;
            } else {
                if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
                    minimum = vertex;
                }
            }
        }
        return minimum;
    }

    private boolean isSettled(DijkstraVertex vertex) {
        return settledNodes.contains(vertex);
    }

    private double getShortestDistance(DijkstraVertex destination) {
        Double d = distance.get(destination);
        if (d == null) {
            return Integer.MAX_VALUE;
        } else {
            return d;
        }
    }

    /*
     * This method returns the path from the source to the selected target and
     * NULL if no path exists
     */
    public LinkedList<DijkstraVertex> getPath(DijkstraVertex target) {
        LinkedList<DijkstraVertex> path = new LinkedList<DijkstraVertex>();
        DijkstraVertex step = target;
        // check if a path exists
        if (predecessors.get(step) == null) {
            return null;
        }
        path.add(step);
        while (predecessors.get(step) != null) {
            step = predecessors.get(step);
            path.add(step);
        }
        // Put it into the correct order
        Collections.reverse(path);
        return path;
    }
}
