package cz.zcu.kiv.mjakubas.piae.sem.core.service.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Activity;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.ActivityAllocationDetail;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.AllocationCell;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IAllocationRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IEmployeeRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.ServiceException;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
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
    public long createEmployee(EmployeeVO employeeVO) {
        String email = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(email);
        if (!pattern.matcher(employeeVO.getEmailAddress()).matches())
            throw new ServiceException();

        Employee employee = new Employee()
                .firstName(employeeVO.getFirstName())
                .lastName(employeeVO.getLastName())
                .orionLogin(employeeVO.getOrionLogin())
                .emailAddress(employeeVO.getEmailAddress())
                .description(employeeVO.getDescription())
                .workplace(Workplace.builder()
                        .id(employeeVO.getWorkplaceId())
                        .build());

        long id = employeeRepository.createEmployee(employee);

        if (id > 0)
            return id;
        else
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
                .description(employeeVO.getDescription())
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

    public List<Allocation> prepareFirst(List<Employee> employees) {
        List<Allocation> firstAllocations = new LinkedList<>();
        employees.forEach(employee -> {
            if (!employee.getProjectsAllocations().isEmpty()) {
                setProjectFirst(employee, firstAllocations);
            } else if (!employee.getCoursesAllocations().isEmpty()) {
                setCourseFirst(employee, firstAllocations);
            } else if (!employee.getFunctionsAllocations().isEmpty()) {
                setFunctionFirst(employee, firstAllocations);
            } else {
                firstAllocations.add(new Allocation().time(-1));
                employee.allocations(new LinkedList<>());
            }
        });

        return firstAllocations;
    }

    public void prepareAllocationsCells(Employee employee) {
        List<Allocation> projectAllocations = employee.getProjectsAllocations();
        List<MergingObject> projectAllocationsByFunctionAndRole = splitByRoleAndEmployeeId(projectAllocations, employee, Allocation::getProject);

        List<Allocation> courseAllocations = employee.getCoursesAllocations();
        List<MergingObject> courseAllocationsByFunctionAndRole = splitByRoleAndEmployeeId(courseAllocations, employee, Allocation::getCourse);

        List<Allocation> functionAllocations = employee.getFunctionsAllocations();
        List<MergingObject> functionAllocationsByFunctionAndRole = splitByRoleAndEmployeeId(functionAllocations, employee, Allocation::getFunction);

        setFirstAndTotalByActitvitiesAllocations(employee, projectAllocationsByFunctionAndRole,
                courseAllocationsByFunctionAndRole,functionAllocationsByFunctionAndRole);

        employee.setProjectOverviewAllocations(prepareActivityCells(employee, projectAllocationsByFunctionAndRole));
        employee.setCourseOverviewAllocations(prepareActivityCells(employee, courseAllocationsByFunctionAndRole));
        employee.setFunctionOverviewAllocations(prepareActivityCells(employee, functionAllocationsByFunctionAndRole));
        prepareTotalCells(employee);
    }

    private void setFirstAndTotalByActitvitiesAllocations(Employee employee,
                                                          List<MergingObject> projectAllocationsByFunctionAndRole,
                                                          List<MergingObject> courseAllocationsByFunctionAndRole,
                                                          List<MergingObject> functionAllocationsByFunctionAndRole) {
        setFirstAndTotal(employee, projectAllocationsByFunctionAndRole);
        setFirstAndTotal(employee, courseAllocationsByFunctionAndRole);
        setFirstAndTotal(employee, functionAllocationsByFunctionAndRole);
    }

    private void setFirstAndTotal(Employee employee, List<MergingObject> allocationsByFunctionAndRole) {
        for (MergingObject o : allocationsByFunctionAndRole) {
            if (employee.getNumberOfYears() == 0 && employee.getFirstYear() == Integer.MAX_VALUE) {
                employee.setFirstYear(o.getFirstYear());
                employee.setNumberOfYears(o.getNumberOfYears());
            }
            if (o.getFirstYear() < employee.getFirstYear()) {
                employee.setNumberOfYears(employee.getNumberOfYears() + Math.abs(o.getFirstYear() - employee.getFirstYear()));
                employee.setFirstYear(o.getFirstYear());
            }
            if (o.getFirstYear() - (employee.getFirstYear() + employee.getNumberOfYears()) >= 0) {
                employee.setNumberOfYears(o.getFirstYear() + o.getNumberOfYears() - (employee.getFirstYear() - employee.getNumberOfYears()));
            }
        }
    }

    private void addToTotal(ActivityAllocationDetail activitiesList,
                            List<AllocationCell> certains,
                            List<AllocationCell> uncertains) {
        for (int i = 0; i < activitiesList.getActivityAllocationCells().size(); ++i) {
            if (activitiesList.getActivityAllocationCells().get(i).getCertain() == 1) {
                certains.set(i, new AllocationCell(
                        certains.get(i).getTime() + activitiesList.getActivityAllocationCells().get(i).getTime(), (float) 1.0));
            } else {
                uncertains.set(i, new AllocationCell(
                        uncertains.get(i).getTime() + activitiesList.getActivityAllocationCells().get(i).getTime(), (float) 1.0));
            }
        }
    }

    private void prepareTotalCells(Employee employee) {
        List<AllocationCell> certains = new java.util.ArrayList<>(Collections.nCopies(
                (int)employee.getNumberOfYears() * 12, new AllocationCell()));

        List<AllocationCell> uncertains = new java.util.ArrayList<>(Collections.nCopies(
                (int)employee.getNumberOfYears() * 12, new AllocationCell()));

        for (ActivityAllocationDetail a : employee.getProjectOverviewAllocations()) {
            addToTotal(a, certains, uncertains);
        }

        for (ActivityAllocationDetail a : employee.getCourseOverviewAllocations()) {
            addToTotal(a, certains, uncertains);
        }

        for (ActivityAllocationDetail a : employee.getFunctionOverviewAllocations()) {
            addToTotal(a, certains, uncertains);
        }

//        employee.getProjectOverviewAllocations().forEach(activityAllocationDetail
//                -> addToTotal(activityAllocationDetail, certains, uncertains));
//
//        employee.getCourseOverviewAllocations().forEach(activityAllocationDetail
//                -> addToTotal(activityAllocationDetail, certains, uncertains));
//
//        employee.getFunctionOverviewAllocations().forEach(activityAllocationDetail
//                -> addToTotal(activityAllocationDetail, certains, uncertains));

//        for (ActivityAllocationDetail activitiesList : employee.getCourseOverviewAllocations()) {
//            for (int i = 0; i < activitiesList.getActivityAllocationCells().size(); ++i) {
//                if (activitiesList.getActivityAllocationCells().get(i).getCertain() == 1) {
//                    certains.set(i, new AllocationCell(
//                            certains.get(i).getTime() + activitiesList.getActivityAllocationCells().get(i).getTime(), (float) 1.0));
//                } else {
//                    uncertains.set(i, new AllocationCell(
//                            uncertains.get(i).getTime() + activitiesList.getActivityAllocationCells().get(i).getTime(), (float) 1.0));
//                }
//            }
//        }
//
//        for (ActivityAllocationDetail activitiesList : employee.getProjectOverviewAllocations()) {
//            for (int i = 0; i < activitiesList.getActivityAllocationCells().size(); ++i) {
//                if (activitiesList.getActivityAllocationCells().get(i).getCertain() == 1) {
//                    certains.set(i, new AllocationCell(
//                            certains.get(i).getTime() + activitiesList.getActivityAllocationCells().get(i).getTime(), (float) 1.0));
//                } else {
//                    uncertains.set(i, new AllocationCell(
//                            uncertains.get(i).getTime() + activitiesList.getActivityAllocationCells().get(i).getTime(), (float) 1.0));
//                }
//            }
//        }
//
//        for (List<AllocationCell> l : employee.getCoursesAllocationCells()) {
//            for (int i = 0; i < l.size(); ++i) {
//                if (l.get(i).getCertain() == 1) {
//                    certains.set(i, new AllocationCell(
//                            certains.get(i).getTime() + l.get(i).getTime(), (float) 1.0));
//                } else {
//                    uncertains.set(i, new AllocationCell(
//                            uncertains.get(i).getTime() + l.get(i).getTime(), (float) 1.0));
//                }
//            }
//        }
//
//        for (List<AllocationCell> l : employee.getFunctionsAllocationCells()) {
//            for (int i = 0; i < l.size(); ++i) {
//                if (l.get(i).getCertain() == 1) {
//                    certains.set(i, new AllocationCell(
//                            certains.get(i).getTime() + l.get(i).getTime(), (float) 1.0));
//                } else {
//                    uncertains.set(i, new AllocationCell(
//                            uncertains.get(i).getTime() + l.get(i).getTime(), (float) 1.0));
//                }
//            }
//        }

        employee.totalCertainAllocationCells(certains);
        employee.totalUncertainAllocationCells(uncertains);
        System.out.println("Employee: " + employee.getFirstName() + " certain: ");
        for (AllocationCell cell : employee.getTotalCertainAllocationCells()) {
            System.out.print(cell.getTime() + " ");
        }

        System.out.println();

        System.out.println("Employee: " + employee.getFirstName() + " uncertain: ");
        for (AllocationCell cell : employee.getTotalUncertainAllocationCells()) {
            System.out.print(cell.getTime() + " ");
        }

        System.out.println();

        System.out.println("Employee: " + employee.getFirstName() + " projects: ");
        for (ActivityAllocationDetail activity : employee.getProjectOverviewAllocations()) {
            System.out.println("Projekt: " + activity.getActivityName() + ", Role: " + activity.getActivityRole());
            for (AllocationCell cell : activity.getActivityAllocationCells())
                System.out.print(cell.getTime() + " ");
            System.out.println();
        }

        System.out.println("Employee: " + employee.getFirstName() + " projects: ");
        for (ActivityAllocationDetail activity : employee.getCourseOverviewAllocations()) {
            System.out.println("Predmet: " + activity.getActivityName() + ", Role: " + activity.getActivityRole());
            for (AllocationCell cell : activity.getActivityAllocationCells())
                System.out.print(cell.getTime() + " ");
            System.out.println();
        }

        System.out.println("Employee: " + employee.getFirstName() + " projects: ");
        for (ActivityAllocationDetail activity : employee.getFunctionOverviewAllocations()) {
            System.out.println("Funkce: " + activity.getActivityName() + ", Role: " + activity.getActivityRole());
            for (AllocationCell cell : activity.getActivityAllocationCells())
                System.out.print(cell.getTime() + " ");
            System.out.println();
        }
    }

    /**
     * Projdu vsechny alokace a spojim ty se stejnou roli a projektem
     * @param current
     * @param employee
     * @return
     */
    private List<MergingObject> splitByRoleAndEmployeeId(List<Allocation> current,
                                                         Employee employee,
                                                         Function<Allocation, Activity> attributeSelector) {

        List<MergingObject> allocationsByActivityAndRole = new LinkedList<>();

        for (Allocation allocation : current) {
            System.out.println("id: " + attributeSelector.apply(allocation).getId());
            System.out.println("role: " + allocation.getRole());
            System.out.println("time: " + allocation.getTime());
            if (allocation.getProject().getName() != null)
                System.out.println("nazev: " + allocation.getProject().getName());
            List<MergingObject> objects = allocationsByActivityAndRole.stream().filter(o ->
                    o.getActivityId() == attributeSelector.apply(allocation).getId() && o.role.equals(allocation.getRole())).toList();
            if (objects.size() == 1) {
                int index = allocationsByActivityAndRole.indexOf(objects.get(0));
                allocationsByActivityAndRole.set(index, allocationsByActivityAndRole.get(index).allocations(allocation));
                System.out.println("allocationsByActivityAndRole: " + allocationsByActivityAndRole.get(index).getAllocations().size());
            } else {
                allocationsByActivityAndRole.add(
                        new MergingObject()
                                .employeeId(employee.getId())
                                .role(allocation.getRole())
                                .activityId(attributeSelector.apply(allocation).getId())
                                .allocations(allocation));
            }

        }

//        Seradim spojene alokace podle pocatecniho datumu
        allocationsByActivityAndRole.forEach(MergingObject::sortAllocationsByDate);
        return allocationsByActivityAndRole;
    }

    public LocalDate convertToLocalDateTime(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private void addAllocationsPerMonth(Allocation allocation,
                                        List<AllocationCell> cellsList,
                                        int firstYear) {
        Date dateFrom = allocation.getDateFrom();
        Date dateUntil = allocation.getDateUntil();
        int yearsDiff = Period.between(convertToLocalDateTime(dateFrom), convertToLocalDateTime(dateUntil)).getYears();
        int monthsDiff = yearsDiff * 12 + Period.between(convertToLocalDateTime(dateFrom), convertToLocalDateTime(dateUntil)).getMonths() + 1; // leden - leden je porad 1 mesic xD

        Calendar date = new GregorianCalendar();
        date.setTime(dateFrom);
        int startIndex = (date.get(Calendar.YEAR) - firstYear) * 12 + date.get(Calendar.MONTH);

        for (int i = 0; i < monthsDiff; ++i) {
            cellsList.set(startIndex + i, new AllocationCell(allocation.getTime(), allocation.getIsCertain()));
        }
    }

    private List<ActivityAllocationDetail> prepareAllocationCells(List<MergingObject> allocationsByActivityAndRole,
                                                              int totalFirstYear,
                                                              int totalNumberOfYears) {
        List<ActivityAllocationDetail> activityAllocationDetailList = new LinkedList<>();
        for (MergingObject object : allocationsByActivityAndRole) {
            ActivityAllocationDetail allocationDetail = new ActivityAllocationDetail();
            int cellsSize = (12 * totalNumberOfYears);
            List<AllocationCell> cellsList = new java.util.ArrayList<>(Collections.nCopies(cellsSize, new AllocationCell()));
            object.allocations.forEach(allocation -> addAllocationsPerMonth(allocation, cellsList, totalFirstYear));
            allocationDetail.setActivityAllocationCells(cellsList);
            if (object.allocations.get(0).getProject().getId() > 0) {
                allocationDetail.setActivityId(object.allocations.get(0).getProject().getId());
                allocationDetail.setActivityName(object.allocations.get(0).getProject().getName());
                allocationDetail.setActivityCertain(object.allocations.get(0).getProject().getProbability()); // TODO mozna prumer?
                allocationDetail.setActivityRole(object.allocations.get(0).getRole());
            } else if (object.allocations.get(0).getCourse().getId() > 0) {
                allocationDetail.setActivityId(object.allocations.get(0).getCourse().getId());
                allocationDetail.setActivityName(object.allocations.get(0).getCourse().getShortcut());
                allocationDetail.setActivityCertain(object.allocations.get(0).getCourse().getProbability());
                allocationDetail.setActivityRole(object.allocations.get(0).getRole());
            } else {
                allocationDetail.setActivityId(object.allocations.get(0).getFunction().getId());
                allocationDetail.setActivityName(object.allocations.get(0).getFunction().getName());
                allocationDetail.setActivityCertain(object.allocations.get(0).getFunction().getProbability());
                allocationDetail.setActivityRole(object.allocations.get(0).getRole());
            }
            activityAllocationDetailList.add(allocationDetail);
        }

        return activityAllocationDetailList;
    }

    public List<ActivityAllocationDetail> prepareActivityCells(Employee employee,
                                                           List<MergingObject> allocationsByActivityAndRole) {

        return prepareAllocationCells(
                allocationsByActivityAndRole,
                (int) employee.getFirstYear(),
                (int) employee.getNumberOfYears());
    }

    private void setProjectFirst(Employee employee, List<Allocation> firstAllocations) {
        if (employee.getProjectsAllocations().size() == 1) {
            firstAllocations.add(employee.getProjectsAllocations().remove(0));
            employee.allocations(new LinkedList<>());
        } else {
            firstAllocations.add(employee.getProjectsAllocations().remove(0));
            employee.allocations(employee.getProjectsAllocations());
            employee.allocations(employee.getCoursesAllocations());
            employee.allocations(employee.getFunctionsAllocations());
        }
    }

    private void setCourseFirst(Employee employee, List<Allocation> firstAllocations) {
        if (employee.getCoursesAllocations().size() == 1) {
            firstAllocations.add(employee.getCoursesAllocations().remove(0));
            employee.allocations(new LinkedList<>());
        } else {
            firstAllocations.add(employee.getCoursesAllocations().remove(0));
            employee.allocations(employee.getCoursesAllocations());
            employee.allocations(employee.getFunctionsAllocations());
        }
    }

    private void setFunctionFirst(Employee employee, List<Allocation> firstAllocations) {
        if (employee.getFunctionsAllocations().size() == 1) {
            firstAllocations.add(employee.getFunctionsAllocations().remove(0));
            employee.allocations(new LinkedList<>());
        } else {
            firstAllocations.add(employee.getFunctionsAllocations().remove(0));
            employee.allocations(employee.getFunctionsAllocations());
        }
    }

    private void setAllocations(Employee employee) {
        List<Allocation> projectAllocations = new LinkedList<>(allocationRepository.fetchEmployeeAllocations(
                employee.getId()).stream().filter(allocation -> allocation.getProject().getId() != 0
                && allocation.getCourse().getId() == 0 && allocation.getFunction().getId() == 0).toList());
        projectAllocations.forEach(allocation -> {
            if (allocation.getProject() != null && allocation.getProject().getId() != 0) {
                allocation.setProject(projectService.getProject(allocation.getProject().getId()));
            }
        });
        employee.setProjectsAllocations(projectAllocations);

        List<Allocation> courseAllocations = new LinkedList<>(allocationRepository.fetchEmployeeAllocations(
                employee.getId()).stream().filter(allocation -> allocation.getCourse().getId() != 0
                && allocation.getProject().getId() == 0 && allocation.getFunction().getId() == 0).toList());
        courseAllocations.forEach(allocation -> {
            if (allocation.getCourse() != null && allocation.getCourse().getId() != 0)
                allocation.setCourse(courseService.getCourse(allocation.getCourse().getId()));
        });
        employee.setCoursesAllocations(courseAllocations);

        List<Allocation> functionAllocations = new LinkedList<>(allocationRepository.fetchEmployeeAllocations(
                employee.getId()).stream().filter(allocation -> allocation.getFunction().getId() != 0
                && allocation.getProject().getId() == 0 && allocation.getCourse().getId() == 0).toList());
        functionAllocations.forEach(allocation -> {
            if (allocation.getFunction() != null && allocation.getFunction().getId() != 0)
                allocation.setFunction(functionService.getFunction(allocation.getFunction().getId()));
        });
        employee.setFunctionsAllocations(functionAllocations);
    }

    private float sumTime(Employee employee) {

        return 1.0F;
    }

    @Data
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private class MergingObject {
        long activityId;
        long employeeId;
        String role;
        long firstYear;
        long numberOfYears;
        List<Allocation> allocations = new LinkedList<>();
        public MergingObject(String role, long employeeId, Allocation allocation) {
            this.employeeId = employeeId;
            this.role = role;
            this.allocations.add(allocation);
        }

        public MergingObject allocations(Allocation allocations) {
            this.allocations.add(allocations);
            return this;
        }

        public MergingObject role(String role) {
            this.role = role;
            return this;
        }

        public MergingObject employeeId(long employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public MergingObject activityId(long activityId) {
            this.activityId = activityId;
            return this;
        }

        public void sortAllocationsByDate() {
            allocations.sort(Comparator.comparing(Allocation::getDateFrom));
            Calendar date = new GregorianCalendar();
            date.setTime(allocations.get(0).getDateFrom());
            firstYear = date.get(Calendar.YEAR);
            date.setTime(allocations.get(allocations.size() - 1).getDateUntil());
            numberOfYears = (date.get(Calendar.YEAR) - firstYear) + 1;
        }
    }
}
