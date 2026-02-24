package tasks.b_SmartHome_Hub;

public class Thermostat extends BaseDevice {
    public int temperature;

    public Thermostat(String name) {
        super(name);
        this.temperature = 20;
    }

    public void setTemperature(int temperature) {
        if (!isOn) {
            System.out.println("Ошибка: Сначала включите термостат " + name);
            return;
        }
        if (temperature< 5 || temperature>35) {
            System.out.println("Ошибка: Температура должна быть от 5 до 35 градусов");
            return;
        }
        this.temperature = temperature;
    }

    public int getTemperature() {
        return temperature;
    }

    public String getStatus() {
        return "Термостат '" + name + "': " + (isOn ? "Вкл" : "Выкл") + ", Температура " + temperature + " градусов";
    }

}
