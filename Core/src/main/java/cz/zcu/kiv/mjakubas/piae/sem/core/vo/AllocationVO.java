package cz.zcu.kiv.mjakubas.piae.sem.core.vo;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Value object for representing {@link Allocation}.
 *
 * @see Allocation
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class AllocationVO {

    private long employeeId;
    private long projectId;
    private long courseId;
    private long functionId;
    private float allocationScope;  // in x/FTE
    private LocalDate dateFrom;
    private LocalDate dateUntil;
    private String description;

    private Boolean isActive = true;
}
