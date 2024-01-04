package cz.zcu.kiv.mjakubas.piae.sem.core.repository;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Function;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import lombok.NonNull;

import java.util.List;

/**
 * Represents function repository interface for working with {@link Function} data class.
 *
 * @see Workplace
 */
public interface IFunctionRepository {

    /**
     * Fetch a function by its id. Throws runtime exception if invalid id is given.
     *
     * @param functionId function id
     * @return fetched {@link Function}
     */
    public Function fetchFunction(long functionId);

    /**
     * Fetch a function by its name. Throws runtime exception if invalid id is given.
     *
     * @param name function name
     * @return fetched {@link Function}
     */
    public Function fetchFunction(String name);

    /**
     * Fetch all functions. Throws runtime exception if error occurs.
     *
     * @return fetched list of {@link Function}
     */
    public List<Function> fetchFunctions();

    /**
     * Fetch all function employees. Throws runtime exception if error occurs.
     *
     * @return fetched list of {@link Employee}
     */
    public List<Employee> fetchFunctionEmployees(long functionId);

    /**
     * Creates new {@link Function} from given employee data. Given data must contain all class attributes.
     * Throws runtime exception if any problem occurs.
     *
     * @param function given {@link Function} data
     * @return id of newly created entity
     */
    public long createFunction(@NonNull Function function);

    /**
     * Updates existing {@link Function} from given workplace data and workplace id.
     * Given data must contain all class attributes. Throws runtime exception if any problem occurs.
     *
     * @param function workplace data
     * @param functionId updated workplace id
     * @return true if workplace was successfully updated else returns false
     */
    public boolean updateFunction(@NonNull Function function, long functionId);

    public boolean removeFunction(long functionId);

    /**
     * Adds employee to a function.
     *
     * @param employeeId employee id
     * @param functionId  function id
     * @return true if employee was successfully created.
     */
    public boolean addEmployee(long employeeId, long functionId);

    public boolean removeEmployee(long employeeId, long functionId);
}
