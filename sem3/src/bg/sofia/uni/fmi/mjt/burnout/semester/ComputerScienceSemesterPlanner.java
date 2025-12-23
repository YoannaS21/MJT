package bg.sofia.uni.fmi.mjt.burnout.semester;

import bg.sofia.uni.fmi.mjt.burnout.exception.CryToStudentsDepartmentException;
import bg.sofia.uni.fmi.mjt.burnout.exception.InvalidSubjectRequirementsException;
import bg.sofia.uni.fmi.mjt.burnout.plan.SemesterPlan;
import bg.sofia.uni.fmi.mjt.burnout.subject.UniversitySubject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ComputerScienceSemesterPlanner extends AbstractSemesterPlanner {
    @Override
    public UniversitySubject[] calculateSubjectList(SemesterPlan semesterPlan) throws InvalidSubjectRequirementsException {
        if (semesterPlan == null) {
            throw new IllegalArgumentException("Semester plan is not valid.");
        }

        checkRequirementsForDupCategories(semesterPlan.subjectRequirements());
        checkForEnoughSubjectsPerCategory(semesterPlan.subjectRequirements(), semesterPlan.subjects());
        checkForEnoughCredits(semesterPlan.subjects(), semesterPlan.minimalAmountOfCredits());

        UniversitySubject[] allSubjects = semesterPlan.subjects();
        int minCredits = semesterPlan.minimalAmountOfCredits();

        UniversitySubject[] sorted = Arrays.copyOf(allSubjects, allSubjects.length);
        Arrays.sort(sorted, new RatingDescendingComparator());

        List<UniversitySubject> chosen = new ArrayList<>();
        int totalCredits = 0;

        for (UniversitySubject subject : sorted) {
            chosen.add(subject);
            totalCredits += subject.credits();

            if (totalCredits >= minCredits) {
                break;
            }
        }
        return chosen.toArray(new UniversitySubject[0]);
    }
}


