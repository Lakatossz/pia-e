package cz.zcu.kiv.mjakubas.piae.sem.jdbc.mapper;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class ProjectMapper implements RowMapper<Project> {

    private static final EmployeeMapper EMPLOYEE_MAPPER = new EmployeeMapper();

    @Override
    public Project mapRow(ResultSet rs, int rowNum) throws SQLException {
        var prId = rs.getLong("project_id");
        var name = rs.getString("pro_name");
        var dateFrom = rs.getObject("pro_date_from", LocalDate.class);
        var dateUntil = rs.getObject("pro_date_until", LocalDate.class);
        var description = rs.getString("pro_description");

        var abbrevation = rs.getString("wrk_abbrevation");
        Long wrkId = null;
        try {
            wrkId = rs.getLong("workplace_id");
        } catch (Exception e) {
            //ignored
        }

        var manager = EMPLOYEE_MAPPER.mapRow(rs, rowNum);

        return new Project()
                .id(prId)
                .name(name)
                .dateFrom(dateFrom)
                .dateUntil(dateUntil)
                .description(description)
                .projectWorkplace(Workplace.builder().id(wrkId).abbreviation(abbrevation).build())
                .projectManager(manager);
    }
}
