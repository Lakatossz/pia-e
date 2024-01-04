package cz.zcu.kiv.mjakubas.piae.sem.core.service;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Course;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Function;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IUserRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.CourseService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.FunctionService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.ProjectService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.WorkplaceService;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import lombok.AllArgsConstructor;
import lombok.NonNull;
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
    private EmployeeService employeeService;
    private ProjectService projectService;
    private CourseService courseService;
    private FunctionService functionService;
    private WorkplaceService workplaceService;

    private PasswordEncoder passwordEncoder;

    /**
     * Creates new user. Proper {@link Employee} must already exist as this only creates wrapper for spring boot.
     *
     * @param orionLogin existing {@link Employee} orion login
     * @param password   plaintext password
     */
    public void createUser(@NonNull String orionLogin, @NonNull String password) {
        var employee = employeeService.getEmployee(orionLogin);
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
    public long createUserUser(@NonNull long id, @NonNull String password) {
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
        var employee = employeeService.getEmployee(orionLogin);

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
        var employee = employeeService.getEmployee(id);


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
        var employee = employeeService.getEmployee(id);

        return auth.getName().equals(employee.getOrionLogin()) && !employee.getSubordinates().isEmpty();
    }

    /**
     * Checks if current logged user is project manager of parameter id of a project.
     *
     * @param projectId project id
     * @return true if yes, otherwise no
     */
    public boolean isProjectManager(Long projectId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var employee = employeeService.getEmployee(auth.getName());
        var project = projectService.getProject(projectId);

        return project.getProjectManager().getId() == employee.getId();
    }

    /**
     * Checks if current logged user is course manager of parameter id of a course.
     *
     * @param courseId course id
     * @return true if yes, otherwise no
     */
    public boolean isCourseManager(Long courseId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var employee = employeeService.getEmployee(auth.getName());
        var course = courseService.getCourse(courseId);

        return course.getCourseManager().getId() == employee.getId();
    }

    /**
     * Checks if current logged user is function manager of parameter id of a function.
     *
     * @param functionId function id
     * @return true if yes, otherwise no
     */
    public boolean isFunctionManager(Long functionId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var employee = employeeService.getEmployee(auth.getName());
        var function = functionService.getFunction(functionId);

        return function.getFunctionManager().getId() == employee.getId();
    }

    /**
     * Checks if current logged user is at least project manager.
     *
     * @return true if yes, otherwise false
     */
    public boolean isAtLeastProjectManager() {
        var projects = projectService.getProjects();
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
        var courses = courseService.getCourses();
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
        var functions = functionService.getFunctions();
        for (Function f : functions) {
            if (isFunctionManager(f.getId()))
                return true;
        }

        return false;
    }

    /**
     * Check if current logged user is workplace manager of project.
     *
     * @param projectId project id
     * @return true if yes, otherwise false
     */
    public boolean isWorkplaceManager(int projectId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var employee = employeeService.getEmployee(auth.getName());
        var workplace = workplaceService.getWorkplace(projectId);

        return workplace.getManager().getId() == employee.getId();
    }

    /**
     * Creates new user account
     *
     * @param employeeVO employee VO
     */
    @Transactional
    public long createUserAccount(@NonNull EmployeeVO employeeVO) {
        long id = employeeService.createEmployee(employeeVO);
        createUser(employeeVO.getOrionLogin(), employeeVO.getPassword());
        return id;
    }
}
