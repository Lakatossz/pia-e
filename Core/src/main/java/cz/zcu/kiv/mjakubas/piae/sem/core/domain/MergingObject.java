package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.ProjectService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MergingObject {
    private long activityId;
    private long employeeId;
    private String role;
    private long firstYear;
    private long numberOfYears;
    private List<Allocation> allocations = new LinkedList<>();

    public MergingObject(long activityId, String role, Allocation allocation) {
        this.activityId = activityId;
        this.role = role;
        this.allocations.add(allocation);
    }

    public MergingObject(String role, long employeeId, Allocation allocation) {
        this.employeeId = employeeId;
        this.role = role;
        this.allocations.add(allocation);
    }

    public MergingObject activityId(long activityId) {
        this.activityId = activityId;
        return this;
    }

    public MergingObject employeeId(long employeeId) {
        this.employeeId = employeeId;
        return this;
    }

    public MergingObject role(String role) {
        this.role = role;
        return this;
    }

    public MergingObject firstYear(long firstYear) {
        this.firstYear = firstYear;
        return this;
    }

    public MergingObject numberOfYears(long numberOfYears) {
        this.numberOfYears = numberOfYears;
        return this;
    }

    public MergingObject allocations(Allocation allocations) {
        this.allocations.add(allocations);
        return this;
    }

    public void sortAllocationsByDate() {
        this.allocations.sort(Comparator.comparing(Allocation::getDateFrom));
        Calendar date = new GregorianCalendar();
        date.setTime(allocations.get(0).getDateFrom());
        this.firstYear = date.get(Calendar.YEAR);
        date.setTime(allocations.get(allocations.size() - 1).getDateUntil());
        this.numberOfYears = (date.get(Calendar.YEAR) - this.firstYear) + 1;
    }
}
