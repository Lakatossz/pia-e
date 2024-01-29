package cz.zcu.kiv.mjakubas.piae.sem.core.vo;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Value object for representing {@link Workplace}.
 *
 * @see Workplace
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class WorkplaceVO {

    private long id;
    private String name;
    private String abbreviation;
    private long manager;      // manager orion login
    private String description;      // manager orion login
}
