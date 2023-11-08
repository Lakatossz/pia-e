package cz.zcu.kiv.mjakubas.piae.sem.core.rules;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents data necessary for allocation validity check.
 */
@AllArgsConstructor
@Getter
public class  AllocationRule {

    final LocalDate minDate;
    final LocalDate maDate;

    final List<AllocationInterval> intervals;
}
