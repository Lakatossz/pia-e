package cz.zcu.kiv.mjakubas.piae.sem.core.repository;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import lombok.NonNull;

import java.util.List;

/**
 * Represents project repository interface for working with {@link Project} data class.
 *
 * @see Workplace
 */
public interface IProjectRepository {

    /**
     * Fetch a project by its id. Throws runtime exception if invalid id is given.
     *
     * @param projectId project id
     * @return fetched {@link Project}
     */
    public Project fetchProject(long projectId);

    /**
     * Fetch a project by its projectName. Throws runtime exception if invalid id is given.
     *
     * @param projectName project name
     * @return fetched {@link Project}
     */
    public Project fetchProject(String projectName);

    /**
     * Fetch all projects. Throws runtime exception if error occurs.
     *
     * @return fetched list of {@link Project}
     */
    public List<Project> fetchProjects();

    /**
     * Fetch all project employees. Throws runtime exception if error occurs.
     *
     * @return fetched list of {@link Employee}
     */
    public List<Employee> fetchProjectEmployees(long projectId);

    /**
     * Creates new {@link Project} from given employee data. Given data must contain all class attributes.
     * Throws runtime exception if any problem occurs.
     *
     * @param project given {@link Project} data
     * @return true if workplace was successfully created else returns false
     */
    public boolean createProject(@NonNull Project project);

    /**
     * Updates existing {@link Project} from given workplace data and workplace id.
     * Given data must contain all class attributes. Throws runtime exception if any problem occurs.
     *
     * @param project   workplace data
     * @param projectId updated workplace id
     * @return true if workplace was successfully updated else returns false
     */
    public boolean updateProject(@NonNull Project project, long projectId);

    public boolean removeProject(long projectId);

    /**
     * Adds employee to a project.
     *
     * @param employeeId employee id
     * @param projectId  project id
     * @return true if employee was successfully created.
     */
    public boolean addEmployee(long employeeId, long projectId);

    public boolean removeEmployee(long employeeId, long projectId);
}
