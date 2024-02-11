package cz.zcu.kiv.mjakubas.piae.sem.jdbc.repository.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IWorkplaceRepository;
import cz.zcu.kiv.mjakubas.piae.sem.jdbc.mapper.EmployeeMapper;
import cz.zcu.kiv.mjakubas.piae.sem.jdbc.mapper.WorkplaceMapper;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Primary
@Repository
@AllArgsConstructor
public class JdbcWorkplaceRepository implements IWorkplaceRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final WorkplaceMapper WORKPLACE_MAPPER = new WorkplaceMapper();

    private static final EmployeeMapper EMPLOYEE_MAPPER = new EmployeeMapper();

    private static final String IS_ENABLED = "isEnabled";

    @Override
    public Workplace fetchWorkplace(long workplaceId) {
        var sql = "SELECT w.*, e.* FROM workplace w " +
                "LEFT JOIN employee e ON e.employee_id=w.wrk_manager_id " +
                "WHERE wrk_enabled=:isEnabled AND workplace_id=:wrk_id;";

        var params = new MapSqlParameterSource();
        params.addValue("wrk_id", workplaceId);
        params.addValue(IS_ENABLED, true);

        return jdbcTemplate.query(sql, params, WORKPLACE_MAPPER).get(0);
    }

    @Override
    public Workplace fetchWorkplace(String abbrevation) {
        var sql = "SELECT w.*, e.* FROM workplace w " +
                "LEFT JOIN employee e ON e.employee_id=w.wrk_manager_id " +
                "WHERE wrk_enabled=:isEnabled AND wrk_abbrevation=:wrk_abbrevation;";

        var params = new MapSqlParameterSource();
        params.addValue("wrk_abbrevation", abbrevation);
        params.addValue(IS_ENABLED, true);

        return jdbcTemplate.query(sql, params, WORKPLACE_MAPPER).get(0);
    }

    @Override
    public List<Workplace> fetchWorkplaces() {
        var sql = "SELECT w.*, e.* FROM workplace w " +
                "LEFT JOIN employee e ON e.employee_id=w.wrk_manager_id " +
                "WHERE w.wrk_enabled=1;";

        return jdbcTemplate.query(sql, WORKPLACE_MAPPER);
    }

    @Override
    public List<Employee> fetchWorkplaceEmployees(long workplaceId) {
        var sql = "SELECT e.*,  w.wrk_abbrevation " +
                "FROM employee e " +
                "INNER JOIN workplace w ON w.workplace_id=e.emp_workplace_id " +
                "WHERE emp_enabled=:isEnabled AND w.workplace_id=:id;";

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, true);
        params.addValue("id", workplaceId);

        return jdbcTemplate.query(sql, EMPLOYEE_MAPPER);
    }

    @Override
    public long createWorkplace(@NonNull Workplace workplace) {
        var sql = "INSERT INTO workplace " +
                "(wrk_enabled, wrk_abbrevation, wrk_name, wrk_description, wrk_manager_id) " +
                "VALUES " +
                "(:wrk_enabled, :wrk_abbreviation, :wrk_name, :wrk_description, :wrk_manager_id);";

        var params = new MapSqlParameterSource();
        params.addValue("wrk_enabled", true);
        params.addValue("wrk_abbreviation", workplace.getAbbreviation());
        params.addValue("wrk_name", workplace.getFullName());
        params.addValue("wrk_manager_id", workplace.getManager().getId());
        params.addValue("wrk_description", workplace.getDescription());

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    @Override
    public boolean updateWorkplace(@NonNull Workplace workplace, long workplaceId) {
        var sql = "UPDATE workplace " +
                "SET wrk_enabled = :wrk_enabled, wrk_abbrevation = :wrk_abbrevation, " +
                "wrk_name = :wrk_name, wrk_manager_id = :wrk_manager_id, wrk_description = :wrk_description " +
                "WHERE workplace_id = :workplace_id;";

        var params = new MapSqlParameterSource();
        params.addValue("wrk_enabled", 1);
        params.addValue("wrk_abbrevation", workplace.getAbbreviation());
        params.addValue("wrk_name", workplace.getFullName());
        if (workplace.getManager() != null) params.addValue("wrk_manager_id", workplace.getManager().getId());
        else params.addValue("wrk_manager_id", null);
        params.addValue("workplace_id", workplace.getId());
        params.addValue("wrk_description", workplace.getDescription());

        var rowsUpdated = jdbcTemplate.update(sql, params);

        return rowsUpdated == 1;
    }

    @Override
    public boolean removeWorkplace(long workplaceId) {
        var sql = "UPDATE workplace " +
                "SET wrk_enabled = :wrk_enabled " +
                "WHERE workplace_id = :workplace_id;";
        var params = new MapSqlParameterSource();
        params.addValue("wrk_enabled", 0);
        params.addValue("workplace_id", workplaceId);

        var rowsUpdated = jdbcTemplate.update(sql, params);

        return rowsUpdated == 1;
    }
}
