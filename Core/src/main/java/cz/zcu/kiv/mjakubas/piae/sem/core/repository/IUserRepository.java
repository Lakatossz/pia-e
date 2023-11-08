package cz.zcu.kiv.mjakubas.piae.sem.core.repository;

import lombok.NonNull;

/**
 * Represents user repository interface for working with spring boot security.
 */
public interface IUserRepository {

    /**
     * Creates new user as a wrapper for already existing employee.
     *
     * @param employeeId employee id
     * @param password   password
     * @return true if user was created successfully
     */
    public boolean createNewUser(long employeeId, @NonNull String password);

    /**
     * Adds new role to user by its employee orion login.
     *
     * @param orionLogin user employee orion login
     * @return true if role was added successfully
     */
    public boolean addUserRole(@NonNull String orionLogin);

    /**
     * Checks if user password is temporary.
     *
     * @param orionLogin orion login
     * @return true if password is temporary
     */
    public boolean isTemporary(@NonNull String orionLogin);

    /**
     * Updates user password.
     *
     * @param employeeId user employee id
     * @param password   password
     * @return true if password was successfully updated.
     */
    public boolean updatePassword(long employeeId, @NonNull String password);
}
