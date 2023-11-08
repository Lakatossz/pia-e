package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.service.SecurityService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.WorkplaceService;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import jakarta.annotation.security.RolesAllowed;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * Contains all sites for working with employees.
 */
@Controller
@RequestMapping("/e")
@AllArgsConstructor
@PreAuthorize("hasAuthority(T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT)")
public class EmployeeV1Controller {

    private final EmployeeService employeeService;

    private final WorkplaceService workplaceService;

    private final SecurityService securityService;

    @GetMapping()
    public String viewEmployees(Model model) {
        var employees = employeeService.getEmployees();
        employees.forEach(employee ->
                employee.getSubordinates().addAll(employeeService.getSubordinates(employee.getId())));

        model.addAttribute("employees", employees);
        return "views/employees";
    }

    @GetMapping("/create")
    public String createEmployee(Model model) {
        var employees = employeeService.getEmployees();
        var workplaces = workplaceService.getWorkplaces();

        model.addAttribute("restrictions", employees);
        model.addAttribute("workplaces", workplaces);

        var employeeVO = new EmployeeVO();
        employeeVO.setPassword(RandomStringUtils.random(7, true, false));
        model.addAttribute("userVO", employeeVO);
        return "forms/employee/create_employee_form";
    }

    @PostMapping("/create")
    public String createEmployee(@ModelAttribute EmployeeVO employeeVO, BindingResult errors, Model model) {
        securityService.createUserAccount(employeeVO);
        return "redirect:/e?create=success";
    }

    @GetMapping("/{id}/edit")
    public String editEmployee(Model model, @PathVariable long id) {
        var employee = employeeService.getEmployee(id);
        var employees = employeeService.getEmployees();
        var workplaces = workplaceService.getWorkplaces();

        model.addAttribute("userVO",
                new EmployeeVO(employee.getFirstName(), employee.getLastName(), employee.getEmailAddress(),
                        employee.getOrionLogin(), employee.getWorkplace().getId(), null));

        model.addAttribute("restrictions", employees);
        model.addAttribute("workplaces", workplaces);

        return "forms/employee/edit_employee_form";
    }

    @PostMapping("/{id}/edit")
    public String editEmployee(@ModelAttribute EmployeeVO userVO, BindingResult errors, Model model,
                               @PathVariable long id) {

        employeeService.updateEmployee(userVO, id);
        return "redirect:/e?edit=success";
    }

    @GetMapping("/{id}/delete")
    public String deleteEmployee(Model model, @PathVariable long id) {

        employeeService.removeEmployee(id);
        return "redirect:/e?delete=success";
    }

    @GetMapping("/{id}/subordinate/add")
    public String addSubordinate(Model model, @PathVariable long id, @ModelAttribute EmployeeVO userVO) {
        model.addAttribute("userVO", new EmployeeVO());

        var employee = employeeService.getEmployee(id);
        var restrictions = employeeService.getSubordinates(employee.getId());
        restrictions.add(employee);

        model.addAttribute("restrictions", restrictions);
        model.addAttribute("employees", employeeService.getEmployees());

        return "forms/employee/create_subordinate_form";
    }

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
