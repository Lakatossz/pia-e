package cz.zcu.kiv.mjakubas.piae.sem.jdbc.repository.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Course;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IAllocationRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.ICourseRepository;
import cz.zcu.kiv.mjakubas.piae.sem.jdbc.mapper.CourseMapper;
import cz.zcu.kiv.mjakubas.piae.sem.jdbc.mapper.EmployeeMapper;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Primary
@Repository
@AllArgsConstructor
public class JdbcCourseRepository implements ICourseRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final CourseMapper COURSE_MAPPER = new CourseMapper();

    private static final EmployeeMapper EMPLOYEE_MAPPER = new EmployeeMapper();

    private final IAllocationRepository allocationRepository;

    private static final String IS_ENABLED = "isEnabled";

    private static final String COURSE_ID = "course_id";

    @Override
    public Course fetchCourse(long courseId) {
        var sql = """
                SELECT c.*, w.wrk_abbrevation, w.workplace_id, e.* FROM course c
                INNER JOIN workplace w ON w.workplace_id=c.crs_workplace_id
                INNER JOIN employee e ON e.employee_id=c.crs_manager_id
                WHERE c.crs_enabled=:isEnabled AND c.course_id=:course_id
                """;

        var params = new MapSqlParameterSource();
        params.addValue(COURSE_ID, courseId);
        params.addValue(IS_ENABLED, true);

        return jdbcTemplate.query(sql, params, COURSE_MAPPER).get(0);
    }

    @Override
    public Course fetchCourse(String name) {
        var sql = """
                SELECT c.*, w.wrk_abbrevation, e.* FROM course c
                INNER JOIN workplace w ON w.workplace_id=c.crs_workplace_id
                INNER JOIN employee e ON e.employee_id=c.crs_manager_id
                WHERE c.crs_enabled=:isEnabled AND c.course_name=:name
                """;

        var params = new MapSqlParameterSource();
        params.addValue("name", name);
        params.addValue(IS_ENABLED, true);

        return jdbcTemplate.query(sql, params, COURSE_MAPPER).get(0);
    }

    @Override
    public List<Course> fetchCourses() {
        var sql = """
                SELECT c.*, w.wrk_abbrevation, w.workplace_id, e.* FROM course c
                INNER JOIN workplace w ON w.workplace_id=c.crs_workplace_id
                INNER JOIN employee e ON e.employee_id=c.crs_manager_id
                WHERE c.crs_enabled=:isEnabled
                """;

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, true);

        return jdbcTemplate.query(sql, params, COURSE_MAPPER);
    }

    @Override
    public List<Employee> fetchCourseEmployees(long courseId) {
        var sql = """
                SELECT
                        e.*, 
                        w.wrk_abbrevation
                FROM assignment ce
                INNER JOIN employee e ON e.employee_id=ce.ass_employee_id
                INNER JOIN workplace w ON w.workplace_id=e.emp_workplace_id
                WHERE ce.ass_enabled=:isEnabled AND ce.ass_course_id=:course_id
                """;

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, true);
        params.addValue(COURSE_ID, courseId);

        return jdbcTemplate.query(sql, params, EMPLOYEE_MAPPER);
    }

    @Override
    public long createCourse(@NonNull Course course) {
        var sql = """
                INSERT INTO course 
                (crs_enabled, crs_name, crs_shortcut, crs_number_of_students, crs_term, crs_lecture, crs_exercise,
                crs_lecture_required, crs_exercise_required,
                crs_lecture_length, crs_exercise_length, crs_credits, crs_date_from,
                crs_date_until, crs_probability, crs_manager_id, crs_workplace_id)
                VALUES
                (:crs_enabled, :crs_name, :crs_shortcut, :crs_number_of_students, :crs_term, :crs_lecture, :crs_exercise,
                :crs_lecture_required, :crs_exercise_required,
                :crs_lecture_length, :crs_exercise_length, :crs_credits, :crs_date_from,
                :crs_date_until, :crs_probability, :crs_manager_id, :crs_workplace_id);
                """;

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, prepareParams(course), keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    @Override
    public boolean updateCourse(@NonNull Course course, long courseId) {
        var sql = """
                UPDATE course
                SET crs_name = :crs_name, crs_shortcut = :crs_shortcut, crs_number_of_students = :crs_number_of_students, crs_term = :crs_term,
                crs_lecture_length = :crs_lecture_length, crs_exercise_length = :crs_exercise_length, crs_credits = :crs_credits,
                crs_lecture_required = :crs_lecture_required, crs_exercise_require = :crs_exercise_require,
                crs_date_until = :crs_date_until, crs_probability = :crs_probability, crs_manager_id = :crs_manager_id,
                crs_workplace_id = :crs_workplace_id
                WHERE course_id = :course_id
                """;

        var params = prepareParams(course);
        params.addValue(COURSE_ID, courseId);

        return jdbcTemplate.update(sql, params) == 1;
    }

    @Override
    public boolean removeCourse(long courseId) {
        var sql = """
                UPDATE course
                SET crs_enabled = :crs_enabled
                WHERE crs_enabled =:isEnabled AND course_id = :course_id
                """;

        var params = new MapSqlParameterSource();
        params.addValue("crs_enabled", 0);
        params.addValue(COURSE_ID, courseId);
        params.addValue(IS_ENABLED, true);

        allocationRepository.fetchAllocationsByCourseId(courseId)
                .forEach(allocation -> allocationRepository.removeAllocation(allocation.getId()));

        return jdbcTemplate.update(sql, params) == 1;
    }

    @Override
    public boolean addEmployee(long employeeId, long courseId) {
        var sql = """
                INSERT INTO course_employee
                (ass_enabled, ass_course_id, ass_employee_id)
                VALUES
                (:isEnabled, :course_id, :ass_employee_id)
                """;

        var params = new MapSqlParameterSource();
        params.addValue(IS_ENABLED, true);
        params.addValue(COURSE_ID, courseId);
        params.addValue("ass_employee_id", employeeId);


        return jdbcTemplate.update(sql, params) == 1;
    }

    @Override
    public boolean removeEmployee(long employeeId, long courseId) {
        var sql = """
                UPDATE course_employee
                SET ass_enabled = :ass_enabled
                WHERE pro_enabled =:isEnabled AND ass.course_id = :course_id AND ass.employee_id=:employee_id
                """;

        var params = new MapSqlParameterSource();
        params.addValue("ass_enabled", 0);
        params.addValue(COURSE_ID, courseId);
        params.addValue(IS_ENABLED, true);
        params.addValue("employee_id", employeeId);

        allocationRepository.fetchAllocationsByCourseId(courseId)
                .forEach(allocation -> {
                    if (allocation.getWorker().getId() == employeeId)
                        allocationRepository.removeAllocation(allocation.getId());
                });

        return jdbcTemplate.update(sql, params) == 1;
    }

    private MapSqlParameterSource prepareParams(Course course) {
        var params = new MapSqlParameterSource();
        params.addValue("crs_enabled", 1);
        params.addValue("crs_name", course.getName());
        params.addValue("crs_shortcut", course.getShortcut());
        params.addValue("crs_number_of_students", course.getNumberOfStudents());
        params.addValue("crs_term", course.getTerm());
        params.addValue("crs_lecture", 5);
        params.addValue("crs_exercise", 5);
        params.addValue("crs_lecture_length", course.getLectureLength());
        params.addValue("crs_exercise_length", course.getExerciseLength());
        params.addValue("crs_lecture_required", 1);
        params.addValue("crs_exercise_required", 1);
        params.addValue("crs_credits", course.getCredits());
        params.addValue("crs_date_from", course.getDateFrom());
        params.addValue("crs_date_until", course.getDateUntil());
        params.addValue("crs_probability", course.getProbability());
        params.addValue("crs_manager_id", course.getCourseManager().getId());
        params.addValue("crs_workplace_id", course.getCourseWorkplace().getId());

        return params;
    }
}
