package cz.zcu.kiv.mjakubas.piae.sem.core.domain.payload;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Course;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Function;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AllocationPayload {

    private List<Project> assignmentsProjects;

    private List<Course> assignmentsCourses;

    private List<Function> assignmentsFunctions;

    private List<Employee> allocationsEmployees;

    private List<Allocation> allocations;

    public AllocationPayload assignmentsProjects(List<Project> assignmentsProjects) {
        this.assignmentsProjects = assignmentsProjects;
        return this;
    }

    public AllocationPayload assignmentsCourses(List<Course> assignmentsCourses) {
        this.assignmentsCourses = assignmentsCourses;
        return this;
    }

    public AllocationPayload assignmentsFunctions(List<Function> assignmentsFunctions) {
        this.assignmentsFunctions = assignmentsFunctions;
        return this;
    }

    public AllocationPayload allocationsEmployees(List<Employee> allocationsEmployees) {
        this.allocationsEmployees = allocationsEmployees;
        return this;
    }

    public AllocationPayload allocations(List<Allocation> allocations) {
        this.allocations = allocations;
        return this;
    }
}
