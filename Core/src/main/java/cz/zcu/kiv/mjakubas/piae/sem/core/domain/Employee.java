package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import cz.zcu.kiv.mjakubas.piae.sem.core.rules.AllocationInterval;
import lombok.*;

import java.time.LocalDate;
import java.util.LinkedList;
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
    private LocalDate dateCreated;
    private float certainTime;
    private float uncertainTime;
    private String description;
    private List<Employee> subordinates = new LinkedList<>();
    private List<Allocation> projectsAllocations = new LinkedList<>();
    private List<Allocation> coursesAllocations = new LinkedList<>();
    private List<Allocation> functionsAllocations = new LinkedList<>();
    private List<Allocation> allocations = new LinkedList<>();
    private List<AllocationInterval> intervals = new LinkedList<>();

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

    public Employee dateCreated(LocalDate dateCreated) {
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

    public Employee description(String description) {
        this.description = description;
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

    public Employee allocations(List<Allocation> allocations) {
        this.allocations.addAll(allocations);
        return this;
    }

    public Employee intervals(List<AllocationInterval> intervals) {
        this.intervals.addAll(intervals);
        return this;
    }
}
