package cz.zcu.kiv.mjakubas.piae.sem.jdbc.mapper;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WorkplaceMapper implements RowMapper<Workplace> {

    private static final EmployeeMapper EMPLOYEE_MAPPER = new EmployeeMapper();

    @Override
    public Workplace mapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = rs.getLong("workplace_id");
        String abbreviation = rs.getString("wrk_abbrevation");
        String fullName = rs.getString("wrk_name");
        Employee manager = null;
        try {
            manager = EMPLOYEE_MAPPER.mapRow(rs, rowNum);
        } catch (Exception e) {
            //ignored
        }

        return new Workplace(id, abbreviation, fullName, manager);
    }
}
