package bg.sofia.uni.fmi.mjt.jobmatch.model.entity;

import java.util.Set;

public class JobPosting {
    private String id;
    private String title;
    private String employerEmail;
    private Set<Skill> requiredSkills;
    private Education requiredEducation;
    private int requiredYearsOfExperience;
    private double salary;

    public JobPosting(String id, String title, String employerEmail,
                      Set<Skill> requiredSkills, Education requiredEducation,
                      int requiredYearsOfExperience, double salary) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id is either null or blank.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is either null or blank.");
        }
        if (employerEmail == null || employerEmail.isBlank()) {
            throw new IllegalArgumentException("Employer email is either null or blank.");
        }
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            throw new IllegalArgumentException("Required skills set is either null or empty.");
        }
        if (requiredYearsOfExperience < 0 || salary < 0) {
            throw new IllegalArgumentException("Years of experience and salary can't be negative.");
        }
        this.id = id;
        this.title = title;
        this.employerEmail = employerEmail;
        this.requiredSkills = requiredSkills;
        this.requiredEducation = requiredEducation;
        this.requiredYearsOfExperience = requiredYearsOfExperience;
        this.salary = salary;
    }

    public String getEmployerEmail() {
        return employerEmail;
    }

    public String getId() {
        return id;
    }

    public Set<Skill> getRequiredSkills() {
        return requiredSkills;
    }

    public String getTitle() {
        return title;
    }

    public double getSalary() {
        return salary;
    }

    public Education getRequiredEducation() {
        return requiredEducation;
    }

    public int getRequiredYearsOfExperience() {
        return requiredYearsOfExperience;
    }

    @Override
    public String toString() {
        return "JobPosting{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", employerEmail='" + employerEmail + '\'' +
                ", requiredSkills=" + requiredSkills +
                ", requiredEducation=" + requiredEducation +
                ", requiredYearsOfExperience=" + requiredYearsOfExperience +
                ", salary=" + salary +
                '}';
    }
}
