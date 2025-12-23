package bg.sofia.uni.fmi.mjt.burnout.semester;

import bg.sofia.uni.fmi.mjt.burnout.exception.CryToStudentsDepartmentException;
import bg.sofia.uni.fmi.mjt.burnout.exception.InvalidSubjectRequirementsException;
import bg.sofia.uni.fmi.mjt.burnout.plan.SemesterPlan;
import bg.sofia.uni.fmi.mjt.burnout.subject.SubjectRequirement;
import bg.sofia.uni.fmi.mjt.burnout.subject.UniversitySubject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SoftwareEngineeringSemesterPlanner extends AbstractSemesterPlanner {
    /**
     * Calculates the subject combination for this semester type based on the subjectRequirements.
     *
     * @param semesterPlan the current semester plan needed for the calculation
     * @return the subject list that balances credits, study time, and requirements
     * @throws CryToStudentsDepartmentException    when a student cannot cover his semester credits.
     * @throws IllegalArgumentException            if the semesterPlan is missing or is null
     * @throws InvalidSubjectRequirementsException if the subjectRequirements contain duplicate categories
     */

    @Override
    public UniversitySubject[] calculateSubjectList(SemesterPlan semesterPlan) throws InvalidSubjectRequirementsException {
        if (semesterPlan == null) {
            throw new IllegalArgumentException("Semester plan is not valid.");
        }

        checkRequirementsForDupCategories(semesterPlan.subjectRequirements());
        checkForEnoughSubjectsPerCategory(semesterPlan.subjectRequirements(), semesterPlan.subjects());
        int allCredits = checkForEnoughCredits(semesterPlan.subjects(), semesterPlan.minimalAmountOfCredits());

        if (allCredits == semesterPlan.minimalAmountOfCredits()) {
            return semesterPlan.subjects();
        }

        SubjectRequirement[] requirements = semesterPlan.subjectRequirements();
        UniversitySubject[] allSubjects = semesterPlan.subjects();
        int minCredits = semesterPlan.minimalAmountOfCredits();

        List<UniversitySubject> chosen = new ArrayList<>();

        for (SubjectRequirement requirement : requirements) {
            List<UniversitySubject> subjectsInCategory = new ArrayList<>();

            for (UniversitySubject subject : allSubjects) {
                if (subject.category() == requirement.category()) {
                    subjectsInCategory.add(subject);
                }
            }

            subjectsInCategory.sort(new CreditsDescendingComparator());

            for (int i = 0; i < requirement.minAmountEnrolled(); i++) {
                chosen.add(subjectsInCategory.get(i));
            }
        }

        int totalCredits = 0;
        for (UniversitySubject subject : chosen) {
            totalCredits += subject.credits();
        }

        if (totalCredits < minCredits) {
            UniversitySubject[] sortedAll = Arrays.copyOf(allSubjects, allSubjects.length);
            Arrays.sort(sortedAll, new CreditsDescendingComparator());

            for (UniversitySubject s : sortedAll) {
                if (!chosen.contains(s)) {
                    chosen.add(s);
                    totalCredits += s.credits();

                    if (totalCredits >= minCredits) {
                        break;
                    }
                }
            }
        }

        if (totalCredits < minCredits) {
            throw new CryToStudentsDepartmentException("Cannot reach minimal credits");
        }
        return chosen.toArray(new UniversitySubject[0]);
    }
}
