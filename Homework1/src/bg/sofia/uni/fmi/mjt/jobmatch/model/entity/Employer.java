package bg.sofia.uni.fmi.mjt.jobmatch.model.entity;

import java.util.Objects;

public record Employer(String companyName, String email) {
    public Employer {
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("Company name is either null or blank.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is either null or blank.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Employer employer = (Employer) o;
        return Objects.equals(email, employer.email) && Objects.equals(companyName, employer.companyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyName, email);
    }

    public String getEmail() {
        return email;

    }
}
