package cz.zcu.kiv.mjakubas.piae.sem.jdbc.mapper;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Course;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class CourseMapper implements RowMapper<Course> {

    private static final EmployeeMapper EMPLOYEE_MAPPER = new EmployeeMapper();

    @Override
    public Course mapRow(ResultSet rs, int rowNum) throws SQLException {

        var crId = rs.getLong("course_id");
        var name = rs.getString("crs_name");
        var dateFrom = rs.getObject("crs_date_from", LocalDate.class);
        var dateUntil = rs.getObject("crs_date_until", LocalDate.class);
        var probability = rs.getFloat("crs_probability");
        var numberOfStudents = rs.getInt("crs_number_of_students");
        var term = rs.getString("crs_term");
        var lectureLength = rs.getInt("crs_lecture_legnth");
        var exerciseLength = rs.getInt("crs_exercise_length");
        var credits = rs.getInt("crs_credits");

        var abbrevation = rs.getString("wrk_abbrevation");
        Long wrkId = null;
        try {
            wrkId = rs.getLong("crs_workplace_id");
        } catch (Exception e) {
            //ignored
        }

        var manager = EMPLOYEE_MAPPER.mapRow(rs, rowNum);

        return new Course()
                .id(crId)
                .name(name)
                .courseManager(manager)
                .courseWorkplace(Workplace.builder().id(wrkId).abbreviation(abbrevation).build())
                .dateFrom(dateFrom)
                .dateUntil(dateUntil)
                .probability(probability)
                .numberOfStudents(numberOfStudents)
                .term(term)
                .lectureLength(lectureLength)
                .exerciseLength(exerciseLength)
                .credits(credits);
    }
}
