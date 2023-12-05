package cz.zcu.kiv.mjakubas.piae.sem.core.service.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IAllocationRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IEmployeeRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.ServiceException;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Service for working with employees.
 */
@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class EmployeeService {

    private final IEmployeeRepository employeeRepository;
    private final IAllocationRepository allocationRepository;
    private final ProjectService projectService;
    private final CourseService courseService;
    private final FunctionService functionService;

    /**
     * Get employee by his orion login. Throws SQL exception if employee doesn't exist.
     *
     * @param orionLogin employee orion login
     * @return employee
     */
    public Employee getEmployee(@NonNull String orionLogin) {
        return employeeRepository.fetchEmployee(orionLogin);
    }

    /**
     * Get employee by his id. Throes SQL exception if employee doesn't exist.
     *
     * @param id employee id
     * @return employee
     */
    public Employee getEmployee(long id) {
        Employee employee = employeeRepository.fetchEmployee(id);
        setAllocations(employee);
        employee.setCertainTime(sumTime(employee));
        employee.setUncertainTime(sumTime(employee));
        return employee;
    }

    /**
     * Gets all employees. Throws sql exception if an error occurs.
     *
     * @return list of {@link Employee}
     */
    public List<Employee> getEmployees() {
        List<Employee> employees = employeeRepository.fetchEmployees();

        employees.forEach(employee -> {
            setAllocations(employee);
            employee.setCertainTime(sumTime(employee));
            employee.setUncertainTime(sumTime(employee));
        });

        return employees;
    }

    /**
     * Gets employee subordinates. Throws SQL exception if employee doesn't exist.
     *
     * @param employeeId superior id
     * @return list of subordinate {@link Employee}
     */
    public List<Employee> getSubordinates(long employeeId) {
        return employeeRepository.fetchSubordinates(employeeId);
    }

    /**
     * Creates new employee. Throws SQL or Service exception if error occurs.
     *
     * @param employeeVO employee vo.
     */
    @Transactional
    public void createEmployee(EmployeeVO employeeVO) {
        String email = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(email);
        if (!pattern.matcher(employeeVO.getEmailAddress()).matches())
            throw new ServiceException();

        Employee employee = new Employee()
                .firstName(employeeVO.getFirstName())
                .lastName(employeeVO.getLastName())
                .orionLogin(employeeVO.getOrionLogin())
                .emailAddress(employeeVO.getEmailAddress())
                .workplace(Workplace.builder()
                        .id(employeeVO.getWorkplaceId())
                        .build());

        if (!employeeRepository.createEmployee(employee))
            throw new ServiceException();
    }

    /**
     * Updates existing employee. Throws SQL or Service exception if data validity check fails.
     *
     * @param employeeVO employee vo
     * @param id         employee id
     */
    @Transactional
    public void updateEmployee(EmployeeVO employeeVO, long id) {
        String email = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(email);
        if (!pattern.matcher(email).matches())
            throw new ServiceException();

        Employee employee = new Employee()
                .id(id)
                .firstName(employeeVO.getFirstName())
                .lastName(employeeVO.getLastName())
                .orionLogin(employeeVO.getOrionLogin())
                .emailAddress(employeeVO.getEmailAddress())
                .workplace(Workplace.builder()
                        .id(employeeVO.getWorkplaceId())
                        .build());

        if (!employeeRepository.updateEmployee(employee, id))
            throw new ServiceException();
    }

    @Transactional
    public void removeEmployee(long id) {
        return;
    }

    /**
     * Adds subordinate to employee. Throws SQL or Service exception if data validity check fails.
     *
     * @param userVO subordinate data
     * @param id     employee id
     */
    @Transactional
    public void addSubordinate(EmployeeVO userVO, long id) {
        var legit = getSubordinates(id);
        var legitId = getEmployee(userVO.getOrionLogin()).getId();
        legit.forEach(employee -> {
            if (employee.getId() == legitId)
                throw new ServiceException();
        });
        if (legitId == id)
            throw new ServiceException();

        if (!employeeRepository.addSubordinate(id, legitId))
            throw new ServiceException();
    }

    private void setAllocations(Employee employee) {
        List<Allocation> projectAllocations = allocationRepository.fetchEmployeeAllocations(
                employee.getId()).stream().filter(allocation -> allocation.getProject().getId() != 0
                && allocation.getCourse().getId() == 0 && allocation.getFunction().getId() == 0).toList();
        projectAllocations.forEach(allocation -> {
            if (allocation.getProject() != null && allocation.getProject().getId() != 0) {
                allocation.setProject(projectService.getProject(allocation.getProject().getId()));
            }
        });
        employee.setProjectsAllocations(projectAllocations);

        List<Allocation> courseAllocations = allocationRepository.fetchEmployeeAllocations(
                employee.getId()).stream().filter(allocation -> allocation.getCourse().getId() != 0
                && allocation.getProject().getId() == 0 && allocation.getFunction().getId() == 0).toList();
        courseAllocations.forEach(allocation -> {
            if (allocation.getCourse() != null && allocation.getCourse().getId() != 0)
                allocation.setCourse(courseService.getCourse(allocation.getCourse().getId()));
        });
        employee.setCoursesAllocations(courseAllocations);

        List<Allocation> functionAllocations = allocationRepository.fetchEmployeeAllocations(
                employee.getId()).stream().filter(allocation -> allocation.getFunction().getId() != 0
                && allocation.getProject().getId() == 0 && allocation.getCourse().getId() == 0).toList();
        functionAllocations.forEach(allocation -> {
            if (allocation.getFunction() != null && allocation.getFunction().getId() != 0)
                allocation.setFunction(functionService.getFunction(allocation.getFunction().getId()));
        });
        employee.setFunctionsAllocations(functionAllocations);
    }

    private float sumTime(Employee employee) {

        return 1.0F;
    }
}
