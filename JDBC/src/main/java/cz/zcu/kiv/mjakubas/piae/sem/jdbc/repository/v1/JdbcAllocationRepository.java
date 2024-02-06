package cz.zcu.kiv.mjakubas.piae.sem.jdbc.repository.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IAllocationRepository;
import cz.zcu.kiv.mjakubas.piae.sem.jdbc.mapper.AllocationMapper;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * JDBC implementation of {@link IAllocationRepository}.
 *
 * @see IAllocationRepository
 */
@Primary
@Repository
@AllArgsConstructor
public class JdbcAllocationRepository implements IAllocationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final AllocationMapper ALLOCATION_MAPPER = new AllocationMapper();

    private static final String IS_ENABLED = "isEnabled";

    private static final String ACTIVE = "active";

    private static final String ID = "id";

    @Override
    public Allocation fetchAllocation(long allocationId) {
        var sql = """
                SELECT a.* FROM assignment a
                WHERE a.ass_enabled=:isEnabled AND a.assignment_id=:id
                """;

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, 1);
        params.addValue(ID, allocationId);

        return jdbcTemplate.query(sql, params, ALLOCATION_MAPPER).get(0);
    }

    @Override
    public List<Allocation> fetchEmployeeAllocations(long employeeId) {
        var sql = """
                SELECT a.* FROM assignment a
                WHERE a.ass_enabled=:isEnabled AND a.ass_employee_id=:id
                """;

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, 1);
        params.addValue(ID, employeeId);

        return jdbcTemplate.query(sql, params, ALLOCATION_MAPPER);
    }

    @Override
    public List<Allocation> fetchSubordinatesAllocations(long superiorId) {
        var sql = """
                SELECT a.* FROM assignment a
                INNER JOIN superior s ON s.sup_employee_id=a.ass_employee_id
                WHERE a.ass_enabled=:isEnabled AND s.sup_superior_id=:id
                """;

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, 1);
        params.addValue(ID, superiorId);

        return jdbcTemplate.query(sql, params, ALLOCATION_MAPPER);
    }

    @Override
    public List<Allocation> fetchAllocationsByProjectId(long projectId) {
        var sql = """
                SELECT a.* FROM assignment a
                WHERE a.ass_enabled=:isEnabled AND a.ass_project_id=:id
                """;

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, 1);
        params.addValue(ID, projectId);

        return jdbcTemplate.query(sql, params, ALLOCATION_MAPPER);
    }

    @Override
    public List<Allocation> fetchAllocationsByCourseId(long courseId) {
        var sql = """
                SELECT a.* FROM assignment a
                WHERE a.ass_enabled=:isEnabled AND a.ass_course_id=:id
                """;

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, 1);
        params.addValue(ID, courseId);

        return jdbcTemplate.query(sql, params, ALLOCATION_MAPPER);
    }

    @Override
    public List<Allocation> fetchAllocationsByFunctionId(long functionId) {
        var sql = """
                SELECT a.* FROM assignment a
                WHERE a.ass_enabled=:isEnabled AND a.ass_function_id=:id
                """;

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, 1);
        params.addValue(ID, functionId);

        return jdbcTemplate.query(sql, params, ALLOCATION_MAPPER);
    }

    @Override
    public List<Allocation> fetchWorkplaceAllocations(long workplaceId) { /* TODO */
        var sql = """
                SELECT a.* FROM workplace w
                INNER JOIN project p ON p.pro_workplace_id=w.workplace_id
                INNER JOIN assignment a ON a.ass_project_id=p.project_id
                WHERE a.ass_enabled=:isEnabled AND w.workplace_id=:id
                """;

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, 1);
        params.addValue(ID, workplaceId);

        return jdbcTemplate.query(sql, params, ALLOCATION_MAPPER);
    }

    @Override
    public long createAllocation(@NonNull Allocation allocation) {

        var sql = """
                INSERT INTO assignment
                (ass_enabled, ass_employee_id, ass_project_id, ass_course_id, ass_function_id, ass_is_certain,
                ass_active_from, ass_active_until, ass_term, ass_scope, ass_time, ass_description, ass_active, ass_role)
                VALUES
                (:isEnabled, :eId, :pId, :cId, :fId, :isCertain, :aFrom, :aUntil, :aTerm, :scope, :time, :descr, :active, :role)
                """;

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, prepareParams(allocation), keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    @Override
    public boolean updateAllocation(@NonNull Allocation allocation, long allocationId) {
        var sql = """
                UPDATE assignment
                SET ass_enabled=:isEnabled, ass_employee_id=:eId, ass_project_id=:pId, 
                ass_course_id=:cId, ass_function_id=:fId, ass_active_from=:aFrom, 
                ass_active_until=:aUntil, ass_term=:aTerm, ass_scope=:scope, ass_time=:time, ass_description=:descr, 
                ass_active=:active, ass_is_certain=:isCertain, ass_role=:role
                WHERE assignment_id=:id
                """;

        var params = prepareParams(allocation);
        params.addValue(ID, allocationId);

        return jdbcTemplate.update(sql, params) == 1;
    }

    @Override
    public boolean disableAllocation(boolean disable, long allocationId) {
        var sql = """
                UPDATE assignment
                SET ass_active=:active
                WHERE assignment_id=:id
                """;

        var params = new MapSqlParameterSource();

        params.addValue(ACTIVE, !disable);
        params.addValue(ID, allocationId);

        return jdbcTemplate.update(sql, params) == 1;
    }

    @Override
    public boolean removeAllocation(long allocationId) {
        var sql = """
                UPDATE assignment
                SET ass_enabled=:active
                WHERE assignment_id=:id
                """;

        var params = new MapSqlParameterSource();

        params.addValue(ACTIVE, 0);
        params.addValue(ID, allocationId);

        return jdbcTemplate.update(sql, params) == 1;
    }

    private MapSqlParameterSource prepareParams(Allocation allocation) {
        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, 1);
        params.addValue("eId", allocation.getWorker().getId());
        params.addValue("pId", allocation.getProject() != null ? allocation.getProject().getId() : null);
        params.addValue("cId", allocation.getCourse() != null ? allocation.getCourse().getId() : null);
        params.addValue("fId", allocation.getFunction() != null ? allocation.getFunction().getId() : null);
        params.addValue("aFrom", allocation.getDateFrom());
        params.addValue("aUntil", allocation.getDateUntil());
        params.addValue("aTerm", allocation.getTerm().getValue());
        params.addValue("scope", allocation.getAllocationScope());
        params.addValue("time", (float) allocation.getAllocationScope() / (40 * 60));
        params.addValue("isCertain", allocation.getIsCertain());
        params.addValue("role", allocation.getRole());
        params.addValue(ACTIVE, allocation.getActive());
        params.addValue("descr", allocation.getDescription());

        return params;
    }
}
