package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller;

import ch.qos.logback.core.util.StringCollectionUtil;
import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IUserRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.SecurityService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class UserManagementTest {

    @Autowired
    EmployeeService employeeService;

    @Autowired
    SecurityService securityService;

    @Test
    public void test_create_user_if_unique() {
        String rng = RandomStringUtils.random(5, true, false);
//        employeeService.createEmployee(new EmployeeVO(rng, rng, rng + "@email.com", rng, 1L, "test"));
//        Assertions.assertDoesNotThrow(() -> securityService.createUser(rng, "test"));
    }

    @Test
    public void test_create_user_if_already_exists() {
        Assertions.assertThrows(RuntimeException.class, () -> securityService.createUser("admin", "test"));
    }

    @Test
    public void test_update_user_password_if_exists() {
        String rng = RandomStringUtils.random(5, true, false);
//        employeeService.createEmployee(new EmployeeVO(rng, rng, rng + "@email.com", rng, 1L, "test"));
//        securityService.createUser(rng, "test");
//        Assertions.assertDoesNotThrow(() -> securityService.updateUserPassword(rng, "test"));
    }

    @Test
    public void test_update_user_password_if_does_not_exists() {
        Assertions.assertThrows(RuntimeException.class, () -> securityService.updateUserPassword("asdasdasdasdadasadasd", "test"));
    }

    @Test
    public void test_if_user_has_temporary_password() {
        String rng = RandomStringUtils.random(5, true, false);
//        employeeService.createEmployee(new EmployeeVO(rng, rng, rng + "@email.com", rng, 1L, "test"));
//        securityService.createUser(rng, "test");
//        Assertions.assertTrue(securityService.isTemporary(rng));
    }

    @Test
    public void test_if_user_does_not_have_temporary_password() {
        String rng = RandomStringUtils.random(5, true, false);
//        employeeService.createEmployee(new EmployeeVO(rng, rng, rng + "@email.com", rng, 1L, "test"));
//        securityService.createUser(rng, "test");
//        securityService.updateUserPassword(rng, "test");
//        Assertions.assertFalse(securityService.isTemporary(rng));
    }

    @WithMockUser("admin")
    @Test
    public void test_if_viewed_page_is_user_page_and_user_can_view_it() {
        Assertions.assertTrue(securityService.isUserView(1L));
    }

    @WithMockUser("druhy")
    @Test
    public void test_if_viewed_page_is_not_user_page_and_user_can_not_view_it() {
        Assertions.assertFalse(securityService.isUserView(1L));
    }

    @WithMockUser("admin")
    @Test
    public void test_if_viewed_page_is_user_page_and_user_can_view_it_sup() {
        Assertions.assertTrue(securityService.isSuperiorView(1L));
    }

    @WithMockUser("druhy")
    @Test
    public void test_if_viewed_page_is_not_user_page_and_user_can_not_view_it_sup() {
        Assertions.assertFalse(securityService.isSuperiorView(1L));
    }

    @WithMockUser("projekt")
    @Test
    public void test_if_user_is_in_fact_project_manager() {
        Assertions.assertTrue(securityService.isProjectManager(1L));
    }

    @WithMockUser("druhy")
    @Test
    public void test_if_user_is_in_fact_not_project_manager() {
        Assertions.assertFalse(securityService.isProjectManager(1L));
    }

    @WithMockUser("projekt")
    @Test
    public void test_if_user_is_in_fact_atleast_project_manager() {
        Assertions.assertTrue(securityService.isAtLeastProjectManager());
    }

    @WithMockUser("druhy")
    @Test
    public void test_if_user_is_in_fact_not_atleast_project_manager() {
        Assertions.assertFalse(securityService.isAtLeastProjectManager());
    }

    @WithMockUser("admin")
    @Test
    public void test_if_user_is_in_fact_workplace_manager() {
        Assertions.assertTrue(securityService.isWorkplaceManager(1));
    }

    @WithMockUser("druhy")
    @Test
    public void test_if_user_is_in_fact_not_workplace_manager() {
        Assertions.assertFalse(securityService.isWorkplaceManager(1));
    }
}
