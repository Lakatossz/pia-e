package cz.zcu.kiv.mjakubas.piae.sem.jdbc.repository.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IAllocationRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IProjectRepository;
import cz.zcu.kiv.mjakubas.piae.sem.jdbc.mapper.EmployeeMapper;
import cz.zcu.kiv.mjakubas.piae.sem.jdbc.mapper.ProjectMapper;
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
public class JdbcProjectRepository implements IProjectRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final ProjectMapper PROJECT_MAPPER = new ProjectMapper();
    private static final EmployeeMapper EMPLOYEE_MAPPER = new EmployeeMapper();

    private final IAllocationRepository allocationRepository;

    private static final String IS_ENABLED = "isEnabled";

    private static final String PROJECT_ID = "project_id";

    @Override
    public Project fetchProject(long projectId) {
        var sql = """
                SELECT p.*, w.wrk_abbrevation, w.workplace_id, e.* FROM project p
                INNER JOIN workplace w ON w.workplace_id=p.pro_workplace_id
                INNER JOIN employee e ON e.employee_id=p.pro_manager_id
                WHERE p.pro_enabled=:isEnabled AND p.project_id=:project_id
                """;

        var params = new MapSqlParameterSource();
        params.addValue(PROJECT_ID, projectId);
        params.addValue(IS_ENABLED, true);

        return jdbcTemplate.query(sql, params, PROJECT_MAPPER).get(0);
    }

    @Override
    public Project fetchProject(String name) {
        var sql = """
                SELECT p.*, w.wrk_abbrevation, e.* FROM project p
                INNER JOIN workplace w ON w.workplace_id=p.pro_workplace_id
                INNER JOIN employee e ON e.employee_id=p.pro_manager_id
                WHERE p.pro_enabled=:isEnabled AND p.pro_name=:name
                """;

        var params = new MapSqlParameterSource();
        params.addValue("name", name);
        params.addValue(IS_ENABLED, true);

        return jdbcTemplate.query(sql, params, PROJECT_MAPPER).get(0);
    }

    @Override
    public List<Project> fetchProjects() {
        var sql = """
                SELECT p.*, w.wrk_abbrevation, e.* FROM project p
                INNER JOIN workplace w ON w.workplace_id=p.pro_workplace_id
                INNER JOIN employee e ON e.employee_id=p.pro_manager_id
                WHERE p.pro_enabled=:isEnabled
                """;

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, true);

        return jdbcTemplate.query(sql, params, PROJECT_MAPPER);
    }

    @Override
    public List<Employee> fetchProjectEmployees(long projectId) {
        var sql = """
                SELECT e.*, w.wrk_abbrevation FROM assignment pe
                INNER JOIN employee e ON e.employee_id=pe.ass_employee_id
                INNER JOIN workplace w ON w.workplace_id=e.emp_workplace_id
                WHERE pe.ass_enabled=:isEnabled AND pe.ass_project_id=:project_id
                """;

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, true);
        params.addValue(PROJECT_ID, projectId);

        return jdbcTemplate.query(sql, params, EMPLOYEE_MAPPER);
    }

    @Override
    public boolean createProject(@NonNull Project project) {
        var sql = """
                INSERT INTO project
                (pro_enabled, pro_name, pro_manager_id, pro_workplace_id,\s
                pro_date_from, pro_date_until, pro_description, pro_probability, 
                pro_budget, pro_participation, pro_total_time)
                VALUES
                (:pro_enabled, :pro_name, :pro_manager_id, :pro_workplace_id,\s
                :pro_date_from, :pro_date_until, :pro_description, :pro_probability,
                :pro_budget, :pro_participation, :pro_total_time);
                """;

        return jdbcTemplate.update(sql, prepareParams(project)) == 1;
    }

    @Override
    public boolean updateProject(@NonNull Project project, long projectId) {
        var sql = """
                UPDATE project
                SET pro_enabled = :pro_enabled, pro_name = :pro_name, pro_manager_id = :pro_manager_id,
                pro_workplace_id = :pro_workplace_id, pro_date_from = :pro_date_from, pro_description = :pro_description,
                pro_probability = :pro_probability, pro_budget = :pro_budget, pro_participation = :pro_participation,
                pro_total_time = :pro_total_time
                WHERE project_id = :project_id
                """;

        var params = prepareParams(project);
        params.addValue(PROJECT_ID, projectId);

        return jdbcTemplate.update(sql, params) == 1;
    }

    @Override
    public boolean removeProject(long projectId) {
        var sql = """
                UPDATE project
                SET pro_enabled = :pro_enabled
                WHERE pro_enabled =:isEnabled AND project_id = :project_id
                """;

        var params = new MapSqlParameterSource();
        params.addValue("pro_enabled", true);
        params.addValue(PROJECT_ID, projectId);
        params.addValue(IS_ENABLED, true);

        allocationRepository.fetchAllocationsByProjectId(projectId)
                .forEach(allocation -> allocationRepository.removeAllocation(allocation.getId()));

        return jdbcTemplate.update(sql, params) == 1;
    }

    @Override
    public boolean addEmployee(long employeeId, long projectId) {
        var sql = """
                INSERT INTO project_employee
                (ass_enabled, ass_project_id, ass_employee_id)
                VALUES
                (:isEnabled, :project_id, :ass_employee_id)
                """;

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, true);
        params.addValue(PROJECT_ID, projectId);
        params.addValue("ass_employee_id", employeeId);


        return jdbcTemplate.update(sql, params) == 1;
    }

    @Override
    public boolean removeEmployee(long employeeId, long projectId) {
        var sql = """
                UPDATE project_employee
                SET ass_enabled = :pro_enabled
                WHERE pro_enabled =:isEnabled AND ass.project_id = :project_id AND ass.employee_id=:employee_id
                """;

        var params = new MapSqlParameterSource();
        params.addValue("ass_enabled", false);
        params.addValue(PROJECT_ID, projectId);
        params.addValue(IS_ENABLED, true);
        params.addValue("employee_id", employeeId);

        allocationRepository.fetchAllocationsByProjectId(projectId)
                .forEach(allocation -> {
                    if (allocation.getWorker().getId() == employeeId)
                        allocationRepository.removeAllocation(allocation.getId());
                });

        return jdbcTemplate.update(sql, params) == 1;
    }

    private MapSqlParameterSource prepareParams(Project project) {
        var params = new MapSqlParameterSource();
        params.addValue("pro_enabled", 1);
        params.addValue("pro_name", project.getName());
        params.addValue("pro_manager_id", project.getProjectManager().getId());
        params.addValue("pro_workplace_id", project.getProjectWorkplace().getId());
        params.addValue("pro_date_from", project.getDateFrom());
        params.addValue("pro_date_until", project.getDateUntil());
        params.addValue("pro_description", project.getDescription());
        params.addValue("pro_probability", project.getProbability());
        params.addValue("pro_budget", project.getBudget());
        params.addValue("pro_participation", project.getParticipation());
        params.addValue("pro_total_time", project.getTotalTime());

        return params;
    }
}
