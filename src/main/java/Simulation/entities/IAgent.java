package Simulation.entities;

import Visualisation.Ambulance;
import Visualisation.Patrol;

public interface IAgent {
    void updateStateSelf() throws IllegalStateException;

    void performAction() throws IllegalStateException;

    void takeOrder(Patrol.Action action);
    void takeOrderAmbulance(Ambulance.Action action);

    Patrol.State getState();
    Ambulance.State getStateAmbulance();
}
