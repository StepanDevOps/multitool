package tasks.b_SmartHome_Hub;

import java.util.ArrayList;
import java.util.List;

public class App {
    public class SmartHome_Hub {
        private List<Device> devices;

        public SmartHome_Hub() {
            devices = new ArrayList<>();
        }

        public void addDevices(Device device) {
            devices.add(device);
            System.out.println("Добавлено устройство: " +  device.getName());
        }

        public void turnOffAll() {
            System.out.println("\n--- Выключаем все устройства ---");
            for (Device device : devices) {
                device.turnOff();
            }
            System.out.println("Все устройства выключены");
        }

        public void showStatus() {
            System.out.println("\n=== СТАТУС СИСТЕМЫ ===");
            if (devices.isEmpty()) {
                System.out.println("Нет добавленных устройств");
                return;
            }
            for (Device device : devices) {
                System.out.println(device.getStatus());
            }
            System.out.println("=======================");
        }

        public Device findDevice(String name) {
            for (Device device : devices) {
                if (device.getName().equalsIgnoreCase(name)) {
                    return device;
                }
            }
            return null;
        }
    }
}
