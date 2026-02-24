package tasks.b_SmartHome_Hub;

public class Light extends BaseDevice {
    private int brightness; //яркость (0 - 100%)

    public Light(String name) {
        super(name); // передает значение в родительский контейнер
        this.brightness = 0;
    }

    public void setBrightness(int brightness) {
        if (!isOn) {
            System.out.println("Ошибка, включите лампочку" + name);
            return;
        }
        if (brightness < 0 || brightness > 100) {
            System.out.println("Яркость должна быть от 0 до 100");
            return;
        }
        this.brightness = brightness;
    }

    public String getStatus() {
        return "Лампа '" + name + "': " + (isOn ? "Вкл" : "Выкл") + ", Яркость " + brightness + "%";
    }
}
