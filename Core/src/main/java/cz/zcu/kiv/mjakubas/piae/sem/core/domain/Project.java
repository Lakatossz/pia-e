package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder(access = AccessLevel.PUBLIC)
@AllArgsConstructor
public class Project {

    private final long id;
    private final String projectName;
    private final Employee projectManager;
    private final Workplace projectWorkplace;
    private final LocalDate validFrom;
    private final LocalDate validUntil;
    private final String description;
    private final List<Employee> employees = new ArrayList<>();
}
