package Simulation;

import Simulation.entities.Entity;
import Simulation.entities.Headquarters;
import Simulation.entities.Hospital;
import Simulation.entities.IAgent;
import Simulation.entities.SWATHeadquarters;
import Visualisation.Ambulance;
import Visualisation.Patrol;
import Visualisation.SWAT;
import de.westnordost.osmapi.map.data.LatLon;

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

        for (int i = 0; i < world.getConfig().getNumberOfAmbulances(); i++) {
            var hospital = world.getAllEntities().stream().filter(Hospital.class::isInstance).findFirst().orElse(null);
            if (hospital != null) {
                var newAmbulance = new Ambulance(hospital.getPosition());
                newAmbulance.setState(Ambulance.State.AVAILABLE);
                world.addEntity(newAmbulance);
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
                    hsAssignTasks();
                    updateStatesOfAgents();
                    performAgentsActions();
                    addAgents(world);
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

    private void hsAssignTasks() {
        var allHs = World.getInstance().getAllEntities().stream().filter(Hospital.class::isInstance).map(Hospital.class::cast).collect(Collectors.toList());
        for (var hs : allHs) {
            hs.assignTasksHospital();
        }
    }

    private void updateStatesOfAgents() {
        var allAgents = World.getInstance().getAllEntities().stream().filter(IAgent.class::isInstance).collect(Collectors.toList());
        var allSWATSquads = World.getInstance().getAllEntities().stream().filter(SWAT.class::isInstance).map(SWAT.class::cast).collect(Collectors.toList());
        for (Entity agents : allAgents) {
            ((IAgent) agents).updateStateSelf();
        }

        for (var squad : allSWATSquads) {
            squad.updateStateSelf();
        }
    }

    private void performAgentsActions() {
        var allAgents = World.getInstance().getAllEntities().stream().filter(IAgent.class::isInstance).collect(Collectors.toList());
        var allSWATSquads = World.getInstance().getAllEntities().stream().filter(SWAT.class::isInstance).map(SWAT.class::cast).collect(Collectors.toList());
        for (Entity agents : allAgents) {
            ((IAgent) agents).performAction();
        }

        for (var squad : allSWATSquads) {
            squad.performAction();
        }
    }

    private void addAgents(World world) {
        var allPatrols = world.getAllEntities().stream().filter(Patrol.class::isInstance).map(Patrol.class::cast).collect(Collectors.toList());
        var hq = world.getAllEntities().stream().filter(Headquarters.class::isInstance).findFirst().orElse(null);

        while(allPatrols.stream().filter(x -> x.getState() == Patrol.State.PATROLLING).count() < world.getConfig().getMinimumNumberOfPatrollingUnits()) {
            var newPatrol = new Patrol(hq.getPosition());
            newPatrol.setState(Patrol.State.PATROLLING);
            world.addEntity(newPatrol);
            allPatrols = world.getAllEntities().stream().filter(Patrol.class::isInstance).map(Patrol.class::cast).collect(Collectors.toList());
        }
    }
}
