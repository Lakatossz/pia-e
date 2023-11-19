package cz.zcu.kiv.mjakubas.piae.sem.jdbc.mapper;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Function;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class FunctionMapper implements RowMapper<Function> {

    private static final EmployeeMapper EMPLOYEE_MAPPER = new EmployeeMapper();
    @Override
    public Function mapRow(ResultSet rs, int rowNum) throws SQLException {
        var prId = rs.getLong("function_id");
        var name = rs.getString("fnc_name");
        var defaultTime = rs.getLong("fnc_default_time");
        var dateFrom = rs.getObject("fnc_date_from", LocalDate.class);
        var dateUntil = rs.getObject("fnc_date_until", LocalDate.class);
        var probability = rs.getFloat("fnc_probability");

        var abbrevation = rs.getString("wrk_abbrevation");
        Long wrkId = null;
        try {
            wrkId = rs.getLong("fnc_workplace_id");
        } catch (Exception e) {
            //ignored
        }

        var manager = EMPLOYEE_MAPPER.mapRow(rs, rowNum);

        return new Function()
                .id(prId)
                .name(name)
                .defaultTime(defaultTime)
                .dateFrom(dateFrom)
                .dateUntil(dateUntil)
                .probability(probability)
                .functionWorkplace(Workplace.builder().id(wrkId).abbreviation(abbrevation).build())
                .functionManager(manager);
    }
}
