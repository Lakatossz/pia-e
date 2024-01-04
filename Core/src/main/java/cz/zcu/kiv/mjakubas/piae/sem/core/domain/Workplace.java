package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(access = AccessLevel.PUBLIC)
@AllArgsConstructor
public class Workplace {

    private final Long id;
    private final String abbreviation;
    private final String fullName;
    private final Employee manager;
    private final String description;
}
