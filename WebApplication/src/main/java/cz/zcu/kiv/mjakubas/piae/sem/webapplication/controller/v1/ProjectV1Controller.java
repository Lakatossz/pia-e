package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.AllocationService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.ProjectService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.WorkplaceService;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.ProjectVO;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * Contains all sites for working with projects.
 */
@Controller
@RequestMapping("/p")
@AllArgsConstructor
@PreAuthorize("hasAuthority(T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT)")
public class ProjectV1Controller {

    private final ProjectService projectService;
    private final EmployeeService employeeService;
    private final WorkplaceService workplaceService;

    private final AllocationService allocationService;

    private static final String EMPLOYEES = "employees";
    private static final String RESTRICTIONS = "restrictions";

    @GetMapping()
    public String getProjects(Model model) {
        var projects = projectService.getProjects();
        projects.forEach(project ->
                project.getEmployees().addAll(projectService.getProjectEmployees(project.getId())));

        model.addAttribute("projects", projects);
        return "views/projects";
    }

    @GetMapping("/create")
    public String createProject(Model model) {
        model.addAttribute("projectVO", new ProjectVO());

        var projects = projectService.getProjects();
        var employees = employeeService.getEmployees();
        var workplaces = workplaceService.getWorkplaces();

        model.addAttribute(EMPLOYEES, employees);
        model.addAttribute("workplaces", workplaces);
        model.addAttribute(RESTRICTIONS, projects);

        return "forms/project/create_project_form";
    }

    @PostMapping("/create")
    public String createProject(@ModelAttribute ProjectVO projectVO, BindingResult bindingResult, Model model) {
        projectVO.setProjectManagerId(projectVO.getProjectManagerId());

        projectService.createProject(projectVO);

        return "redirect:/p?create=success";
    }

    @PreAuthorize("hasAuthority(T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT)" +
            " or @securityService.isProjectManager(#id) or @securityService.isWorkplaceManager(#id)")
    @GetMapping("/{id}/edit")
    public String editProject(Model model, @PathVariable long id,
                              @RequestParam(required = false) Boolean manage) {
        Project data = projectService.getProject(id);

        model.addAttribute("projectVO",
                new ProjectVO()
                        .name(data.getName())
                        .projectManagerId(data.getProjectManager().getId())
                        .workplaceId(data.getProjectWorkplace().getId())
                        .dateFrom(data.getDateFrom())
                        .dateUntil(data.getDateUntil())
                        .description(data.getDescription())
                        .budget(data.getBudget())
                        .participation(data.getParticipation())
                        .totalTime(data.getTotalTime()));

        model.addAttribute(EMPLOYEES, employeeService.getEmployees());
        model.addAttribute("workplaces", workplaceService.getWorkplaces());

        model.addAttribute(RESTRICTIONS, projectService.getProjects());

        model.addAttribute("manage", manage);

        return "forms/project/edit_project_form";
    }

    @PreAuthorize("hasAuthority(T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT) or @securityService.isProjectManager(#id) or @securityService.isWorkplaceManager(#id)")
    @PostMapping("/{id}/edit")
    public String editProject(Model model, @PathVariable long id, @ModelAttribute ProjectVO projectVO,
                              BindingResult errors, @RequestParam(required = false) Boolean manage) {
        projectVO.setProjectManagerId(projectVO.getProjectManagerId());

        projectService.editProject(projectVO, id);

        if (Boolean.TRUE.equals(manage))
            return String.format("redirect:/p/%s/manage?edit=success", id);

        return "redirect:/p?edit=success";
    }

    @GetMapping("/{id}/delete")
    public String deleteProject(Model model, @PathVariable long id) {
        return "redirect:/p?delete=success";
    }

    @GetMapping("/{id}/employee/add")
    public String addSubordinate(Model model, @PathVariable long id, @ModelAttribute EmployeeVO userVO) {
        model.addAttribute("userVO", new EmployeeVO());

        var project = projectService.getProject(id);

        var restrictions = projectService.getProjectEmployees(project.getId());

        model.addAttribute(RESTRICTIONS, restrictions);
        model.addAttribute(EMPLOYEES, employeeService.getEmployees());

        return "forms/project/create_project_employee_form";
    }

    @PostMapping("/{id}/employee/add")
    public String addSubordinate(Model model, @PathVariable long id, @ModelAttribute EmployeeVO userVO,
                                 BindingResult errors) {
        model.addAttribute("userVO", userVO);
        var project = projectService.getProject(id);

        /* it is what it is... */
        String str = userVO.getOrionLogin();
        String orion = str.substring(str.indexOf("(") + 1, str.indexOf(")"));
        userVO.setOrionLogin(orion);

        projectService.assignEmployee(userVO, project.getId());

        return "redirect:/p?addemployee=success";
    }

    @PreAuthorize("@securityService.isProjectManager(#id) or @securityService.isWorkplaceManager(#id)")
    @GetMapping("/{id}/manage")
    public String manageProject(Model model, @PathVariable long id) {
        var project = projectService.getProject(id);
        project.getEmployees().addAll(projectService.getProjectEmployees(project.getId()));

        var payload = allocationService.getProjectAllocations(id);
        model.addAttribute("allocations", payload.getAllocations());
        model.addAttribute("projects", payload.getAssignmentsProjects());
        model.addAttribute(EMPLOYEES, payload.getAllocationsEmployees());

        model.addAttribute("project", project);
        return "views/project_management";
    }
}
