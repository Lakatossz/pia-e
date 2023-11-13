package cz.zcu.kiv.mjakubas.piae.sem.jdbc.repository.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IAllocationRepository;
import cz.zcu.kiv.mjakubas.piae.sem.jdbc.mapper.AllocationMapper;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    @Override
    public Allocation fetchAllocation(long allocationId) {
        var sql = """
                SELECT a.*, p.pro_name FROM assignment a
                INNER JOIN project p ON p.project_id=a.ass_project_id
                WHERE a.ass_enabled=:isEnabled AND a.assignment_id=:id
                """;

        var params = new MapSqlParameterSource();
        params.addValue("isEnabled", 1);
        params.addValue("id", allocationId);

        return jdbcTemplate.query(sql, params, ALLOCATION_MAPPER).get(0);
    }

    @Override
    public List<Allocation> fetchEmployeeAllocations(long employeeId) {
        var sql = """
                SELECT a.*, p.pro_name FROM assignment a
                INNER JOIN project p ON p.project_id=a.ass_project_id
                WHERE a.ass_enabled=:isEnabled AND a.ass_employee_id=:id
                """;

        var params = new MapSqlParameterSource();
        params.addValue("isEnabled", 1);
        params.addValue("id", employeeId);

        return jdbcTemplate.query(sql, params, ALLOCATION_MAPPER);
    }

    @Override
    public List<Allocation> fetchSubordinatesAllocations(long superiorId) {
        var sql = """
                SELECT a.*, p.pro_name FROM assignment a
                INNER JOIN project p ON p.project_id=a.ass_project_id
                INNER JOIN superior s ON s.sup_employee_id=a.ass_employee_id
                WHERE a.ass_enabled=:isEnabled AND s.sup_superior_id=:id
                """;

        var params = new MapSqlParameterSource();
        params.addValue("isEnabled", 1);
        params.addValue("id", superiorId);

        return jdbcTemplate.query(sql, params, ALLOCATION_MAPPER);
    }

    @Override
    public List<Allocation> fetchProjectAllocations(long projectId) {
        var sql = """
                SELECT a.* FROM assignment a
                WHERE a.ass_enabled=:isEnabled AND a.ass_project_id=:id
                """;

        var params = new MapSqlParameterSource();
        params.addValue("isEnabled", 1);
        params.addValue("id", projectId);

        return jdbcTemplate.query(sql, params, ALLOCATION_MAPPER);
    }

    @Override
    public List<Allocation> fetchWorkplaceAllocations(long workplaceId) {
        var sql = """
                SELECT a.* FROM workplace w
                INNER JOIN project p ON p.pro_workplace_id=w.workplace_id
                INNER JOIN assignment a ON a.ass_project_id=p.project_id
                WHERE a.ass_enabled=:isEnabled AND w.workplace_id=:id
                """;

        var params = new MapSqlParameterSource();
        params.addValue("isEnabled", 1);
        params.addValue("id", workplaceId);

        return jdbcTemplate.query(sql, params, ALLOCATION_MAPPER);
    }

    @Override
    public boolean createAllocation(@NonNull Allocation allocation) {
        var sql = """
                INSERT INTO assignment
                (ass_enabled, ass_employee_id, ass_project_id, ass_active_from, ass_active_until, ass_scope, ass_description, ass_active)
                VALUES
                (:isEnabled, :eId, :pId, :aFrom, :aUntil, :scope, :descr, :active)
                """;

        var params = new MapSqlParameterSource();
        params.addValue("isEnabled", 1);
        params.addValue("eId", allocation.getWorker().getId());
        params.addValue("pId", allocation.getProject().getId());
        params.addValue("aFrom", allocation.getDateFrom());
        params.addValue("aUntil", allocation.getDateUntil());
        params.addValue("scope", allocation.getAllocationScope());
        params.addValue("descr", allocation.getDescription());
        params.addValue("active", 1);

        return jdbcTemplate.update(sql, params) == 1;
    }

    @Override
    public boolean updateAllocation(@NonNull Allocation allocation, long allocationId) {
        var sql = """
                UPDATE assignment
                SET ass_enabled=:isEnabled, ass_employee_id=:eId, ass_project_id=:pId, ass_active_from=:aFrom,\s
                ass_active_until=:aUntil, ass_scope=:scope, ass_description=:descr, ass_active=:active
                WHERE assignment_id=:id
                """;

        var params = new MapSqlParameterSource();
        params.addValue("isEnabled", 1);
        params.addValue("eId", allocation.getWorker().getId());
        params.addValue("pId", allocation.getProject().getId());
        params.addValue("aFrom", allocation.getDateFrom());
        params.addValue("aUntil", allocation.getDateUntil());
        params.addValue("scope", allocation.getAllocationScope());
        params.addValue("active", allocation.getActive());
        params.addValue("id", allocationId);
        params.addValue("descr", allocation.getDescription());

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

        params.addValue("active", !disable);
        params.addValue("id", allocationId);

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

        params.addValue("active", true);
        params.addValue("id", allocationId);

        return jdbcTemplate.update(sql, params) == 1;
    }
}
