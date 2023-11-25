package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import cz.zcu.kiv.mjakubas.piae.sem.core.rules.AllocationInterval;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Employee {

    private long id;
    private String firstName;
    private String lastName;
    private String orionLogin;
    private String emailAddress;
    private Workplace workplace;
    private LocalDateTime dateCreated;
    private float certainTime;
    private float uncertainTime;
    private List<Employee> subordinates = new ArrayList<>();
    private List<Allocation> projectsAllocations = new ArrayList<>();
    private List<Allocation> coursesAllocations = new ArrayList<>();
    private List<Allocation> functionsAllocations = new ArrayList<>();
    private long participatingProjects;
    private long participatingCourses;
    private long participatingFunctions;
    private List<AllocationInterval> intervals = new ArrayList<>();

    public Employee id(long id) {
        this.id = id;
        return this;
    }

    public Employee firstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public Employee lastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public Employee orionLogin(String orionLogin) {
        this.orionLogin = orionLogin;
        return this;
    }

    public Employee emailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
        return this;
    }

    public Employee workplace(Workplace workplace) {
        this.workplace = workplace;
        return this;
    }

    public Employee dateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
        return this;
    }

    public Employee subordinates(List<Employee> subordinates) {
        this.subordinates.addAll(subordinates);
        return this;
    }

    public Employee certainTime(long certainTime) {
        this.certainTime = certainTime;
        return this;
    }

    public Employee uncertainTime(long uncertainTime) {
        this.uncertainTime = uncertainTime;
        return this;
    }

    public Employee coursesAllocations(List<Allocation> coursesAllocations) {
        this.coursesAllocations.addAll(coursesAllocations);
        return this;
    }

    public Employee projectsAllocations(List<Allocation> projectsAllocations) {
        this.projectsAllocations.addAll(projectsAllocations);
        return this;
    }

    public Employee functionsAllocations(List<Allocation> functionsAllocations) {
        this.functionsAllocations.addAll(functionsAllocations);
        return this;
    }

    public Employee participatingProjects(long participatingProjects) {
        this.participatingProjects = participatingProjects;
        return this;
    }

    public Employee participatingCourses(long participatingCourses) {
        this.participatingCourses = participatingCourses;
        return this;
    }

    public Employee participatingFunctions(long participatingFunctions) {
        this.participatingFunctions = participatingFunctions;
        return this;
    }

    public Employee intervals(List<AllocationInterval> intervals) {
        this.intervals.addAll(intervals);
        return this;
    }
}
