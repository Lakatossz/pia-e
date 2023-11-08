package cz.zcu.kiv.mjakubas.piae.sem.core.vo;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Value object for representing {@link Project}.
 *
 * @see Project
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ProjectVO {

    private String projectName;
    private String projectManagerOrionLogin;
    private Long workplaceId;
    private LocalDate dateValidFrom;
    private LocalDate dateValidUntil;
    private String Description;
}
