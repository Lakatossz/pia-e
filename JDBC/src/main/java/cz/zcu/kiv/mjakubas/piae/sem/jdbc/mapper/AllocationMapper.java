package cz.zcu.kiv.mjakubas.piae.sem.jdbc.mapper;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class AllocationMapper implements RowMapper<Allocation> {
    @Override
    public Allocation mapRow(ResultSet rs, int rowNum) throws SQLException {
        var id = rs.getLong("assignment_id");
        var employeeId = rs.getLong("ass_employee_id");
        var projectId = rs.getLong("ass_project_id");
        LocalDate activeFrom = rs.getObject("ass_active_from", LocalDate.class);
        LocalDate activeUntil = rs.getObject("ass_active_until", LocalDate.class);
        var scope = rs.getInt("ass_scope");
        var description = rs.getString("ass_description");
        var active = rs.getBoolean("ass_active");

        Project project = null;
        try {
            var name = rs.getString("pro_name");
            project = new Project().id(projectId).name(name);
        } catch (Exception e) {
            project = new Project().id(projectId);
        }

        return new Allocation()
                .id(id)
                .worker(Employee.builder().id(employeeId).build())
                .project(project)
                .dateFrom(activeFrom)
                .dateUntil(activeUntil)
                .allocationScope(scope)
                .description(description)
                .active(active);
    }
}
