package com.lorbeer.randomland.generator.domain;

import org.locationtech.jts.math.Vector2D;

public class SuggestedNode {
    private Vector2D position;
    private RoadType roadType;
    private Node parentNode;
    private boolean suggestMore = true;

    public SuggestedNode(Vector2D position, RoadType roadType, Node parentNode) {
        this.position = position;
        this.roadType = roadType;
        this.parentNode = parentNode;
    }

    public Vector2D getPosition() {
        return position;
    }

    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public RoadType getRoadType() {
        return roadType;
    }

    public void setRoadType(RoadType roadType) {
        this.roadType = roadType;
    }

    public Node getParentNode() {
        return parentNode;
    }

    public void setParentNode(Node parentNode) {
        this.parentNode = parentNode;
    }

    public boolean isSuggestMore() {
        return suggestMore;
    }

    public void setSuggestMore(boolean suggestMore) {
        this.suggestMore = suggestMore;
    }

    public double getDirectionAngle() {
        return parentNode.position().angle(position);
    }
}
