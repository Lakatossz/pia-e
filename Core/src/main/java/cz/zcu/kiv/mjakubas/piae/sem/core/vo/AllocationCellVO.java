package cz.zcu.kiv.mjakubas.piae.sem.core.vo;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Value object for representing cell no FE tables.
 *
 * @see Allocation
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class AllocationCellVO {
    long key;
    float time;
}
