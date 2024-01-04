package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.mandatorytests;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IEmployeeRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IUserRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.SecurityService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.ServiceException;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.ProjectService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.WorkplaceService;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class UserServiceUnitTests {

    @InjectMocks
    SecurityService securityService;
    @InjectMocks
    EmployeeService employeeServiceMocked;

    @Mock
    EmployeeService employeeService;
    @Mock
    IEmployeeRepository employeeRepository;
    @Mock
    IUserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    ProjectService projectService;
    @Mock
    WorkplaceService workplaceService;


    @Test
    public void test_create_employee_if_correct() {
        Employee employee = new Employee()
                .firstName("foo")
                .lastName("foo")
                .orionLogin("foo")
                .emailAddress("foo@foo.fo")
                .workplace(Workplace.builder()
                        .id(1L)
                        .build());
        when(employeeRepository.createEmployee(employee)).thenReturn(1L);
//        Assertions.assertDoesNotThrow(() -> employeeServiceMocked.createEmployee(
//                new EmployeeVO("foo", "foo", "foo@foo.fo", "foo", 1L, "foo")
//        ));
    }

    @Test
    public void test_create_employee_if_not_valid_email() {
        when(employeeRepository.createEmployee(any())).thenReturn(-1L); // not used
//        Assertions.assertThrows(ServiceException.class, () -> employeeServiceMocked.createEmployee(
//                new EmployeeVO("foo", "foo", "definitely wrong email address", "foo", 1L, "foo")
//        ));
    }

    @Test
    public void test_create_employee_if_employee_creation_was_unsuccessful() {
        when(employeeRepository.createEmployee(any())).thenReturn(-1L);
//        Assertions.assertThrows(ServiceException.class, () -> employeeServiceMocked.createEmployee(
//                new EmployeeVO("foo", "foo", "foo@foo.fo", "foo", 1L, "foo")
//        ));
    }

    @Test
    public void test_create_user_if_correct() {
        when(employeeService.getEmployee("foo")).thenReturn(new Employee().id(1L).orionLogin("foo"));
        when(userRepository.createNewUser(1L, "foo")).thenReturn(1L);
        when(userRepository.addUserRole("foo")).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("foo");

        Assertions.assertDoesNotThrow(() -> securityService.createUser("foo", "foo"));
    }

    @Test
    public void test_create_user_if_create_new_user_fails() {
        when(employeeService.getEmployee("foo")).thenReturn(new Employee().id(1L).orionLogin("foo"));
        when(userRepository.createNewUser(1L, "foo")).thenReturn(-1L);
        when(userRepository.addUserRole("foo")).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("foo");

        Assertions.assertThrows(ServiceException.class, () -> securityService.createUser("foo", "foo"));
    }

    @Test
    public void test_create_user_if_add_new_role_fails() {
        when(employeeService.getEmployee("foo")).thenThrow(RuntimeException.class);
        when(userRepository.createNewUser(1L, "foo")).thenReturn(-1L);
        when(userRepository.addUserRole("foo")).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("foo");

        Assertions.assertThrows(RuntimeException.class, () -> securityService.createUser("foo", "foo"));
    }

    @Test
    public void test_update_password_if_correct() {
        when(employeeService.getEmployee("foo")).thenReturn(new Employee().id(1L));
        when(userRepository.updatePassword(1L, "foo")).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("foo");
        Assertions.assertDoesNotThrow(() -> securityService.updateUserPassword("foo", "foo"));
    }

    @Test
    public void test_update_password_if_user_does_not_exist() {
        when(employeeService.getEmployee("foo")).thenThrow(RuntimeException.class);
        when(userRepository.updatePassword(1L, "foo")).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("foo");
        Assertions.assertThrows(RuntimeException.class, () -> securityService.updateUserPassword("foo", "foo"));
    }

    @Test
    public void test_update_password_if_update_password_fails() {
        when(employeeService.getEmployee("foo")).thenReturn(new Employee().id(1L));
        when(userRepository.updatePassword(1L, "foo")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("foo");
        Assertions.assertThrows(RuntimeException.class, () -> securityService.updateUserPassword("foo", "foo"));
    }

    @Test
    public void test_if_user_has_temporary_password_if_yes() {
        when(userRepository.isTemporary("foo")).thenReturn(true);
        Assertions.assertTrue(securityService.isTemporary("foo"));
    }

    @Test
    public void test_if_user_has_temporary_password_if_no() {
        when(userRepository.isTemporary("foo")).thenReturn(false);
        Assertions.assertFalse(securityService.isTemporary("foo"));
    }

    @Test
    public void test_if_user_has_temporary_password_if_user_does_not_exist() {
        when(userRepository.isTemporary("foo")).thenThrow(RuntimeException.class);
        Assertions.assertThrows(RuntimeException.class, () -> securityService.isTemporary("foo"));
    }

    @Test
    @WithMockUser("foo")
    public void test_if_current_view_is_user_view() {
        when(employeeService.getEmployee(1L)).thenReturn(new Employee().id(1L).orionLogin("foo"));
        Assertions.assertTrue(securityService.isUserView(1L));
    }

    @Test
    @WithMockUser("foo")
    public void test_if_current_view_is_not_user_view() {
        when(employeeService.getEmployee(1L)).thenReturn(new Employee().id(1L).orionLogin("foo"));
        Assertions.assertFalse(securityService.isUserView(1L));
    }

    @Test
    @WithMockUser("foo")
    public void test_if_current_view_is_user_superior_view() {
        when(employeeService.getEmployee(1L)).thenReturn(new Employee().id(1L).orionLogin("foo"));
        Assertions.assertTrue(securityService.isSuperiorView(1L));
    }

    @Test
    @WithMockUser("foo")
    public void test_if_current_view_is_not_user_superior_view() {
        when(employeeService.getEmployee(1L)).thenReturn(new Employee().id(1L).orionLogin("foo"));
        Assertions.assertFalse(securityService.isSuperiorView(1L));
    }

    @Test
    @WithMockUser("foo")
    public void test_if_user_is_project_manager_if_true() {
        when(employeeService.getEmployee("foo")).thenReturn(new Employee().id(1L).orionLogin("foo"));
        when(projectService.getProject(1L)).thenReturn(new Project().projectManager(new Employee().id(1L)));

        Assertions.assertTrue(securityService.isProjectManager(1L));
    }

    @Test
    @WithMockUser("foo")
    public void test_if_user_is_project_manager_if_false() {
        when(employeeService.getEmployee("foo")).thenReturn(new Employee().id(1L).orionLogin("foo"));
        when(projectService.getProject(1L)).thenReturn(new Project().projectManager(new Employee().id(2L)));

        Assertions.assertFalse(securityService.isProjectManager(1L));
    }

    @Test
    @WithMockUser("foo")
    public void test_if_user_is_project_manager_if_project_does_not_exist() {
        when(employeeService.getEmployee("foo")).thenReturn(new Employee().id(1L).orionLogin("foo"));
        when(projectService.getProject(1L)).thenThrow(RuntimeException.class);

        Assertions.assertThrows(RuntimeException.class, () -> securityService.isProjectManager(1L));
    }

    @Test
    @WithMockUser("foo")
    public void test_if_user_is_at_least_project_manager_if_is() {
        List<Project> projects = new ArrayList<>();
        projects.add(new Project().id(1L).projectManager(new Employee().id(1L)));
        when(projectService.getProjects()).thenReturn(projects);
        when(employeeService.getEmployee("foo")).thenReturn(new Employee().id(1L).orionLogin("foo"));
        when(projectService.getProject(1L)).thenReturn(new Project().id(1L).projectManager(new Employee().id(1)));

        Assertions.assertTrue(securityService.isAtLeastProjectManager());
    }

    @Test
    @WithMockUser("foo")
    public void test_if_user_is_at_least_project_manager_if_is_not() {
        List<Project> projects = new ArrayList<>();
        projects.add(new Project().id(1L).projectManager(new Employee().id(2L)));
        when(projectService.getProjects()).thenReturn(projects);
        when(employeeService.getEmployee("foo")).thenReturn(new Employee().id(1L).orionLogin("foo"));
        when(projectService.getProject(1L)).thenReturn(new Project().id(1L).projectManager(new Employee().id(2L)));

        Assertions.assertFalse(securityService.isAtLeastProjectManager());
    }

    @Test
    @WithMockUser("foo")
    public void test_if_user_is_workplace_manager_if_yes() {
        when(employeeService.getEmployee("foo")).thenReturn(new Employee().id(1L).orionLogin("foo"));
        when(projectService.getProject(1L)).thenReturn(new Project().projectWorkplace(Workplace.builder().id(1L).build()));
        when(workplaceService.getWorkplace(1L)).thenReturn(Workplace.builder().manager(new Employee().id(1L)).build());

        Assertions.assertTrue(securityService.isWorkplaceManager(1));
    }

    @Test
    @WithMockUser("foo")
    public void test_if_user_is_workplace_manager_if_no() {
        when(employeeService.getEmployee("foo")).thenReturn(new Employee().id(1L).orionLogin("foo"));
        when(projectService.getProject(1L)).thenReturn(new Project().projectWorkplace(Workplace.builder().id(1L).build()));
        when(workplaceService.getWorkplace(1L)).thenReturn(Workplace.builder().manager(new Employee().id(2L)).build());

        Assertions.assertFalse(securityService.isWorkplaceManager(1));
    }

    @Test
    @WithMockUser("foo")
    public void test_if_user_is_workplace_manager_if_project_does_not_exist() {
        when(employeeService.getEmployee("foo")).thenReturn(new Employee().id(1L).orionLogin("foo"));
        when(projectService.getProject(1L)).thenThrow(RuntimeException.class);
        when(workplaceService.getWorkplace(1L)).thenReturn(Workplace.builder().manager(new Employee().id(2L)).build());

        Assertions.assertThrows(RuntimeException.class, () -> securityService.isWorkplaceManager(1));
    }
}
