package cz.zcu.kiv.mjakubas.piae.sem.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

/*
 * Objekt predstavujici aktivitu v prehledu po mesicich - radek v prehledu.
 */
public class ActivityAllocationDetail {

    private List<AllocationCell> activityAllocationCells = new LinkedList<>();
    private List<Float> isCertainForYear;
    private List<String> isSameForWholeYear;
    private long activityId;
    private String activityName;
    private float activityCertain;
    private String activityRole;
}
