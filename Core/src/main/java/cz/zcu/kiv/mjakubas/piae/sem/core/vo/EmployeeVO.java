package cz.zcu.kiv.mjakubas.piae.sem.core.vo;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Value object for representing {@link Employee}.
 *
 * @see Employee
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class EmployeeVO {

    private long id;
    private String firstName;
    private String lastName;
    private String orionLogin;
    private String emailAddress;
    private Long workplaceId;
    private String password;
    private LocalDateTime dateCreated;
    private float certainTime;
    private float uncertainTime;
    private String description;
}
