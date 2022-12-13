package entities;

public interface IAgent {
    void updateStateSelf() throws IllegalStateException;

    void performAction() throws IllegalStateException;

    void takeOrder(Patrol.Action action);
}
