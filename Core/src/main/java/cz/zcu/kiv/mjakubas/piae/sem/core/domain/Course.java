package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Course extends Activity {

    private boolean enabled;
    private Employee courseManager;
    private Workplace courseWorkplace;
    private int numberOfStudents;
    private String term;
    private int lectureLength;
    private int exerciseLength;
    private int credits;

    public Course() {

    }

    public Course(long id, String name, LocalDate dateFrom, LocalDate dateUntil, float probability,
                  Employee projectManager, Workplace projectWorkplace, int numberOfStudents,
                  String term, int lectureLength, int excerciseLength, int credits, boolean enabled) {
        super(id, name, dateFrom, dateUntil, probability);
        this.enabled = enabled;
        this.courseManager = projectManager;
        this.courseWorkplace = projectWorkplace;
        this.numberOfStudents = numberOfStudents;
        this.term = term;
        this.lectureLength = lectureLength;
        this.exerciseLength = excerciseLength;
        this.credits = credits;
    }

    public Course id(long id) {
        super.id(id);
        return this;
    }

    public Course name(String name) {
        super.name(name);
        return this;
    }

    public Course dateFrom(LocalDate dateFrom) {
        super.dateFrom(dateFrom);
        return this;
    }

    public Course dateUntil(LocalDate dateUntil) {
        super.dateUntil(dateUntil);
        return this;
    }

    public Course probability(float probability) {
        super.probability(probability);
        return this;
    }

    public Course enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Course courseWorkplace(Workplace courseWorkplace) {
        this.courseWorkplace = courseWorkplace;
        return this;
    }

    public Course courseManager(Employee courseManager) {
        this.courseManager = courseManager;
        return this;
    }

    public Course numberOfStudents(int numberOfStudents) {
        this.numberOfStudents = numberOfStudents;
        return this;
    }

    public Course term(String term) {
        this.term = term;
        return this;
    }

    public Course lectureLength(int lectureLength) {
        this.lectureLength = lectureLength;
        return this;
    }

    public Course exerciseLength(int exerciseLength) {
        this.exerciseLength = exerciseLength;
        return this;
    }


    public Course credits(int credits) {
        this.credits = credits;
        return this;
    }

    @Override
    public String toString() {
        return "Course{" + super.toString() +
                ", courseManager=" + courseManager +
                ", courseWorkplace=" + courseWorkplace +
                ", numberOfStudents=" + numberOfStudents +
                ", term='" + term + '\'' +
                ", lectureLength=" + lectureLength +
                ", exerciseLength=" + exerciseLength +
                ", credits=" + credits +
                '}';
    }
}
