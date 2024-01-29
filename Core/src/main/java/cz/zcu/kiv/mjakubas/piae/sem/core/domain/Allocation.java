package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Allocation {

    private long id;
    private Employee worker;
    private Project project;
    private Course course;
    private Function function;
    private float time; /* TODO - pro zobrazeni nevalidni alokace pouzivam time = -1 */
    private float isCertain;
    private int allocationScope;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateFrom;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateUntil;
    private TermState term;
    private String role;
    private String description;
    private Boolean active;

    private AssignmentState currentState;

    public enum AssignmentState {
        ACTIVE, INACTIVE, PAST, UNREALIZED, UNKNOWN
    }

    private AssignmentState calculateState() {
        if (this.dateFrom == null || this.dateUntil == null)
            return AssignmentState.UNKNOWN;

        if (!this.active)
            return AssignmentState.INACTIVE;

        if (new Date().before(this.dateFrom))
            return AssignmentState.UNREALIZED;

        if (new Date().after(this.dateUntil))
            return AssignmentState.PAST;

        return AssignmentState.ACTIVE;
    }

    public String getScopeInFTE() {
        return String.format("%.2f", (allocationScope / 60.0 / 40.0));
    }

    public String getScopeInHPW() {
        return String.format("%.2f", (allocationScope / 60.0));
    }

    public Allocation id(long id) {
        this.id = id;
        return this;
    }

    public Allocation worker(Employee worker) {
        this.worker = worker;
        return this;
    }

    public Allocation project(Project project) {
        this.project = project;
        return this;
    }

    public Allocation course(Course course) {
        this.course = course;
        return this;
    }

    public Allocation function(Function function) {
        this.function = function;
        return this;
    }

    public Allocation time(float time) {
        this.time = time;
        return this;
    }

    public Allocation isCertain(float isCertain) {
        this.isCertain = isCertain;
        return this;
    }

    public Allocation allocationScope(int allocationScope) {
        this.allocationScope = allocationScope;
        return this;
    }

    public Allocation dateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
        return this;
    }

    public Allocation dateUntil(Date dateUntil) {
        this.dateUntil = dateUntil;
        return this;
    }

    public Allocation term(TermState term) {
        this.term = term;
        return this;
    }

    public Allocation description(String description) {
        this.description = description;
        return this;
    }

    public Allocation role(String role) {
        this.role = role;
        return this;
    }

    public Allocation active(Boolean active) {
        if (active == null)
            this.active = true;
        else
            this.active = active;
        return this;
    }

    public Allocation currentState(AssignmentState currentState) {
        this.currentState = currentState;
        return this;
    }

    @Override
    public String toString() {
        return "Allocation{" +
                "id=" + id +
                ", worker=" + worker +
                ", project=" + project +
                ", allocationScope=" + allocationScope +
                ", time=" + time +
                ", isCertain=" + isCertain +
                ", dateFrom=" + dateFrom +
                ", dateUntil=" + dateUntil +
                ", term=" + term +
                ", description='" + description + '\'' +
                ", active=" + active +
                ", role=" + role +
                ", currentState=" + currentState +
                '}';
    }
}
