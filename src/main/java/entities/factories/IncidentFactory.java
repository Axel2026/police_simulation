package entities.factories;

import world.World;
import entities.District;
import entities.Firing;
import entities.Intervention;

import java.util.concurrent.ThreadLocalRandom;

public class IncidentFactory {

    private static final World world = World.getInstance();
    private static final int MIN_EVENT_DURATION = world.getConfig().getMinimumInterventionDuration() * 60; // seconds
    private static final int MAX_EVENT_DURATION = world.getConfig().getMaximumInterventionDuration() * 60; // seconds
    private static final int MIN_FIRING_STRENGTH = world.getConfig().getMinimumFiringStrength() * 60;
    private static final int MAX_FIRING_STRENGTH = world.getConfig().getMaximumFiringStrength() * 60;

    private IncidentFactory(){}

    public static Intervention createRandomInterventionForDistrict(District district) {
        var randomNode = district.getAllNodesInDistrict().get(ThreadLocalRandom.current().nextInt(0, district.getAllNodesInDistrict().size()));
        var latitude = randomNode.getPosition().getLatitude();
        var longitude = randomNode.getPosition().getLongitude();
        var duration = calculateDurationOfIncident(district, MIN_EVENT_DURATION, MAX_EVENT_DURATION + 1);

        // Will change into firing
        if (ThreadLocalRandom.current().nextDouble() < threatLevelToFiringChance(district.getThreatLevel())) {
            var timeToChange = ThreadLocalRandom.current().nextInt(0, duration);
            return new Intervention(latitude, longitude, duration, true, timeToChange, district);
        } else {
            return new Intervention(latitude, longitude, duration, district);
        }
    }

    public static Firing createRandomFiringFromIntervention(Intervention intervention) {
        var strength = calculateDurationOfIncident(intervention.getDistrict(), MIN_FIRING_STRENGTH, MAX_FIRING_STRENGTH + 1);
        var ceil = (int) Math.ceil(strength / (15 * 60.0));
        var numberOfRequiredPatrols = ThreadLocalRandom.current().nextInt(ceil > 4 ? ceil - 3 : 1, ceil + 1);
        strength *= numberOfRequiredPatrols;
        return new Firing(intervention.getLatitude(), intervention.getLongitude(), numberOfRequiredPatrols, strength, intervention.getDistrict());
    }

    private static double threatLevelToFiringChance(District.ThreatLevelEnum threatLevel) {
        return world.getConfig().getFiringChanceForThreatLevel(threatLevel);
    }

    private static int calculateDurationOfIncident(District district, int minDuration, int maxDuration) {
        var threatLevelValue = district.getThreatLevel().value;
        if (threatLevelValue == 1) {
            return ThreadLocalRandom.current().nextInt(minDuration, minDuration + (maxDuration - minDuration) / 2);
        } else if (threatLevelValue == 2) {
            return ThreadLocalRandom.current().nextInt(minDuration + (maxDuration - minDuration) / 4, maxDuration - (maxDuration - minDuration) / 4);
        } else if (threatLevelValue == 3) {
            return ThreadLocalRandom.current().nextInt(minDuration + (maxDuration - minDuration) / 2, maxDuration);
        }
        return (minDuration + (maxDuration - minDuration) / 2);
    }
}
