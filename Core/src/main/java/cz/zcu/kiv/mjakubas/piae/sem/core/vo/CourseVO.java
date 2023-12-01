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
    private String shortcut;
    private Long courseManagerId;
    private String courseManagerName;
    private Long courseWorkplace;
    private int numberOfStudents;
    private String term;
    private int lectureLength;
    private int exerciseLength;
    private int credits;
    private LocalDate introduced;
    private int lectureRequired;
    private int exerciseRequired;

    public CourseVO() {

    }

    public CourseVO(long id, String name, LocalDate dateFrom, LocalDate dateUntil, float probability, String shortcut,
                    Long courseManagerId, String courseManagerName, Long projectWorkplace, int numberOfStudents,
                    String term, int lectureLength, int excerciseLength, int credits, boolean enabled,
                    LocalDate introduced, int lectureRequired, int exerciseRequired) {
        this.id = id;
        this.name = name;
        this.dateFrom = dateFrom;
        this.dateUntil = dateUntil;
        this.probability = probability;
        this.enabled = enabled;
        this.shortcut = shortcut;
        this.courseManagerId = courseManagerId;
        this.courseManagerName = courseManagerName;
        this.courseWorkplace = projectWorkplace;
        this.numberOfStudents = numberOfStudents;
        this.term = term;
        this.lectureLength = lectureLength;
        this.exerciseLength = excerciseLength;
        this.credits = credits;
        this.introduced = introduced;
        this.lectureRequired = lectureRequired;
        this.exerciseRequired = exerciseRequired;
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

    public CourseVO shortcut(String shortcut) {
        this.shortcut = shortcut;
        return this;
    }

    public CourseVO courseWorkplace(Long courseWorkplace) {
        this.courseWorkplace = courseWorkplace;
        return this;
    }

    public CourseVO courseManagerId(Long courseManagerId) {
        this.courseManagerId = courseManagerId;
        return this;
    }

    public CourseVO courseManagerName(String courseManagerName) {
        this.courseManagerName = courseManagerName;
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

    public CourseVO introduced(LocalDate introduced) {
        this.introduced = introduced;
        return this;
    }

    public CourseVO lectureRequired(int lectureRequired) {
        this.lectureRequired = lectureRequired;
        return this;
    }

    public CourseVO exerciseRequired(int exerciseRequired) {
        this.exerciseRequired = exerciseRequired;
        return this;
    }

    @Override
    public String toString() {
        return "CourseVO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dateFrom=" + dateFrom +
                ", dateUntil=" + dateUntil +
                ", probability=" + probability +
                ", courseManagerId=" + courseManagerId +
                ", courseManagerName=" + courseManagerName +
                ", numberOfStudents=" + numberOfStudents +
                ", term='" + term + '\'' +
                ", lectureLength=" + lectureLength +
                ", exerciseLength=" + exerciseLength +
                ", credits=" + credits +
                ", introduced=" + introduced +
                ", lectureRequired=" + lectureRequired +
                ", exerciseRequired=" + exerciseRequired +
                '}';
    }
}
