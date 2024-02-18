package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller;

import cz.zcu.kiv.mjakubas.piae.sem.core.exceptions.SecurityException;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.AllocationService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.ProjectService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controls index view.
 */
@Controller
@RequestMapping("/")
@AllArgsConstructor
public class IndexController {

    private final EmployeeService employeeService;

    private final ProjectService projectService;

    private static final String PERMISSION_ERROR = "permissionError";
    private static final String GENEREAL_ERROR = "generalError";

    @GetMapping("/")
    public String red() {
        return "redirect:/index";
    }

    @GetMapping("/index")
    public String viewIndex(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            var me = employeeService.getEmployee(auth.getName());
            var myProjects = projectService.getManagerProjects(me.getId());
            var managerProjects = projectService.getWorkplaceManagerProjects(me.getId());

            var employees = employeeService.getEmployees();
            employees.forEach(employee ->
                    employee.getSubordinates().addAll(employeeService.getSubordinates(employee.getId())));

            employees.forEach(employeeService::prepareAllocationsCells);

            model.addAttribute("employees", employees);

            model.addAttribute("me", me);
            model.addAttribute("myProjects", myProjects);
            model.addAttribute("workplaceProjects", managerProjects);
            return "main";
        } catch (SecurityException e) {
            model.addAttribute(PERMISSION_ERROR, true);
            return "main";
        } catch (Exception e) {
            model.addAttribute(GENEREAL_ERROR, true);
            e.printStackTrace();
            return "main";
        }
    }
}
