package bg.sofia.uni.fmi.mjt.jobmatch;

import bg.sofia.uni.fmi.mjt.jobmatch.api.JobMatchAPI;
import bg.sofia.uni.fmi.mjt.jobmatch.exceptions.CandidateNotFoundException;
import bg.sofia.uni.fmi.mjt.jobmatch.exceptions.JobPostingNotFoundException;
import bg.sofia.uni.fmi.mjt.jobmatch.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.jobmatch.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.jobmatch.matching.CosineSimilarity;
import bg.sofia.uni.fmi.mjt.jobmatch.matching.SimilarityStrategy;
import bg.sofia.uni.fmi.mjt.jobmatch.model.PlatformStatistics;
import bg.sofia.uni.fmi.mjt.jobmatch.model.entity.Candidate;
import bg.sofia.uni.fmi.mjt.jobmatch.model.entity.Employer;
import bg.sofia.uni.fmi.mjt.jobmatch.model.entity.JobPosting;
import bg.sofia.uni.fmi.mjt.jobmatch.model.entity.Skill;
import bg.sofia.uni.fmi.mjt.jobmatch.model.match.CandidateJobMatch;
import bg.sofia.uni.fmi.mjt.jobmatch.model.match.CandidateSimilarityMatch;
import bg.sofia.uni.fmi.mjt.jobmatch.model.match.SkillRecommendation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JobMatch implements JobMatchAPI {
    private final Map<String, Candidate> candidatesByEmail;
    private final Map<String, Employer> employersByEmail;
    private final Map<String, JobPosting> jobPostingsById;

    public JobMatch() {
        this.candidatesByEmail = new HashMap<>();
        this.employersByEmail = new HashMap<>();
        this.jobPostingsById = new HashMap<>();
    }

    /**
     * Registers a new candidate in the system.
     *
     * @param candidate The candidate to register
     * @return The registered candidate (same instance that was passed in)
     * @throws IllegalArgumentException   if candidate is null
     * @throws UserAlreadyExistsException if a candidate with the same email already exists
     */
    @Override
    public Candidate registerCandidate(Candidate candidate) {
        if (candidate == null) {
            throw new IllegalArgumentException("Candidate is null.");
        }
        if (candidatesByEmail.containsKey(candidate.getEmail())) {
            throw new UserAlreadyExistsException("Candidate with the same email already registered.");
        }
        candidatesByEmail.put(candidate.getEmail(), candidate);
        return candidate;
    }

    /**
     * Registers a new employer in the system.
     *
     * @param employer The employer to register
     * @return The registered employer (same instance that was passed in)
     * @throws IllegalArgumentException   if employer is null
     * @throws UserAlreadyExistsException if an employer with the same email already exists
     */
    @Override
    public Employer registerEmployer(Employer employer) {
        if (employer == null) {
            throw new IllegalArgumentException("Employer is null.");
        }
        if (employersByEmail.containsKey(employer.getEmail())) {
            throw new UserAlreadyExistsException("Employer with the same email already registered.");
        }
        employersByEmail.put(employer.getEmail(), employer);
        return employer;
    }

    /**
     * Posts a new job posting in the system.
     *
     * @param jobPosting The job posting to publish
     * @return The published job posting (same instance that was passed in)
     * @throws IllegalArgumentException if jobPosting is null
     * @throws UserNotFoundException    if the employer publishing the job posting is not registered
     */
    @Override
    public JobPosting postJobPosting(JobPosting jobPosting) {
        if (jobPosting == null) {
            throw new IllegalArgumentException("JobPosting is null.");
        }
        if (!employersByEmail.containsKey(jobPosting.getEmployerEmail())) {
            throw new UserNotFoundException("Employer not registered.");
        }
        if (!jobPostingsById.containsKey(jobPosting.getId())) {
            jobPostingsById.put(jobPosting.getId(), jobPosting);
        }
        return jobPosting;
    }

    private List<CandidateJobMatch> fillCandidatesForJobList(String jobPostingId,
                                                             SimilarityStrategy strategy) {
        List<CandidateJobMatch> NBest = new ArrayList<>();
        for (Candidate candidate : candidatesByEmail.values()) {
            double similarityScore = strategy.calculateSimilarity(candidate.getSkills(),
                    jobPostingsById.get(jobPostingId).getRequiredSkills());
            if (Double.compare(similarityScore, 0) == 0) {
                continue;
            }
            CandidateJobMatch candidateJobMatch = new CandidateJobMatch(candidate,
                    jobPostingsById.get(jobPostingId), similarityScore);
            NBest.add(candidateJobMatch);
        }
        return NBest;
    }

    /**
     * Finds the top N candidates that best match a given job posting.
     * Candidates with zero similarity are not included in the result.
     * The matching is based on the similarity between the candidate's skills
     * and the job requirements, calculated using the provided strategy.
     * <p>
     * Results are sorted by:
     * 1. Similarity score in descending order (higher similarity first)
     * 2. If scores are equal, by candidate name in alphabetical order (case-sensitive)
     *
     * @param jobPostingId The ID of the job posting
     * @param limit        The maximum number of candidates to return
     * @param strategy     The similarity calculation strategy to use
     * @return An unmodifiable list of CandidateJobMatch objects, sorted as described above.
     * If there are fewer than 'limit' candidates, return all of them.
     * If there are no candidates with non-zero similarity, return an empty list.
     * @throws IllegalArgumentException    if jobPostingId is null, empty or blank,
     *                                     limit is non-positive, or strategy is null
     * @throws JobPostingNotFoundException if no job posting with this ID exists
     */
    @Override
    public List<CandidateJobMatch> findTopNCandidatesForJob(String jobPostingId, int limit,
                                                            SimilarityStrategy strategy) {
        if (jobPostingId == null || jobPostingId.isBlank()) {
            throw new IllegalArgumentException("JobPostingId is either null or blank.");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit should be positive.");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy can't be null.");
        }
        if (!jobPostingsById.containsKey(jobPostingId)) {
            throw new JobPostingNotFoundException("The jobPostingId does not exists.");
        }

        List<CandidateJobMatch> NBest = fillCandidatesForJobList(jobPostingId, strategy);
        NBest.sort(new DescendingSimilarityNameComparator());
        if (NBest.size() < limit) {
            return Collections.unmodifiableList(NBest);
        }

        return Collections.unmodifiableList(NBest.subList(0, limit));
    }

    private List<CandidateJobMatch> fillJobsForCandidateList(String candidateEmail,
                                                             SimilarityStrategy strategy) {
        List<CandidateJobMatch> NBestJobs = new ArrayList<>();
        for (JobPosting jobPosting : jobPostingsById.values()) {
            double similarityScore = strategy.calculateSimilarity(jobPosting.getRequiredSkills(),
                    candidatesByEmail.get(candidateEmail).getSkills());
            if (Double.compare(similarityScore, 0) == 0) {
                continue;
            }
            CandidateJobMatch candidateJobMatch = new CandidateJobMatch(candidatesByEmail.get(candidateEmail),
                    jobPosting, similarityScore);
            NBestJobs.add(candidateJobMatch);
        }
        return NBestJobs;
    }

    /**
     * Finds the top N job postings that best match a given candidate.
     * Job postings with zero similarity are not included in the result.
     * The matching is based on the similarity between the job requirements and the candidate's skills,
     * calculated using the provided strategy.
     * <p>
     * Results are sorted by:
     * 1. Similarity score in descending order (higher similarity first)
     * 2. If scores are equal, by job title in alphabetical order (case-sensitive)
     *
     * @param candidateEmail The email of the candidate
     * @param limit          The maximum number of jobs to return
     * @param strategy       The similarity calculation strategy to use
     * @return An unmodifiable list of CandidateJobMatch objects, sorted as described above.
     * If there are fewer than 'limit' jobs, return all of them.
     * If there are no jobs with non-zero similarity, return an empty list.
     * @throws IllegalArgumentException   if candidateEmail is null or blank,
     *                                    limit is non-positive, or strategy is null
     * @throws CandidateNotFoundException if no candidate with this email exists
     */
    @Override
    public List<CandidateJobMatch> findTopNJobsForCandidate(String candidateEmail, int limit,
                                                            SimilarityStrategy strategy) {
        if (candidateEmail == null || candidateEmail.isBlank()) {
            throw new IllegalArgumentException("Candidate emails is either null or blank.");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit should be positive.");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy can't be null.");
        }
        if (!candidatesByEmail.containsKey(candidateEmail)) {
            throw new CandidateNotFoundException("Candidate email does not exists.");
        }
        List<CandidateJobMatch> NBestJobs = fillJobsForCandidateList(candidateEmail, strategy);
        NBestJobs.sort(new DescendingSimilarityJobComparator());
        if (NBestJobs.size() < limit) {
            return Collections.unmodifiableList(NBestJobs);
        }

        return Collections.unmodifiableList(NBestJobs.subList(0, limit));
    }

    private List<CandidateSimilarityMatch> fillSimilarCandidates(String candidateEmail,
                                                                 SimilarityStrategy strategy) {
        List<CandidateSimilarityMatch> similarCandidates = new ArrayList<>();
        for (Candidate candidate : candidatesByEmail.values()) {
            if (candidateEmail.equals(candidate.getEmail())) {
                continue;
            }
            double similarityScore = strategy.calculateSimilarity(candidate.getSkills(),
                    candidatesByEmail.get(candidateEmail).getSkills());
            if (Double.compare(similarityScore, 0) == 0) {
                continue;
            }
            CandidateSimilarityMatch candidateJobMatch = new CandidateSimilarityMatch(
                    candidatesByEmail.get(candidateEmail),
                    candidate, similarityScore);
            similarCandidates.add(candidateJobMatch);
        }
        return similarCandidates;
    }

    /**
     * Finds candidates with similar professional profiles based on skills similarity.
     * This is analogous to LinkedIn's "People also viewed" or "People similar to this profile" feature.
     * <p>
     * The method calculates skill similarity between the given candidate and all other candidates
     * using the provided strategy. Results are sorted by:
     * 1. Similarity score in descending order
     * 2. If scores are equal, by candidate name in alphabetical order (case-sensitive)
     * Candidates with zero similarity are not included in the result.
     *
     * @param candidateEmail The email of the candidate
     * @param limit          The maximum number of similar candidates to return
     * @param strategy       The similarity calculation strategy to use
     * @return An unmodifiable list of CandidateSimilarityMatch objects representing similar candidates,
     * sorted as described above. The given candidate is NOT included in the results.
     * If there are fewer than 'limit' similar candidates, return all of them.
     * If there are no other candidates, return an empty list.
     * @throws IllegalArgumentException   if candidateEmail is null or blank,
     *                                    limit is non-positive, or strategy is null
     * @throws CandidateNotFoundException if no candidate with this email exists
     */
    @Override
    public List<CandidateSimilarityMatch> findSimilarCandidates(String candidateEmail, int limit,
                                                                SimilarityStrategy strategy) {
        if (candidateEmail == null || candidateEmail.isBlank()) {
            throw new IllegalArgumentException("Candidate emails is either null or blank.");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit should be positive.");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy can't be null.");
        }
        if (!candidatesByEmail.containsKey(candidateEmail)) {
            throw new CandidateNotFoundException("Candidate email does not exists.");
        }

        List<CandidateSimilarityMatch> similarCandidates = fillSimilarCandidates(candidateEmail, strategy);
        similarCandidates.sort(new DescendingSimilarityMatchComparator());
        if (similarCandidates.size() < limit) {
            return Collections.unmodifiableList(similarCandidates);
        }

        return Collections.unmodifiableList(similarCandidates.subList(0, limit));
    }

    private Map<String, Double> fillImprovementMap(Candidate candidate, Map<String, Integer> maxSkillLevelInSystem) {
        Map<String, Double> improvementMap = new HashMap<>();

        Set<String> candidateSkills = new HashSet<>();
        for (Skill skill : candidate.getSkills()) {
            candidateSkills.add(skill.name());
        }

        for (String skillName : maxSkillLevelInSystem.keySet()) {
            if (candidateSkills.contains(skillName)) {
                continue;
            }

            double totalImprovement = 0.0;

            for (JobPosting jobPosting : jobPostingsById.values()) {
                double oldSimilarity = new CosineSimilarity().calculateSimilarity(candidate.getSkills(),
                        jobPosting.getRequiredSkills());

                Set<Skill> tempSkills = new HashSet<>(candidate.getSkills());
                Skill toAdd = new Skill(skillName, maxSkillLevelInSystem.get(skillName));
                tempSkills.add(toAdd);

                double newSimilarity = new CosineSimilarity().calculateSimilarity(
                        tempSkills, jobPosting.getRequiredSkills());
                double improvement = newSimilarity - oldSimilarity;
                totalImprovement += improvement;
            }
            improvementMap.put(skillName, totalImprovement);
        }
        return improvementMap;
    }


    private List<SkillRecommendation> buildSortedRecommendations(Map<String, Double> improvementMap, int limit) {
        if (improvementMap.isEmpty()) {
            return List.of();
        }

        List<SkillRecommendation> recommendations = new ArrayList<>();
        for (Map.Entry<String, Double> entry : improvementMap.entrySet()) {
            recommendations.add(new SkillRecommendation(entry.getKey(), entry.getValue()));
        }

        recommendations.sort(new SkillRecommendationsComparator());

        if (recommendations.size() > limit) {
            return Collections.unmodifiableList(recommendations.subList(0, limit));
        }

        return Collections.unmodifiableList(recommendations);
    }

    /**
     * Provides intelligent skill recommendations for a candidate to improve their job match scores.
     * <p>
     * This method analyzes ALL job postings in the system.
     * <p>
     * The algorithm works as follows:
     * <p>
     * 1. For each job posting, calculate current similarity score with the candidate
     * 2. For each skill the candidate is MISSING (present in job but not in candidate profile):
     * - Temporarily add that skill to candidate's profile with level equal to the max. required
     * level across all job postings
     * - Recalculate similarity score
     * - Calculate improvement: new_score - old_score
     * 3. Aggregate (sum up) improvements across all job postings for each missing skill
     * 4. Return top N skills ranked by total improvement potential
     * <p>
     * Results are sorted by:
     * 1. Total improvement score in descending order (highest impact first)
     * 2. If improvement scores are equal, by skill name alphabetically (case-sensitive)
     * <p>
     * Example:
     * - Candidate has: {Java:4, Python:3}
     * - Job1 requires: {Java:5, Python:4, AWS:3} - similarity: 0.905
     * - Job2 requires: {Java:4, AWS:4, Docker:3} - similarity: 0.500
     * <p>
     * Missing skills analysis:
     * - Adding AWS:4 to candidate → Job1 similarity becomes 0.972 (improvement: 0.067)
     * - Adding AWS:4 to candidate → Job2 similarity becomes 0.780 (improvement: 0.280)
     * - Total AWS improvement: 0.347
     * <p>
     * - Adding Docker:3 to candidate → Job1 similarity becomes 0.776 (improvement: -0.129)
     * - Adding Docker:3 to candidate → Job2 similarity becomes 0.670 (improvement: 0.170)
     * - Total Docker improvement: 0.041
     * <p>
     * Result: [SkillRecommendation(AWS, 0.347), SkillRecommendation(Docker, 0.041)]
     * <p>
     * IMPLEMENTATION NOTE:
     * The platform's default similarity strategy is Cosine Similarity (considers skill levels).
     *
     * @param candidateEmail The email of the candidate
     * @param limit          The maximum number of skill recommendations to return
     * @return An unmodifiable list of SkillRecommendation objects, sorted as described above.
     * If there are no missing skills across all job postings, return an empty list.
     * If there are fewer than 'limit' missing skills, return all of them.
     * @throws IllegalArgumentException   if candidateEmail is null, empty or blank or limit is non-positive
     * @throws CandidateNotFoundException if no candidate with this email exists
     */
    @Override
    public List<SkillRecommendation> getSkillRecommendationsForCandidate(String candidateEmail, int limit) {
        if (candidateEmail == null || candidateEmail.isBlank()) {
            throw new IllegalArgumentException("Candidate email is either null or blank.");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit should be positive.");
        }
        if (!candidatesByEmail.containsKey(candidateEmail)) {
            throw new IllegalArgumentException("No candidate with this email exists.");
        }

        Candidate candidate = candidatesByEmail.get(candidateEmail);
        if (jobPostingsById.isEmpty()) {
            return List.of();
        }

        Map<String, Integer> maxSkillLevelInSystem = new HashMap<>();
        for (JobPosting jobPosting : jobPostingsById.values()) {
            for (Skill skill : jobPosting.getRequiredSkills()) {
                int level = skill.level();
                maxSkillLevelInSystem.merge(skill.name(), level, Math::max);
            }
        }
        Map<String, Double> improvementMap = fillImprovementMap(candidate, maxSkillLevelInSystem);
        return buildSortedRecommendations(improvementMap, limit);
    }

    private String getMostCommonSkillName() {
        String mostCommonSkillName = null;
        if (!candidatesByEmail.isEmpty()) {
            Map<String, Integer> skillCount = new HashMap<>();
            for (Candidate candidate : candidatesByEmail.values()) {
                for (Skill skill : candidate.getSkills()) {
                    skillCount.merge(skill.name(), 1, Integer::sum);
                }
            }

            int maxCount = 0;
            for (Map.Entry<String, Integer> entry : skillCount.entrySet()) {
                int count = entry.getValue();
                String skillName = entry.getKey();
                if (count > maxCount || (count == maxCount && skillName.compareTo(mostCommonSkillName) < 0)) {
                    maxCount = count;
                    mostCommonSkillName = skillName;
                }
            }
        }
        return mostCommonSkillName;
    }

    private String getHighestPaidJob() {
        String highestPaidJobTitle = null;
        if (!jobPostingsById.isEmpty()) {
            double maxSalary = Double.MIN_VALUE;
            for (JobPosting jobPosting : jobPostingsById.values()) {
                if (jobPosting.getSalary() > maxSalary) {
                    maxSalary = jobPosting.getSalary();
                    highestPaidJobTitle = jobPosting.getTitle();
                } else if (jobPosting.getSalary() == maxSalary) {
                    if (highestPaidJobTitle == null || jobPosting.getTitle().compareTo(highestPaidJobTitle) < 0) {
                        highestPaidJobTitle = jobPosting.getTitle();
                    }
                }
            }
        }
        return highestPaidJobTitle;
    }

    /**
     * Returns comprehensive statistics about the platform.
     * - totalCandidates: the total number of registered candidates
     * - totalEmployers: the total number of registered employers
     * - totalJobPostings: the total number of posted job postings
     * - mostCommonSkillName: the name of the skill that appears most frequently across all candidates.
     * In case of a tie, return the skill name that comes first alphabetically (case-sensitive).
     * If there are no candidates, return null.
     * - highestPaidJobTitle: the title of the job posting with the highest salary.
     * In case of a tie, return the job title that comes first alphabetically (case-sensitive).
     * If there are no job postings, return null.
     *
     * @return A PlatformStatistics object containing various metrics
     */
    @Override
    public PlatformStatistics getPlatformStatistics() {
        int totalCandidates = candidatesByEmail.size();
        int totalEmployers = employersByEmail.size();
        int totalJobPostings = jobPostingsById.size();
        String mostCommonSkillName = getMostCommonSkillName();
        String highestPaidJob = getHighestPaidJob();

        return new PlatformStatistics(totalCandidates, totalEmployers, totalJobPostings,
                mostCommonSkillName, highestPaidJob);
    }

//    public static void main(String[] args) {
//        JobMatch jm = new JobMatch();
//
//        System.out.println("=== REGISTER CANDIDATE ===");
//        Candidate c1 = new Candidate("Pesho", "a@a.a",
//                Set.of(
//                        new Skill("Java", 4),
//                        new Skill("Python", 3)
//                ), Education.MASTERS, 3);
//        jm.registerCandidate(c1);
//
//        Candidate c2 = new Candidate("Gosho", "b@b.b",
//                Set.of(
//                        new Skill("Java", 2),
//                        new Skill("Docker", 3)
//                ), Education.BACHELORS, 1);
//        jm.registerCandidate(c2);
//
//        System.out.println("Registered candidates OK\n");
//
//        System.out.println("=== REGISTER EMPLOYER ===");
//        Employer e1 = new Employer("SoftUni Ltd", "emp@emp.com");
//        jm.registerEmployer(e1);
//        System.out.println("Registered employer OK\n");
//
//        System.out.println("=== POST JOBS ===");
//        JobPosting j1 = new JobPosting(
//                "JOB1",
//                "Software Engineer",
//                "emp@emp.com",
//                Set.of(
//                        new Skill("Java", 5),
//                        new Skill("Python", 4),
//                        new Skill("AWS", 3)
//                ),
//                Education.BACHELORS, 2, 3000
//        );
//        jm.postJobPosting(j1);
//
//        JobPosting j2 = new JobPosting(
//                "JOB2",
//                "DevOps Engineer",
//                "emp@emp.com",
//                Set.of(
//                        new Skill("Java", 5),
//                        new Skill("Python", 4),
//                        new Skill("AWS", 3)
//                ),
//                Education.HIGH_SCHOOL, 0, 2000
//        );
//        jm.postJobPosting(j2);
//
//        System.out.println("Posted jobs OK\n");
//
//        System.out.println("=== TEST findTopNCandidatesForJob ===");
//        List<?> match1 = jm.findTopNCandidatesForJob("JOB1", 5, new CosineSimilarity());
//        System.out.println(match1 + "\n");
//
//        System.out.println("=== TEST findTopNJobsForCandidate ===");
//        List<?> match2 = jm.findTopNJobsForCandidate("a@a.a", 5, new CosineSimilarity());
//        System.out.println(match2 + "\n");
//
//        System.out.println("=== TEST findSimilarCandidates ===");
//        List<?> match3 = jm.findSimilarCandidates("a@a.a", 5, new CosineSimilarity());
//        System.out.println(match3 + "\n");
//
//        System.out.println("=== TEST getSkillRecommendationsForCandidate ===");
//        List<?> rec = jm.getSkillRecommendationsForCandidate("a@a.a", 10);
//        System.out.println(rec + "\n");
//
//        System.out.println("=== TEST getPlatformStatistics ===");
//        System.out.println(jm.getPlatformStatistics());
//
//        * - Candidate has: {Java:4, Python:3}
//     * - Job1 requires: {Java:5, Python:4, AWS:3} - similarity: 0.905
//                * - Job2 requires: {Java:4, AWS:4, Docker:3} - similarity: 0.500
//                * <p>
//                * Missing skills analysis:
//     * - Adding AWS:4 to candidate → Job1 similarity becomes 0.972 (improvement: 0.067)
//     * - Adding AWS:4 to candidate → Job2 similarity becomes 0.780 (improvement: 0.280)
//     * - Total AWS improvement: 0.347
//                * <p>
//                * - Adding Docker:3 to candidate → Job1 similarity becomes 0.776 (improvement: -0.129)
//     * - Adding Docker:3 to candidate → Job2 similarity becomes 0.670 (improvement: 0.170)
//     * - Total Docker improvement: 0.041
//                * <p>
//                * Result: [SkillRecommendation(AWS, 0.347), SkillRecommendation(Docker, 0.041)]
//        Candidate c3 = new Candidate("Candidate1", "c1@gmail.com",
//                Set.of(
//                        new Skill("Java", 4),
//                        new Skill("Python", 3)
//                ), Education.MASTERS, 3);
//        jm.registerCandidate(c3);
//
//        JobPosting j3 = new JobPosting(
//                "JOB3",
//                "DevOps Engineer",
//                "emp@emp.com",
//                Set.of(
//                        new Skill("Java", 5),
//                        new Skill("Python", 4),
//                        new Skill("AWS", 3)
//                ),
//                Education.HIGH_SCHOOL, 0, 2000
//        );
//        jm.postJobPosting(j3);
//
//        JobPosting j4 = new JobPosting(
//                "JOB4",
//                "DevOps Engineer",
//                "emp@emp.com",
//                Set.of(
//                        new Skill("Java", 4),
//                        new Skill("Docker", 3),
//                        new Skill("AWS", 4)
//                ),
//                Education.HIGH_SCHOOL, 0, 2000
//        );
//        jm.postJobPosting(j4);
//
//        JobPosting j5 = new JobPosting(
//                "JOB5",
//                "DevOps Engineer",
//                "emp@emp.com",
//                Set.of(
//                        new Skill("Python", 5),
//                        new Skill("Docker", 2)
//                ),
//                Education.HIGH_SCHOOL, 0, 2000
//        );
//        jm.postJobPosting(j5);
//
//        List<?> rec2 = jm.getSkillRecommendationsForCandidate("c1@gmail.com", 10);
//        System.out.println(rec2 + "\n");
//
//    }
}
