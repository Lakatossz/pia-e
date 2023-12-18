package cz.zcu.kiv.mjakubas.piae.sem.core.rules;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;

@AllArgsConstructor
@Getter
@Setter
public class AllocationInterval {

    private final Date from;
    private final Date until;
    private final float time;
    private HashMap<Allocation.AssignmentState, Integer> scopes;

    public boolean isFromInterval(Allocation allocation) {
        var alFrom = allocation.getDateFrom();
        var alUntil = allocation.getDateUntil();

        return (alFrom.before(this.until) || alFrom.equals(this.until)) && (alUntil.after(this.from) || alUntil.equals(this.from));
    }


    public int getScopeOfAll() {
        return scopes.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getScopeOfActive() {
        return scopes.getOrDefault(Allocation.AssignmentState.ACTIVE, 0);
    }

    public int getScopeOfPast() {
        return scopes.getOrDefault(Allocation.AssignmentState.PAST, 0);
    }

    public int getScopeOfUnrealized() {
        return scopes.getOrDefault(Allocation.AssignmentState.UNREALIZED, 0);
    }

    public String formatInFTE(int scope) {
        return String.format("%.2f", (scope / 60f / 40f));
    }

    public String formatInHPW(int scope) {
        return String.format("%.2f", (scope / 60f));
    }
}
