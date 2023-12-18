package cz.zcu.kiv.mjakubas.piae.sem.core.vo;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * Value object for representing {@link Employee}.
 *
 * @see Employee
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class EmployeeVO {

    private long id;
    private String firstName;
    private String lastName;
    private String orionLogin;
    private String emailAddress;
    private Long workplaceId;
    private String password;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateCreated;
    private float certainTime;
    private float uncertainTime;
    private String description;
}
