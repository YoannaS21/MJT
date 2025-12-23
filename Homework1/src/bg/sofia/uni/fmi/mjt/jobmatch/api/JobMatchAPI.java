package bg.sofia.uni.fmi.mjt.jobmatch.api;

import bg.sofia.uni.fmi.mjt.jobmatch.exceptions.CandidateNotFoundException;
import bg.sofia.uni.fmi.mjt.jobmatch.exceptions.JobPostingNotFoundException;
import bg.sofia.uni.fmi.mjt.jobmatch.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.jobmatch.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.jobmatch.matching.SimilarityStrategy;
import bg.sofia.uni.fmi.mjt.jobmatch.model.entity.Candidate;
import bg.sofia.uni.fmi.mjt.jobmatch.model.match.CandidateJobMatch;
import bg.sofia.uni.fmi.mjt.jobmatch.model.match.CandidateSimilarityMatch;
import bg.sofia.uni.fmi.mjt.jobmatch.model.entity.Employer;
import bg.sofia.uni.fmi.mjt.jobmatch.model.entity.JobPosting;
import bg.sofia.uni.fmi.mjt.jobmatch.model.PlatformStatistics;
import bg.sofia.uni.fmi.mjt.jobmatch.model.match.SkillRecommendation;

import java.util.List;

public interface JobMatchAPI {

    /**
     * Registers a new candidate in the system.
     *
     * @param candidate The candidate to register
     * @return The registered candidate (same instance that was passed in)
     * @throws IllegalArgumentException   if candidate is null
     * @throws UserAlreadyExistsException if a candidate with the same email already exists
     */
    Candidate registerCandidate(Candidate candidate);

    /**
     * Registers a new employer in the system.
     *
     * @param employer The employer to register
     * @return The registered employer (same instance that was passed in)
     * @throws IllegalArgumentException   if employer is null
     * @throws UserAlreadyExistsException if an employer with the same email already exists
     */
    Employer registerEmployer(Employer employer);

    /**
     * Posts a new job posting in the system.
     *
     * @param jobPosting The job posting to publish
     * @return The published job posting (same instance that was passed in)
     * @throws IllegalArgumentException if jobPosting is null
     * @throws UserNotFoundException    if the employer publishing the job posting is not registered
     */
    JobPosting postJobPosting(JobPosting jobPosting);

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
     * @throws IllegalArgumentException    if jobPostingId is null, empty or blank, limit is non-positive, or strategy is null
     * @throws JobPostingNotFoundException if no job posting with this ID exists
     */
    List<CandidateJobMatch> findTopNCandidatesForJob(String jobPostingId, int limit, SimilarityStrategy strategy);

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
     * @throws IllegalArgumentException   if candidateEmail is null or blank, limit is non-positive, or strategy is null
     * @throws CandidateNotFoundException if no candidate with this email exists
     */
    List<CandidateJobMatch> findTopNJobsForCandidate(String candidateEmail, int limit, SimilarityStrategy strategy);

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
     * @throws IllegalArgumentException   if candidateEmail is null or blank, limit is non-positive, or strategy is null
     * @throws CandidateNotFoundException if no candidate with this email exists
     */
    List<CandidateSimilarityMatch> findSimilarCandidates(String candidateEmail, int limit, SimilarityStrategy strategy);

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
    List<SkillRecommendation> getSkillRecommendationsForCandidate(String candidateEmail, int limit);

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
    PlatformStatistics getPlatformStatistics();

}