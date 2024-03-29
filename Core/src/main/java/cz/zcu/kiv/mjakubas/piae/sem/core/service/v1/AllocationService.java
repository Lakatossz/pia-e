package cz.zcu.kiv.mjakubas.piae.sem.core.service.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Course;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Function;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.TermState;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.payload.AllocationPayload;
import cz.zcu.kiv.mjakubas.piae.sem.core.exceptions.CollisionException;
import cz.zcu.kiv.mjakubas.piae.sem.core.exceptions.SecurityException;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IAllocationRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.ICourseRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IEmployeeRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IFunctionRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IProjectRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.rules.AllocationInterval;
import cz.zcu.kiv.mjakubas.piae.sem.core.rules.AllocationRule;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.MyUtils;
import cz.zcu.kiv.mjakubas.piae.sem.core.exceptions.ServiceException;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.SecurityService;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.AllocationVO;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Service for working with allocations.
 */
@Service
@Transactional()
@AllArgsConstructor
public class AllocationService {

    private final IAllocationRepository assignmentRepository;
    private final IProjectRepository projectRepository;
    private final ICourseRepository courseRepository;
    private final IFunctionRepository functionRepository;
    private final IEmployeeRepository employeeRepository;

    @Autowired
    private SecurityService securityService;
    private final MyUtils utils = new MyUtils();

    /**
     * Creates new assignment. Throws SQL or Service exceptions if data validity fails.
     *
     * @param allocationVO assignment data
     */
    @Transactional
    public long createAllocation(AllocationVO allocationVO) {
        if (causeCollison(allocationVO))
            throw new CollisionException();

        int scope = (int) (allocationVO.getAllocationScope() * 40 * 60); // fte to minutes
//        var allocationProject = projectRepository.fetchProject(allocationVO.getProjectId()); TODO Kontrola datumu
//        if (allocationVO.getDateFrom().before(allocationProject.getDateFrom())
//                || allocationVO.getDateUntil().after(allocationProject.getDateUntil()))
//            throw new ServiceException();

        Allocation allocation = new Allocation()
                .worker(new Employee().id(allocationVO.getWorkerId()))
                .project(new Project().id(allocationVO.getProjectId()))
                .course(new Course().id(allocationVO.getCourseId()))
                .function(new Function().id(allocationVO.getFunctionId()))
                .role(allocationVO.getRole())
                .allocationScope(scope)
                .isCertain(allocationVO.getIsCertain())
                .dateFrom(utils.convertToDate(allocationVO.getDateFrom()))
                .dateUntil(utils.convertToDate(allocationVO.getDateUntil()))
                .term(TermState.getByValue(allocationVO.getTerm()))
                .description(allocationVO.getDescription())
                .active(allocationVO.getIsActive());

//        var processedAllocations = processAllocations(getEmployeeAllocations(allocationVO.getEmployeeId())); TODO kontrola alokace -> Muze jeste zamestnanec nejakou dostat?
//        for (AllocationInterval interval : processedAllocations) {
//            if (interval.isFromInterval(allocation)) {
//                int sum = interval.getScopeOfPast() + interval.getScopeOfActive() + interval.getScopeOfUnrealized();
//                if ((sum + allocation.getAllocationScope()) > 40 * 60) {
//                    throw new ServiceException();
//                }
//            }
//        }

        long id = assignmentRepository.createAllocation(allocation);

        if (id > 0)
            return id;
        else
            throw new ServiceException();
    }

    /**
     * Edits assignment. Throws SQL or Service exceptions if data validity fails.
     *
     * @param allocationVO assignment data
     */
    @Transactional
    public void updateAllocation(AllocationVO allocationVO, long id) {
        if (causeCollison(allocationVO))
            throw new CollisionException();

        int scope = (int) (allocationVO.getAllocationScope() * 40 * 60); // fte to minutes
        Allocation allocation = assignmentRepository.fetchAllocation(id);
        allocation = allocation.worker(new Employee().id(allocationVO.getWorkerId()))
                .role(allocationVO.getRole())
                .allocationScope(scope)
                .description(allocationVO.getDescription())
                .isCertain(allocationVO.getIsCertain())
                .term(TermState.valueOf(allocationVO.getTerm()))
                .active(Boolean.TRUE);

        if (allocationVO.getDateFrom() != null && allocationVO.getDateUntil() != null) {
            allocation
                    .dateFrom(utils.convertToDate(allocationVO.getDateFrom()))
                    .dateUntil(utils.convertToDate(allocationVO.getDateUntil()));
        }

        if (allocationVO.getProjectId() > 0) {
            allocation = allocation.project(projectRepository.fetchProject(allocationVO.getProjectId()));
        } else if (allocationVO.getCourseId() > 0) {
            allocation = allocation.course(courseRepository.fetchCourse(allocationVO.getCourseId()));
        } else {
            allocation = allocation.function(functionRepository.fetchFunction(allocationVO.getFunctionId()));
        }

//        if (Boolean.TRUE.equals(allocationVO.getIsActive())) {
//            var processedAllocations = processAllocations(getEmployeeAllocations(allocationVO.getEmployeeId()));
//            for (AllocationInterval interval : processedAllocations) {
//                if (interval.isFromInterval(allocation)) {
//                    int sum = interval.getScopeOfPast() + interval.getScopeOfActive() + interval.getScopeOfUnrealized();
//                    if ((sum + allocation.getAllocationScope()) > 40 * 60) {
//                        throw new ServiceException();
//                    }
//                }
//            }
//        }
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
                new LinkedList<>(projects.stream().toList()),
                new LinkedList<>(courses.stream().toList()),
                new LinkedList<>(functions.stream().toList()),
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
     * Gets all employee projects allocations. Throws SQL exception if employee doesn't exist.
     *
     * @param id employee id
     * @return list of {@link Allocation}
     */
    public List<Allocation> getEmployeeProjectsAllocations(long id) {
        return injectProjectActivity(assignmentRepository.fetchEmployeeAllocations(id));
    }

    /**
     * Gets all employee courses allocations. Throws SQL exception if employee doesn't exist.
     *
     * @param id employee id
     * @return list of {@link Allocation}
     */
    public List<Allocation> getEmployeeCoursesAllocations(long id) {
        return injectCourseActivity(assignmentRepository.fetchEmployeeAllocations(id));
    }

    /**
     * Gets all employee functions allocations. Throws SQL exception if employee doesn't exist.
     *
     * @param id employee id
     * @return list of {@link Allocation}
     */
    public List<Allocation> getEmployeeFunctionsAllocations(long id) {
        return injectFunctionActivity(assignmentRepository.fetchEmployeeAllocations(id));
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
        List<Employee> employees = employeeRepository.fetchSubordinates(superiorId);

        return new AllocationPayload(
                new LinkedList<>(projects.stream().toList()),
                new LinkedList<>(courses.stream().toList()),
                new LinkedList<>(functions.stream().toList()),
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
        var allocationList = injectEmployee(assignmentRepository.fetchAllocationsByFunctionId(id));
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

        Date minDate = project.getDateFrom();
        Date maxDate = project.getDateUntil();

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

        Date minDate = course.getDateFrom();
        Date maxDate = course.getDateUntil();

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

        Date minDate = function.getDateFrom();
        Date maxDate = function.getDateUntil();

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
            return new LinkedList<>();

        Set<Date> dates = new HashSet<>();
        allocations.forEach(allocation -> {
            dates.add(allocation.getDateFrom());
            dates.add(allocation.getDateUntil());
        });

        List<Date> sortedDates = new LinkedList<>(dates.stream().toList());
        Collections.sort(sortedDates);

        List<AllocationInterval> intervals = new LinkedList<>();
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

        List<Allocation> withActivity = new LinkedList<>();

        for (Allocation a : allocations) {
            var project = mapProjects.get(a.getProject().getId());

            withActivity.add(new Allocation()
                    .id(a.getId())
                    .worker(a.getWorker())
                    .project(project)
                    .allocationScope(a.getAllocationScope())
                    .dateFrom(a.getDateFrom())
                    .role(a.getRole())
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
                    .role(a.getRole())
                    .dateUntil(a.getDateUntil()).description(a.getDescription()).active(a.getActive()));
        }

        for (Allocation a : allocations) {
            var function = mapFunctions.get(a.getFunction().getId());

            withActivity.add(new Allocation()
                    .id(a.getId())
                    .worker(a.getWorker())
                    .function(function)
                    .role(a.getRole())
                    .allocationScope(a.getAllocationScope())
                    .dateFrom(a.getDateFrom())
                    .dateUntil(a.getDateUntil()).description(a.getDescription()).active(a.getActive()));
        }

        return withActivity;
    }

    /**
     * Filter all non-project allocations.
     *
     * @param allocations employee allocations
     * @return list of {@link Allocation} with {@link Employee}
     */
    private List<Allocation> injectProjectActivity(@NonNull List<Allocation> allocations) {
        var projects = projectRepository.fetchProjects();
        Map<Long, Project> mapProjects = new HashMap<>();
        projects.forEach(project -> mapProjects.putIfAbsent(project.getId(), project));

        List<Allocation> withActivity = new LinkedList<>();

        for (Allocation a : allocations) {
            var project = mapProjects.get(a.getProject().getId());

            withActivity.add(new Allocation()
                    .id(a.getId())
                    .worker(a.getWorker())
                    .project(project)
                    .allocationScope(a.getAllocationScope())
                    .dateFrom(a.getDateFrom())
                    .role(a.getRole())
                    .dateUntil(a.getDateUntil()).description(a.getDescription()).active(a.getActive()));
        }

        return withActivity;
    }

    /**
     * Filter all non-course allocations.
     *
     * @param allocations employee allocations
     * @return list of {@link Allocation} with {@link Employee}
     */
    private List<Allocation> injectCourseActivity(@NonNull List<Allocation> allocations) {
        var courses = courseRepository.fetchCourses();
        Map<Long, Course> mapCourses = new HashMap<>();
        courses.forEach(course -> mapCourses.putIfAbsent(course.getId(), course));

        List<Allocation> withActivity = new LinkedList<>();

        for (Allocation a : allocations) {
            var course = mapCourses.get(a.getCourse().getId());

            withActivity.add(new Allocation()
                    .id(a.getId())
                    .worker(a.getWorker())
                    .course(course)
                    .allocationScope(a.getAllocationScope())
                    .dateFrom(a.getDateFrom())
                    .role(a.getRole())
                    .dateUntil(a.getDateUntil()).description(a.getDescription()).active(a.getActive()));
        }

        return withActivity;
    }

    /**
     * Filter all non-function allocations.
     *
     * @param allocations employee allocations
     * @return list of {@link Allocation} with {@link Employee}
     */
    private List<Allocation> injectFunctionActivity(@NonNull List<Allocation> allocations) {
        var functions = functionRepository.fetchFunctions();
        Map<Long, Function> mapFunctions = new HashMap<>();
        functions.forEach(function -> mapFunctions.putIfAbsent(function.getId(), function));

        List<Allocation> withActivity = new LinkedList<>();

        for (Allocation a : allocations) {
            var function = mapFunctions.get(a.getFunction().getId());

            withActivity.add(new Allocation()
                    .id(a.getId())
                    .worker(a.getWorker())
                    .function(function)
                    .role(a.getRole())
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
        var employees = employeeRepository.fetchEmployees();
        Map<Long, Employee> mapEmployees = new HashMap<>();
        employees.forEach(employee -> mapEmployees.putIfAbsent(employee.getId(), employee));

        List<Allocation> withEmployee = new ArrayList<>();
        for (Allocation a : allocations) {
            var worker = mapEmployees.get(a.getWorker().getId());

            withEmployee.add(new Allocation()
                    .id(a.getId())
                    .dateFrom(a.getDateFrom())
                    .dateUntil(a.getDateUntil())
                    .term(a.getTerm())
                    .worker(worker)
                    .project(a.getProject())
                    .course(a.getCourse())
                    .function(a.getFunction())
                    .role(a.getRole())
                    .allocationScope(a.getAllocationScope())
                    .description(a.getDescription())
                    .time(a.getTime())
                    .isCertain(a.getIsCertain())
                    .active(a.getActive()));
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

    public boolean removeAllocation(long id) {
        Allocation allocation = assignmentRepository.fetchAllocation(id);

        if (allocation.getProject() != null && securityService.isProjectManager(allocation.getProject().getId())) {
            return assignmentRepository.removeAllocation(id);
        } else if (allocation.getCourse() != null && securityService.isCourseManager(allocation.getCourse().getId())) {
            return assignmentRepository.removeAllocation(id);
        } else if (allocation.getFunction() != null && securityService.isFunctionManager(allocation.getFunction().getId())) {
            return assignmentRepository.removeAllocation(id);
        } else {
            throw new SecurityException();
        }
    }

    private boolean causeCollison(AllocationVO newAllocation) {
        List<Allocation> allocations;

        if (newAllocation.getProjectId() > 0) {
            allocations = getEmployeeAllocations(
                    newAllocation.getWorkerId()).stream().filter(
                            allocation -> allocation.getProject() != null && allocation.getProject().getId() == newAllocation.getProjectId()).toList();
        } else if (newAllocation.getCourseId() > 0) {
            allocations = getEmployeeAllocations(
                    newAllocation.getWorkerId()).stream().filter(
                            allocation -> allocation.getCourse() != null && allocation.getCourse().getId() == newAllocation.getCourseId()).toList();
        } else {
            allocations = getEmployeeAllocations(
                    newAllocation.getWorkerId()).stream().filter(
                            allocation -> allocation.getFunction() != null && allocation.getFunction().getId() == newAllocation.getFunctionId()).toList();
        }

        if (allocations.isEmpty())
            return false;

        LocalDate newAllocationFrom = newAllocation.getDateFrom();
        LocalDate newAllocationUntil = newAllocation.getDateUntil();

        for (Allocation allocation : allocations) {
            LocalDate allocationFrom = utils.convertToLocalDateTime(allocation.getDateFrom());
            LocalDate allocationUntil = utils.convertToLocalDateTime(allocation.getDateUntil());
            if (allocation.getId() != newAllocation.getId() && allocation.getRole().equals(newAllocation.getRole())
                    && (((newAllocationFrom.isAfter(allocationFrom) || newAllocationFrom.isEqual(allocationFrom))
                    && (newAllocationFrom.isBefore(allocationUntil) || newAllocationFrom.isEqual(allocationUntil)))
                    || ((newAllocationUntil.isAfter(allocationFrom) || newAllocationUntil.isEqual(allocationFrom))
                    && (newAllocationUntil.isBefore(allocationUntil) || newAllocationUntil.isEqual(allocationUntil))))) {
                return true;
            }
        }

        return false;
    }
}
