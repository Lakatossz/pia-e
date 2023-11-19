package cz.zcu.kiv.mjakubas.piae.sem.core.vo;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Course;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Value object for representing {@link Course}.
 *
 * @see Course
 */

@Setter
@Getter
public class CourseVO {

    private long id;
    private String name;
    private LocalDate dateFrom;
    private LocalDate dateUntil;
    private float probability;
    private boolean enabled;
    private Long courseManager;
    private Long courseWorkplace;
    private int numberOfStudents;
    private String term;
    private int lectureLength;
    private int exerciseLength;
    private int credits;

    public CourseVO() {

    }

    public CourseVO(long id, String name, LocalDate dateFrom, LocalDate dateUntil, float probability,
                  Long projectManager, Long projectWorkplace, int numberOfStudents,
                  String term, int lectureLength, int excerciseLength, int credits, boolean enabled) {
        this.id = id;
        this.name = name;
        this.dateFrom = dateFrom;
        this.dateUntil = dateUntil;
        this.probability = probability;
        this.enabled = enabled;
        this.courseManager = projectManager;
        this.courseWorkplace = projectWorkplace;
        this.numberOfStudents = numberOfStudents;
        this.term = term;
        this.lectureLength = lectureLength;
        this.exerciseLength = excerciseLength;
        this.credits = credits;
    }

    public CourseVO id(long id) {
        this.id = id;
        return this;
    }

    public CourseVO name(String name) {
        this.name = name;
        return this;
    }

    public CourseVO dateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
        return this;
    }

    public CourseVO dateUntil(LocalDate dateUntil) {
        this.dateUntil = dateUntil;
        return this;
    }

    public CourseVO probability(float probability) {
        this.probability = probability;
        return this;
    }

    public CourseVO enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public CourseVO courseWorkplace(Long courseWorkplace) {
        this.courseWorkplace = courseWorkplace;
        return this;
    }

    public CourseVO courseManager(Long courseManager) {
        this.courseManager = courseManager;
        return this;
    }

    public CourseVO numberOfStudents(int numberOfStudents) {
        this.numberOfStudents = numberOfStudents;
        return this;
    }

    public CourseVO term(String term) {
        this.term = term;
        return this;
    }

    public CourseVO lectureLength(int lectureLength) {
        this.lectureLength = lectureLength;
        return this;
    }

    public CourseVO exerciseLength(int exerciseLength) {
        this.exerciseLength = exerciseLength;
        return this;
    }


    public CourseVO credits(int credits) {
        this.credits = credits;
        return this;
    }

    @Override
    public String toString() {
        return "CourseVO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dateFrom=" + dateFrom +
                ", dateUntil=" + dateUntil +
                ", probability=" + probability+
                ", numberOfStudents=" + numberOfStudents +
                ", term='" + term + '\'' +
                ", lectureLength=" + lectureLength +
                ", exerciseLength=" + exerciseLength +
                ", credits=" + credits +
                '}';
    }
}
