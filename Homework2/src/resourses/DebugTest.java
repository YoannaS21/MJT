package resourses;

import bg.sofia.uni.fmi.mjt.space.MJTSpaceScanner;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

public class DebugTest {
    public static void main(String[] args) {
        try {
            // Покажи текущата директория
            System.out.println("Working Directory: " + System.getProperty("user.dir"));

            // Опитай с директен път
            String missionsPath = "src/resourses/all-missions-from-1957.csv";
            String rocketsPath = "src/resourses/all-rockets-from-1957.csv";

            File missionsFile = new File(missionsPath);
            File rocketsFile = new File(rocketsPath);

            System.out.println("\n=== Проверка на файловете ===");
            System.out.println("Missions файл съществува: " + missionsFile.exists());
            System.out.println("Missions път: " + missionsFile.getAbsolutePath());
            System.out.println("Rockets файл съществува: " + rocketsFile.exists());
            System.out.println("Rockets път: " + rocketsFile.getAbsolutePath());

            if (!missionsFile.exists() || !rocketsFile.exists()) {
                System.err.println("\n❌ Файловете не се намират!");
                System.out.println("\nОпитай да промениш пътищата:");
                System.out.println("- src/resourses/all-missions-from-1957.csv");
                System.out.println("- all-missions-from-1957.csv");
                System.out.println("- /пълен/път/към/файла");
                return;
            }

            // Прочети първите редове
            System.out.println("\n=== Първите 3 реда от missions ===");
            try (BufferedReader br = new BufferedReader(new FileReader(missionsFile))) {
                for (int i = 0; i < 3; i++) {
                    String line = br.readLine();
                    if (line == null) break;
                    System.out.println(i + ": " + line);
                }
            }

            System.out.println("\n=== Първите 3 реда от rockets ===");
            try (BufferedReader br = new BufferedReader(new FileReader(rocketsFile))) {
                for (int i = 0; i < 3; i++) {
                    String line = br.readLine();
                    if (line == null) break;
                    System.out.println(i + ": " + line);
                }
            }

            // Създай scanner
            System.out.println("\n=== Създаване на scanner ===");

            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            final int aesKeySize = 128;
            keyGen.init(aesKeySize);
            SecretKey key = keyGen.generateKey();

            Reader missionsReader = new FileReader(missionsFile);
            Reader rocketsReader = new FileReader(rocketsFile);

            MJTSpaceScanner scanner = new MJTSpaceScanner(missionsReader, rocketsReader, key);

            System.out.println("Брой мисии: " + scanner.getAllMissions().size());
            System.out.println("Брой ракети: " + scanner.getAllRockets().size());

            if (!scanner.getAllMissions().isEmpty()) {
                System.out.println("\n✅ Успешно заредени данни!");
            } else {
                System.out.println("\n⚠️ ПРОБЛЕМ: Няма заредени мисии!");
            }

        } catch (Exception e) {
            System.err.println("Грешка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}