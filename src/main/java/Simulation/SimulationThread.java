package Simulation;

import Simulation.entities.Entity;
import Simulation.entities.Headquarters;
import Simulation.entities.IAgent;
import Visualisation.Patrol;
import main.Main;

import java.util.stream.Collectors;

public class SimulationThread extends Thread {

    @Override
    public void run() {
        var world = World.getInstance();
        World.getInstance().simulationStart();

        for (int i = 0; i < world.getConfig().getNumberOfPolicePatrols(); i++) {
            var hq = world.getAllEntities().stream().filter(Headquarters.class::isInstance).findFirst().orElse(null);
            if (hq != null) {
                System.out.println("Test3");
                var newPatrol = new Patrol(hq.getPosition());
                newPatrol.setState(Patrol.State.PATROLLING);
                world.addEntity(newPatrol);
                Main.getPatrolPanel().addNewPatrolUnit(newPatrol);
            } else {
                try {
                    throw new IllegalStateException("HQ location is not defined");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        while (!world.hasSimulationDurationElapsed() && !world.isSimulationFinished()) {
            if (!world.isSimulationPaused()) {
                try {
                    hqAssignTasks();
                    updateStatesOfAgents();
                    performAgentsActions();
                    //addAgents(world);
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
            System.out.println(((IAgent) agents).getState());
            System.out.println(allAgents.size() + "ABC");
            Main.getPatrolPanel().updatePatrolUnit((Patrol) agents);
        }
    }

    private void addAgents(World world) {
        var allPatrols = world.getAllEntities().stream().filter(Patrol.class::isInstance).map(Patrol.class::cast).collect(Collectors.toList());
        var hq = world.getAllEntities().stream().filter(Headquarters.class::isInstance).findFirst().orElse(null);

        while(allPatrols.stream().filter(x -> x.getState() == Patrol.State.PATROLLING).count() + allPatrols.stream().filter(x -> x.getState() == Patrol.State.CALCULATING_PATH).count() < world.getConfig().getMinimumNumberOfPatrollingUnits()) {
            var newPatrol = new Patrol(hq.getPosition());
            System.out.println("Test4");
            newPatrol.setState(Patrol.State.PATROLLING);
            world.addEntity(newPatrol);
            allPatrols = world.getAllEntities().stream().filter(Patrol.class::isInstance).map(Patrol.class::cast).collect(Collectors.toList());
        }
    }
}
