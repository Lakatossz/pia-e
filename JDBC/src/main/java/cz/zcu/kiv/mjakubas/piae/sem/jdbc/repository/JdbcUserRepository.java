package cz.zcu.kiv.mjakubas.piae.sem.jdbc.repository;

import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IUserRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Primary
@Repository
@AllArgsConstructor
public class JdbcUserRepository implements IUserRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public boolean createNewUser(long employeeId, @NonNull String password) {
        var sql = """
                INSERT INTO user (password, enabled, us_employee, us_is_temp)
                VALUES (:pw, 1, :id, 1);
                """;

        var params = new MapSqlParameterSource();
        params.addValue("pw", password);
        params.addValue("id", employeeId);

        return jdbcTemplate.update(sql, params) == 1;
    }

    @Override
    public boolean addUserRole(@NonNull String orionLogin) {
        var sqlUsers = """
                SELECT user_id FROM user
                INNER JOIN employee e ON e.employee_id=us_employee
                WHERE e.emp_orion_login=:orionLogin
                """;

        var params = new MapSqlParameterSource();
        params.addValue("orionLogin", orionLogin);

        var id = jdbcTemplate.query(sqlUsers, params, (rs, rowNum) -> rs.getLong("user_id"));

        var sql = """
                INSERT INTO authority (auth_user_id, auth_authotity)
                VALUES (:id, 'USER');
                """;

        params = new MapSqlParameterSource();
        params.addValue("id", id);

        return jdbcTemplate.update(sql, params) == 1;
    }

    @Override
    public boolean isTemporary(@NonNull String orionLogin) {
        var sql = """
                SELECT us_is_temp FROM user
                INNER JOIN employee e ON e.employee_id=us_employee
                WHERE e.emp_orion_login=:orionLogin
                """;

        var params = new MapSqlParameterSource();
        params.addValue("orionLogin", orionLogin);

        return (jdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getBoolean("us_is_temp")).get(0));
    }

    @Override
    public boolean updatePassword(long employeeId, @NonNull String password) {
        var sqlUsers = """
                SELECT user_id FROM user
                INNER JOIN employee e ON e.employee_id=us_employee
                WHERE e.employee_id=:id
                """;

        var params = new MapSqlParameterSource();
        params.addValue("id", employeeId);

        var id = jdbcTemplate.query(sqlUsers, params, (rs, rowNum) -> rs.getLong("user_id"));

        var sql = """
                UPDATE user SET password=:pw, us_is_temp=0
                WHERE user_id=:id
                """;

        params = new MapSqlParameterSource();
        params.addValue("id", id);
        params.addValue("pw", password);

        return jdbcTemplate.update(sql, params) == 1;
    }
}