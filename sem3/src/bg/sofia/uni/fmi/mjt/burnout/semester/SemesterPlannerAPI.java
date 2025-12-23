package bg.sofia.uni.fmi.mjt.burnout.semester;


import bg.sofia.uni.fmi.mjt.burnout.exception.CryToStudentsDepartmentException;
import bg.sofia.uni.fmi.mjt.burnout.exception.DisappointmentException;
import bg.sofia.uni.fmi.mjt.burnout.exception.InvalidSubjectRequirementsException;
import bg.sofia.uni.fmi.mjt.burnout.plan.SemesterPlan;
import bg.sofia.uni.fmi.mjt.burnout.subject.UniversitySubject;

public sealed interface SemesterPlannerAPI permits AbstractSemesterPlanner {
    /**
     * Calculates the subject combination for this semester type based on the subjectRequirements.
     *
     * @param semesterPlan the current semester plan needed for the calculation
     * @return the subject list that balances credits, study time, and requirements
     * @throws CryToStudentsDepartmentException    when a student cannot cover his semester credits.
     * @throws IllegalArgumentException            if the semesterPlan is missing or is null
     * @throws InvalidSubjectRequirementsException if the subjectRequirements contain duplicate categories
     */
    UniversitySubject[] calculateSubjectList(SemesterPlan semesterPlan) throws InvalidSubjectRequirementsException;

    /**
     * Calculates the amount of jars grandma will send you
     *
     * @param subjects         the subjects to calculate jar count for
     * @param maximumSlackTime the rest days grandma gave as limit before stopping the jar food deliveries
     * @param semesterDuration the duration of the semester in days
     * @return the number of jars grandma sends that are needed for survival
     * @throws IllegalArgumentException if the subjects are missing or null, or maximumSlackTime/semesterDuration are not positive integers
     * @throws DisappointmentException  if you cannot make grandma happy.
     */
    int calculateJarCount(UniversitySubject[] subjects, int maximumSlackTime, int semesterDuration);
}


