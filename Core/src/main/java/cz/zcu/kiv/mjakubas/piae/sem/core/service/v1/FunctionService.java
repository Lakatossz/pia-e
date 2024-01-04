package cz.zcu.kiv.mjakubas.piae.sem.core.service.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Function;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IEmployeeRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IFunctionRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IWorkplaceRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.ServiceException;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.FunctionVO;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Service for working with allocations.
 */
@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class FunctionService {

    private final IFunctionRepository functionRepository;
    private final IEmployeeRepository employeeRepository;
    private final IWorkplaceRepository workplaceRepository;

    private final AllocationService allocationService;

    /**
     * Gets function by its id. Throws SQL error if function doesn't exist.
     *
     * @param id function id
     * @return function
     */
    public Function getFunction(long id) {
        Function function = functionRepository.fetchFunction(id);
        List<Allocation> allocations = allocationService.getFunctionAllocations(id).getAllocations();
        if (!allocations.isEmpty()) {
            function.setYearAllocation(prepareAllocations(allocations));
            function.setFunctionAllocations(allocations);
            function.setEmployees(functionRepository.fetchFunctionEmployees(id));
        }
        return function;
    }

    /**
     * Gets all functions. Throws SQL error if something happens.
     *
     * @return list of {@link Function}
     */
    public List<Function> getFunctions() {
        List<Function> functions = functionRepository.fetchFunctions();
        functions.forEach(function -> {
            List<Allocation> allocations = allocationService.getFunctionAllocations(function.getId()).getAllocations();
            if (!allocations.isEmpty()) {
                function.setYearAllocation(prepareAllocations(allocations));
                function.setFunctionAllocations(allocations);
                function.setEmployees(functionRepository.fetchFunctionEmployees(function.getId()));
            }
        });
        return functions;
    }

    /**
     * Gets all function employees. Throws SQL error if function doesn't exist.
     *
     * @param id function id
     * @return list of function {@link Employee}
     */
    public List<Employee> getFunctionEmployees(long id) {
        List<Employee> employees = functionRepository.fetchFunctionEmployees(id);
        employees.forEach(employee -> {
            employee.setUncertainTime((float) 0.0);
            employee.setCertainTime((float) 0.0);
        });
        return employees;
    }

    /**
     * Gets all functions of workplace manager. Throws SQL error if manager doesn't exist.
     *
     * @param id manager id
     * @return list of workplace {@link Function}
     */
    public List<Function> getWorkplaceManagerFunctions(long id) {
        var workplaces = workplaceRepository.fetchWorkplaces();
        var myWorkplaces = new LinkedList<Workplace>();

        for (Workplace w : workplaces) {
            if (w.getManager().getId() == id)
                myWorkplaces.add(w);
        }

        var functions = functionRepository.fetchFunctions();
        var myFunctions = new LinkedList<Function>();
        for (Function f : functions) {
            for (Workplace myW : myWorkplaces) {
                if (Objects.equals(f.getFunctionWorkplace().getId(), myW.getId()))
                    myFunctions.add(f);
            }
        }

        return myFunctions;
    }

    /**
     * Creates new function. All {@link FunctionVO} data must be available.
     * Might throw SQL or Service exception if data validation fails.
     *
     * @param functionVO functionVO
     */
    @Transactional
    public long createFunction(@NonNull FunctionVO functionVO) {
        var manager = employeeRepository.fetchEmployee(functionVO.getFunctionManagerId());
        if (functionVO.getDateUntil() != null && (functionVO.getDateFrom().after(functionVO.getDateUntil())))
                {throw new ServiceException();
        }

        Function function = new Function()
                .name(functionVO.getName())
                .dateFrom(functionVO.getDateFrom())
                .dateUntil(functionVO.getDateUntil() != null ? functionVO.getDateUntil() : Date.from(
                        Instant.from(LocalDate.of(9999, 9, 9))))
                .probability(functionVO.getProbability())
                .functionManager(manager)
                .functionWorkplace(Workplace.builder().id(functionVO.getFunctionWorkplace()).build())
                .defaultTime(functionVO.getDefaultTime());

        long id = functionRepository.createFunction(function);

        if (id > 0)
            return id;
        else
            throw new ServiceException();
    }

    /**
     * Updates existing function. Throws SQL or Service exception if function doesn't exist or data validation fails.
     *
     * @param functionVO function data
     * @param id        function id
     */
    @Transactional
    public void editFunction(@NonNull FunctionVO functionVO, long id) {
        var manager = employeeRepository.fetchEmployee(functionVO.getFunctionManagerId());
        if (functionVO.getDateUntil() != null && (functionVO.getDateFrom().after(functionVO.getDateUntil())))
                {throw new ServiceException();
        }
        var processed = allocationService.processAllocations(allocationService.getFunctionAllocations(id).getAllocations());
        if (!processed.isEmpty() && (processed.get(0).getFrom().before(functionVO.getDateFrom())
                    || processed.get(processed.size() - 1).getUntil().after(functionVO.getDateUntil())))
                    throw new ServiceException();

        Function function = new Function()
                .id(id)
                .name(functionVO.getName())
                .dateFrom(functionVO.getDateFrom())
                .dateUntil(functionVO.getDateUntil() != null ? functionVO.getDateUntil() : Date.from(
                        Instant.from(LocalDate.of(9999, 9, 9))))
                .probability(functionVO.getProbability())
                .functionManager(manager)
                .functionWorkplace(Workplace.builder().id(functionVO.getFunctionWorkplace()).build())
                .defaultTime(functionVO.getDefaultTime())
                .description(functionVO.getDescription());

        if (!functionRepository.updateFunction(function, id))
            throw new ServiceException();
    }

    /**
     * Assigns employee to a function.
     *
     * @param userVO user data
     * @param id     function id
     */
    @Transactional
    public void assignEmployee(@NonNull EmployeeVO userVO, long id) {
        var legitId = employeeRepository.fetchEmployee(userVO.getOrionLogin()).getId();

        var employees = functionRepository.fetchFunctionEmployees(id);
        var check = new HashSet<Long>();
        for (Employee e : employees) {
            check.add(e.getId());
        }
        if (check.contains(legitId))
            throw new ServiceException();

        if (!functionRepository.addEmployee(legitId, id))
            throw new ServiceException();
    }

    /**
     * Gets all functions of a function employee.
     *
     * @param employeeId function employee id
     * @return list of {@link Function}
     */
    public List<Function> getEmployeeFunctions(long employeeId) {
        var functions = functionRepository.fetchFunctions();
        var myFunctions = new LinkedList<Function>();
        functions.forEach(function -> {
            function.setEmployees(functionRepository.fetchFunctionEmployees(function.getId()));
            function.setYearAllocation(
                    prepareAllocations(allocationService.getFunctionAllocations(function.getId()).getAllocations()));
            if (function.getEmployees().stream().filter(employee -> employee.getId() == employeeId).toList().size() == 1)
                myFunctions.add(function);
            function.setFunctionAllocations(new LinkedList<>(allocationService.getFunctionAllocations(function.getId()).getAllocations()
                    .stream().filter(allocation -> allocation.getWorker().getId() == employeeId).toList()));
        });

        return myFunctions;
    }

    /**
     * Gets all functions of a function manager.
     *
     * @param employeeId function manager id
     * @return list of {@link Function}
     */
    public List<Function> getManagerFunctions(long employeeId) {
        var functions = functionRepository.fetchFunctions();
        var myFunctions = new LinkedList<Function>();
        functions.forEach(function -> {
            function.setYearAllocation(
                    prepareAllocations(allocationService.getFunctionAllocations(function.getId()).getAllocations()));
            if (function.getFunctionManager().getId() == employeeId)
                myFunctions.add(function);
        });
        return myFunctions;
    }

    public List<Allocation> prepareFirst(List<Function> functions) {
        List<Allocation> firstAllocations = new LinkedList<>();
        functions.forEach(function -> {
            switch(function.getFunctionAllocations().size()) {
                case 0: {
                    firstAllocations.add(new Allocation().time(-1));
                    function.functionAllocations(new LinkedList<>());
                    break;
                }
                case 1: {
                    firstAllocations.add(function.getFunctionAllocations().get(0));
                    function.functionAllocations(new LinkedList<>());
                    break;
                }
                default: {
                    firstAllocations.add(function.getFunctionAllocations().remove(0));
                    function.functionAllocations(function.getFunctionAllocations());
                    break;
                }
            }
        });

        return firstAllocations;
    }

    /**
     * Prepares allocations times for using.
     * @param allocations allocations
     * @return list of allocations
     */
    private List<Float> prepareAllocations(List<Allocation> allocations) {
        List<Float> yearAllocations = new LinkedList<>(Collections.nCopies(12, (float) 0));

        List<Allocation> thisYearsAllocations = new LinkedList<>();
        allocations.forEach(allocation -> {
            if (isThisYearAllocation(allocation))
                thisYearsAllocations.add(allocation);
        });

        int allocationsIndex = 0;

        if (!thisYearsAllocations.isEmpty()) {
            Allocation allocation = thisYearsAllocations.get(allocationsIndex);

//        Here I will go through every month of the year and add to list 0 or time for function.
            for (int i = 1; i < 13; i++) {
                if ((allocation.getDateFrom().getMonth() <= i
                        && allocation.getDateUntil().getMonth() >= i)
                        || (i == 1 && isThisYearAllocation(allocation)
                        && allocation.getDateFrom().getYear() < LocalDate.now().getYear())
                        || (i == 12 && isThisYearAllocation(allocation)
                        && allocation.getDateUntil().getYear() > LocalDate.now().getYear()))
                    yearAllocations.set(i - 1, allocation.getTime());
                else {
                    if (allocation.getDateUntil().getMonth() == i)
                        allocation = thisYearsAllocations.get(allocationsIndex++);
                    else
                        yearAllocations.set(i - 1, (float) 0);
                }
            }
            return yearAllocations;
        } else
            return new LinkedList<>();
    }

    private boolean isThisYearAllocation(Allocation allocation) {
        return allocation.getDateFrom().getYear() == LocalDate.now().getYear()
                || allocation.getDateUntil().getYear() == LocalDate.now().getYear();
    }
}
