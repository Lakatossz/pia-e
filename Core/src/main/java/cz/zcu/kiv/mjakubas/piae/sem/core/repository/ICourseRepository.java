package cz.zcu.kiv.mjakubas.piae.sem.core.repository;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Course;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import lombok.NonNull;

import java.util.List;

/**
 * Represents course repository interface for working with {@link Course} data class.
 *
 * @see Workplace
 */

public interface ICourseRepository {

    /**
     * Fetch a course by its id. Throws runtime exception if invalid id is given.
     *
     * @param courseId course id
     * @return fetched {@link Course}
     */
    public Course fetchCourse(long courseId);

    /**
     * Fetch a course by its name. Throws runtime exception if invalid id is given.
     *
     * @param name course name
     * @return fetched {@link Course}
     */
    public Course fetchCourse(String name);

    /**
     * Fetch all courses. Throws runtime exception if error occurs.
     *
     * @return fetched list of {@link Course}
     */
    public List<Course> fetchCourses();

    /**
     * Fetch all course employees. Throws runtime exception if error occurs.
     *
     * @return fetched list of {@link Employee}
     */
    public List<Employee> fetchCourseEmployees(long courseId);

    /**
     * Creates new {@link Course} from given employee data. Given data must contain all class attributes.
     * Throws runtime exception if any problem occurs.
     *
     * @param course given {@link Course} data
     * @return true if workplace was successfully created else returns false
     */
    public boolean createCourse(@NonNull Course course);

    /**
     * Updates existing {@link Course} from given workplace data and workplace id.
     * Given data must contain all class attributes. Throws runtime exception if any problem occurs.
     *
     * @param course   workplace data
     * @param courseId updated workplace id
     * @return true if workplace was successfully updated else returns false
     */
    public boolean updateCourse(@NonNull Course course, long courseId);

    public boolean removeCourse(long courseId);

    /**
     * Adds employee to a course.
     *
     * @param employeeId employee id
     * @param courseId  course id
     * @return true if employee was successfully created.
     */
    public boolean addEmployee(long employeeId, long courseId);

    public boolean removeEmployee(long employeeId, long courseId);
}
