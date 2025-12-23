package bg.sofia.uni.fmi.mjt.space;

import bg.sofia.uni.fmi.mjt.space.algorithm.Rijndael;
import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import bg.sofia.uni.fmi.mjt.space.exception.TimeFrameMismatchException;
import bg.sofia.uni.fmi.mjt.space.mission.Detail;
import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;
import bg.sofia.uni.fmi.mjt.space.rocket.Rocket;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MJTSpaceScanner implements SpaceScannerAPI {
    private static final int DETAIL_PARTS = 2;
    private static final int FIELD_ID = 0;
    private static final int FIELD_COMPANY = 1;
    private static final int FIELD_LOCATION = 2;
    private static final int FIELD_DATE = 3;
    private static final int FIELD_DETAIL = 4;
    private static final int FIELD_ROCKET_STATUS = 5;
    private static final int FIELD_COST = 6;
    private static final int FIELD_MISSION_STATUS = 7;
    private static final int ROCKET_FIELD_ID = 0;
    private static final int ROCKET_FIELD_NAME = 1;
    private static final int ROCKET_FIELD_WIKI = 2;
    private static final int ROCKET_FIELD_HEIGHT = 3;
    private static final int MAX_FIELDS_SIZE = 8;
    private static final int MAX_ROCKET_FIELDS_SIZE = 3;
    private final List<Mission> missions;
    private final List<Rocket> rockets;
    final Rijndael rijndael;

    public MJTSpaceScanner(Reader missionsReader, Reader rocketsReader, SecretKey secretKey) {
        this.missions = parseMissions(missionsReader);
        this.rockets = parseRockets(rocketsReader);
        this.rijndael = new Rijndael(secretKey);
    }

    private List<Mission> parseMissions(Reader reader) {
        List<Mission> result = new ArrayList<>();
        BufferedReader buffReader = new BufferedReader(reader);
        try {
            buffReader.readLine();
            String line;
            while ((line = buffReader.readLine()) != null) {
                Mission mission = parseMissionLine(line);
                if (mission != null) {
                    result.add(mission);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error parsing missions", e);
        }
        return result;
    }

    private List<String> parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields;
    }

    private LocalDate parseDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd, yyyy", Locale.ENGLISH);
        return LocalDate.parse(dateStr, formatter);
    }

    private Detail parseDetail(String detailStr) {
        String[] parts = detailStr.split("\\|", DETAIL_PARTS);
        String rocketName = parts[0].trim();
        String payload = parts.length > 1 ? parts[1].trim() : "";
        return new Detail(rocketName, payload);
    }

    private RocketStatus parseRocketStatus(String status) {
        status = status.trim();
        return status.equals("StatusRetired") ? RocketStatus.STATUS_RETIRED : RocketStatus.STATUS_ACTIVE;
    }

    private Optional<Double> parseCost(String costStr) {
        if (costStr == null || costStr.isBlank()) {
            return Optional.empty();
        }

        String numericPart = costStr.replaceAll("[,\\s]", "");

        try {
            return Optional.of(Double.parseDouble(numericPart));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private MissionStatus parseMissionStatus(String status) {
        return switch (status) {
            case "Success" -> MissionStatus.SUCCESS;
            case "Failure" -> MissionStatus.FAILURE;
            case "Partial Failure" -> MissionStatus.PARTIAL_FAILURE;
            case "Prelaunch Failure" -> MissionStatus.PRELAUNCH_FAILURE;
            default -> MissionStatus.FAILURE;
        };
    }

    private Mission parseMissionLine(String line) {
        List<String> fields = parseCSVLine(line);
        if (fields.size() < MAX_FIELDS_SIZE) return null;

        try {
            String id = fields.get(FIELD_ID).trim();
            String company = fields.get(FIELD_COMPANY).trim();
            String location = fields.get(FIELD_LOCATION).trim();
            LocalDate date = parseDate(fields.get(FIELD_DATE).trim());
            Detail detail = parseDetail(fields.get(FIELD_DETAIL).trim());
            RocketStatus rocketStatus = parseRocketStatus(fields.get(FIELD_ROCKET_STATUS).trim());
            Optional<Double> cost = parseCost(fields.get(FIELD_COST).trim());
            MissionStatus missionStatus = parseMissionStatus(fields.get(FIELD_MISSION_STATUS).trim());

            return new Mission(id, company, location, date, detail, rocketStatus, cost, missionStatus);
        } catch (Exception e) {
            return null;
        }
    }

    private List<Rocket> parseRockets(Reader reader) {
        List<Rocket> result = new ArrayList<>();
        BufferedReader br = new BufferedReader(reader);
        try {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                Rocket rocket = parseRocketLine(line);
                if (rocket != null) {
                    result.add(rocket);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error parsing rockets", e);
        }
        return result;
    }

    private Rocket parseRocketLine(String line) {
        List<String> fields = parseCSVLine(line);
        if (fields.size() < MAX_ROCKET_FIELDS_SIZE) return null;

        try {
            String id = fields.get(ROCKET_FIELD_ID).trim();
            String name = fields.get(ROCKET_FIELD_NAME).trim();
            Optional<String> wiki;
            if (!fields.get(ROCKET_FIELD_WIKI).trim().isEmpty()) {
                wiki = Optional.of(fields.get(ROCKET_FIELD_WIKI).trim());
            } else {
                wiki = Optional.empty();
            }

            Optional<Double> height;
            if (!fields.get(ROCKET_FIELD_HEIGHT).trim().isEmpty()) {
                height = parseHeight(fields.get(ROCKET_FIELD_HEIGHT).trim());
            } else {
                height = Optional.empty();
            }

            return new Rocket(id, name, wiki, height);
        } catch (Exception e) {
            return null;
        }
    }

    private Optional<Double> parseHeight(String heightStr) {
        if (heightStr == null || heightStr.trim().isEmpty()) {
            return Optional.empty();
        }
        String numericPart = heightStr.trim().replaceAll("m", "").trim();
        try {
            return Optional.of(Double.parseDouble(numericPart));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Override
    public Collection<Mission> getAllMissions() {
        if (missions == null || missions.isEmpty()) {
            return Collections.emptyList();
        }
        return missions.stream()
            .filter(mission -> mission != null)
            .toList();
    }

    /**
     * Returns all missions in the dataset with a given status.
     * If there are no missions, return an empty collection.
     *
     * @param missionStatus the status of the missions
     * @throws IllegalArgumentException if missionStatus is null
     */

    @Override
    public Collection<Mission> getAllMissions(MissionStatus missionStatus) {
        if (missionStatus == null) {
            throw new IllegalArgumentException("MissionStatus  is null");
        }
        if (missions == null || missions.isEmpty()) {
            return Collections.emptyList();
        }
        return missions.stream()
            .filter(mission -> mission != null)
            .filter(mission -> mission.missionStatus() == missionStatus)
            .toList();
    }

    /**
     * Returns the company with the most successful missions in a given time period.
     * Success is defined as MissionStatus.SUCCESS.
     * If multiple companies have the same number of successful missions, return any of them.
     * If there are no successful missions in the period, return an empty string.
     * If there are no missions at all, return an empty string.
     *
     * @param from the inclusive beginning of the time frame
     * @param to   the inclusive end of the time frame
     * @throws IllegalArgumentException   if from or to is null
     * @throws TimeFrameMismatchException if to is before from
     */
    @Override
    public String getCompanyWithMostSuccessfulMissions(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("From and to can't be null");
        }
        if (to.isBefore(from)) {
            throw new TimeFrameMismatchException("To can't be before from.");
        }
        Map<String, Long> companySuccess = missions.stream()
            .filter(mission -> mission != null)
            .filter(mission -> !mission.date().isBefore(from) && !mission.date().isAfter(to))
            .filter(mission -> mission.missionStatus() == MissionStatus.SUCCESS)
            .collect(Collectors.groupingBy(Mission::company, Collectors.counting()));

        return companySuccess.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("");
    }

    /**
     * Groups missions by country.
     * If there are no missions, return an empty map.
     */
    @Override
    public Map<String, Collection<Mission>> getMissionsPerCountry() {
        if (missions == null || missions.isEmpty()) {
            return Collections.emptyMap();
        }
        return missions.stream()
            .filter(m -> m != null)
            .collect(Collectors.groupingBy(
                m -> m.location().substring(m.location().lastIndexOf(",") + 1).trim(),
                Collectors.toCollection(ArrayList::new)
            ));
    }

    /**
     * Returns the top N least expensive missions, ordered from cheapest to more expensive.
     * If there are no missions, return an empty list.
     *
     * @param n             the number of missions to be returned
     * @param missionStatus the status of the missions
     * @param rocketStatus  the status of the rockets
     * @throws IllegalArgumentException if n is less than or equal to 0, missionStatus or rocketStatus is null
     */
    @Override
    public List<Mission> getTopNLeastExpensiveMissions(int n, MissionStatus missionStatus, RocketStatus rocketStatus) {
        if (n <= 0) {
            throw new IllegalArgumentException("N must be positive");
        }
        if (missionStatus == null || rocketStatus == null) {
            throw new IllegalArgumentException("Statuses can't be null");
        }
        if (missions == null || missions.isEmpty()) {
            return Collections.emptyList();
        }

        return missions.stream()
            .filter(mission -> mission != null)
            .filter(mission -> mission.missionStatus() == missionStatus)
            .filter(mission -> mission.rocketStatus() == rocketStatus)
            .filter(mission -> mission.cost().isPresent())
            .sorted(Comparator.comparing(mission -> mission.cost().get()))
            .limit(n)
            .toList();
    }

    /**
     * Returns the most desired location for each company.
     * Most desired = location with the highest number of missions for that company.
     * Location is defined as the value in the "Location" column (e.g., "Kennedy Space Center, FL, USA").
     * If a company has multiple locations with the same count, return any of them.
     * If there are no missions, return an empty map.
     *
     * @return a map where keys are company names and values are their most used mission locations
     */
    @Override
    public Map<String, String> getMostDesiredLocationForMissionsPerCompany() {
        if (missions == null || missions.isEmpty()) {
            return Collections.emptyMap();
        }
        return missions.stream()
            .filter(m -> m != null)
            .collect(Collectors.groupingBy(
                Mission::company,
                Collectors.collectingAndThen(
                    Collectors.groupingBy(
                        Mission::location,
                        Collectors.counting()
                    ),
                    locationCounts -> locationCounts.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("")
                )
            ));
    }

    /**
     * Returns the location with most successful missions for each company in a given time period.
     * Successful = MissionStatus.SUCCESS.
     * For each company, finds the location where that company had the most successful missions.
     * If a company has multiple locations with the same count of successful missions, return any of them.
     * If a company has no successful missions in the period, it is NOT included in the result.
     * If there are no missions at all, return an empty map.
     *
     * @param from the inclusive beginning of the time frame (inclusive)
     * @param to   the inclusive end of the time frame (inclusive)
     * @return a map where keys are company names and values are their locations with most successful missions
     * in the period
     * @throws IllegalArgumentException   if from or to is null
     * @throws TimeFrameMismatchException if to is before from
     */
    @Override
    public Map<String, String> getLocationWithMostSuccessfulMissionsPerCompany(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("From and to can't be null");
        }
        if (to.isBefore(from)) {
            throw new TimeFrameMismatchException("To can't be before from.");
        }
        return missions.stream()
            .filter(mission -> mission != null)
            .filter(mission -> mission.missionStatus() == MissionStatus.SUCCESS)
            .filter(mission -> !mission.date().isBefore(from) && !mission.date().isAfter(to))
            .collect(Collectors.groupingBy(
                Mission::company,
                Collectors.collectingAndThen(
                    Collectors.groupingBy(Mission::location, Collectors.counting()),
                    locations -> locations.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("")
                )
            ));
    }

    @Override
    public Collection<Rocket> getAllRockets() {
        if (rockets == null || rockets.isEmpty()) {
            return Collections.emptyList();
        }
        return rockets.stream()
            .filter(rocket -> rocket != null)
            .toList();
    }

    /**
     * Returns the top N tallest rockets, in decreasing order.
     * If there are no rockets, return an empty list.
     *
     * @param n the number of rockets to be returned
     * @throws IllegalArgumentException if n is less than or equal to 0
     */
    @Override
    public List<Rocket> getTopNTallestRockets(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("N must be positive");
        }
        if (rockets == null || rockets.isEmpty()) {
            return Collections.emptyList();
        }
        return rockets.stream()
            .filter(r -> r != null && r.height().isPresent())
            .sorted(Comparator.comparing((Rocket r) -> r.height().get()).reversed())
            .limit(n)
            .toList();
    }

    /**
     * Returns a mapping of rockets (by name) to their respective wiki page (if present).
     * If there are no rockets, return an empty map.
     */
    @Override
    public Map<String, Optional<String>> getWikiPageForRocket() {
        if (rockets == null || rockets.isEmpty()) {
            return Collections.emptyMap();
        }
        return rockets.stream()
            .filter(rocket -> rocket != null)
            .collect(Collectors.toMap(
                Rocket::name,
                Rocket::wiki,
                (wiki1, wiki2) -> wiki1.isPresent() ? wiki1 : wiki2
            ));
    }

    /**
     * Returns the wiki pages for the rockets used in the N most expensive missions.
     * If there are no missions, return an empty list.
     *
     * @param n             the number of missions to be returned
     * @param missionStatus the status of the missions
     * @param rocketStatus  the status of the rockets
     * @throws IllegalArgumentException if n is less than or equal to 0, or missionStatus or rocketStatus is null
     */
    @Override
    public List<String> getWikiPagesForRocketsUsedInMostExpensiveMissions(int n, MissionStatus missionStatus,
                                                                          RocketStatus rocketStatus) {
        if (n <= 0) {
            throw new IllegalArgumentException("N must be positive");
        }
        if (missionStatus == null || rocketStatus == null) {
            throw new IllegalArgumentException("Statuses cannot be null");
        }
        if (missions == null || missions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Mission> topMissions = missions.stream()
            .filter(m -> m != null)
            .filter(m -> m.missionStatus() == missionStatus)
            .filter(m -> m.rocketStatus() == rocketStatus)
            .filter(m -> m.cost().isPresent())
            .sorted(Comparator.comparing((Mission m) -> m.cost().get()).reversed())
            .limit(n)
            .toList();

        Map<String, Optional<String>> wikiMap = getWikiPageForRocket();

        return topMissions.stream()
            .map(m -> m.detail().rocketName())
            //.distinct()
            .map(wikiMap::get)
            .filter(opt -> opt != null && opt.isPresent())
            .map(Optional::get)
            .toList();
    }

    /**
     * Saves the name of the most reliable rocket in a given time period in an encrypted format.
     *
     * <p><b>Important:</b> The implementation is expected to wrap {@code outputStream} in a
     * {@link javax.crypto.CipherOutputStream}. Since block ciphers (e.g. AES) write the final block
     * only on {@code close()}, this method <b>must close</b> the stream after writing.
     *
     * @param outputStream the output stream where the encrypted result is written into;
     *                     it will be closed by this method
     * @param from         the inclusive beginning of the time frame
     * @param to           the inclusive end of the time frame
     * @throws IllegalArgumentException   if outputStream, from or to is null
     * @throws CipherException            if the encrypt/decrypt operation cannot be completed successfully
     * @throws TimeFrameMismatchException if to is before from
     */
    @Override
    public void saveMostReliableRocket(OutputStream outputStream, LocalDate from, LocalDate to) throws CipherException {
        if (outputStream == null || from == null || to == null) {
            throw new IllegalArgumentException("Stream, from and to can't be null.");
        }
        if (to.isBefore(from)) {
            throw new TimeFrameMismatchException("To can't be before from.");
        }

        String mostReliableRocketName = getMostReliableRocketName(from, to);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(mostReliableRocketName.getBytes())) {
            rijndael.encrypt(bais, outputStream);
        } catch (Exception e) {
            throw new CipherException("Failed to save the most reliable rocket", e);
        }
    }

    String getMostReliableRocketName(LocalDate from, LocalDate to) {
        Map<String, List<Mission>> missionsByRocket =
            missions.stream()
                .filter(m -> m != null)
                .filter(m -> !m.date().isBefore(from) && !m.date().isAfter(to))
                .collect(Collectors.groupingBy(m -> m.detail().rocketName()));

        return missionsByRocket.entrySet().stream()
            .max(Comparator.comparing(entry -> calculateReliability(entry.getValue())))
            .map(Map.Entry::getKey)
            .orElse("");
    }

    double calculateReliability(List<Mission> missions) {
        int total = missions.size();
        if (total == 0) {
            return 0.0;
        }

        long successCount = missions.stream()
            .filter(m -> m.missionStatus() == MissionStatus.SUCCESS)
            .count();

        return (2.0 * successCount + (total - successCount)) / (2.0 * total);
    }
}
