package cz.zcu.kiv.mjakubas.piae.sem.jdbc;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

import java.sql.SQLException;

/**
 * This is a remnant of a past idea of handling all backend errors nicely.
 */
public class CustomSQLErrorCodeTranslator extends SQLErrorCodeSQLExceptionTranslator {

    @Override
    protected DataAccessException customTranslate(final String task, final String sql, final SQLException sqlException) {
        return new InvalidDataAccessApiUsageException(sqlException.getMessage());
    }
}
