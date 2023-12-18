package cz.zcu.kiv.mjakubas.piae.sem.jdbc.mapper;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;


public class EmployeeMapper implements RowMapper<Employee> {

    @Override
    public Employee mapRow(ResultSet rs, int rowNum) throws SQLException {

        long id = rs.getLong("employee_id");
        String firstName = rs.getString("emp_first_name");
        String lastName = rs.getString("emp_last_name");
        String orionLogin = rs.getString("emp_orion_login");
        String email = rs.getString("emp_email");
        String description = rs.getString("emp_description");

        long workplaceId = rs.getLong("emp_workplace_id");
        Workplace workplace = null;
        String workplaceAbbreviation = null;
        try {
            workplaceAbbreviation = rs.getString("wrk_abbrevation");
            workplace = Workplace.builder().id(workplaceId).abbreviation(workplaceAbbreviation).build();
        } catch (Exception e) {
            workplace = Workplace.builder().id(workplaceId).build();
        }

        return new Employee()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .orionLogin(orionLogin)
                .emailAddress(email)
                .workplace(workplace)
                .dateCreated(new Date())
                .description(description);
    }
}
