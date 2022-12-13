package simulation;

import world.World;
import entities.Incident;

public class EventUpdater extends Thread {

    private final World world = World.getInstance();

    @Override
    public void run() {
        while (!world.hasSimulationDurationElapsed()) {
            if (!world.isSimulationPaused()) {
                var activeEvents = world.getEvents();
                for (var incident : activeEvents) {
                    if (incident.isActive()){
                        incident.updateState();
                    }
                    else {
                        world.removeEntity((Incident)incident);
                    }
                }
            }
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }
}