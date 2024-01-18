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
 * Objekt predstavujici aktivitu v prehledu po mesicich.
 */
public class ActivityAllocationDetail {

    private List<List<AllocationCell>> projectsAllocationCells = new LinkedList<>();
    private List<List<AllocationCell>> coursesAllocationCells = new LinkedList<>();
    private List<List<AllocationCell>> functionsAllocationCells = new LinkedList<>();
    private List<AllocationCell> totalCertainAllocationCells = new LinkedList<>();
    private List<AllocationCell> totalUncertainAllocationCells = new LinkedList<>();
    private String projectsName;
    private String coursesName;
    private String functionsName;
    private float projectsCertain;
    private float coursesCertain;
    private float functionsCertain;
    private long firstYear = Integer.MAX_VALUE;
    private long numberOfYears = 0;
}
