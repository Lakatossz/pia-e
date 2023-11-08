package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import cz.zcu.kiv.mjakubas.piae.sem.core.rules.AllocationInterval;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder(access = AccessLevel.PUBLIC)
@AllArgsConstructor
public class Employee {

    private final long id;
    private final String firstName;
    private final String lastName;
    private final String orionLogin;
    private final String emailAddress;
    private final Workplace workplace;
    private final LocalDateTime dateCreated;
    private final List<Employee> subordinates = new ArrayList<>();

    private final List<AllocationInterval> intervals = new ArrayList<>();
}
