package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Course extends Activity {

    private int numberOfStudents;
    private String term;
    private int lectureLength;
    private int excerciseLength;
    private int credits;

    public Course() {

    }

    public Course(long id, String name, LocalDate dateFrom, LocalDate dateUntil, float probability, int numberOfStudents, String term, int lectureLength, int excerciseLength, int credits) {
        super(id, name, dateFrom, dateUntil, probability);
        this.numberOfStudents = numberOfStudents;
        this.term = term;
        this.lectureLength = lectureLength;
        this.excerciseLength = excerciseLength;
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

    public Course numberOfStudents(int numberOfStudents) {
        this.numberOfStudents = numberOfStudents;
        return this;
    }

    public Course term(String term) {
        this.term = term;
        return this;
    }

    public void lectureLength(int lectureLength) {
        this.lectureLength = lectureLength;
    }

    public Course excerciseLength(int excerciseLength) {
        this.excerciseLength = excerciseLength;
        return this;
    }


    public Course credits(int credits) {
        this.credits = credits;
        return this;
    }

    @Override
    public String toString() {
        return "Course{" + super.toString() +
                ", numberOfStudents=" + numberOfStudents +
                ", term='" + term + '\'' +
                ", lectureLength=" + lectureLength +
                ", excerciseLength=" + excerciseLength +
                ", credits=" + credits +
                '}';
    }
}
