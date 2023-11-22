//package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.mandatorytests;
//
//import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
//import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
//import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IEmployeeRepository;
//import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IUserRepository;
//import org.apache.commons.lang3.RandomStringUtils;
//import org.junit.Test;
//import org.junit.jupiter.api.Assertions;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//@RunWith(SpringRunner.class)
//@AutoConfigureMockMvc
//@SpringBootTest
//public class UserServiceIntegrationTests {
//    @Autowired
//    IUserRepository userRepository;
//    @Autowired
//    IEmployeeRepository employeeRepository;
//
//    @Test
//    public void test_create_user_employee_correct() {
//        String rng = RandomStringUtils.random(5, true, false);
//        var employee = new Employee(0, rng, rng, rng, rng + "@rng.com", Workplace.builder().id(1L).build(), null);
//        var result = employeeRepository.createEmployee(employee);
//        Assertions.assertTrue(result);
//    }
//
//    @Test
//    public void test_create_user_employee_if_orionLogin_already_exists() {
//        String rng = RandomStringUtils.random(5, true, false);
//        var employee = new Employee(0, rng, rng, "admin", rng + "@rng.com", Workplace.builder().id(1L).build(), null);
//        Assertions.assertThrows(RuntimeException.class, () -> employeeRepository.createEmployee(employee));
//    }
//
//    @Test
//    public void test_create_user_employee_if_email_already_exists() {
//        String rng = RandomStringUtils.random(5, true, false);
//        var employee = new Employee(0, rng, rng, rng, "admin" + "@admin.adm", Workplace.builder().id(1L).build(), null);
//        Assertions.assertThrows(RuntimeException.class, () -> employeeRepository.createEmployee(employee));
//    }
//
//    @Test
//    public void test_create_user_employee_if_missing_attributes() {
//        var employee = new Employee(0, null, null, null, null, null, null);
//        Assertions.assertThrows(RuntimeException.class, () -> employeeRepository.createEmployee(employee));
//    }
//
//    @Test
//    public void test_create_user_user_correct() {
//        String rng = RandomStringUtils.random(5, true, false);
//        var employee = new Employee(0, rng, rng, rng, rng + "@rng.com", Workplace.builder().id(1L).build(), null);
//        var result = employeeRepository.createEmployee(employee);
//        var empId = employeeRepository.fetchEmployee(rng).getId();
//        Assertions.assertTrue(userRepository.createNewUser(empId, rng));
//    }
//
//    @Test
//    public void test_create_user_user_incorrect() {
//        Assertions.assertThrows(RuntimeException.class, () -> userRepository.createNewUser(10000L, "weeeeee"));
//    }
//
//    @Test
//    public void test_create_user_user_if_already_exists() {
//        Assertions.assertThrows(RuntimeException.class, () -> userRepository.createNewUser(1L, "weeeeee"));
//    }
//
//    @Test
//    public void test_add_user_role_to_user() {
//        String rng = RandomStringUtils.random(5, true, false);
//        var employee = new Employee(0, rng, rng, rng, rng + "@rng.com", Workplace.builder().id(1L).build(), null);
//        var result = employeeRepository.createEmployee(employee);
//        var empId = employeeRepository.fetchEmployee(rng).getId();
//        userRepository.createNewUser(empId, rng);
//        Assertions.assertTrue(userRepository.addUserRole(rng));
//    }
//
//    @Test
//    public void test_add_user_role_if_user_does_not_exists() {
//        String rng = RandomStringUtils.random(5, true, false);
//        Assertions.assertThrows(RuntimeException.class, () -> userRepository.addUserRole(rng));
//    }
//
//    @Test
//    public void test_update_password_if_user_exists() {
//        String rng = RandomStringUtils.random(5, true, false);
//        var employee = new Employee(0, rng, rng, rng, rng + "@rng.com", Workplace.builder().id(1L).build(), null);
//        var result = employeeRepository.createEmployee(employee);
//        var empId = employeeRepository.fetchEmployee(rng).getId();
//        userRepository.createNewUser(empId, rng);
//        Assertions.assertTrue(userRepository.updatePassword(empId, rng));
//    }
//
//    @Test
//    public void test_update_password_if_user_does_not_exist() {
//        Assertions.assertThrows(RuntimeException.class, () -> userRepository.updatePassword(10000000L, "weee"));
//    }
//
//
//    @Test
//    public void test_if_user_password_is_temporary_if_temporary() {
//        String rng = RandomStringUtils.random(5, true, false);
//        var employee = new Employee(0, rng, rng, rng, rng + "@rng.com", Workplace.builder().id(1L).build(), null);
//        var result = employeeRepository.createEmployee(employee);
//        var empId = employeeRepository.fetchEmployee(rng).getId();
//        userRepository.createNewUser(empId, rng);
//        Assertions.assertTrue(userRepository.isTemporary(rng));
//    }
//
//    @Test
//    public void test_if_user_password_is_temporary_if_not_temporary() {
//        String rng = RandomStringUtils.random(5, true, false);
//        var employee = new Employee(0, rng, rng, rng, rng + "@rng.com", Workplace.builder().id(1L).build(), null);
//        var result = employeeRepository.createEmployee(employee);
//        var empId = employeeRepository.fetchEmployee(rng).getId();
//        userRepository.createNewUser(empId, rng);
//        userRepository.updatePassword(empId, rng);
//        Assertions.assertFalse(userRepository.isTemporary(rng));
//    }
//
//    @Test
//    public void test_is_user_password_is_temporary_if_user_does_not_exist() {
//        Assertions.assertThrows(RuntimeException.class, () -> userRepository.isTemporary("notExistingUser"));
//    }
//}
