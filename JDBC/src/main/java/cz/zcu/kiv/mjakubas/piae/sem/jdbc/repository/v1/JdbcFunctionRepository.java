package cz.zcu.kiv.mjakubas.piae.sem.jdbc.repository.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Function;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IAllocationRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IFunctionRepository;
import cz.zcu.kiv.mjakubas.piae.sem.jdbc.mapper.EmployeeMapper;
import cz.zcu.kiv.mjakubas.piae.sem.jdbc.mapper.FunctionMapper;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Primary
@Repository
@AllArgsConstructor
public class JdbcFunctionRepository implements IFunctionRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final FunctionMapper FUNCTION_MAPPER = new FunctionMapper();
    private static final EmployeeMapper EMPLOYEE_MAPPER = new EmployeeMapper();

    private final IAllocationRepository allocationRepository;

    private static final String IS_ENABLED = "isEnabled";

    private static final String FUNCTION_ID = "function_id";

    @Override
    public Function fetchFunction(long functionId) {
        var sql = """
                SELECT f.*, w.wrk_abbrevation, w.workplace_id, e.* FROM `function` f
                INNER JOIN workplace w ON w.workplace_id=f.fnc_workplace_id
                INNER JOIN employee e ON e.employee_id=f.fnc_manager_id
                WHERE f.fnc_enabled=:isEnabled AND f.function_id=:function_id
                """;

        var params = new MapSqlParameterSource();
        params.addValue(FUNCTION_ID, functionId);
        params.addValue(IS_ENABLED, true);

        return jdbcTemplate.query(sql, params, FUNCTION_MAPPER).get(0);
    }

    @Override
    public Function fetchFunction(String name) {
        var sql = """
                SELECT f.*, w.wrk_abbrevation, e.* FROM `function` f
                INNER JOIN workplace w ON w.workplace_id=f.fnc_workplace_id
                INNER JOIN employee e ON e.employee_id=f.fnc_manager_id
                WHERE f.fnc_enabled=:isEnabled AND f.fnc_name=:name
                """;

        var params = new MapSqlParameterSource();
        params.addValue("name", name);
        params.addValue(IS_ENABLED, true);

        return jdbcTemplate.query(sql, params, FUNCTION_MAPPER).get(0);
    }

    @Override
    public List<Function> fetchFunctions() {
        var sql = """
                SELECT f.*, w.wrk_abbrevation, e.* FROM `function` f
                INNER JOIN workplace w ON w.workplace_id=f.fnc_workplace_id
                INNER JOIN employee e ON e.employee_id=f.fnc_manager_id
                WHERE f.fnc_enabled=:isEnabled
                """;

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, true);

        return jdbcTemplate.query(sql, params, FUNCTION_MAPPER);
    }

    @Override
    public List<Employee> fetchFunctionEmployees(long functionId) {
        var sql = """
                SELECT 
                        e.*, 
                        w.wrk_abbrevation
                FROM assignment fe
                INNER JOIN employee e ON e.employee_id=fe.ass_employee_id
                INNER JOIN workplace w ON w.workplace_id=e.emp_workplace_id
                WHERE fe.ass_enabled=:isEnabled AND fe.ass_function_id=:function_id
                """;

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, true);
        params.addValue(FUNCTION_ID, functionId);

        return jdbcTemplate.query(sql, params, EMPLOYEE_MAPPER);
    }

    @Override
    public boolean createFunction(@NonNull Function function) {
        var sql = """
                INSERT INTO `function`
                (fnc_enabled, fnc_name, fnc_manager_id, fnc_workplace_id,\s
                fnc_date_from, fnc_date_until, fnc_description, fnc_probability, 
                fnc_default_time)
                VALUES
                (:fnc_enabled, :fnc_name, :fnc_manager_id, :fnc_workplace_id,\s
                :fnc_date_from, :fnc_date_until, :fnc_description, :fnc_probability,
                :fnc_default_time);
                """;

        return jdbcTemplate.update(sql, prepareParams(function)) == 1;
    }

    @Override
    public boolean updateFunction(@NonNull Function function, long functionId) {
        var sql = """
                UPDATE `function`
                SET fnc_enabled = :fnc_enabled, fnc_name = :fnc_name, fnc_manager_id = :fnc_manager_id, 
                fnc_workplace_id = :fnc_workplace_id, fnc_date_from = :fnc_date_from, fnc_date_until = :fnc_date_until,
                fnc_description = :fnc_description, fnc_probability = :fnc_probability, 
                fnc_default_time = :fnc_default_time, fnc_description = :fnc_description
                WHERE function_id = :function_id;
                """;

        var params = prepareParams(function);
        params.addValue(FUNCTION_ID, function.getId());

        return jdbcTemplate.update(sql, params) == 1;
    }

    @Override
    public boolean removeFunction(long functionId) {
        var sql = """
                UPDATE `function`
                SET fnc_enabled = :fnc_enabled
                WHERE fnc_enabled =:isEnabled AND function_id = :function_id
                """;

        var params = new MapSqlParameterSource();
        params.addValue("fnc_enabled", true);
        params.addValue(FUNCTION_ID, functionId);
        params.addValue(IS_ENABLED, true);

        allocationRepository.fetchAllocationsByFunctionId(functionId)
                .forEach(allocation -> allocationRepository.removeAllocation(allocation.getId()));

        return jdbcTemplate.update(sql, params) == 1;
    }

    @Override
    public boolean addEmployee(long employeeId, long functionId) {
        var sql = """
                INSERT INTO function_employee
                (ass_enabled, ass_function_id, ass_employee_id)
                VALUES
                (:isEnabled, :function_id, :ass_employee_id)
                """;

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, true);
        params.addValue(FUNCTION_ID, functionId);
        params.addValue("ass_employee_id", employeeId);


        return jdbcTemplate.update(sql, params) == 1;
    }

    @Override
    public boolean removeEmployee(long employeeId, long functionId) {
        var sql = """
                UPDATE function_employee
                SET ass_enabled = :pro_enabled
                WHERE pro_enabled =:isEnabled AND ass.function_id = :function_id AND ass.employee_id=:employee_id
                """;

        var params = new MapSqlParameterSource();
        params.addValue("ass_enabled", false);
        params.addValue(FUNCTION_ID, functionId);
        params.addValue(IS_ENABLED, true);
        params.addValue("employee_id", employeeId);

        allocationRepository.fetchAllocationsByFunctionId(functionId)
                .forEach(allocation -> {
                    if (allocation.getWorker().getId() == employeeId)
                        allocationRepository.removeAllocation(allocation.getId());
                });

        return jdbcTemplate.update(sql, params) == 1;
    }

    private MapSqlParameterSource prepareParams(Function function) {
        var params = new MapSqlParameterSource();
        params.addValue("fnc_enabled", 1);
        params.addValue("fnc_name", function.getName());
        params.addValue("fnc_manager_id", function.getFunctionManager().getId());
        params.addValue("fnc_workplace_id", function.getFunctionWorkplace().getId());
        params.addValue("fnc_date_from", function.getDateFrom());
        params.addValue("fnc_date_until", function.getDateUntil());
        params.addValue("fnc_probability", function.getProbability());
        params.addValue("fnc_default_time", function.getDefaultTime());
        params.addValue("fnc_description", function.getDescription());

        return params;
    }
}
