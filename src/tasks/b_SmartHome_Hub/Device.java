package tasks.b_SmartHome_Hub;

public interface Device {
    void turnOn();
    void turnOff();
    String getStatus();
    String getName();
}
