package simulation;

import world.World;
import entities.Entity;
import entities.Headquarters;
import entities.IAgent;
import entities.Patrol;

import java.util.stream.Collectors;

public class SimulationThread extends Thread {

    @Override
    public void run() {
        var world = World.getInstance();
        World.getInstance().simulationStart();

        for (int i = 0; i < world.getConfig().getNumberOfPolicePatrols(); i++) {
            var hq = world.getAllEntities().stream().filter(Headquarters.class::isInstance).findFirst().orElse(null);
            if (hq != null) {
                var newPatrol = new Patrol(hq.getPosition());
                newPatrol.setState(Patrol.State.PATROLLING);
                world.addEntity(newPatrol);
            } else {
                try {
                    throw new IllegalStateException("HQ location is not defined");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        while (!world.hasSimulationDurationElapsed()) {
            if (!world.isSimulationPaused()) {
                try {
                    hqAssignTasks();
                    updateStatesOfAgents();
                    performAgentsActions();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                sleep(40);
            } catch (Exception e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void hqAssignTasks() {
        var allHQs = World.getInstance().getAllEntities().stream().filter(Headquarters.class::isInstance).map(Headquarters.class::cast).collect(Collectors.toList());
        for (var hqs : allHQs) {
            hqs.assignTasks();
        }
    }

    private void updateStatesOfAgents() {
        var allAgents = World.getInstance().getAllEntities().stream().filter(IAgent.class::isInstance).collect(Collectors.toList());
        for (Entity agents : allAgents) {
            ((IAgent) agents).updateStateSelf();
        }
    }

    private void performAgentsActions() {
        var allAgents = World.getInstance().getAllEntities().stream().filter(IAgent.class::isInstance).collect(Collectors.toList());
        for (Entity agents : allAgents) {
            ((IAgent) agents).performAction();
        }
    }
}
