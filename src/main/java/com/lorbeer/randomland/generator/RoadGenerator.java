package com.lorbeer.randomland.generator;

import com.lorbeer.randomland.exception.NodeTreeException;
import com.lorbeer.randomland.generator.domain.Node;
import com.lorbeer.randomland.generator.domain.NodeTree;
import com.lorbeer.randomland.generator.domain.RoadType;
import com.lorbeer.randomland.generator.domain.SuggestedNode;
import com.lorbeer.randomland.rules.Rules;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.locationtech.jts.math.Vector2D;

import java.util.*;

@ApplicationScoped
public class RoadGenerator {
    private static final Logger Log = Logger.getLogger(RoadGenerator.class);

    NodeTree nodeTree;

    @Inject
    Rules rules;

    @ConfigProperty(name = "city.repetitions")
    int repetitions;

    @ConfigProperty(name = "city.minimumRoadCount")
    int minRoadCount;

    @ConfigProperty(name = "highway.length")
    double highwayLength;

    @ConfigProperty(name = "highway.mergeThreshold")
    double highwayMergeThreshold;

    @ConfigProperty(name = "street.mergeThreshold")
    double streetMergeThreshold;

    @ConfigProperty(name = "highway.subdivideCount")
    Integer highwaySubdivideCount;

    @ConfigProperty(name = "highway.scaleFactor")
    Integer highwayScaleFactor;

    @ConfigProperty(name = "city.width")
    int width;
    @ConfigProperty(name = "city.height")
    int height;

    private final Queue<SuggestedNode> suggestions = new ArrayDeque<>();

    public void startGeneration(String id) throws NodeTreeException {
        nodeTree = new NodeTree(width, height);
        try {
            generate(initHighwaySuggestions());

            nodeTree.scaleAndSubdivide(highwayScaleFactor, highwaySubdivideCount);

            generate(initStreetSuggestions());
        } catch (NodeTreeException e) {
            throw new NodeTreeException(e.getMessage(), id);
        }
    }

    private void generate(List<SuggestedNode> suggestedNodes) throws NodeTreeException {
        suggestions.addAll(suggestedNodes);

        while (!suggestions.isEmpty()) {
            if (suggestions.size() % 100 == 0) {
                Log.info("suggestions " + suggestions.size());
            }
            final SuggestedNode suggestedNode = suggestions.poll();
            if (suggestedNode != null) {
                if (areLocalConstraintsSatisfied(suggestedNode)) {
                    if (!nodeTree.isInRange(suggestedNode.getPosition().getX(), suggestedNode.getPosition().getY())) {
                        continue;
                    }
                    final Node addedNode = nodeTree.addNode(suggestedNode.getPosition(), suggestedNode.getRoadType());
                    nodeTree.addNodeEdge(addedNode, suggestedNode.getParentNode());
                    if (suggestedNode.isSuggestMore()) {
                        suggestions.addAll(suggestNodesRegardingGlobalGoals(suggestedNode, addedNode));
                    }
                }

            }
        }
    }

    private List<SuggestedNode> suggestNodesRegardingGlobalGoals(SuggestedNode suggestedNode, Node parentNode) {
        return rules.suggestNodeRegardingGlobalRules(suggestedNode, parentNode);
    }

    private boolean areLocalConstraintsSatisfied(SuggestedNode suggestedNode) {
        if (!nodeTree.isInRange(suggestedNode.getPosition().getX(), suggestedNode.getPosition().getY())) {
            return false;
        }
        if (nodeTree.hasNode(suggestedNode.getPosition())) {
            return false;
        }

        final double mergeThreshold = suggestedNode.getRoadType().equals(RoadType.HIGHWAY) ? highwayMergeThreshold : streetMergeThreshold;

        final Vector2D neighborToMerge = findNearestNeighbor(suggestedNode, mergeThreshold);

        if (neighborToMerge != null) {
            suggestedNode.setPosition(neighborToMerge);
            suggestedNode.setSuggestMore(false);
        }
        return true;
    }

    private Vector2D findNearestNeighbor(SuggestedNode node, double threshold) {
        final List<Vector2D> neighbors = nodeTree.getNearestNeighbors(4, node.getPosition());
        final TreeMap<Double, Vector2D> possibleNeighbours = new TreeMap<>();
        final double SqThreshold = threshold * threshold;
        for (Vector2D neighbor : neighbors) {
            if (neighbor.equals(node.getPosition()) || neighbor.equals(node.getParentNode().position())) {
                continue;
            }
            final double distance = calculateDistanceSq(node.getPosition(), neighbor);
            if (distance <= SqThreshold) {
                possibleNeighbours.put(distance, neighbor);
            }
        }

        if (!possibleNeighbours.isEmpty()) {
            return possibleNeighbours.firstEntry().getValue();
        }
        return null;
    }

    private double calculateDistanceSq(Vector2D v1, Vector2D v2) {
        final double disX = v2.getX() - v1.getX();
        final double disY = v2.getY() - v1.getY();
        return (disX * disX) + (disY * disY);
    }

    private List<SuggestedNode> initHighwaySuggestions() throws NodeTreeException {
        Log.info("initHighwaySuggestions");
        final Node node = nodeTree.addNode(new Vector2D((double) nodeTree.getWidth() / 2, (double) nodeTree.getHeight() / 2), RoadType.HIGHWAY);
        final SuggestedNode suggestedNode = new SuggestedNode(new Vector2D(node.position().getX(), node.position().getY() + highwayLength), RoadType.HIGHWAY, node);

        final SuggestedNode reverseSuggestedNode = new SuggestedNode(new Vector2D(node.position().getX(), node.position().getY() - highwayLength), RoadType.HIGHWAY, node);

        return List.of(suggestedNode, reverseSuggestedNode);
    }

    private List<SuggestedNode> initStreetSuggestions() {
        Log.info("initStreetSuggestions");
        final List<SuggestedNode> suggestedStreetNodes = new ArrayList<>();

        nodeTree.getNodeEdges().entrySet().stream().filter(road -> road.getKey().roadType().equals(RoadType.HIGHWAY)).forEach(road -> {

            final Node node = road.getKey();
            final Set<Node> neighbors = road.getValue();

            if (neighbors.size() != 1) {
                return;
            }

            final Node neighbor = neighbors.iterator().next();

            double angle = neighbor.position().angle(node.position());
            double length = neighbor.position().distance(node.position());

            angle += Math.PI / 2;

            final double ax = node.position().getX() + (Math.cos(angle) * length);
            final double ay = node.position().getY() + (Math.sin(angle) * length);

            final double bx = node.position().getX() - (Math.cos(angle) * length);
            final double by = node.position().getY() - (Math.sin(angle) * length);

            suggestedStreetNodes.add(new SuggestedNode(new Vector2D(ax, ay), RoadType.STREET, node));
            suggestedStreetNodes.add(new SuggestedNode(new Vector2D(bx, by), RoadType.STREET, node));
        });

        return suggestedStreetNodes;
    }


    public NodeTree getNodeTree() {
        return nodeTree;
    }

    @Override
    public String toString() {
        return "RoadGenerator{" + "nodeTree=" + nodeTree + ", rules=" + rules + ", repetitions=" + repetitions + ", minRoadCount=" + minRoadCount + ", highwayLength=" + highwayLength + ", highwayMergeThreshold=" + highwayMergeThreshold + ", streetMergeThreshold=" + streetMergeThreshold + ", highwaySubdivideCount=" + highwaySubdivideCount + ", highwayScaleFactor=" + highwayScaleFactor + ", suggestions=" + suggestions + '}';
    }
}
