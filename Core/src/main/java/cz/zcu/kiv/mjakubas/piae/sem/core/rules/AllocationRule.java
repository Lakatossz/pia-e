package cz.zcu.kiv.mjakubas.piae.sem.core.rules;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;
import java.util.List;

/**
 * Represents data necessary for allocation validity check.
 */
@AllArgsConstructor
@Getter
public class  AllocationRule {

    final Date minDate;
    final Date maDate;

    final List<AllocationInterval> intervals;
}
