package cz.zcu.kiv.mjakubas.piae.sem.webapplication.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

/**
 * Config for security.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    public DataSource dataSource;

    @Autowired
    public PasswordEncoder encoder;

    @Autowired
    public void configureAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        var sqlUsers = """
                SELECT e.emp_orion_login as username, password, enabled FROM user
                INNER JOIN employee e ON e.employee_id=us_employee
                WHERE e.emp_orion_login=?
                """;
        var sqlRoles = """
                SELECT e.emp_orion_login as username, a.auth_authotity FROM user
                INNER JOIN employee e ON e.employee_id=us_employee
                INNER JOIN authority a ON a.auth_user_id=user_id
                WHERE e.emp_orion_login=?
                """;

        auth.jdbcAuthentication().passwordEncoder(encoder)
                .dataSource(dataSource)
                .usersByUsernameQuery(sqlUsers)
                .authoritiesByUsernameQuery(sqlRoles);
    }
}
