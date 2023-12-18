package cz.zcu.kiv.mjakubas.piae.sem.jdbc.mapper;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Course;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Function;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class AllocationMapper implements RowMapper<Allocation> {
    @Override
    public Allocation mapRow(ResultSet rs, int rowNum) throws SQLException {
        var id = rs.getLong("assignment_id");
        var employeeId = rs.getLong("ass_employee_id");
        var projectId = rs.getLong("ass_project_id");
        var courseId = rs.getLong("ass_course_id");
        var functionId = rs.getLong("ass_function_id");
        Date activeFrom = rs.getObject("ass_active_from", Date.class);
        Date activeUntil = rs.getObject("ass_active_until", Date.class);
        var scope = rs.getInt("ass_scope");
        var description = rs.getString("ass_description");
        var active = rs.getBoolean("ass_active");
        var time = rs.getFloat("ass_time");
        var isCertain = rs.getLong("ass_is_certain");
        var role = rs.getString("ass_role");

        Project project = new Project().id(projectId);
        Course course = new Course().id(courseId);
        Function function = new Function().id(functionId);

        return new Allocation()
                .id(id)
                .worker(new Employee().id(employeeId))
                .project(project)
                .course(course)
                .function(function)
                .dateFrom(activeFrom)
                .dateUntil(activeUntil)
                .allocationScope(scope)
                .description(description)
                .time(time)
                .isCertain(isCertain)
                .role(role)
                .active(active);
    }
}
