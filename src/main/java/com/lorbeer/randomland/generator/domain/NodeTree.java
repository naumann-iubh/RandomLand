package com.lorbeer.randomland.generator.domain;

import com.jwetherell.algorithms.data_structures.KdTree;
import com.lorbeer.randomland.exception.NodeTreeException;
import org.jboss.logging.Logger;
import org.locationtech.jts.math.Vector2D;

import java.util.*;

import static java.util.stream.Collectors.toList;


public class NodeTree {
    private static final Logger Log = Logger.getLogger(NodeTree.class);

    int width;
    int height;

    private Map<Vector2D, Node> nodes = new HashMap<>();
    private Map<Node, Set<Node>> nodeEdges = new HashMap<>();
    private KdTree<SpatialNode> spatialKdTree = new KdTree<>();

    public NodeTree(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Node addNode(Vector2D point, RoadType roadType) throws NodeTreeException {

        if (!isInRange(point.getX(), point.getY())) {
            throw new NodeTreeException("Node is out of range: " + point.toString(), "");
        }

        Node node = nodes.get(point);
        if (node == null) {
            node = new Node(point, roadType);
            nodes.put(point, node);
            spatialKdTree.add(new SpatialNode(node.position()));
        }
        return node;
    }

    public boolean hasNode(Vector2D point) {
        return nodes.containsKey(point);
    }

    public Map<Vector2D, Node> getNodes() {
        return nodes;
    }

    public void addNodeEdge(Node from, Node to) {
        Set<Node> n = nodeEdges.computeIfAbsent(from, k -> new HashSet<>());
        n.add(to);
    }

    public Map<Node, Set<Node>> getNodeEdges() {
        return nodeEdges;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }


    public boolean isInRange(double x, double y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }


    public void scaleAndSubdivide(int factor, int subdivision) throws NodeTreeException {
        Log.info("scaleAndSubdivide called");
        width *= factor;
        height *= factor;

        Map<Node, Set<Node>> nodeEdgesCopy = new LinkedHashMap<>(nodeEdges);

        nodeEdges.clear();
        nodes.clear();

        for (SpatialNode spatialNode : spatialKdTree) {
            spatialKdTree.remove(spatialNode);
        }

        for (Map.Entry<Node, Set<Node>> entry : nodeEdgesCopy.entrySet()) {
            Node node = entry.getKey();
            Vector2D sourcePoint = Vector2D.create(node.position().getX() * factor, node.position().getY() * factor);

            for (Node neighbor : entry.getValue()) {
                Vector2D sourceNeighbor = Vector2D.create(neighbor.position().getX() * factor, neighbor.position().getY() * factor);
                Vector2D dir = sourceNeighbor.subtract(sourcePoint).normalize();

                double lengthEach = sourcePoint.distance(sourceNeighbor) / subdivision;

                List<Node> newNodes = new ArrayList<>();
                for (int i = 0; i < subdivision; i++) {
                    Vector2D newPoint = dir.multiply(lengthEach * i).add(sourcePoint);
                    Node next = addNode(newPoint, node.roadType());
                    newNodes.add(next);
                }

                for (int i = 0; i < newNodes.size() - 1; i++) {
                    addNodeEdge(newNodes.get(i), newNodes.get(i + 1));
                }

            }
        }
    }

    public List<Vector2D> getNearestNeighbors(int amount, Vector2D point) {
        SpatialNode spatialNode = new SpatialNode(point);
        Collection<SpatialNode> spatialResult = spatialKdTree.nearestNeighbourSearch(amount, spatialNode);
        return spatialResult.stream().map(sr -> new Vector2D(sr.getX(), sr.getY())).collect(toList());
    }

    private static class SpatialNode extends KdTree.XYZPoint {

        public SpatialNode(Vector2D point) {
            super(point.getX(), point.getY());
        }
    }

    @Override
    public String toString() {
        return "NodeTree{" +
                "width=" + width +
                ", height=" + height +
                ", nodes=" + nodes +
                ", nodeEdges=" + nodeEdges +
                ", spatialKdTree=" + spatialKdTree +
                '}';
    }
}
