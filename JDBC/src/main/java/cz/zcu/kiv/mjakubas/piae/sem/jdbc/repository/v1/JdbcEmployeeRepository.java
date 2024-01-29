package cz.zcu.kiv.mjakubas.piae.sem.jdbc.repository.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IAllocationRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IEmployeeRepository;
import cz.zcu.kiv.mjakubas.piae.sem.jdbc.mapper.EmployeeMapper;
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
public class JdbcEmployeeRepository implements IEmployeeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final EmployeeMapper EMPLOYEE_MAPPER = new EmployeeMapper();

    private final IAllocationRepository allocationRepository;

    private static final String IS_ENABLED = "isEnabled";

    private static final String EMPLOYEE_ID = "employee_id";

    @Override
    public Employee fetchEmployee(long employeeId) {
        var sql = """
                SELECT e.*, w.wrk_abbrevation FROM employee e
                INNER JOIN workplace w ON w.workplace_id = e.emp_workplace_id
                WHERE emp_enabled=:isEnabled AND employee_id=:employee_id;
                """;

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, 1);
        params.addValue(EMPLOYEE_ID, employeeId);

        return jdbcTemplate.query(sql, params, EMPLOYEE_MAPPER).get(0);
    }

    @Override
    public Employee fetchEmployee(String orionLogin) {
        var sql = """
                SELECT e.*, w.wrk_abbrevation FROM employee e
                INNER JOIN workplace w ON w.workplace_id = e.emp_workplace_id
                WHERE emp_enabled=:isEnabled AND emp_orion_login=:orionLogin;
                """;

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, 1);
        params.addValue("orionLogin", orionLogin);

        return jdbcTemplate.query(sql, params, EMPLOYEE_MAPPER).get(0);
    }

    @Override
    public List<Employee> fetchEmployees() {
        var sql = """
                SELECT e.*, w.wrk_abbrevation
                FROM employee e
                INNER JOIN workplace w ON w.workplace_id = e.emp_workplace_id
                WHERE emp_enabled=:isEnabled;
                """;

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, 1);

        return jdbcTemplate.query(sql, params, EMPLOYEE_MAPPER);
    }

    @Override
    public List<Employee> fetchSubordinates(long superiorId) {
        var sql = """
                SELECT e.*, w.wrk_abbrevation FROM employee e
                INNER JOIN superior s ON s.sup_employee_id=e.employee_id
                INNER JOIN workplace w ON w.workplace_id=e.emp_workplace_id
                WHERE sup_enabled=:isEnabled AND s.sup_superior_id=:superior_id;
                """;

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, 1);
        params.addValue("superior_id", superiorId);

        return jdbcTemplate.query(sql, params, EMPLOYEE_MAPPER);
    }

    @Override
    public long createEmployee(@NonNull Employee employee) {
        var sql = """
                INSERT INTO employee
                (emp_enabled, emp_workplace_id, emp_first_name, emp_last_name, emp_orion_login, emp_email, emp_description)
                VALUES
                (:emp_enabled, :emp_workplace_id, :emp_first_name, :emp_last_name, :emp_orion_login, :emp_email, :emp_description)
                """;

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, prepareParams(employee), keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    @Override
    public boolean updateEmployee(@NonNull Employee employee, long employeeId) {
        var sql = """
                UPDATE employee
                SET emp_enabled = :emp_enabled, emp_workplace_id = :emp_workplace_id, emp_first_name = :emp_first_name,
                 emp_last_name = :emp_last_name, emp_orion_login = :emp_orion_login, emp_email = :emp_email, 
                 emp_description = :emp_description
                WHERE emp_enabled=:emp_enabled AND employee_id = :employee_id
                """;

        var params = prepareParams(employee);
        params.addValue(EMPLOYEE_ID, employeeId);

        return jdbcTemplate.update(sql, params) == 1;
    }

    @Override
    public boolean removeEmployee(long employeeId) {
        var sql = """
                UPDATE employee
                SET emp_enabled = :remove
                WHERE emp_enabled=:isEnabled AND employee_id = :employee_id
                """;

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, 1);
        params.addValue(EMPLOYEE_ID, employeeId);
        params.addValue("remove", false);

        var allocations = allocationRepository.fetchEmployeeAllocations(employeeId);
        allocations.forEach(allocation -> allocationRepository.removeAllocation(employeeId));

        return jdbcTemplate.update(sql, params) == 1;
    }

    @Override
    public boolean addSubordinate(long superiorId, long subordinateId) {
        var sql = """
                INSERT INTO superior
                (sup_enabled, sup_superior_id, sup_employee_id)
                VALUES
                (:sup_enabled, :sup_superior_id, :sup_employee_id)
                """;

        var params = new MapSqlParameterSource();
        params.addValue("sup_enabled", 1);
        params.addValue("sup_superior_id", superiorId);
        params.addValue("sup_employee_id", subordinateId);

        return jdbcTemplate.update(sql, params) == 1;
    }

    @Override
    public boolean removeSubordinate(long subordinateItemId) {
        var sql = """
                UPDATE superior
                SET emp_enabled = :remove
                WHERE sup_enabled=:isEnabled
                """;

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, 1);
        params.addValue(EMPLOYEE_ID, subordinateItemId);
        params.addValue("remove", true);

        return jdbcTemplate.update(sql, params) == 1;
    }

    private MapSqlParameterSource prepareParams(Employee employee) {
        var params = new MapSqlParameterSource();
        params.addValue("emp_enabled", 1);
        params.addValue("emp_workplace_id", employee.getWorkplace().getId());
        params.addValue("emp_first_name", employee.getFirstName());
        params.addValue("emp_last_name", employee.getLastName());
        params.addValue("emp_orion_login", employee.getOrionLogin());
        params.addValue("emp_email", employee.getEmailAddress());
        params.addValue("emp_description", employee.getDescription());

        return params;
    }
}
