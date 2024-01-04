package cz.zcu.kiv.mjakubas.piae.sem.core.vo;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Course;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * Value object for representing {@link Course}.
 *
 * @see Course
 */

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class CourseVO {

    private long id;
    private String name;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateFrom;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateUntil;
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
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date introduced;
    private int lectureRequired;
    private int exerciseRequired;
    private String description;

    public CourseVO id(long id) {
        this.id = id;
        return this;
    }

    public CourseVO name(String name) {
        this.name = name;
        return this;
    }

    public CourseVO dateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
        return this;
    }

    public CourseVO dateUntil(Date dateUntil) {
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

    public CourseVO introduced(Date introduced) {
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

    public CourseVO description(String description) {
        this.description = description;
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
                ", description=" + description +
                '}';
    }
}
