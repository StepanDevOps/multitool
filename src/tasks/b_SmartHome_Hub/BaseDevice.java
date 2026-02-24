// базовый класс
package tasks.b_SmartHome_Hub;

public abstract class BaseDevice implements Device { // abstract class - класс, который нельзя использовать для создания объеков, но от которого можно наследовать
    protected String name;
    protected boolean isOn;

    public BaseDevice(String name) {
        this.name = name;
        isOn = false;
    }

    public void turnOn() {
        isOn = true;
    }

    public void turnOff() {
        isOn = false;
    }

    public String getName() {
        return name;
    }
}
