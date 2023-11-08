package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Allocation {

    private final long id;
    private final Employee worker;
    private final Project project;
    private final int allocationScope;
    private final LocalDate validFrom;
    private final LocalDate validUntil;
    private final String description;
    private final Boolean active;

    private final AssignmentState currentState;

    @Builder(access = AccessLevel.PUBLIC)
    public Allocation(long id, Employee worker, Project project, int allocationScope,
                      LocalDate validFrom, LocalDate validUntil, String description, Boolean active) {
        this.id = id;
        this.worker = worker;
        this.project = project;
        this.allocationScope = allocationScope;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.description = description;
        if (active == null)
            this.active = true;
        else
            this.active = active;

        this.currentState = calculateState();
    }

    public enum AssignmentState {
        ACTIVE, INACTIVE, PAST, UNREALIZED, UNKNOWN
    }

    private AssignmentState calculateState() {
        if (this.validFrom == null || this.validUntil == null)
            return AssignmentState.UNKNOWN;

        if (!this.active)
            return AssignmentState.INACTIVE;

        if (LocalDate.now().isBefore(this.validFrom))
            return AssignmentState.UNREALIZED;

        if (LocalDate.now().isAfter(this.validUntil))
            return AssignmentState.PAST;

        return AssignmentState.ACTIVE;
    }

    public String getScopeInFTE() {
        return String.format("%.2f", (allocationScope / 60.0 / 40.0));
    }

    public String getScopeInHPW() {
        return String.format("%.2f", (allocationScope / 60.0));
    }
}
