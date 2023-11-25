package cz.zcu.kiv.mjakubas.piae.sem.core.service.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Course;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Function;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.payload.AllocationPayload;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IAllocationRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.ICourseRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IFunctionRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IProjectRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.rules.AllocationInterval;
import cz.zcu.kiv.mjakubas.piae.sem.core.rules.AllocationRule;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.ServiceException;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.AllocationVO;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Service for working with allocations.
 */
@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class AllocationService {

    private final IAllocationRepository assignmentRepository;
    private final IProjectRepository projectRepository;
    private final ICourseRepository courseRepository;
    private final IFunctionRepository functionRepository;
    private final EmployeeService employeeService;

    /**
     * Creates new assignment. Throws SQL or Service exceptions if data validity fails.
     *
     * @param allocationVO assignment data
     */
    @Transactional
    public void createAllocation(AllocationVO allocationVO) {
        int scope = (int) (allocationVO.getAllocationScope() * 40 * 60); // fte to minutes
        var allocationProject = projectRepository.fetchProject(allocationVO.getProjectId());
        if (allocationVO.getDateFrom().isBefore(allocationProject.getDateFrom()) || allocationVO.getDateUntil().isAfter(allocationProject.getDateUntil()))
            throw new ServiceException();

        Allocation allocation = new Allocation()
                .worker(new Employee().id(allocationVO.getEmployeeId()))
                .project(new Project().id(allocationVO.getProjectId()))
                .course(new Course().id(allocationVO.getCourseId()))
                .function(new Function().id(allocationVO.getFunctionId()))
                .allocationScope(scope)
                .dateFrom(allocationVO.getDateFrom())
                .dateUntil(allocationVO.getDateUntil())
                .description(allocationVO.getDescription());

        var processedAllocations = processAllocations(getEmployeeAllocations(allocationVO.getEmployeeId()));
        for (AllocationInterval interval : processedAllocations) {
            if (interval.isFromInterval(allocation)) {
                int sum = interval.getScopeOfPast() + interval.getScopeOfActive() + interval.getScopeOfUnrealized();
                if ((sum + allocation.getAllocationScope()) > 40 * 60) {
                    throw new ServiceException();
                }
            }
        }

        if (!assignmentRepository.createAllocation(allocation)) {
            throw new ServiceException();
        }
    }

    /**
     * Edits assignment. Throws SQL or Service exceptions if data validity fails.
     *
     * @param allocationVO assignment data
     */
    @Transactional
    public void updateAllocation(AllocationVO allocationVO, long id) {
        int scope = (int) (allocationVO.getAllocationScope() * 40 * 60); // fte to minutes
        var allocationProject = projectRepository.fetchProject(allocationVO.getProjectId());
        if (allocationVO.getDateFrom().isBefore(allocationProject.getDateFrom()) || allocationVO.getDateUntil().isAfter(allocationProject.getDateUntil()))
            throw new ServiceException();

        Allocation allocation = new Allocation()
                .worker(new Employee().id(allocationVO.getEmployeeId()))
                .project(new Project().id(allocationVO.getProjectId()))
                .course(new Course().id(allocationVO.getCourseId()))
                .function(new Function().id(allocationVO.getFunctionId()))
                .allocationScope(scope)
                .dateFrom(allocationVO.getDateFrom())
                .dateUntil(allocationVO.getDateUntil())
                .description(allocationVO.getDescription())
                .active(allocationVO.getIsActive());

        if (Boolean.TRUE.equals(allocationVO.getIsActive())) {
            var processedAllocations = processAllocations(getEmployeeAllocations(allocationVO.getEmployeeId()));
            for (AllocationInterval interval : processedAllocations) {
                if (interval.isFromInterval(allocation)) {
                    int sum = interval.getScopeOfPast() + interval.getScopeOfActive() + interval.getScopeOfUnrealized();
                    if ((sum + allocation.getAllocationScope()) > 40 * 60) {
                        throw new ServiceException();
                    }
                }
            }
        }
        if (!assignmentRepository.updateAllocation(allocation, id)) {
            throw new ServiceException();
        }
    }

    /**
     * Gets all employee assignments. Throws SQL exception if employee doesn't exist.
     *
     * @param id employee id
     * @return employee assignments
     */
    public AllocationPayload getEmployeeAllocationsPayload(long id) {
        var assignmentsList = assignmentRepository.fetchEmployeeAllocations(id);
        Set<Project> projects = new HashSet<>();
        for (Allocation allocation : assignmentsList) {
            if (allocation.getProject() != null)
                projects.add(allocation.getProject());
        }

        Set<Course> courses = new HashSet<>();
        for (Allocation allocation : assignmentsList) {
            if (allocation.getCourse() != null)
                courses.add(allocation.getCourse());
        }

        Set<Function> functions = new HashSet<>();
        for (Allocation allocation : assignmentsList) {
            if (allocation.getFunction() != null)
                functions.add(allocation.getFunction());
        }

        return new AllocationPayload(
                projects.stream().toList(),
                courses.stream().toList(),
                functions.stream().toList(),
                null,
                injectActivity(assignmentsList));
    }

    /**
     * Gets all employee allocations. Throws SQL exception if employee doesn't exist.
     *
     * @param id employee id
     * @return list of {@link Allocation}
     */
    public List<Allocation> getEmployeeAllocations(long id) {
        return injectActivity(assignmentRepository.fetchEmployeeAllocations(id));
    }

    /**
     * Gets all subordinates allocations. Throws SQL exception if superior doesn't exist.
     *
     * @param superiorId superior id.
     * @return payload
     */
    public AllocationPayload getSubordinatesAllocations(long superiorId) {
        var assignmentsList = assignmentRepository.fetchSubordinatesAllocations(superiorId);
        Set<Project> projects = new HashSet<>();
        for (Allocation allocation : assignmentsList) {
            if (allocation.getProject() != null)
                projects.add(allocation.getProject());
        }

        Set<Course> courses = new HashSet<>();
        for (Allocation allocation : assignmentsList) {
            if (allocation.getCourse() != null)
                courses.add(allocation.getCourse());
        }

        Set<Function> functions = new HashSet<>();
        for (Allocation allocation : assignmentsList) {
            if (allocation.getFunction() != null)
                functions.add(allocation.getFunction());
        }
        List<Employee> employees = employeeService.getSubordinates(superiorId);

        return new AllocationPayload(
                projects.stream().toList(),
                courses.stream().toList(),
                functions.stream().toList(),
                employees,
                injectEmployee(injectActivity(assignmentsList)));
    }

    /**
     * Gets all project allocations. Throws SQL exception if project doesn't exist.
     *
     * @param id project id
     * @return payload
     */
    public AllocationPayload getProjectAllocations(long id) {
        var allocationList = injectEmployee(assignmentRepository.fetchAllocationsByProjectId(id));
        List<Employee> employees = projectRepository.fetchProjectEmployees(id);

        return new AllocationPayload(null, null, null, employees, allocationList);
    }

    /**
     * Gets all course allocations. Throws SQL exception if course doesn't exist.
     *
     * @param id course id
     * @return payload
     */
    public AllocationPayload getCourseAllocations(long id) {
        var allocationList = injectEmployee(assignmentRepository.fetchAllocationsByCourseId(id));
        List<Employee> employees = courseRepository.fetchCourseEmployees(id);

        return new AllocationPayload(null, null, null, employees, allocationList);
    }

    /**
     * Gets all function allocations. Throws SQL exception if function doesn't exist.
     *
     * @param id function id
     * @return payload
     */
    public AllocationPayload getFunctionAllocations(long id) {
        var allocationList = injectEmployee(assignmentRepository.fetchAllocationsByProjectId(id));
        List<Employee> employees = functionRepository.fetchFunctionEmployees(id);

        return new AllocationPayload(null, null, null, employees, allocationList);
    }

    /**
     * Gets allocation rule.
     *
     * @param projectId  project id
     * @param employeeId employee id
     * @return allocation rule
     */
    public AllocationRule getProjectAllocationsRules(long projectId, long employeeId) {
        var project = projectRepository.fetchProject(projectId);

        LocalDate minDate = project.getDateFrom();
        LocalDate maxDate = project.getDateUntil();

        var employeeAllocations = assignmentRepository.fetchEmployeeAllocations(employeeId);

        var intervals = processAllocations(employeeAllocations);

        return new AllocationRule(minDate, maxDate, intervals);
    }

    /**
     * Gets allocation rule.
     *
     * @param courseId  course id
     * @param employeeId employee id
     * @return allocation rule
     */
    public AllocationRule getCourseAllocationsRules(long courseId, long employeeId) {
        var course = courseRepository.fetchCourse(courseId);

        LocalDate minDate = course.getDateFrom();
        LocalDate maxDate = course.getDateUntil();

        var employeeAllocations = assignmentRepository.fetchEmployeeAllocations(employeeId);

        var intervals = processAllocations(employeeAllocations);

        return new AllocationRule(minDate, maxDate, intervals);
    }

    /**
     * Gets allocation rule.
     *
     * @param functionId  function id
     * @param employeeId employee id
     * @return allocation rule
     */
    public AllocationRule getFunctionAllocationsRules(long functionId, long employeeId) {
        var function = functionRepository.fetchFunction(functionId);

        LocalDate minDate = function.getDateFrom();
        LocalDate maxDate = function.getDateUntil();

        var employeeAllocations = assignmentRepository.fetchEmployeeAllocations(employeeId);

        var intervals = processAllocations(employeeAllocations);

        return new AllocationRule(minDate, maxDate, intervals);
    }

    /**
     * Creates statistics out of allocations.
     *
     * @param allocations list of {@link Allocation}
     * @return statistics
     */
    public List<AllocationInterval> processAllocations(@NonNull List<Allocation> allocations) {
        if (allocations.isEmpty())
            return new ArrayList<>();

        Set<LocalDate> dates = new HashSet<>();
        allocations.forEach(allocation -> {
            dates.add(allocation.getDateFrom());
            dates.add(allocation.getDateUntil());
        });

        List<LocalDate> sortedDates = new ArrayList<>(dates.stream().toList());
        Collections.sort(sortedDates);

        List<AllocationInterval> intervals = new ArrayList<>();
        for (int i = 0; i < sortedDates.size() - 1; i++) {
            var interval = new AllocationInterval(sortedDates.get(i), sortedDates.get(i + 1), 0, new HashMap<>());
            intervals.add(interval);
        }

        intervals.forEach(interval -> allocations.forEach(allocation -> {
            if (!interval.isFromInterval(allocation))
                return;
            interval.getScopes().putIfAbsent(allocation.getCurrentState(), 0);
            int newVal = interval.getScopes().get(allocation.getCurrentState()) + allocation.getAllocationScope();
            interval.getScopes().replace(allocation.getCurrentState(), newVal);
        }));

        return intervals;
    }

    /**
     * Adds all employees to allocations.
     *
     * @param allocations employee allocations
     * @return list of {@link Allocation} with {@link Employee}
     */
    private List<Allocation> injectActivity(@NonNull List<Allocation> allocations) {
        var projects = projectRepository.fetchProjects();
        Map<Long, Project> mapProjects = new HashMap<>();
        projects.forEach(project -> mapProjects.putIfAbsent(project.getId(), project));

        var courses = courseRepository.fetchCourses();
        Map<Long, Course> mapCourses = new HashMap<>();
        courses.forEach(course -> mapCourses.putIfAbsent(course.getId(), course));

        var functions = functionRepository.fetchFunctions();
        Map<Long, Function> mapFunctions = new HashMap<>();
        functions.forEach(function -> mapFunctions.putIfAbsent(function.getId(), function));

        List<Allocation> withActivity = new ArrayList<>();

        for (Allocation a : allocations) {
            var project = mapProjects.get(a.getProject().getId());

            withActivity.add(new Allocation()
                    .id(a.getId())
                    .worker(a.getWorker())
                    .project(project)
                    .allocationScope(a.getAllocationScope())
                    .dateFrom(a.getDateFrom())
                    .dateUntil(a.getDateUntil()).description(a.getDescription()).active(a.getActive()));
        }

        for (Allocation a : allocations) {
            var course = mapCourses.get(a.getCourse().getId());

            withActivity.add(new Allocation()
                    .id(a.getId())
                    .worker(a.getWorker())
                    .course(course)
                    .allocationScope(a.getAllocationScope())
                    .dateFrom(a.getDateFrom())
                    .dateUntil(a.getDateUntil()).description(a.getDescription()).active(a.getActive()));
        }

        for (Allocation a : allocations) {
            var function = mapFunctions.get(a.getFunction().getId());

            withActivity.add(new Allocation()
                    .id(a.getId())
                    .worker(a.getWorker())
                    .function(function)
                    .allocationScope(a.getAllocationScope())
                    .dateFrom(a.getDateFrom())
                    .dateUntil(a.getDateUntil()).description(a.getDescription()).active(a.getActive()));
        }

        return withActivity;
    }


    /**
     * Adds all employees to allocations.
     *
     * @param allocations employee allocations
     * @return list of {@link Allocation} with {@link Employee}
     */
    private List<Allocation> injectEmployee(@NonNull List<Allocation> allocations) {
        var employees = employeeService.getEmployees();
        Map<Long, Employee> mapEmployees = new HashMap<>();
        employees.forEach(employee -> mapEmployees.putIfAbsent(employee.getId(), employee));

        List<Allocation> withEmployee = new ArrayList<>();
        for (Allocation a : allocations) {
            var worker = mapEmployees.get(a.getWorker().getId());

            withEmployee.add(new Allocation()
                    .id(a.getId())
                    .worker(worker)
                    .project(a.getProject())
                    .course(a.getCourse())
                    .function(a.getFunction())
                    .allocationScope(a.getAllocationScope())
                    .dateFrom(a.getDateFrom())
                    .dateUntil(a.getDateUntil()).description(a.getDescription()).active(a.getActive()));
        }

        return withEmployee;
    }

    /**
     * Gets allocation by its id.
     *
     * @param id allocation id
     * @return allocation
     */
    public Allocation getAllocation(long id) {
        return assignmentRepository.fetchAllocation(id);
    }
}
