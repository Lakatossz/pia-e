package cz.zcu.kiv.mjakubas.piae.sem.core.repository;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import lombok.NonNull;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.List;

/**
 * Represents workplace repository interface for working with {@link Workplace} data class.
 *
 * @see Workplace
 */
public interface IWorkplaceRepository {

    /**
     * Fetch a workplace by its id. Throws runtime exception if invalid id is given.
     *
     * @param workplaceId workplace id
     * @return fetched {@link Workplace}
     */
    public Workplace fetchWorkplace(long workplaceId);

    /**
     * Fetch a workplace by its abbrevation. Throws runtime exception if invalid id is given.
     *
     * @param abbrevation workplace abbrevation
     * @return fetched {@link Workplace}
     */
    public Workplace fetchWorkplace(String abbrevation);

    /**
     * Fetch all workplace. Throws runtime exception if error occurs.
     *
     * @return fetched list of {@link Workplace}
     */
    public List<Workplace> fetchWorkplaces();

    /**
     * Fetch all workplace employees. Throws runtime exception if error occurs.
     *
     * @return fetched list of {@link Employee}
     */
    public List<Employee> fetchWorkplaceEmployees(long workplaceId);

    /**
     * Creates new {@link Workplace} from given employee data. Given data must contain all class attributes.
     * Throws runtime exception if any problem occurs.
     *
     * @param workplace given {@link Workplace} data
     * @return id of newly created entity
     */
    public long createWorkplace(@NonNull Workplace workplace);

    /**
     * Updates existing {@link Workplace} from given workplace data and workplace id.
     * Given data must contain all class attributes. Throws runtime exception if any problem occurs.
     *
     * @param workplace   workplace data
     * @param workplaceId updated workplace id
     * @return true if workplace was successfully updated else returns false
     */
    public boolean updateWorkplace(@NonNull Workplace workplace, long workplaceId);

    /**
     * Removes {@link Workplace} given by its id. Throws runtime exception if any problem occurs.
     * Removes also all of employee {@link Workplace}.
     *
     * @param workplaceId workplace id
     * @return true if workplace was successfully removed else returns false
     */
    public boolean removeWorkplace(long workplaceId);
}
