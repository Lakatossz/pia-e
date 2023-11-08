package cz.zcu.kiv.mjakubas.piae.sem.core.domain.payload;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import lombok.Data;

import java.util.List;

@Data
public class AllocationPayload {

    final List<Project> assignmentsProjects;

    final List<Employee> allocationsEmployees;
    final List<Allocation> allocations;
}
