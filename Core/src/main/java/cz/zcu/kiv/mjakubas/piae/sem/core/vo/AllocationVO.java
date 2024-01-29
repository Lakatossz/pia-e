package cz.zcu.kiv.mjakubas.piae.sem.core.vo;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

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
@Data
public class AllocationVO {

    private long id;
    private long workerId;
    private long projectId;
    private long courseId;
    private long functionId;
    private float allocationScope;  // in x/FTE
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate dateFrom;
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate dateUntil;
    private String description;
    private String role;
    private String term;
    private float isCertain;

    private Boolean isActive = true;
}
