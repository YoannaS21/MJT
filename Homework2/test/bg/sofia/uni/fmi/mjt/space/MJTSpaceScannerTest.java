package bg.sofia.uni.fmi.mjt.space;

import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import bg.sofia.uni.fmi.mjt.space.exception.TimeFrameMismatchException;
import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;
import bg.sofia.uni.fmi.mjt.space.rocket.Rocket;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MJTSpaceScannerTest {
    private SecretKey secretKey;

    @BeforeEach
    void setUp() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        secretKey = keyGen.generateKey();
    }

    private Reader createMissionsReader(String data) {
        return new StringReader(
            "ID,Company,Location,Date,Detail,RocketStatus,Cost,MissionStatus" + System.lineSeparator() + data);
    }

    private Reader createRocketsReader(String data) {
        return new StringReader("ID,Name,Wiki,Height" + System.lineSeparator() + data);
    }

    @Test
    void testGetAllMissionsEmpty() {
        MJTSpaceScanner scanner = new MJTSpaceScanner(createMissionsReader(""), createRocketsReader(""), secretKey);
        Collection<Mission> missions = scanner.getAllMissions();
        assertTrue(missions.isEmpty());
    }

    @Test
    void testGetAllMissionsByStatusWithNull() {
        MJTSpaceScanner scanner = new MJTSpaceScanner(
            createMissionsReader(""),
            createRocketsReader(""),
            secretKey
        );

        assertThrows(IllegalArgumentException.class, () -> {
            scanner.getAllMissions(null);
        });
    }

    @Test
    void testGetAllMissionsByStatus() {
        String missionData =
            "M1,SpaceX,KSC,\"Mon Jan 01, 2024\",Falcon|Payload,StatusActive,1000,Success" +
                System.lineSeparator() +
                "M2,NASA,BocaChica,\"Tue Jan 02, 2024\",Apollo|Payload,StatusActive,1500,Success" +
                System.lineSeparator() +
                "M3,BlueOrigin,KSC,\"Wed Jan 03, 2024\",NewShepard|Payload,StatusActive,800,Failure" +
                System.lineSeparator();

        MJTSpaceScanner scanner = new MJTSpaceScanner(
            createMissionsReader(missionData),
            createRocketsReader(""),
            secretKey
        );

        Collection<Mission> successMissions = scanner.getAllMissions(MissionStatus.SUCCESS);
        assertEquals(2, successMissions.size());

        Collection<Mission> failureMissions = scanner.getAllMissions(MissionStatus.FAILURE);
        assertEquals(1, failureMissions.size());
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissions() {
        String missionData =
            "M1,SpaceX,KSC,\"Mon Jan 01, 2024\",Falcon|Payload,StatusActive,1000,Success" +
                System.lineSeparator() +
                "M2,SpaceX,BocaChica,\"Tue Jan 02, 2024\",Apollo|Payload,StatusActive,1500,Success" +
                System.lineSeparator() +
                "M3,BlueOrigin,KSC,\"Wed Jan 03, 2024\",NewShepard|Payload,StatusActive,800,Success" +
                System.lineSeparator();
        MJTSpaceScanner scanner =
            new MJTSpaceScanner(createMissionsReader(missionData), createRocketsReader(""), secretKey);

        String topCompany = scanner.getCompanyWithMostSuccessfulMissions(LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 31));
        assertEquals("SpaceX", topCompany);
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissionsNullDates() {
        MJTSpaceScanner scanner = new MJTSpaceScanner(
            createMissionsReader(""),
            createRocketsReader(""),
            secretKey
        );

        assertThrows(IllegalArgumentException.class, () ->
            scanner.getCompanyWithMostSuccessfulMissions(null, LocalDate.now())
        );

        assertThrows(IllegalArgumentException.class, () ->
            scanner.getCompanyWithMostSuccessfulMissions(LocalDate.now(), null)
        );
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissionsTimeFrameMismatch() {
        MJTSpaceScanner scanner = new MJTSpaceScanner(
            createMissionsReader(""),
            createRocketsReader(""),
            secretKey
        );

        LocalDate from = LocalDate.of(2024, 1, 2);
        LocalDate to = LocalDate.of(2024, 1, 1);

        assertThrows(TimeFrameMismatchException.class, () ->
            scanner.getCompanyWithMostSuccessfulMissions(from, to)
        );
    }

    @Test
    void testGetMissionsPerCountry() {
        String missionData =
            "M1,SpaceX,\"Kennedy Space Center, FL, USA\",\"Mon Jan 01, 2024\",Falcon|Payload,StatusActive,1000,Success" +
                System.lineSeparator() +
                "M2,NASA,\"Vandenberg Space Force Base, CA, USA\",\"Tue Jan 02, 2024\",Delta|Payload,StatusActive,1500,Failure" +
                System.lineSeparator() +
                "M3,Roscosmos,\"Baikonur Cosmodrome, Kazakhstan\",\"Wed Jan 03, 2024\",Soyuz|Payload,StatusActive,800,Success" +
                System.lineSeparator();

        MJTSpaceScanner scanner =
            new MJTSpaceScanner(createMissionsReader(missionData), createRocketsReader(""), secretKey);

        Map<String, Collection<Mission>> missionsPerCountry = scanner.getMissionsPerCountry();

        assertEquals(2, missionsPerCountry.size());

        assertTrue(missionsPerCountry.containsKey("USA"));
        assertTrue(missionsPerCountry.containsKey("Kazakhstan"));

        assertEquals(2, missionsPerCountry.get("USA").size());
        assertEquals(1, missionsPerCountry.get("Kazakhstan").size());
    }


    @Test
    void testGetTopNLeastExpensiveMissions() {
        String missionData =
            "M1,SpaceX,KSC,\"Mon Jan 01, 2024\",Falcon|Payload,StatusActive,1000,Success" + System.lineSeparator() +
                "M2,SpaceX,KSC,\"Tue Jan 02, 2024\",Falcon|Payload,StatusActive,500,Success" + System.lineSeparator();
        MJTSpaceScanner scanner =
            new MJTSpaceScanner(createMissionsReader(missionData), createRocketsReader(""), secretKey);

        List<Mission> topMissions =
            scanner.getTopNLeastExpensiveMissions(1, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE);
        assertEquals(1, topMissions.size());
        assertEquals(500.0, topMissions.get(0).cost().get());
    }

    @Test
    void testGetTopNLeastExpensiveMissionsInvalidN() {
        MJTSpaceScanner scanner = new MJTSpaceScanner(
            createMissionsReader(""),
            createRocketsReader(""),
            secretKey
        );

        assertThrows(IllegalArgumentException.class, () ->
            scanner.getTopNLeastExpensiveMissions(0, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE)
        );

        assertThrows(IllegalArgumentException.class, () ->
            scanner.getTopNLeastExpensiveMissions(-1, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE)
        );
    }

    @Test
    void testGetTopNLeastExpensiveMissionsNullStatuses() {
        MJTSpaceScanner scanner = new MJTSpaceScanner(
            createMissionsReader(""),
            createRocketsReader(""),
            secretKey
        );

        assertThrows(IllegalArgumentException.class, () ->
            scanner.getTopNLeastExpensiveMissions(1, null, RocketStatus.STATUS_ACTIVE)
        );

        assertThrows(IllegalArgumentException.class, () ->
            scanner.getTopNLeastExpensiveMissions(1, MissionStatus.SUCCESS, null)
        );

        assertThrows(IllegalArgumentException.class, () ->
            scanner.getTopNLeastExpensiveMissions(1, null, null)
        );
    }

    @Test
    void testGetMostDesiredLocationForMissionsPerCompany() {
        String ls = System.lineSeparator();

        String missionData =
            "M1,SpaceX,\"Kennedy Space Center, FL, USA\",\"Mon Jan 01, 2024\",Falcon|Payload,StatusActive,1000,Success" +
                ls +
                "M2,SpaceX,\"Kennedy Space Center, FL, USA\",\"Tue Jan 02, 2024\",Falcon|Payload,StatusActive,1100,Success" +
                ls +
                "M3,SpaceX,\"Vandenberg Space Force Base, CA, USA\",\"Wed Jan 03, 2024\",Falcon|Payload,StatusActive,900,Failure" +
                ls +
                "M4,NASA,\"Vandenberg Space Force Base, CA, USA\",\"Thu Jan 04, 2024\",Delta|Payload,StatusActive,1500,Success" +
                ls +
                "M5,NASA,\"Vandenberg Space Force Base, CA, USA\",\"Fri Jan 05, 2024\",Delta|Payload,StatusActive,1600,Success" +
                ls +
                "M6,NASA,\"Kennedy Space Center, FL, USA\",\"Sat Jan 06, 2024\",Delta|Payload,StatusActive,1400,Failure";

        MJTSpaceScanner scanner =
            new MJTSpaceScanner(createMissionsReader(missionData), createRocketsReader(""), secretKey);

        Map<String, String> result = scanner.getMostDesiredLocationForMissionsPerCompany();

        assertEquals(2, result.size());
        assertEquals("Kennedy Space Center, FL, USA", result.get("SpaceX"));
        assertEquals("Vandenberg Space Force Base, CA, USA", result.get("NASA"));
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompany() {
        String ls = System.lineSeparator();

        String missionData =
            "M1,SpaceX,\"Kennedy Space Center, FL, USA\",\"Mon Jan 01, 2024\",Falcon|Payload,StatusActive,1000,Success" +
                ls +
                "M2,SpaceX,\"Kennedy Space Center, FL, USA\",\"Tue Jan 02, 2024\",Falcon|Payload,StatusActive,1100,Success" +
                ls +
                "M3,SpaceX,\"Vandenberg Space Force Base, CA, USA\",\"Wed Jan 03, 2024\",Falcon|Payload,StatusActive,900,Failure" +
                ls +
                "M4,SpaceX,\"Vandenberg Space Force Base, CA, USA\",\"Thu Jan 04, 2024\",Falcon|Payload,StatusActive,1200,Success" +
                ls +
                "M5,NASA,\"Vandenberg Space Force Base, CA, USA\",\"Fri Jan 05, 2024\",Delta|Payload,StatusActive,1500,Success" +
                ls +
                "M6,NASA,\"Vandenberg Space Force Base, CA, USA\",\"Sat Jan 06, 2024\",Delta|Payload,StatusActive,1600,Success" +
                ls +
                "M7,NASA,\"Kennedy Space Center, FL, USA\",\"Sun Jan 07, 2024\",Delta|Payload,StatusActive,1400,Failure";

        MJTSpaceScanner scanner =
            new MJTSpaceScanner(createMissionsReader(missionData), createRocketsReader(""), secretKey);

        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 1, 6);

        Map<String, String> result =
            scanner.getLocationWithMostSuccessfulMissionsPerCompany(from, to);

        assertEquals(2, result.size());
        assertEquals("Kennedy Space Center, FL, USA", result.get("SpaceX"));
        assertEquals("Vandenberg Space Force Base, CA, USA", result.get("NASA"));
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompanyNullDates() {
        MJTSpaceScanner scanner = new MJTSpaceScanner(
            createMissionsReader(""),
            createRocketsReader(""),
            secretKey
        );

        LocalDate validDate = LocalDate.of(2024, 1, 1);
        LocalDate nullDate = null;

        assertThrows(IllegalArgumentException.class, () ->
            scanner.getLocationWithMostSuccessfulMissionsPerCompany(nullDate, validDate)
        );

        assertThrows(IllegalArgumentException.class, () ->
            scanner.getLocationWithMostSuccessfulMissionsPerCompany(validDate, nullDate)
        );

        assertThrows(IllegalArgumentException.class, () ->
            scanner.getLocationWithMostSuccessfulMissionsPerCompany(nullDate, nullDate)
        );
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompanyInvalidPeriod() {
        MJTSpaceScanner scanner = new MJTSpaceScanner(
            createMissionsReader(""),
            createRocketsReader(""),
            secretKey
        );

        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        LocalDate toDateBefore = LocalDate.of(2023, 12, 31);

        assertThrows(TimeFrameMismatchException.class, () ->
            scanner.getLocationWithMostSuccessfulMissionsPerCompany(fromDate, toDateBefore)
        );
    }


    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompanyNoSuccess() {
        String ls = System.lineSeparator();

        String missionData =
            "M1,SpaceX,\"Kennedy Space Center, FL, USA\",\"Mon Jan 01, 2024\",Falcon|Payload,StatusActive,1000,Failure" +
                ls +
                "M2,NASA,\"Vandenberg Space Force Base, CA, USA\",\"Tue Jan 02, 2024\",Delta|Payload,StatusActive,1500,Success";

        MJTSpaceScanner scanner =
            new MJTSpaceScanner(createMissionsReader(missionData), createRocketsReader(""), secretKey);

        Map<String, String> result =
            scanner.getLocationWithMostSuccessfulMissionsPerCompany(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 3)
            );

        assertEquals(1, result.size());
        assertFalse(result.containsKey("SpaceX"));
        assertEquals("Vandenberg Space Force Base, CA, USA", result.get("NASA"));

    }

    @Test
    void testGetAllRockets() {
        String ls = System.lineSeparator();

        String rocketsData =
            "R1,Falcon 9,https://en.wikipedia.org/wiki/Falcon_9,70" + ls +
                "R2,Saturn V,https://en.wikipedia.org/wiki/Saturn_V,110";

        MJTSpaceScanner scanner =
            new MJTSpaceScanner(createMissionsReader(""), createRocketsReader(rocketsData), secretKey);

        Collection<Rocket> rockets = scanner.getAllRockets();

        assertEquals(2, rockets.size());
    }

    @Test
    void testGetTopNTallestRockets() {
        String ls = System.lineSeparator();

        String rocketsData =
            "R1,Falcon 9,https://en.wikipedia.org/wiki/Falcon_9,70" + ls +
                "R2,Saturn V,https://en.wikipedia.org/wiki/Saturn_V,110" + ls +
                "R3,Starship,https://en.wikipedia.org/wiki/SpaceX_Starship,120";

        MJTSpaceScanner scanner =
            new MJTSpaceScanner(createMissionsReader(""), createRocketsReader(rocketsData), secretKey);

        List<Rocket> topRockets = scanner.getTopNTallestRockets(2);

        assertEquals(2, topRockets.size());
        assertEquals(120.0, topRockets.get(0).height().get());
        assertEquals(110.0, topRockets.get(1).height().get());
    }

    @Test
    void testGetTopNTallestRocketsInvalidN() {
        MJTSpaceScanner scanner = new MJTSpaceScanner(
            createMissionsReader(""),
            createRocketsReader(""),
            secretKey
        );

        assertThrows(IllegalArgumentException.class, () ->
            scanner.getTopNTallestRockets(0)
        );

        assertThrows(IllegalArgumentException.class, () ->
            scanner.getTopNTallestRockets(-5)
        );
    }

    @Test
    void testGetTopNTallestRocketsEmptyList() {
        MJTSpaceScanner scanner = new MJTSpaceScanner(
            createMissionsReader(""),
            createRocketsReader(""),
            secretKey
        );

        List<Rocket> rockets = scanner.getTopNTallestRockets(3);
        assertTrue(rockets.isEmpty(), "Expected empty list when no rockets are available");
    }


    @Test
    void testGetTopNTallestRocketsMoreThanAvailable() {
        String ls = System.lineSeparator();

        String rocketsData =
            "R1,Falcon 9,https://en.wikipedia.org/wiki/Falcon_9,70" + ls +
                "R2,Saturn V,https://en.wikipedia.org/wiki/Saturn_V,110";

        MJTSpaceScanner scanner =
            new MJTSpaceScanner(createMissionsReader(""), createRocketsReader(rocketsData), secretKey);

        List<Rocket> topRockets = scanner.getTopNTallestRockets(5);

        assertEquals(2, topRockets.size());
    }

    @Test
    void testGetWikiPageForRocket() {
        String ls = System.lineSeparator();

        String rocketsData =
            "R1,Falcon 9,https://en.wikipedia.org/wiki/Falcon_9,70" + ls +
                "R2,Saturn V,,110" + ls +
                "R3,Starship,https://en.wikipedia.org/wiki/SpaceX_Starship,120" + ls +
                "R4,Falcon 9,https://example.com/duplicate,70";

        MJTSpaceScanner scanner =
            new MJTSpaceScanner(createMissionsReader(""), createRocketsReader(rocketsData), secretKey);

        Map<String, Optional<String>> wikiMap = scanner.getWikiPageForRocket();

        assertEquals(3, wikiMap.size());

        assertTrue(wikiMap.get("Falcon 9").isPresent());
        assertEquals("https://en.wikipedia.org/wiki/Falcon_9", wikiMap.get("Falcon 9").get());

        assertTrue(wikiMap.get("Saturn V").isEmpty());
        assertTrue(wikiMap.get("Starship").isPresent());
        assertEquals("https://en.wikipedia.org/wiki/SpaceX_Starship", wikiMap.get("Starship").get());
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissions() {
        String ls = System.lineSeparator();

        String rocketsData =
            "R1,Falcon 9,https://en.wikipedia.org/wiki/Falcon_9,70" + ls +
                "R2,Starship,https://en.wikipedia.org/wiki/SpaceX_Starship,120" + ls;

        String missionsData =
            "M1,SpaceX,\"KSC, FL, USA\",\"Mon Jan 01, 2024\",Falcon 9|Payload,StatusActive,1000,Success" + ls +
                "M2,SpaceX,\"KSC, FL, USA\",\"Tue Jan 02, 2024\",Starship|Payload,StatusActive,1500,Success" + ls;

        MJTSpaceScanner scanner =
            new MJTSpaceScanner(createMissionsReader(missionsData), createRocketsReader(rocketsData), secretKey);

        List<String> wikiPages = scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(
            2, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE
        );

        assertEquals(2, wikiPages.size());
        assertTrue(wikiPages.contains("https://en.wikipedia.org/wiki/Falcon_9"));
        assertTrue(wikiPages.contains("https://en.wikipedia.org/wiki/SpaceX_Starship"));
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsInvalidN() {
        MJTSpaceScanner scanner = new MJTSpaceScanner(createMissionsReader(""), createRocketsReader(""), secretKey);

        assertThrows(IllegalArgumentException.class, () ->
            scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(0, MissionStatus.SUCCESS,
                RocketStatus.STATUS_ACTIVE)
        );
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsNullStatuses() {
        MJTSpaceScanner scanner = new MJTSpaceScanner(createMissionsReader(""), createRocketsReader(""), secretKey);

        assertThrows(IllegalArgumentException.class, () ->
            scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(1, null, RocketStatus.STATUS_ACTIVE)
        );

        assertThrows(IllegalArgumentException.class, () ->
            scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(1, MissionStatus.SUCCESS, null)
        );
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsEmptyMissions() {
        MJTSpaceScanner scanner = new MJTSpaceScanner(createMissionsReader(""), createRocketsReader(""), secretKey);

        List<String> result = scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(
            1, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE
        );
        assertTrue(result.isEmpty());
    }


    @Test
    void testSaveMostReliableRocket() throws Exception, CipherException {
        String ls = System.lineSeparator();

        String missionsData =
            "M1,SpaceX,\"KSC, FL, USA\",\"Mon Jan 01, 2024\",Falcon 9|Payload,StatusActive,1000,Success" + ls +
                "M2,SpaceX,\"Boca Chica, TX, USA\",\"Mon Jan 02, 2024\",Starship|Payload,StatusActive,1500,Failure" +
                ls +
                "M3,SpaceX,\"KSC, FL, USA\",\"Mon Jan 03, 2024\",Falcon 9|Payload,StatusActive,1200,Success" + ls;

        MJTSpaceScanner scanner =
            new MJTSpaceScanner(createMissionsReader(missionsData), createRocketsReader(""), secretKey);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 1, 31);

        scanner.saveMostReliableRocket(baos, from, to);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ByteArrayOutputStream decrypted = new ByteArrayOutputStream();
        scanner.rijndael.decrypt(bais, decrypted);

        String result = decrypted.toString();

        assertEquals("Falcon 9", result);
    }

    @Test
    void testSaveMostReliableRocketNullStream() {
        MJTSpaceScanner scanner = new MJTSpaceScanner(createMissionsReader(""), createRocketsReader(""), secretKey);
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 1, 2);

        assertThrows(IllegalArgumentException.class, () ->
            scanner.saveMostReliableRocket(null, from, to)
        );
    }

    @Test
    void testSaveMostReliableRocketNullDates() {
        MJTSpaceScanner scanner = new MJTSpaceScanner(createMissionsReader(""), createRocketsReader(""), secretKey);
        OutputStream os = new ByteArrayOutputStream();

        assertThrows(IllegalArgumentException.class, () ->
            scanner.saveMostReliableRocket(os, null, LocalDate.of(2024, 1, 2))
        );

        assertThrows(IllegalArgumentException.class, () ->
            scanner.saveMostReliableRocket(os, LocalDate.of(2024, 1, 1), null)
        );
    }

    @Test
    void testSaveMostReliableRocketTimeFrameMismatch() {
        MJTSpaceScanner scanner = new MJTSpaceScanner(createMissionsReader(""), createRocketsReader(""), secretKey);
        OutputStream os = new ByteArrayOutputStream();
        LocalDate from = LocalDate.of(2024, 1, 2);
        LocalDate to = LocalDate.of(2024, 1, 1);

        assertThrows(TimeFrameMismatchException.class, () ->
            scanner.saveMostReliableRocket(os, from, to)
        );
    }

    @Test
    void testCalculateReliabilityFalcon() {
        String ls = System.lineSeparator();
        String missionsData =
            "M1,SpaceX,\"KSC, FL, USA\",\"Mon Jan 01, 2024\",Falcon 9|Payload,StatusActive,1000,Success" + ls +
                "M2,SpaceX,\"KSC, FL, USA\",\"Tue Jan 02, 2024\",Falcon 9|Payload,StatusActive,1500,Failure";

        MJTSpaceScanner scanner =
            new MJTSpaceScanner(createMissionsReader(missionsData), createRocketsReader(""), secretKey);

        List<Mission> falconMissions = scanner.getAllMissions().stream()
            .filter(m -> m.detail().rocketName().equals("Falcon 9"))
            .toList();

        double falconReliability = scanner.calculateReliability(falconMissions);
        assertEquals(0.75, falconReliability, 1e-6);
    }

    @Test
    void testCalculateReliabilityStarship() {
        String ls = System.lineSeparator();
        String missionsData =
            "M3,NASA,\"Boca Chica, TX, USA\",\"Wed Jan 03, 2024\",Starship|Payload,StatusActive,1200,Success" + ls +
                "M4,NASA,\"Boca Chica, TX, USA\",\"Thu Jan 04, 2024\",Starship|Payload,StatusActive,1300,Success";

        MJTSpaceScanner scanner =
            new MJTSpaceScanner(createMissionsReader(missionsData), createRocketsReader(""), secretKey);

        List<Mission> starshipMissions = scanner.getAllMissions().stream()
            .filter(m -> m.detail().rocketName().equals("Starship"))
            .toList();

        double starshipReliability = scanner.calculateReliability(starshipMissions);
        assertEquals(1.0, starshipReliability, 1e-6);
    }

    @Test
    void testGetMostReliableRocketName() {
        String ls = System.lineSeparator();
        String missionsData =
            "M1,SpaceX,\"KSC, FL, USA\",\"Mon Jan 01, 2024\",Falcon 9|Payload,StatusActive,1000,Success" + ls +
                "M2,SpaceX,\"KSC, FL, USA\",\"Tue Jan 02, 2024\",Falcon 9|Payload,StatusActive,1500,Failure" + ls +
                "M3,NASA,\"Boca Chica, TX, USA\",\"Wed Jan 03, 2024\",Starship|Payload,StatusActive,1200,Success" + ls +
                "M4,NASA,\"Boca Chica, TX, USA\",\"Thu Jan 04, 2024\",Starship|Payload,StatusActive,1300,Success";

        MJTSpaceScanner scanner =
            new MJTSpaceScanner(createMissionsReader(missionsData), createRocketsReader(""), secretKey);

        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 1, 5);

        String mostReliable = scanner.getMostReliableRocketName(from, to);
        assertEquals("Starship", mostReliable);
    }

    @Test
    void testSaveMostReliableRocketThrowsCipherException() throws Exception {
        MJTSpaceScanner scanner = new MJTSpaceScanner(createMissionsReader(""), createRocketsReader(""), secretKey);

        OutputStream failingStream = new OutputStream() {
            @Override
            public void write(int b) {
                throw new RuntimeException("Simulated IO failure");
            }
        };

        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        assertThrows(CipherException.class, () ->
            scanner.saveMostReliableRocket(failingStream, from, to)
        );
    }

}