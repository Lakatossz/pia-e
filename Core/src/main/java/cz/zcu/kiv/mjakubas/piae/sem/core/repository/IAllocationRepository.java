package cz.zcu.kiv.mjakubas.piae.sem.core.repository;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import lombok.NonNull;

import java.util.List;

/**
 * Represents allocation repository interface for working with {@link Allocation} data class.
 *
 * @see Allocation
 */
public interface IAllocationRepository {

    /**
     * Fetch an allocation by its id. Throws runtime exception if invalid id is given.
     *
     * @param allocationId allocation id
     * @return fetched {@link Allocation}
     */
    public Allocation fetchAllocation(long allocationId);

    /**
     * Fetch all employee allocations by employee id. Throws runtime exception if invalid id is given.
     *
     * @param employeeId employee id
     * @return fetched list of {@link Allocation}
     */
    public List<Allocation> fetchEmployeeAllocations(long employeeId);

    /**
     * Fetch all employee subordinates allocations by superior id. Throws runtime exception if invalid id is given.
     *
     * @param superiorId superior id
     * @return fetched list of {@link Allocation}
     */
    public List<Allocation> fetchSubordinatesAllocations(long superiorId);

    /**
     * Fetch all project employees allocations by project id. Throws runtime exception if invalid id is given.
     *
     * @param projectId project id
     * @return fetched list of {@link Allocation}
     */
    public List<Allocation> fetchProjectAllocations(long projectId);

    /**
     * Fetch all workplace employees allocations by workplace id. Throws runtime exception if invalid id is given.
     *
     * @param workplaceId workplace id
     * @return fetched list of {@link Allocation}
     */
    public List<Allocation> fetchWorkplaceAllocations(long workplaceId);

    /**
     * Creates new {@link Allocation} from given allocation data. Given data must contain all class attributes.
     * Throws runtime exception if any problem occurs.
     *
     * @param allocation given {@link Allocation} data
     * @return true if allocation was successfully created else returns false
     */
    public boolean createAllocation(@NonNull Allocation allocation);

    /**
     * Updates existing {@link Allocation} from given allocation data and allocation id.
     * Given data must contain all class attributes. Throws runtime exception if any problem occurs.
     *
     * @param allocation   allocation data
     * @param allocationId updated allocation id
     * @return true if allocation was successfully updated else returns false
     */
    public boolean updateAllocation(@NonNull Allocation allocation, long allocationId);

    /**
     * Disables {@link Allocation} given by its id. Throws runtime exception if any problem occurs.
     *
     * @param disable      new active status
     * @param allocationId allocation id
     * @return true if allocation was successfully disabled else returns false
     */
    public boolean disableAllocation(boolean disable, long allocationId);

    /**
     * Removes {@link Allocation} given by its id. Throws runtime exception if any problem occurs.
     *
     * @param allocationId allocation id
     * @return true if allocation was successfully removed else returns false
     */
    public boolean removeAllocation(long allocationId);
}
