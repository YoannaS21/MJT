package bg.sofia.uni.fmi.mjt.burnout.semester;

import bg.sofia.uni.fmi.mjt.burnout.exception.CryToStudentsDepartmentException;
import bg.sofia.uni.fmi.mjt.burnout.exception.DisappointmentException;
import bg.sofia.uni.fmi.mjt.burnout.exception.InvalidSubjectRequirementsException;
import bg.sofia.uni.fmi.mjt.burnout.subject.SubjectRequirement;
import bg.sofia.uni.fmi.mjt.burnout.subject.UniversitySubject;

import java.util.Arrays;

public abstract sealed class AbstractSemesterPlanner implements SemesterPlannerAPI permits ComputerScienceSemesterPlanner, SoftwareEngineeringSemesterPlanner {
    private int jarCount(int studyDays, int restDays, int semesterDuration) {
        int jarsFromGrandma = studyDays / 5;

        if (semesterDuration < studyDays + restDays) {
            jarsFromGrandma *= 2;
        }

        return jarsFromGrandma;
    }

    @Override
    public int calculateJarCount(UniversitySubject[] subjects, int maximumSlackTime, int semesterDuration) {
        if (subjects == null || subjects.length == 0 || maximumSlackTime <= 0 || semesterDuration <= 0) {
            throw new IllegalArgumentException("Subject, slack time or semester duration is not ok.");
        }
        int studyDays = 0;
        int restDays = 0;

        for (UniversitySubject sub : subjects) {
            studyDays += sub.neededStudyTime();
            double rest = Math.ceil(sub.neededStudyTime() * sub.category().getCoefficient());
            restDays += (int) rest;
        }

        if (restDays > maximumSlackTime) {
            throw new DisappointmentException("You rest too much!!");
        }

        return jarCount(studyDays, restDays, semesterDuration);
    }

    protected void checkRequirementsForDupCategories(SubjectRequirement[] subjectRequirements) throws InvalidSubjectRequirementsException {
        for (int i = 0; i < subjectRequirements.length; i++) {
            for (int j = i + 1; j < subjectRequirements.length; j++) {
                if (subjectRequirements[i].category() == subjectRequirements[j].category()) {
                    throw new InvalidSubjectRequirementsException("Duplicate categories.");
                }
            }
        }
    }

    protected void checkForEnoughSubjectsPerCategory(SubjectRequirement[] subjectRequirements, UniversitySubject[] universitySubjects) {
        for (SubjectRequirement req : subjectRequirements) {
            int countSubjectsPerCategory = 0;
            for (UniversitySubject subject : universitySubjects) {
                if (subject.category() == req.category()) {
                    countSubjectsPerCategory++;
                }
            }

            if (countSubjectsPerCategory < req.minAmountEnrolled()) {
                throw new CryToStudentsDepartmentException("Not enough subjects by category.");
            }
        }
    }

    protected int checkForEnoughCredits(UniversitySubject[] universitySubjects, int minCredits) {
        int allCredits = 0;
        for (UniversitySubject sub : universitySubjects) {
            allCredits += sub.credits();
        }
        if (allCredits < minCredits) {
            throw new CryToStudentsDepartmentException("Total credits are less than the min needed.");
        }
        return allCredits;
    }
}
