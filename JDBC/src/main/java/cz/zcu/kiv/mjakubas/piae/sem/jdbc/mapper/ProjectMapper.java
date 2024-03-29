package cz.zcu.kiv.mjakubas.piae.sem.jdbc.mapper;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.ProjectState;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class ProjectMapper implements RowMapper<Project> {

    private static final EmployeeMapper EMPLOYEE_MAPPER = new EmployeeMapper();

    @Override
    public Project mapRow(ResultSet rs, int rowNum) throws SQLException {
        var prId = rs.getLong("project_id");
        var name = rs.getString("pro_name");
        var shortcut = rs.getString("pro_shortcut");
        var dateFrom = rs.getObject("pro_date_from", Date.class);
        var dateUntil = rs.getObject("pro_date_until", Date.class);
        var description = rs.getString("pro_description");
        var budget = rs.getInt("pro_budget");
        var budgetParticipation = rs.getInt("pro_budget_participation");
        var probability = rs.getFloat("pro_probability");
        var totalTime = rs.getFloat("pro_total_time");
        var agency = rs.getString("pro_agency");
        var grantTitle = rs.getString("pro_grant_title");
        var state = rs.getString("pro_state");

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
                .shortcut(shortcut)
                .dateFrom(dateFrom)
                .dateUntil(dateUntil)
                .description(description)
                .budget(budget)
                .budgetParticipation(budgetParticipation)
                .probability(probability)
                .totalTime(totalTime)
                .agency(agency)
                .grantTitle(grantTitle)
                .projectWorkplace(Workplace.builder().id(wrkId).abbreviation(abbrevation).build())
                .projectManager(manager)
                .state(ProjectState.getByValue(state));
    }
}
