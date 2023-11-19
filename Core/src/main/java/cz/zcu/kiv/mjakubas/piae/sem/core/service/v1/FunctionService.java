package cz.zcu.kiv.mjakubas.piae.sem.core.service.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Function;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IFunctionRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.ServiceException;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.FunctionVO;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
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
    private final EmployeeService employeeService;
    private final WorkplaceService workplaceService;

    private final AllocationService allocationService;

    /**
     * Gets function by its id. Throws SQL error if function doesn't exist.
     *
     * @param id function id
     * @return function
     */
    public Function getFunction(long id) {
        return functionRepository.fetchFunction(id);
    }

    /**
     * Gets all functions. Throws SQL error if something happens.
     *
     * @return list of {@link Function}
     */
    public List<Function> getFunctions() {
        return functionRepository.fetchFunctions();
    }

    /**
     * Gets all function employees. Throws SQL error if function doesn't exist.
     *
     * @param id function id
     * @return list of function {@link Employee}
     */
    public List<Employee> getFunctionEmployees(long id) {
        return functionRepository.fetchFunctionEmployees(id);
    }

    /**
     * Gets all functions of workplace manager. Throws SQL error if manager doesn't exist.
     *
     * @param id manager id
     * @return list of workplace {@link Function}
     */
    public List<Function> getWorkplaceManagerFunctions(long id) {
        var workplaces = workplaceService.getWorkplaces();
        var myWorkplaces = new ArrayList<Workplace>();

        for (Workplace w : workplaces) {
            if (w.getManager().getId() == id)
                myWorkplaces.add(w);
        }

        var functions = functionRepository.fetchFunctions();
        var myFunctions = new ArrayList<Function>();
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
    public void createFunction(@NonNull FunctionVO functionVO) {
        var data = employeeService.getEmployee(functionVO.getFunctionManager());
        if (functionVO.getDateUntil() != null) {
            if (functionVO.getDateFrom().isAfter(functionVO.getDateUntil()))
                throw new ServiceException();
        }

        Function function = new Function()
                .name(functionVO.getName())
                .functionManager(Employee.builder().id(data.getId()).build())
                .functionWorkplace(Workplace.builder().id(functionVO.getFunctionWorkplace()).build())
                .dateFrom(functionVO.getDateFrom())
                .dateUntil(functionVO.getDateUntil() != null ? functionVO.getDateUntil() : LocalDate.of(9999, 9, 9));

        if (!functionRepository.createFunction(function))
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
        var data = employeeService.getEmployee(functionVO.getFunctionManager());
        if (functionVO.getDateUntil() != null) {
            if (functionVO.getDateFrom().isAfter(functionVO.getDateUntil()))
                throw new ServiceException();
        }
        var processed = allocationService.processAllocations(allocationService.getFunctionAllocations(id).getAllocations());
        if (!processed.isEmpty()) {
            if (processed.get(0).getFrom().isBefore(functionVO.getDateFrom())
                    || processed.get(processed.size() - 1).getUntil().isAfter(functionVO.getDateUntil()))
                throw new ServiceException();
        }


        Function function = new Function()
                .id(id)
                .name(functionVO.getName())
                .functionManager(Employee.builder().id(data.getId()).build())
                .functionWorkplace(Workplace.builder().id(functionVO.getFunctionWorkplace()).build())
                .dateFrom(functionVO.getDateFrom())
                .dateUntil(functionVO.getDateUntil() != null ? functionVO.getDateUntil() : LocalDate.of(9999, 9, 9));

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
        var legitId = employeeService.getEmployee(userVO.getOrionLogin()).getId();

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
     * Gets all functions of a function manager.
     *
     * @param employeeId function manager id
     * @return list of {@link Function}
     */
    public List<Function> getManagerFunctions(long employeeId) {
        var functions = functionRepository.fetchFunctions();
        var myFunctions = new ArrayList<Function>();
        functions.forEach(function -> {
            if (function.getFunctionManager().getId() == employeeId)
                myFunctions.add(function);
        });
        return myFunctions;
    }
}
