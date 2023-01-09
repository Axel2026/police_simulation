package Simulation.entities;

import Visualisation.Patrol;

public interface IAgent {
    void updateStateSelf() throws IllegalStateException;

    void performAction() throws IllegalStateException;

    void takeOrder(Patrol.Action action);

    Patrol.State getState();
}
