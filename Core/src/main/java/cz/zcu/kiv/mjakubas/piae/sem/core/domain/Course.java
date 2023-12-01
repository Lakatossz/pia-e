package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Course extends Activity {

//    Values saved directly in DB table.
    private boolean enabled;
    private Employee courseManager;
    private Workplace courseWorkplace;
    private int numberOfStudents;
    private String term;
    private int lectureLength;
    private int exerciseLength;
    private int credits;
    private LocalDate introduced;
    private int lectureRequired;
    private int exerciseRequired;
//    Values joined from different tables.
    private List<Employee> employees = new ArrayList<>();
    private List<Float> yearAllocation = new ArrayList<>(Collections.nCopies(12, (float) 0));
    private List<Allocation> courseAllocations = new ArrayList<>();

    @Override
    public Course id(long id) {
        super.id(id);
        return this;
    }

    @Override
    public Course name(String name) {
        super.name(name);
        return this;
    }

    @Override
    public Course shortcut(String shortcut) {
        super.shortcut(shortcut);
        return this;
    }

    @Override
    public Course dateFrom(LocalDate dateFrom) {
        super.dateFrom(dateFrom);
        return this;
    }

    @Override
    public Course dateUntil(LocalDate dateUntil) {
        super.dateUntil(dateUntil);
        return this;
    }

    @Override
    public Course probability(float probability) {
        super.probability(probability);
        return this;
    }

    @Override
    public Course description(String description) {
        super.description(description);
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

    public Course introduced(LocalDate introduced) {
        this.introduced = introduced;
        return this;
    }

    public Course lectureRequired(int lectureRequired) {
        this.lectureRequired = lectureRequired;
        return this;
    }

    public Course exerciseRequired(int exerciseRequired) {
        this.exerciseRequired = exerciseRequired;
        return this;
    }

    public Course employees(List<Employee> employees) {
        this.employees = employees;
        return this;
    }

    public Course yearAllocation(List<Float> yearAllocation) {
        this.yearAllocation.addAll(yearAllocation);
        return this;
    }

    public Course courseAllocations(List<Allocation> courseAllocations) {
        this.courseAllocations.addAll(courseAllocations);
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
                ", introduced=" + introduced +
                ", lectureRequired=" + lectureRequired +
                ", exerciseRequired=" + exerciseRequired +
                ", employees=" + employees +
                ", yearAllocation=" + yearAllocation +
                ", courseAllocations=" + courseAllocations +
                '}';
    }
}
