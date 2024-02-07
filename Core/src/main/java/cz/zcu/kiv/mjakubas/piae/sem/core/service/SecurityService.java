package cz.zcu.kiv.mjakubas.piae.sem.core.service;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Course;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Function;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import cz.zcu.kiv.mjakubas.piae.sem.core.exceptions.ServiceException;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.ICourseRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IEmployeeRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IFunctionRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IProjectRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IUserRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IWorkplaceRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.CourseService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.FunctionService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.ProjectService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.WorkplaceService;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Represents service for handling security tasks -> such as creating new user or changing his password.
 * Also provides method for securing individual mvc end-points.
 */
@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class SecurityService {

    private IUserRepository userRepository;
    private IEmployeeRepository employeeRepository;
    private IProjectRepository projectRepository;
    private ICourseRepository courseRepository;
    private IFunctionRepository functionRepository;
    private IWorkplaceRepository workplaceRepository;

    private PasswordEncoder passwordEncoder;

    private final String DEFAULT_PASSWORD = "piae";

    /**
     * Creates new user. Proper {@link Employee} must already exist as this only creates wrapper for spring boot.
     *
     * @param orionLogin existing {@link Employee} orion login
     * @param password   plaintext password
     */
    public void createUser(@NonNull String orionLogin, @NonNull String password) throws ServiceException {
        var employee = employeeRepository.fetchEmployee(orionLogin);
        createUserUser(employee.getId(), password);
        createUserRole(employee.getOrionLogin());
    }

    /**
     * Creates new user. As creating user consist of 2 task -> firstly create new user -> secondly create new user role.
     * This method does first task.
     *
     * @param id       user id
     * @param password plaintext password
     */
    public long createUserUser(@NonNull long id, @NonNull String password) throws ServiceException {
        var pw = passwordEncoder.encode(password);
        long userId = userRepository.createNewUser(id, pw);

        if (userId > 0)
            return userId;
        else
            throw new ServiceException();
    }

    /**
     * Creates new user. As creating user consist of 2 task -> firstly create new user -> secondly create new user role.
     * This method does second task.
     *
     * @param orionLogin employee orion login
     */
    public void createUserRole(@NonNull String orionLogin) {
        if (!userRepository.addUserRole(orionLogin)) {
            throw new ServiceException();
        }
    }

    /**
     * Updates user password -> this also makes new user password not temporary
     * -> thus allowing user to use the application.
     *
     * @param orionLogin user orion login
     * @param password   new plaintext password
     */
    @Transactional
    public void updateUserPassword(@NonNull String orionLogin, @NonNull String password) {
        var employee = employeeRepository.fetchEmployee(orionLogin);

        var pw = passwordEncoder.encode(password);
        if (!userRepository.updatePassword(employee.getId(), pw)) {
            throw new ServiceException();
        }
    }

    /**
     * Checks if logged user has temporary password.
     *
     * @param orionLogin logged user orion login
     * @return true if temporary, false otherwise
     */
    public boolean isTemporary(@NonNull String orionLogin) {
        return userRepository.isTemporary(orionLogin);
    }

    /**
     * Checks if parameter id is current logged user id.
     *
     * @param id possible user id
     * @return true if yes, no otherwise
     */
    public boolean isUserView(Long id) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var employee = employeeRepository.fetchEmployee(id);

        return auth.getName().equals(employee.getOrionLogin());
    }

    /**
     * Checks if parameter id is current logged user id and the user is superior.
     *
     * @param id possible user id
     * @return true if yes, no otherwise
     */
    public boolean isSuperiorView(Long id) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var employee = employeeRepository.fetchEmployee(id);

        return auth.getName().equals(employee.getOrionLogin()) && !employee.getSubordinates().isEmpty();
    }

    /**
     * Checks if current logged user is project manager of parameter id of a project.
     *
     * @param projectId project id
     * @return true if yes, otherwise no
     */
    public boolean isProjectManager(Long projectId) {
        if (projectId > 0) {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            var employee = employeeRepository.fetchEmployee(auth.getName());
            var project = projectRepository.fetchProject(projectId);

            return project.getProjectManager().getId() == employee.getId();
        } else
            return false;
    }

    /**
     * Checks if current logged user is course manager of parameter id of a course.
     *
     * @param courseId course id
     * @return true if yes, otherwise no
     */
    public boolean isCourseManager(Long courseId) {
        if (courseId > 0) {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            var employee = employeeRepository.fetchEmployee(auth.getName());
            var course = courseRepository.fetchCourse(courseId);

            return course.getCourseManager().getId() == employee.getId();
        } else
            return false;
    }

    /**
     * Checks if current logged user is function manager of parameter id of a function.
     *
     * @param functionId function id
     * @return true if yes, otherwise no
     */
    public boolean isFunctionManager(Long functionId) {
        if (functionId > 0) {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            var employee = employeeRepository.fetchEmployee(auth.getName());
            var function = functionRepository.fetchFunction(functionId);

            return function.getFunctionManager().getId() == employee.getId();
        } else
            return false;
    }

    /**
     * Checks if current logged user is at least project manager.
     *
     * @return true if yes, otherwise false
     */
    public boolean isAtLeastProjectManager() {
        var projects = projectRepository.fetchProjects();
        for (Project p : projects) {
            if (isProjectManager(p.getId()))
                return true;
        }

        return false;
    }

    /**
     * Checks if current logged user is at least course manager.
     *
     * @return true if yes, otherwise false
     */
    public boolean isAtLeastCourseManager() {
        var courses = courseRepository.fetchCourses();
        for (Course c : courses) {
            if (isCourseManager(c.getId()))
                return true;
        }

        return false;
    }

    /**
     * Checks if current logged user is at least function manager.
     *
     * @return true if yes, otherwise false
     */
    public boolean isAtLeastFunctionManager() {
        var functions = functionRepository.fetchFunctions();
        for (Function f : functions) {
            if (isFunctionManager(f.getId()))
                return true;
        }

        return false;
    }

    /**
     * Check if current logged user is workplace manager of project.
     *
     * @param workplaceId project id
     * @return true if yes, otherwise false
     */
    public boolean isWorkplaceManager(int workplaceId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var employee = employeeRepository.fetchEmployee(auth.getName());
        var workplace = workplaceRepository.fetchWorkplace(workplaceId);

        return workplace.getManager().getId() == employee.getId();
    }

    /**
     * Creates new user account
     *
     * @param employee employee
     */
    @Transactional
    public long createUserAccount(@NonNull Employee employee) throws ServiceException {
        try {
            long id = employeeRepository.createEmployee(employee);
            createUser(employee.getOrionLogin(), "DEFAULT_PASSWORD");
            return id;
        } catch (InvalidDataAccessApiUsageException e) {
            throw new ServiceException();
        }
    }
}
