package com.lorbeer.randomland.rules;

import com.lorbeer.randomland.generator.PopulationGenerator;
import com.lorbeer.randomland.generator.domain.Node;
import com.lorbeer.randomland.generator.domain.RoadType;
import com.lorbeer.randomland.generator.domain.SuggestedNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.locationtech.jts.math.Vector2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@ApplicationScoped
public class Rules {
    @Inject
    PopulationGenerator populationGenerator;

    @ConfigProperty(name = "highway.angleMin")
    double highwayAngleMin;
    @ConfigProperty(name = "highway.angleMax")
    double highwayAngleMax;
    @ConfigProperty(name = "highway.chance")
    double highwayChance;
    @ConfigProperty(name = "highway.length")
    double highwayLength;

    @ConfigProperty(name = "street.angleMin")
    double streetAngleMin;
    @ConfigProperty(name = "street.angleMax")
    double streetAngleMax;
    @ConfigProperty(name = "street.chance")
    double streetChance;
    @ConfigProperty(name = "street.length")
    double streetLength;

    final Random rnd = new Random();

    public List<SuggestedNode> suggestNodeRegardingGlobalRules(SuggestedNode source, Node parent) {

        final List<SuggestedNode> suggestions = new ArrayList<>();
        final double angleMin = source.getRoadType().equals(RoadType.HIGHWAY) ? highwayAngleMin : streetAngleMin;
        final double angleMax = source.getRoadType().equals(RoadType.HIGHWAY) ? highwayAngleMax : streetAngleMax;
        final double chanceForNewRoad = source.getRoadType().equals(RoadType.HIGHWAY) ? highwayChance : streetChance;
        final double length = source.getRoadType().equals(RoadType.HIGHWAY) ? highwayLength : streetLength;

        final float populationDensity = populationGenerator.getPopulationAtPosition(source.getPosition().getX(), source.getPosition().getY());

        final double angleVariation = scale(populationDensity, 0.0, 1.0, angleMin, angleMax);
        final double angleOffset = populationDensity / angleVariation;

        final double currentAngle = source.getDirectionAngle();
        final double[] gridAngles = {-Math.PI / 2, 0, Math.PI};

        // left, forward, right
        for (int i = 0; i < 3; i++) {
            if (rnd.nextDouble() < chanceForNewRoad) {
                final double suggestedAngle = gridAngles[i] + currentAngle + angleOffset;
                final double suggestedX = source.getPosition().getX() + (Math.cos(suggestedAngle) * length);
                final double suggestedY = source.getPosition().getY() + (Math.sin(suggestedAngle) * length);

                suggestions.add(new SuggestedNode(new Vector2D(suggestedX, suggestedY), source.getRoadType(), parent));
            }
        }
        return suggestions;
    }

    private double scale(final double valueIn, final double baseMin, final double baseMax,
                         final double limitMin, final double limitMax) {
        return ((limitMax - limitMin) * (valueIn - baseMin) / (baseMax - baseMin)) + limitMin;
    }
}
