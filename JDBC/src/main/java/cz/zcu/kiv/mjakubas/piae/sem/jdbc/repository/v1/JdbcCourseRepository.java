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
import org.springframework.stereotype.Repository;

import java.util.List;

@Primary
@Repository
@AllArgsConstructor
public class JdbcCourseRepository implements ICourseRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final CourseMapper COURSE_MAPPER = new CourseMapper();

    private static final EmployeeMapper EMPLOYEE_MAPPER = new EmployeeMapper();

    private final IAllocationRepository allocationRepository;

    @Override
    public Course fetchCourse(long courseId) {
        var sql = """
                SELECT c.*, w.wrk_abbrevation, w.workplace_id, e.* FROM course c
                INNER JOIN workplace w ON w.workplace_id=c.crs_workplace_id
                INNER JOIN employee e ON e.employee_id=c.crs_manager_id
                WHERE c.crs_enabled=:isEnabled AND c.course_id=:id
                """;

        var params = new MapSqlParameterSource();
        params.addValue("id", courseId);
        params.addValue("isEnabled", true);

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
        params.addValue("isEnabled", true);

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
        params.addValue("isEnabled", true);

        return jdbcTemplate.query(sql, params, COURSE_MAPPER);
    }

    @Override
    public List<Employee> fetchCourseEmployees(long courseId) {
        var sql = """
                SELECT e.*, w.wrk_abbrevation FROM assignment ce
                INNER JOIN workplace w ON w.workplace_id=e.emp_workplace_id
                INNER JOIN employee e ON e.employee_id=ce.ass_employee_id
                WHERE c.crs_enabled=:isEnabled AND ce.ass.course_id=:course_id
                """;

        var params = new MapSqlParameterSource();
        params.addValue("isEnabled", true);
        params.addValue("course_id", courseId);

        return jdbcTemplate.query(sql, params, EMPLOYEE_MAPPER);
    }

    @Override
    public boolean createCourse(@NonNull Course course) {
        var sql = """
                INSERT INTO course 
                (course_id, crs_name, crs_number_of_students, crs_term,
                crs_lecture_length, crs_exercise_length, crs_credits, crs_date_from,
                crs_date_until, crs_probability, crs_manager_id, crs_workplace_id)
                VALUES (
                (:course_id, :crs_name, :crs_number_of_students, :crs_term,
                :crs_lecture_length, :crs_exercise_length, :crs_credits, :crs_date_from,
                :crs_date_until, :crs_probability, :crs_manager_id, :crs_workplace_id);
                
                """;

        var params = new MapSqlParameterSource();
        params.addValue("crs_enabled", 1);
        params.addValue("crs_name", course.getName());
        params.addValue("crs_number_of_students", course.getNumberOfStudents());
        params.addValue("crs_term", course.getTerm());
        params.addValue("crs_lecture_length", course.getLectureLength());
        params.addValue("crs_exercise_length", course.getExerciseLength());
        params.addValue("crs_credits", course.getCredits());
        params.addValue("crs_date_from", course.getDateFrom());
        params.addValue("crs_date_until", course.getDateUntil());
        params.addValue("crs_probability", course.getProbability());
        params.addValue("crs_manager_id", course.getCourseManager());
        params.addValue("crs_workplace_id", course.getCourseWorkplace());


        return jdbcTemplate.update(sql, params) == 1;
    }

    @Override
    public boolean updateCourse(@NonNull Course course, long courseId) {
        var sql = """
                UPDATE course
                SET crs_name = :crs_name, crs_number_of_students = :crs_number_of_students, crs_term = :crs_term,
                crs_lecture_length = :crs_lecture_length, crs_exercise_length = :crs_exercise_length, crs_credits = :crs_credits,
                crs_date_until = :crs_date_until, crs_probability = :crs_probability, crs_manager_id = :crs_manager_id,
                crs_workplace_id = :crs_workplace_id
                WHERE course_id = :course_id
                """;

        var params = new MapSqlParameterSource();
        params.addValue("crs_enabled", 1);
        params.addValue("crs_name", course.getName());
        params.addValue("crs_number_of_students", course.getNumberOfStudents());
        params.addValue("crs_term", course.getTerm());
        params.addValue("crs_lecture_length", course.getLectureLength());
        params.addValue("crs_exercise_length", course.getExerciseLength());
        params.addValue("crs_credits", course.getCredits());
        params.addValue("crs_date_from", course.getDateFrom());
        params.addValue("crs_date_until", course.getDateUntil());
        params.addValue("crs_probability", course.getProbability());
        params.addValue("crs_manager_id", course.getCourseManager());
        params.addValue("crs_workplace_id", course.getCourseWorkplace());
        params.addValue("crs_workplace_id", course.getCourseWorkplace());
        params.addValue("course_id", courseId);

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
        params.addValue("crs_enabled", true);
        params.addValue("course_id", courseId);
        params.addValue("isEnabled", true);

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
                (:ass_enabled, :ass_course_id, :ass_employee_id)
                """;

        var params = new MapSqlParameterSource();
        params.addValue("ass_enabled", true);
        params.addValue("ass_course_id", courseId);
        params.addValue("ass_employee_id", employeeId);


        return jdbcTemplate.update(sql, params) == 1;
    }

    @Override
    public boolean removeEmployee(long employeeId, long courseId) {
        var sql = """
                UPDATE course_employee
                SET ass_enabled = :pro_enabled
                WHERE pro_enabled =:isEnabled AND ass.course_id_id = :course_id_id AND ass.employee_id=:employee_id
                """;

        var params = new MapSqlParameterSource();
        params.addValue("ass_enabled", false);
        params.addValue("course_id", courseId);
        params.addValue("isEnabled", true);
        params.addValue("employee_id", employeeId);

        allocationRepository.fetchAllocationsByCourseId(courseId)
                .forEach(allocation -> {
                    if (allocation.getWorker().getId() == employeeId)
                        allocationRepository.removeAllocation(allocation.getId());
                });

        return jdbcTemplate.update(sql, params) == 1;
    }
}
