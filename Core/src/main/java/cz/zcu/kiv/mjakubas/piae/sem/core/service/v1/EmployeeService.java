package cz.zcu.kiv.mjakubas.piae.sem.core.service.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Activity;
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
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
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

    /* Pokus o kodovani pro nasledne zpracovani na FE -> YYYYMM. */
    void codeYearAndMonth(List<List<AllocationCell>> lists, Employee employee) {
        for (List<AllocationCell> list : lists) {
            for (AllocationCell cell : list) {
                if (cell.getKey() / 100 < employee.getFirstYear())
                    employee.setFirstYear(cell.getKey() / 100);
            }
            if (list.size() / 12 > employee.getNumberOfYears()) {
                employee.setNumberOfYears(list.size() / 12);
            }
        }
    }

    public void prepareProjectsCells(Employee employee) {
        List<Allocation> current = employee.getProjectsAllocations();
        List<MergingObject> allocationsByProjectAndRole = new LinkedList<>();

//        Zakoduju ostatni akce a pritom zjistim prvni rok a delku
        codeYearAndMonth(employee.getCoursesAllocationCells(), employee);
        codeYearAndMonth(employee.getFunctionsAllocationCells(), employee);

//        Projdu vsechny alokace a spojim ty se stejnou roli a projektem
        for (Allocation allocation : current) {
            List<MergingObject> list = allocationsByProjectAndRole.stream().filter(
                    o -> o.getActivityId() == allocation.getProject().getId()
                            && o.getRole().equals(allocation.getRole())).toList();
            if (!list.isEmpty()) {
                MergingObject currentObject = list.get(0);
                int index = allocationsByProjectAndRole.indexOf(currentObject);
                if (index != -1)
                    allocationsByProjectAndRole.get(index).allocations(allocation);
                else
                    allocationsByProjectAndRole.add(
                            new MergingObject(allocation.getProject().getId(), allocation.getRole(), allocation));
            } else
                allocationsByProjectAndRole.add(
                        new MergingObject(allocation.getProject().getId(), allocation.getRole(), allocation));
        }

        if (!allocationsByProjectAndRole.isEmpty()) {
//            Seradim spojene alokace podle pocatecniho datumu
            allocationsByProjectAndRole.forEach(MergingObject::sortAllocationsByDate);

//            Zjistim prvni rok a delku a pripadne nastavim u zamestnance
            for (MergingObject o : allocationsByProjectAndRole) {
                if (o.getFirstYear() < employee.getFirstYear())
                    employee.setFirstYear(o.getFirstYear());
                if (o.getNumberOfYears() > employee.getNumberOfYears()) {
                    employee.setNumberOfYears(o.getNumberOfYears());
                }
            }

            List<List<AllocationCell>> list = new LinkedList<>();
            for (MergingObject object : allocationsByProjectAndRole) {
                int size = (int) (12 * employee.getNumberOfYears());
                List<AllocationCell> allList = new java.util.ArrayList<>(Collections.nCopies(size, new AllocationCell()));
                int index = 0;
                int i = 0;
                while (i < size && object.getAllocations().size() > index && object.getAllocations().get(index) != null) {
                    if (object.getFirstYear() > employee.getFirstYear()
                            || object.getFirstYear() + object.getNumberOfYears() < employee.getFirstYear() + employee.getNumberOfYears()) {
                        for (int j = 0; j < 12; ++j) {
                            allList.set(i, new AllocationCell(employee.getFirstYear() * 100 + j, 0, 0));
                            i++;
                        }
                    } else {
                        int j = 0;
                        Date lastDate = object.getAllocations().get(index).getDateFrom();
                        long lastYear = employee.getFirstYear();
                        Date nextEnd = object.getAllocations().get(index).getDateUntil();
                        while(i < size && nextEnd.after(lastDate)) {
                            allList.set(i, new AllocationCell(lastYear * 100 + j,
                                    object.getAllocations().get(index).getTime(),
                                    object.getAllocations().get(index).getIsCertain()));
                            if (j == 11) {
                                lastYear++;
                                j = 0;
                            }
                            lastDate = DateUtils.addMonths(lastDate, 1);
                            ++j;
                            ++i;
                        }
                        index++;
                    }
                }
                list.add(allList);
                employee.setProjectsAllocationCells(list);
            }
        }
    }

    /**
     * Projdu vsechny alokace a spojim ty se stejnou roli a projektem
     * @param current
     * @param attributeSelector
     * @return
     */
    private List<MergingObject> splitByRoleAndActivityId(Employee employee,
                                                         List<Allocation> current,
                                                         Function<Allocation, Activity> attributeSelector) {
        List<MergingObject> allocationsByActivityAndRole = new LinkedList<>();

        for (Allocation allocation : current) {
            List<MergingObject> list = allocationsByActivityAndRole.stream().filter(
                    o -> o.getActivityId() == attributeSelector.apply(allocation).getId()
                            && o.getRole().equals(allocation.getRole())).toList();
            if (!list.isEmpty()) {
                MergingObject currentObject = list.get(0);
                int index = allocationsByActivityAndRole.indexOf(currentObject);
                if (index != -1)
                    allocationsByActivityAndRole.get(index).allocations(allocation);
                else
                    allocationsByActivityAndRole.add(
                            new MergingObject(attributeSelector.apply(allocation).getId(), allocation.getRole(), allocation));
            } else
                allocationsByActivityAndRole.add(
                        new MergingObject(attributeSelector.apply(allocation).getId(), allocation.getRole(), allocation));
        }

//        Seradim spojene alokace podle pocatecniho datumu
        allocationsByActivityAndRole.forEach(MergingObject::sortAllocationsByDate);

//        Zjistim prvni rok a delku a pripadne nastavim u zamestnance
        for (MergingObject o : allocationsByActivityAndRole) {
            if (o.getFirstYear() < employee.getFirstYear())
                employee.setFirstYear(o.getFirstYear());
            if (o.getNumberOfYears() > employee.getNumberOfYears()) {
                employee.setNumberOfYears(o.getNumberOfYears());
            }
        }

        return allocationsByActivityAndRole;
    }

    public void prepareActivityCells(Employee employee,
                                     List<MergingObject> allocationsByActivityAndRole,
                                     int totalFirstYear, int totalNumberOfYears) {
        if (!allocationsByActivityAndRole.isEmpty()) {
            List<List<AllocationCell>> list = new LinkedList<>();
            for (MergingObject object : allocationsByActivityAndRole) {
                int size = (12 * totalNumberOfYears);
                List<AllocationCell> allList = new java.util.ArrayList<>(Collections.nCopies(size, new AllocationCell()));
                int index = 0;
                int i = 0;
                while (i < size && object.getAllocations().size() > index
                        && object.getAllocations().get(index) != null) {
                    if (object.getFirstYear() > totalFirstYear
                            || object.getFirstYear() + object.getNumberOfYears() < totalFirstYear + totalNumberOfYears) {
                        for (int j = 0; j < 12; ++j) {
                            allList.set(i, new AllocationCell(employee.getFirstYear() * 100 + j, 0, 0));
                            i++;
                        }
                    } else {
                        int j = 0;
                        Date lastDate = object.getAllocations().get(index).getDateFrom();
                        long lastYear = totalFirstYear;
                        Date nextEnd = object.getAllocations().get(index).getDateUntil();
                        while(i < size && nextEnd.after(lastDate)) {
                            allList.set(i, new AllocationCell(lastYear * 100 + j,
                                    object.getAllocations().get(index).getTime(),
                                    object.getAllocations().get(index).getIsCertain()));
                            if (j == 11) {
                                lastYear++;
                                j = 0;
                            }
                            lastDate = DateUtils.addMonths(lastDate, 1);
                            ++j;
                            ++i;
                        }
                        index++;
                    }
                }
                list.add(allList);
                employee.setProjectsAllocationCells(list);
            }
        }
    }

    /**
     * Nastavi bunky pro alokace na projektu pro detail projektu.
     * Oproti predchozi metode nastavuje pro vsechny zamestnance u projektu.
     * @param employee Aktualni zamestannec
     * @param totalFirstYear Prvni rok u zamestnancu u projektu
     * @param totalNumberOfYears Celkovy pocet roku u zamestnancu u projektu
     */
    public void prepareProjectsCells(Employee employee, int totalFirstYear, int totalNumberOfYears) {
        List<Allocation> current = employee.getProjectsAllocations();

//        Zakoduju ostatni akce a pritom zjistim prvni rok a delku
        codeYearAndMonth(employee.getCoursesAllocationCells(), employee);
        codeYearAndMonth(employee.getFunctionsAllocationCells(), employee);

        List<MergingObject> allocationsByProjectAndRole = splitByRoleAndActivityId(employee, current, Allocation::getProject);

        prepareActivityCells(employee, allocationsByProjectAndRole, totalFirstYear, totalNumberOfYears);
    }

    public void prepareCoursesCells(Employee employee) {
        List<Allocation> current = employee.getCoursesAllocations();

        codeYearAndMonth(employee.getProjectsAllocationCells(), employee);
        codeYearAndMonth(employee.getFunctionsAllocationCells(), employee);

        List<MergingObject> allocationsByCourseAndRole = splitByRoleAndActivityId(employee, current, Allocation::getCourse);

        if (!allocationsByCourseAndRole.isEmpty()) {
            List<List<AllocationCell>> list = new LinkedList<>();
            for (MergingObject object : allocationsByCourseAndRole) {
                int size = (int) (12 * employee.getNumberOfYears());
                List<AllocationCell> allList = new java.util.ArrayList<>(Collections.nCopies(size, new AllocationCell()));
                int index = 0;
                int i = 0;
                while (i < size && object.getAllocations().size() > index && object.getAllocations().get(index) != null) {
                    if (object.getFirstYear() > employee.getFirstYear()
                            || object.getFirstYear() + object.getNumberOfYears() < employee.getFirstYear() + employee.getNumberOfYears()) {
                        for (int j = 0; j < 12; ++j) {
                            allList.set(i, new AllocationCell(employee.getFirstYear() * 100 + j, 0, 0));
                            i++;
                        }
                    } else {
                        int j = 0;
                        Date lastDate = object.getAllocations().get(index).getDateFrom();
                        long lastYear = employee.getFirstYear();
                        Date nextEnd = object.getAllocations().get(index).getDateUntil();
                        while(i < size && nextEnd.after(lastDate)) {
                            allList.set(i, new AllocationCell(lastYear * 100 + j,
                                    object.getAllocations().get(index).getTime(),
                                    object.getAllocations().get(index).getIsCertain()));
                            if (j == 11) {
                                lastYear++;
                                j = 0;
                            }
                            lastDate = DateUtils.addMonths(lastDate, 1);
                            ++j;
                            ++i;
                        }
                        index++;
                    }
                }
                list.add(allList);
                employee.setCoursesAllocationCells(list);
            }
        }
    }

    public void prepareFunctionsCells(Employee employee) {
        List<Allocation> current = employee.getFunctionsAllocations();

        codeYearAndMonth(employee.getProjectsAllocationCells(), employee);
        codeYearAndMonth(employee.getCoursesAllocationCells(), employee);

        List<MergingObject> allocationsByFunctionAndRole = splitByRoleAndActivityId(employee, current, Allocation::getFunction);

        if (!allocationsByFunctionAndRole.isEmpty()) {
            List<List<AllocationCell>> list = new LinkedList<>();
            for (MergingObject object : allocationsByFunctionAndRole) {
                int size = (int) (12 * employee.getNumberOfYears());
                List<AllocationCell> allList = new java.util.ArrayList<>(Collections.nCopies(size, new AllocationCell()));
                int index = 0;
                int i = 0;
                while (i < size && object.getAllocations().size() > index && object.getAllocations().get(index) != null) {
                    if (object.getFirstYear() > employee.getFirstYear()
                            || object.getFirstYear() + object.getNumberOfYears() < employee.getFirstYear() + employee.getNumberOfYears()) {
                        for (int j = 0; j < 12; ++j) {
                            allList.set(i, new AllocationCell(employee.getFirstYear() * 100 + j, 0, 0));
                            i++;
                        }
                    } else {
                        int j = 0;
                        Date lastDate = object.getAllocations().get(index).getDateFrom();
                        long lastYear = employee.getFirstYear();
                        Date nextEnd = object.getAllocations().get(index).getDateUntil();
                        while(i < size && nextEnd.after(lastDate)) {
                            allList.set(i, new AllocationCell(lastYear * 100 + j,
                                    object.getAllocations().get(index).getTime(),
                                    object.getAllocations().get(index).getIsCertain()));
                            if (j == 11) {
                                lastYear++;
                                j = 0;
                            }
                            lastDate = DateUtils.addMonths(lastDate, 1);
                            ++j;
                            ++i;
                        }
                        index++;
                    }
                }
                list.add(allList);
                employee.setFunctionsAllocationCells(list);
            }
        }
    }

    public void prepareTotalCells(Employee employee) {

        if (!employee.getProjectsAllocationCells().isEmpty()) {
            employee.setFirstYear(employee.getProjectsAllocationCells().get(0).get(0).getKey() / 100);
            employee.setNumberOfYears(employee.getProjectsAllocationCells().get(0).size());
        } else if (!employee.getCoursesAllocationCells().isEmpty()) {
            employee.setFirstYear(employee.getCoursesAllocationCells().get(0).get(0).getKey() / 100);
            employee.setNumberOfYears(employee.getCoursesAllocationCells().get(0).size());
        } else if (!employee.getFunctionsAllocationCells().isEmpty()) {
            employee.setFirstYear(employee.getFunctionsAllocationCells().get(0).get(0).getKey() / 100);
            employee.setNumberOfYears(employee.getFunctionsAllocationCells().get(0).size());
        } else {
            employee.totalCertainAllocationCells(Collections.nCopies(12, new AllocationCell(0,0L, 0)));
            employee.totalUncertainAllocationCells(Collections.nCopies(12, new AllocationCell(0,0L, 0)));
            return;
        }

        int size = Integer.max(Integer.max(employee.getProjectsAllocationCells().size(),
                employee.getCoursesAllocationCells().size()), employee.getFunctionsAllocationCells().size());

        for (int i = 0; i < size; ++i) {
            List<AllocationCell> currentCertain = new LinkedList<>();
            List<AllocationCell> currentUnCertain = new LinkedList<>();

            for (int j = 0; j < employee.getNumberOfYears(); ++j) {
                for (int k = 0; k < 12; ++k) {
                    currentCertain.add(j, new AllocationCell((employee.getFirstYear() + j) * 100L + k, 0L, 0));
                    currentUnCertain.add(j, new AllocationCell((employee.getFirstYear() + j) * 100L + k, 0L, 0));
                }
            }

            if (employee.getProjectsAllocationCells().size() > i) {
                for (int j = 0; j < employee.getProjectsAllocationCells().get(i).size(); ++j) {
                    if (employee.getProjectsAllocationCells().get(i).get(j).getCertain() == 1)
                        currentCertain.set(j, new AllocationCell(currentCertain.get(i).getKey(),
                                currentCertain.get(j).getTime() + employee.getProjectsAllocationCells().get(i).get(j).getTime(),
                                employee.getProjectsAllocationCells().get(i).get(j).getCertain()));
                    else
                        currentUnCertain.set(j, new AllocationCell(currentUnCertain.get(i).getKey(),
                                currentUnCertain.get(j).getTime() + employee.getProjectsAllocationCells().get(i).get(j).getTime(),
                                employee.getProjectsAllocationCells().get(i).get(j).getCertain()));
                }
            }
            if (employee.getCoursesAllocationCells().size() > i) {
                for (int j = 0; j < employee.getCoursesAllocationCells().get(i).size(); ++j) {
                    if (employee.getCoursesAllocationCells().get(i).get(j).getCertain() == 1)
                        currentCertain.set(j, new AllocationCell(currentUnCertain.get(i).getKey(),
                                currentUnCertain.get(j).getTime() + employee.getCoursesAllocationCells().get(i).get(j).getTime(),
                                employee.getCoursesAllocationCells().get(i).get(j).getCertain()));
                    else
                        currentUnCertain.set(j, new AllocationCell(currentUnCertain.get(i).getKey(),
                                currentUnCertain.get(j).getTime() + employee.getCoursesAllocationCells().get(i).get(j).getTime(),
                                employee.getCoursesAllocationCells().get(i).get(j).getCertain()));
                }
            }
            if (employee.getFunctionsAllocationCells().size() > i) {
                for (int j = 0; j < employee.getFunctionsAllocationCells().get(i).size(); ++j) {
                    if (employee.getFunctionsAllocationCells().get(i).get(j).getCertain() == 1)
                        currentCertain.set(j, new AllocationCell(currentCertain.get(i).getKey(),
                                currentCertain.get(j).getTime() + employee.getFunctionsAllocationCells().get(i).get(j).getTime(),
                                employee.getFunctionsAllocationCells().get(i).get(j).getCertain()));
                    else
                        currentUnCertain.set(j, new AllocationCell(currentUnCertain.get(i).getKey(),
                                currentUnCertain.get(j).getTime() + employee.getFunctionsAllocationCells().get(i).get(j).getTime(),
                                employee.getFunctionsAllocationCells().get(i).get(j).getCertain()));
                }
            }
            employee.totalCertainAllocationCells(currentCertain);
            employee.totalUncertainAllocationCells(currentUnCertain);
        }
    }

    public void prepareTotalCells(Employee employee, int first, int length) {

        if (!employee.getProjectsAllocationCells().isEmpty()) {
            employee.setFirstYear(employee.getProjectsAllocationCells().get(0).get(0).getKey() / 100);
            employee.setNumberOfYears(employee.getProjectsAllocationCells().get(0).size());
        } else if (!employee.getCoursesAllocationCells().isEmpty()) {
            employee.setFirstYear(employee.getCoursesAllocationCells().get(0).get(0).getKey() / 100);
            employee.setNumberOfYears(employee.getCoursesAllocationCells().get(0).size());
        } else if (!employee.getFunctionsAllocationCells().isEmpty()) {
            employee.setFirstYear(employee.getFunctionsAllocationCells().get(0).get(0).getKey() / 100);
            employee.setNumberOfYears(employee.getFunctionsAllocationCells().get(0).size());
        } else {
            employee.totalCertainAllocationCells(Collections.nCopies(12, new AllocationCell(0,0L, 0)));
            employee.totalUncertainAllocationCells(Collections.nCopies(12, new AllocationCell(0,0L, 0)));
            return;
        }

        int size = Integer.max(Integer.max(employee.getProjectsAllocationCells().size(),
                employee.getCoursesAllocationCells().size()), employee.getFunctionsAllocationCells().size());

        for (int i = 0; i < size; ++i) {
            List<AllocationCell> currentCertain = new LinkedList<>();
            List<AllocationCell> currentUnCertain = new LinkedList<>();

            for (int j = 0; j < employee.getNumberOfYears(); ++j) {
                for (int k = 0; k < 12; ++k) {
                    currentCertain.add(j, new AllocationCell((employee.getFirstYear() + j) * 100L + k, 0L, 0));
                    currentUnCertain.add(j, new AllocationCell((employee.getFirstYear() + j) * 100L + k, 0L, 0));
                }
            }

            if (employee.getProjectsAllocationCells().size() > i) {
                for (int j = 0; j < employee.getProjectsAllocationCells().get(i).size(); ++j) {
                    if (employee.getProjectsAllocationCells().get(i).get(j).getCertain() == 1)
                        currentCertain.set(j, new AllocationCell(currentCertain.get(i).getKey(),
                                currentCertain.get(j).getTime() + employee.getProjectsAllocationCells().get(i).get(j).getTime(),
                                employee.getProjectsAllocationCells().get(i).get(j).getCertain()));
                    else
                        currentUnCertain.set(j, new AllocationCell(currentUnCertain.get(i).getKey(),
                                currentUnCertain.get(j).getTime() + employee.getProjectsAllocationCells().get(i).get(j).getTime(),
                                employee.getProjectsAllocationCells().get(i).get(j).getCertain()));
                }
            }
            if (employee.getCoursesAllocationCells().size() > i) {
                for (int j = 0; j < employee.getCoursesAllocationCells().get(i).size(); ++j) {
                    if (employee.getCoursesAllocationCells().get(i).get(j).getCertain() == 1)
                        currentCertain.set(j, new AllocationCell(currentUnCertain.get(i).getKey(),
                                currentUnCertain.get(j).getTime() + employee.getCoursesAllocationCells().get(i).get(j).getTime(),
                                employee.getCoursesAllocationCells().get(i).get(j).getCertain()));
                    else
                        currentUnCertain.set(j, new AllocationCell(currentUnCertain.get(i).getKey(),
                                currentUnCertain.get(j).getTime() + employee.getCoursesAllocationCells().get(i).get(j).getTime(),
                                employee.getCoursesAllocationCells().get(i).get(j).getCertain()));
                }
            }
            if (employee.getFunctionsAllocationCells().size() > i) {
                for (int j = 0; j < employee.getFunctionsAllocationCells().get(i).size(); ++j) {
                    if (employee.getFunctionsAllocationCells().get(i).get(j).getCertain() == 1)
                        currentCertain.set(j, new AllocationCell(currentCertain.get(i).getKey(),
                                currentCertain.get(j).getTime() + employee.getFunctionsAllocationCells().get(i).get(j).getTime(),
                                employee.getFunctionsAllocationCells().get(i).get(j).getCertain()));
                    else
                        currentUnCertain.set(j, new AllocationCell(currentUnCertain.get(i).getKey(),
                                currentUnCertain.get(j).getTime() + employee.getFunctionsAllocationCells().get(i).get(j).getTime(),
                                employee.getFunctionsAllocationCells().get(i).get(j).getCertain()));
                }
            }
            employee.totalCertainAllocationCells(currentCertain);
            employee.totalUncertainAllocationCells(currentUnCertain);
        }
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
        String role;
        long firstYear;
        long numberOfYears;
        List<Allocation> allocations = new LinkedList<>();

        public MergingObject(long activityId, String role, Allocation allocation) {
            this.activityId = activityId;
            this.role = role;
            this.allocations.add(allocation);
        }

        public MergingObject allocations(Allocation allocations) {
            this.allocations.add(allocations);
            return this;
        }

        public void sortAllocationsByDate() {
            this.allocations.sort(Comparator.comparing(Allocation::getDateFrom));
            Calendar date = new GregorianCalendar();
            date.setTime(allocations.get(0).getDateFrom());
            this.firstYear = date.get(Calendar.YEAR);
            date.setTime(allocations.get(allocations.size() - 1).getDateUntil());
            this.numberOfYears = date.get(Calendar.YEAR) - this.firstYear;
        }
    }
}
