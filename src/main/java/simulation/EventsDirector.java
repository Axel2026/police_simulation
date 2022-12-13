package simulation;

import world.World;
import entities.District;
import entities.Entity;
import entities.Patrol;
import entities.factories.IncidentFactory;
import utils.DelayedActionWithTargetSimulationTime;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class EventsDirector extends Thread {

    private final World world = World.getInstance();

    private int eventSpawnCounter = 0;

    @Override
    public void run() {
        while (!world.hasSimulationDurationElapsed()) {
            if (!world.isSimulationPaused() && eventSpawnCounter <= (world.getSimulationTimeLong() / 3600)) {
                eventSpawnCounter++;

                for (var district : world.getDistricts()) {
                    generateNewEventsInDistrict(district);
                }

                removeNeutralizedPatrols();

                // Sleep until next full hour in simulation time. (Due to ability to pause the simulation, we can expect that Events Director will wake up early (but not late).
                // In that case, we will check if it is (almost) full hour and if not, then we will put him to sleep.)

                // Director goes to sleep until next full hour of simulation time
                var sleepTime = ((3600 - (world.getSimulationTime() % 3600)) * 1000) / world.getConfig().getTimeRate();
                try {
                    sleep((long) sleepTime, (int) ((sleepTime - (long) sleepTime) * 1000000));
                } catch (InterruptedException e) {
                    // Ignore
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void removeNeutralizedPatrols() {
        var collect = world.getAllEntities()
                .stream()
                .filter(x -> x instanceof Patrol && ((Patrol) x).getState() == Patrol.State.NEUTRALIZED)
                .collect(Collectors.toList());
        for (Entity patrol : collect) {
            World.getInstance().removeEntity(patrol);
        }
        world.setNeutralizedPatrolsTotal(collect.size());
    }

    private void generateNewEventsInDistrict(District district) {
        var numberOfIncidentsInNextHour = ThreadLocalRandom.current().nextInt(world.getConfig().getMaxIncidentForThreatLevel(district.getThreatLevel()) + 1);

        for (var i = 0; i < numberOfIncidentsInNextHour; i++) {
            if (!district.getAllNodesInDistrict().isEmpty()){
                var newEvent = IncidentFactory.createRandomInterventionForDistrict(district);
                var sleepTime = ThreadLocalRandom.current().nextInt(3600);
                new DelayedActionWithTargetSimulationTime(sleepTime, () -> world.addEntity(newEvent)).start();
            }
        }
    }
}
