package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Employee;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import cz.zcu.kiv.mjakubas.piae.sem.core.exceptions.ServiceException;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.MyUtils;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.SecurityService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.AllocationService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.CourseService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.FunctionService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.ProjectService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.WorkplaceService;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.SerializationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * Contains all sites for working with employees.
 */
@Controller
@RequestMapping("/e")
@AllArgsConstructor
@PreAuthorize("hasAnyAuthority(" +
        "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
        "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
public class EmployeeV1Controller {

    private final EmployeeService employeeService;
    private final WorkplaceService workplaceService;
    private final SecurityService securityService;
    private final ProjectService projectService;
    private final CourseService courseService;
    private final FunctionService functionService;
    private final AllocationService allocationService;

    private final MyUtils utils;

    private static final String RESTRICITONS = "restrictions";
    private static final String WORKLPACES = "workplaces";
    private static final String USER_VO = "userVO";

    @GetMapping()
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_GUARANTOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_GUARANTOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String viewEmployees(Model model) {
        var employees = employeeService.getEmployees();
        employees.forEach(employee ->
                employee.getSubordinates().addAll(employeeService.getSubordinates(employee.getId())));

        List<Allocation> firstAllocations = employeeService.prepareFirst(employees);

        model.addAttribute("employees", employees);
        model.addAttribute("firstAllocations", firstAllocations);
        return "views/employees";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String createEmployee(Model model, RedirectAttributes redirectAttributes) {
        var employees = employeeService.getEmployees();
        var workplaces = workplaceService.getWorkplaces();

        model.addAttribute(RESTRICITONS, employees);
        model.addAttribute(WORKLPACES, workplaces);

        String orion = RandomStringUtils.random(4, true, true);

        var employeeVO = new EmployeeVO();
        employeeVO.setFirstName("Nový");
        employeeVO.setLastName("Uživatel");
        employeeVO.setOrionLogin(orion);
        employeeVO.setEmailAddress(orion + "@zcu.cz");
        employeeVO.setDateCreated(utils.convertToDate(LocalDate.now()));
        employeeVO.setPassword(RandomStringUtils.random(7, true, false));
        model.addAttribute("employee", employeeVO);
        return "forms/employee/create_employee_form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String createEmployee(@ModelAttribute EmployeeVO employeeVO,
                                 BindingResult errors, Model model,
                                 RedirectAttributes redirectAttributes) {

        Employee employee = new Employee()
                .firstName(employeeVO.getFirstName())
                .lastName(employeeVO.getLastName())
                .orionLogin(employeeVO.getOrionLogin())
                .emailAddress(employeeVO.getEmailAddress())
                .workplace(Workplace.builder().id(employeeVO.getWorkplaceId()).build())
                .dateCreated(employeeVO.getDateCreated())
                .description(employeeVO.getDescription());

        try {
            long id = securityService.createUserAccount(employee);
            redirectAttributes.addAttribute("id", id);
            return "redirect:/e/{id}/detail?create=success";
        } catch (ServiceException e) {

            var workplaces = workplaceService.getWorkplaces();
            model.addAttribute(WORKLPACES, workplaces);

            employeeVO.setOrionLogin(null);
            employeeVO.setEmailAddress(null);

            model.addAttribute("employee", employeeVO);
            return "forms/employee/create_employee_form";
        }
    }

    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_GUARANTOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_GUARANTOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String detailEmployee(Model model, @PathVariable long id) {
        var employee = employeeService.getEmployee(id);
        EmployeeVO employeeVO = new EmployeeVO(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getOrionLogin(),
                employee.getEmailAddress(),
                employee.getWorkplace().getId(),
                null,
                new Date(),
                employee.getCertainTime(),
                employee.getUncertainTime(),
                employee.getDescription());
        model.addAttribute("employee", employeeVO);

        var employees = employeeService.getEmployees();
        model.addAttribute(RESTRICITONS, employees);
        var workplaces = workplaceService.getWorkplaces();
        model.addAttribute(WORKLPACES, workplaces);

        var projects = projectService.getEmployeeProjects(id);
        model.addAttribute("projects", projects);
        var courses = courseService.getEmployeesCourses(id);
        model.addAttribute("courses", courses);
        var functions = functionService.getEmployeeFunctions(id);
        model.addAttribute("functions", functions);
        List<Allocation> firstProjectsAllocations = projectService.prepareFirst(projects);
        model.addAttribute("firstProjectsAllocations", firstProjectsAllocations);
        List<Allocation> firstCoursesAllocations = courseService.getFirstAllocationsForEachCourse(courses);
        model.addAttribute("firstCoursesAllocations", firstCoursesAllocations);
        List<Allocation> firstFunctionsAllocations = functionService.prepareFirst(functions);
        model.addAttribute("firstFunctionsAllocations", firstFunctionsAllocations);

        return "details/employee_detail";
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_GUARANTOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_GUARANTOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @GetMapping("/{id}/edit")
    public String editEmployee(Model model, @PathVariable long id) {
        var employee = employeeService.getEmployee(id);

        EmployeeVO employeeVO = new EmployeeVO(employee.getId(), employee.getFirstName(), employee.getLastName(),
                employee.getOrionLogin(), employee.getEmailAddress(), employee.getWorkplace().getId(),
                null, employee.getDateCreated(), employee.getCertainTime(), employee.getUncertainTime(),
                employee.getDescription());

        model.addAttribute(USER_VO, employeeVO);

        var employees = employeeService.getEmployees();
        model.addAttribute(RESTRICITONS, employees);
        var workplaces = workplaceService.getWorkplaces();
        model.addAttribute(WORKLPACES, workplaces);

        return "forms/employee/edit_employee_form";
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_GUARANTOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_GUARANTOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @PostMapping("/{id}/edit")
    public String editEmployee(@ModelAttribute EmployeeVO userVO, BindingResult errors, Model model,
                               @PathVariable long id) {
        employeeService.updateEmployee(userVO, id);
        return "redirect:/e/{id}/detail?edit=success";
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @PostMapping("/{id}/delete")
    public String deleteEmployee(Model model, @PathVariable long id) {
        employeeService.removeEmployee(id);
        return "redirect:/e?delete=success";
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_GUARANTOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_GUARANTOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @GetMapping("/{id}/subordinate/add")
    public String addSubordinate(Model model, @PathVariable long id, @ModelAttribute EmployeeVO userVO) {
        model.addAttribute(USER_VO, new EmployeeVO());

        var employee = employeeService.getEmployee(id);
        var restrictions = employeeService.getSubordinates(employee.getId());
        restrictions.add(employee);

        model.addAttribute(RESTRICITONS, restrictions);
        model.addAttribute("employees", employeeService.getEmployees());

        return "forms/employee/create_subordinate_form";
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_GUARANTOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SCHEDULER, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).COURSE_SUPERVISOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).FUNCTION_GUARANTOR, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    @PostMapping("/{id}/subordinate/add")
    public String addSubordinate(Model model, @PathVariable long id, @ModelAttribute EmployeeVO userVO,
                                 BindingResult errors) {

        /* it is what it is... */
        String str = userVO.getOrionLogin();
        String orion = str.substring(str.indexOf("(") + 1, str.indexOf(")"));
        userVO.setOrionLogin(orion);

        employeeService.addSubordinate(userVO, id);

        return "redirect:/e?subordinate=success";
    }
}
