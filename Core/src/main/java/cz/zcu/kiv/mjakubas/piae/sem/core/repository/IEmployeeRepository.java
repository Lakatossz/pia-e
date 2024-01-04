package cz.zcu.kiv.mjakubas.piae.sem.core.repository;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import lombok.NonNull;

import java.util.List;

/**
 * Represents employee repository interface for working with {@link Employee} data class.
 *
 * @see Employee
 */
public interface IEmployeeRepository {

    /**
     * Fetch an employee by its id. Throws runtime exception if invalid id is given.
     *
     * @param employeeId employee id
     * @return fetched {@link Employee}
     */
    public Employee fetchEmployee(long employeeId);

    /**
     * Fetch an employee by its orion login. Throws runtime exception if invalid id is given.
     *
     * @param orionLogin employee orion login
     * @return fetched {@link Employee}
     */
    public Employee fetchEmployee(String orionLogin);

    /**
     * Fetch all employees. Throws runtime exception if error occurs.
     *
     * @return fetched list of {@link Employee}
     */
    public List<Employee> fetchEmployees();

    /**
     * Fetch all employee subordinates by its id. Throws runtime exception if invalid id is given.
     *
     * @return fetched list of {@link Employee}
     */
    public List<Employee> fetchSubordinates(long superiorId);

    /**
     * Creates new {@link Employee} from given employee data. Given data must contain all class attributes.
     * Throws runtime exception if any problem occurs.
     *
     * @param employee given {@link Employee} data
     * @return id of newly created entity
     */
    public long createEmployee(@NonNull Employee employee);

    /**
     * Updates existing {@link Employee} from given employee data and employee id.
     * Given data must contain all class attributes. Throws runtime exception if any problem occurs.
     *
     * @param employee   employee data
     * @param employeeId updated employee id
     * @return true if employee was successfully updated else returns false
     */
    public boolean updateEmployee(@NonNull Employee employee, long employeeId);

    /**
     * Removes {@link Employee} given by its id. Throws runtime exception if any problem occurs.
     * Removes also all of employee {@link Allocation}.
     *
     * @param employeeId employee id
     * @return true if employee was successfully removed else returns false
     */
    public boolean removeEmployee(long employeeId);

    /**
     * Creates new {@link Employee} subordinate by its subordinate id and superior id.
     * Throws runtime exception if any problem occurs.
     *
     * @param subordinateId subordinate {@link Employee} id
     * @param superiorId    superior {@link Employee} id
     * @return true if employees subordinate was successfully created else returns false
     */
    public boolean addSubordinate(long superiorId, long subordinateId);

    /**
     * Removes {@link Employee} subordinate by its subordinate item id. Throws runtime exception if any problem occurs.
     *
     * @param subordinateItemId subordinate item id
     * @return true if subordinate was successfully removed else returns false
     */
    public boolean removeSubordinate(long subordinateItemId);
}
