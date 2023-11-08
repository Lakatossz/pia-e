package cz.zcu.kiv.mjakubas.piae.sem.core.vo;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import lombok.AllArgsConstructor;
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
public class WorkplaceVO {

    private String name;
    private String abbreviation;
    private String manager;      // manager orion login
}
