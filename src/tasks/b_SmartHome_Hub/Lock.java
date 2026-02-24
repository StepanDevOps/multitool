package tasks.b_SmartHome_Hub;

public class Lock extends BaseDevice{
    private boolean isLocked;

    public Lock(String name) {
        super(name);
        this.isLocked = true;
    }

    public void lock() {
        if (!isOn) {
            System.out.println("Ошибка: Сначала включите замок " + name);
            return;
        }
        isLocked = true;
        System.out.println("Замок " + name + " закрыт");
    }

    public void unlock() {
        if (!isOn) {
            System.out.println("Ошибка: Сначала включите замок " + name);
            return;
        }
        isLocked = false;
        System.out.println("Замок " + name + " открыт");
    }

    public boolean isLocked() {
        return isLocked;
    }

    public String getStatus() {
        return "Замок '" + name + "': " + (isOn ? "Вкл" : "Выкл") + (isLocked ? "Закрыт" : "Открыт");
    }
}
