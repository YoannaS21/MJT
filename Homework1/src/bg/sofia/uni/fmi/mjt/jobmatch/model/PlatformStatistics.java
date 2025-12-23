package bg.sofia.uni.fmi.mjt.jobmatch.model;

public record PlatformStatistics(int totalCandidates, int totalEmployers,
                                 int totalJobPostings, String mostCommonSkillName,
                                 String highestPaidJobTitle) {
    public PlatformStatistics {
        if (totalCandidates < 0 || totalEmployers < 0 || totalJobPostings < 0) {
            throw new IllegalArgumentException("Total candidates, employers or job postings can't be negative.");
        }
        if (mostCommonSkillName.isBlank() || highestPaidJobTitle.isBlank()) {
            throw new IllegalArgumentException("Either skill name or job title string is blank.");
        }
    }
}
