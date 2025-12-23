package resourses;

import bg.sofia.uni.fmi.mjt.space.MJTSpaceScanner;
import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;
import bg.sofia.uni.fmi.mjt.space.rocket.Rocket;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TestMain {
    private static final int AES_KEY_SIZE = 128;
    private static final int YEAR_2010 = 2010;
    private static final int YEAR_2015 = 2015;
    private static final int YEAR_2020 = 2020;
    private static final int MONTH_JANUARY = 1;
    private static final int DAY_FIRST = 1;
    private static final int MONTH_DECEMBER = 12;
    private static final int DAY_LAST = 31;
    private static final int TOP_N_FIVE = 5;
    private static final int TOP_N_THREE = 3;

    public static void main(String[] args) {
        testSpaceScanner();
    }

    private static void testSpaceScanner() {
        try {
            SecretKey secretKey = generateSecretKey();
            MJTSpaceScanner scanner = createScanner(secretKey);

            testAllMissions(scanner);
            testSuccessfulMissions(scanner);
            testTopCompany(scanner);
            testMissionsByCountry(scanner);
            testCheapMissions(scanner);
            testDesiredLocations(scanner);
            testAllRockets(scanner);
            testTallestRockets(scanner);
            testWikiPages(scanner);
            testExpensiveWikis(scanner);
            testReliableRocket(scanner);

            System.out.println("\n✅ Всички тестове завършиха успешно!");

        } catch (CipherException e) {
            System.err.println("❌ Cipher грешка: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Грешка: " + e.getMessage());
        }
    }

    private static SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(AES_KEY_SIZE);
        return keyGenerator.generateKey();
    }

    private static MJTSpaceScanner createScanner(SecretKey secretKey) throws IOException {
        // Абсолютни пътища към CSV файловете
        String missionsPath = "src/resourses/all-missions-from-1957.csv";
        String rocketsPath = "src/resourses/all-rockets-from-1957.csv";

        File missionsFile = new File(missionsPath);
        File rocketsFile = new File(rocketsPath);
        Reader missionsReader = new FileReader(missionsFile);
        Reader rocketsReader = new FileReader(rocketsFile);

        return new MJTSpaceScanner(missionsReader, rocketsReader, secretKey);
    }

    private static void testAllMissions(MJTSpaceScanner scanner) {
        System.out.println("=== Тест 1: Всички мисии ===");
        Collection<Mission> allMissions = scanner.getAllMissions();
        System.out.println("Брой мисии: " + allMissions.size());
        if (!allMissions.isEmpty()) {
            Mission firstMission = allMissions.iterator().next();
            System.out.println("Първа мисия: " + firstMission.company() + " - " + firstMission.date());
        }
    }

    private static void testSuccessfulMissions(MJTSpaceScanner scanner) {
        System.out.println("\n=== Тест 2: Успешни мисии ===");
        Collection<Mission> successfulMissions = scanner.getAllMissions(MissionStatus.SUCCESS);
        System.out.println("Брой успешни мисии: " + successfulMissions.size());
    }

    private static void testTopCompany(MJTSpaceScanner scanner) {
        System.out.println("\n=== Тест 3: Компания с най-много успешни мисии (2010-2020) ===");
        String topCompany = scanner.getCompanyWithMostSuccessfulMissions(
            LocalDate.of(YEAR_2010, MONTH_JANUARY, DAY_FIRST),
            LocalDate.of(YEAR_2020, MONTH_DECEMBER, DAY_LAST)
        );
        System.out.println("Топ компания: " + topCompany);
    }

    private static void testMissionsByCountry(MJTSpaceScanner scanner) {
        System.out.println("\n=== Тест 4: Мисии по държава ===");
        Map<String, Collection<Mission>> missionsByCountry = scanner.getMissionsPerCountry();
        System.out.println("Брой държави: " + missionsByCountry.size());
        missionsByCountry.forEach((country, missions) ->
            System.out.println(country + ": " + missions.size() + " мисии")
        );
    }

    private static void testCheapMissions(MJTSpaceScanner scanner) {
        System.out.println("\n=== Тест 5: Топ 5 най-евтини успешни мисии ===");

        List<Mission> cheapMissions = scanner.getTopNLeastExpensiveMissions(
            TOP_N_FIVE, MissionStatus.SUCCESS, RocketStatus.STATUS_RETIRED
        );
        cheapMissions.forEach(m ->
            System.out.println(m.company() + " - " + m.id() + " - " + m.cost().orElse(0.0) + "M")
        );
    }

    private static void testDesiredLocations(MJTSpaceScanner scanner) {
        System.out.println("\n=== Тест 6: Най-желани локации за компании ===");
        Map<String, String> desiredLocations = scanner.getMostDesiredLocationForMissionsPerCompany();
        desiredLocations.entrySet().stream().limit(TOP_N_FIVE).forEach(entry ->
            System.out.println(entry.getKey() + " -> " + entry.getValue())
        );
    }

    private static void testAllRockets(MJTSpaceScanner scanner) {
        System.out.println("\n=== Тест 7: Всички ракети ===");
        Collection<Rocket> allRockets = scanner.getAllRockets();
        System.out.println("Брой ракети: " + allRockets.size());
    }

    private static void testTallestRockets(MJTSpaceScanner scanner) {
        System.out.println("\n=== Тест 8: Топ 5 най-високи ракети ===");
        List<Rocket> tallestRockets = scanner.getTopNTallestRockets(TOP_N_FIVE);
        tallestRockets.forEach(r ->
            System.out.println(r.name() + " - " + r.height().orElse(0.0) + "m")
        );
    }

    private static void testWikiPages(MJTSpaceScanner scanner) {
        System.out.println("\n=== Тест 9: Wiki страници (първи 3) ===");
        Map<String, Optional<String>> wikiPages = scanner.getWikiPageForRocket();
        wikiPages.entrySet().stream().limit(TOP_N_THREE).forEach(entry ->
            System.out.println(entry.getKey() + " -> " + entry.getValue().orElse("N/A"))
        );
    }

    private static void testExpensiveWikis(MJTSpaceScanner scanner) {
        System.out.println("\n=== Тест 10: Wiki за ракети в топ 3 най-скъпи мисии ===");
        List<String> expensiveWikis = scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(
            TOP_N_THREE, MissionStatus.SUCCESS, RocketStatus.STATUS_RETIRED
        );
        expensiveWikis.forEach(System.out::println);
    }

    private static void testReliableRocket(MJTSpaceScanner scanner) throws CipherException {
        System.out.println("\n=== Тест 11: Най-надеждна ракета (2015-2020) ===");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        scanner.saveMostReliableRocket(
            outputStream,
            LocalDate.of(YEAR_2015, MONTH_JANUARY, DAY_FIRST),
            LocalDate.of(YEAR_2020, MONTH_DECEMBER, DAY_LAST)
        );
        System.out.println("Криптирана дължина: " + outputStream.toByteArray().length + " bytes");
        System.out.println("Криптирано съдържание (Base64): " +
            Base64.getEncoder().encodeToString(outputStream.toByteArray()));
    }
}