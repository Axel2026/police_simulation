package entities;

public interface IEvent {
    void updateState();

    boolean isActive();
}
