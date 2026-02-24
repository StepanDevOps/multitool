package tasks.a_Мышка;

import java.io.*;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        try {
            // объект Scanner для чтения данных из файла input
            // new File() - создает объект файла, который нужно прочитать
            Scanner sc = new Scanner(new File("src/tasks/a_Мышка/input.txt"));
            int W = sc.nextInt();
            int H = sc.nextInt();
            int R = sc.nextInt();
            sc.close(); // закрытие Scanner

            boolean canHide = (2 * R <= W) && (2*R <=H);

            //PrintWriter - класс для записи текста в файл
            PrintWriter writer = new PrintWriter("src/tasks/a_Мышка/output.txt");
            writer.println(canHide ? "Yes" : "No"); // тернарный оператор
            writer.close(); // закрытие файла
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
