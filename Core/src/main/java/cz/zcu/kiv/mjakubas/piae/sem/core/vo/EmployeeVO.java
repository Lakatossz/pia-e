package cz.zcu.kiv.mjakubas.piae.sem.core.vo;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private String firstName;
    private String lastName;
    private String email;
    private String orionLogin;
    private Long workplaceId;
    private String password;
}
