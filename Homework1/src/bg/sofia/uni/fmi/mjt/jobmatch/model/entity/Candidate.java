package bg.sofia.uni.fmi.mjt.jobmatch.model.entity;

import java.util.Set;

public class Candidate {
    private String name;
    private String email;
    private Set<Skill> skills;
    private Education education;
    private int yearsOfExperience;

    public Candidate(String name, String email, Set<Skill> skills,
                     Education education, int yearsOfExperience) {
        if (name == null || name.isBlank() || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Name or email is either null or blank.");
        }
        if (skills == null || skills.isEmpty()) {
            throw new IllegalArgumentException("Set of skills shouldn't be empty or null.");
        }
        if (yearsOfExperience < 0) {
            throw new IllegalArgumentException("Years of experience can't be negative.");
        }
        this.name = name;
        this.email = email;
        this.skills = skills;
        this.education = education;
        this.yearsOfExperience = yearsOfExperience;
    }

    public String getEmail() {
        return email;
    }

    public Set<Skill> getSkills() {
        return skills;
    }

    public String getName() {
        return name;
    }

    public Education getEducation() {
        return education;
    }

    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    @Override
    public String toString() {
        return "Candidate{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", skills=" + skills +
                ", education=" + education +
                ", yearsOfExperience=" + yearsOfExperience +
                '}';
    }
}
