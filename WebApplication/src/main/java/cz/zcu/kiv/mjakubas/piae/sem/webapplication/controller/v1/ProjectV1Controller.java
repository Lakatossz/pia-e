package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.v1;

import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Allocation;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.AllocationCell;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Project;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.ProjectState;
import cz.zcu.kiv.mjakubas.piae.sem.core.domain.Workplace;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.MyUtils;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.AllocationService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.EmployeeService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.ProjectService;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.v1.WorkplaceService;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.AllocationVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.EmployeeVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.ProjectVO;
import cz.zcu.kiv.mjakubas.piae.sem.core.vo.WorkplaceVO;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
    private final MyUtils utils;

    private static final String EMPLOYEES = "employees";
    private static final String RESTRICTIONS = "restrictions";

    private static final String PROJECT = "project";

    @GetMapping()
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String getProjects(Model model) {
        var projects = projectService.getProjects();
        projects.forEach(project ->
                project.getEmployees().addAll(projectService.getProjectEmployees(project.getId())));
        List<Allocation> firstAllocations = projectService.prepareFirst(projects);

        model.addAttribute("projects", projects);
        model.addAttribute("firstAllocations", firstAllocations);
        return "views/projects";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String createProject(Model model) {
        model.addAttribute(PROJECT, newProject());

        var projects = projectService.getProjects();
        var employees = employeeService.getEmployees();
        var workplaces = workplaceService.getWorkplaces();

        model.addAttribute("states", ProjectState.values());

        model.addAttribute(EMPLOYEES, employees);
        model.addAttribute("workplaces", workplaces);
        model.addAttribute(RESTRICTIONS, projects);

        return "forms/project/create_project_form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String createProject(@ModelAttribute ProjectVO projectVO,
                                BindingResult bindingResult, Model model,
                                RedirectAttributes redirectAttributes) {
        long id = projectService.createProject(projectVO);
        redirectAttributes.addAttribute("id", id);
        return "redirect:/p/{id}/detail?create=success";
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isProjectManager(#id) or @securityService.isWorkplaceManager(#id)")
    @PostMapping("/{id}/edit")
    public String editProject(Model model, @PathVariable long id, @ModelAttribute ProjectVO projectVO,
                              BindingResult errors, @RequestParam(required = false) Boolean manage) {

        System.out.println(projectVO);

        projectService.editProject(projectVO, id);

//        if (Boolean.TRUE.equals(manage))
//            return String.format("redirect:/p/%s/manage?edit=success", id);

        return "redirect:/p/{id}/detail?edit=success";
    }

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isProjectManager(#id) or @securityService.isWorkplaceManager(#id)")
    @GetMapping("/{id}/detail")
    public String detailProject(Model model, @PathVariable long id,
                               @RequestParam(required = false) Boolean manage) {
        Project project = projectService.getProject(id);
        var allocations = allocationService.getProjectAllocations(id).getAllocations();

        var allocationAverage = projectService.averageAllocation(project, allocations);

        System.out.println("project.getProjectWorkplace().getId(): " + project.getProjectWorkplace().getId());

        ProjectVO projectVO = new ProjectVO()
                .name(project.getName())
                .projectManagerId(project.getProjectManager().getId())
                .projectManagerName(project.getProjectManager().getLastName())
                .workplaceId(project.getProjectWorkplace().getId())
                .dateFrom(project.getDateFrom())
                .dateUntil(project.getDateUntil())
                .description(project.getDescription())
                .budget(project.getBudget())
                .probability(project.getProbability())
                .budgetParticipation(project.getBudgetParticipation())
                .totalTime(project.getTotalTime())
                .agency(project.getAgency())
                .grantTitle(project.getGrantTitle())
                .state(project.getState().getValue());

        model.addAttribute("allocations", mapAllocations(allocations));
        model.addAttribute(PROJECT, projectVO);

        model.addAttribute("newAllocation", newProjectAllocation(project));

        model.addAttribute("states", ProjectState.values());

        var employees = employeeService.getEmployees();
        employees.forEach(employee ->
                employee.getSubordinates().addAll(employeeService.getSubordinates(employee.getId())));

        List<List<AllocationCell>> allList = projectService.prepareProjectsCells(allocations);
        List<AllocationCell> certainList =
                !allList.isEmpty() ? new java.util.ArrayList<>(Collections.nCopies(allList.get(0).size(), new AllocationCell())) : new LinkedList<>();
        List<AllocationCell> uncertainList =
                !allList.isEmpty() ? new java.util.ArrayList<>(Collections.nCopies(allList.get(0).size(), new AllocationCell())) : new LinkedList<>();
        if (!allList.isEmpty()) {
            projectService.prepareProjectsCells(allList, certainList, uncertainList);
        }

        model.addAttribute("certainAllocations", certainList);
        model.addAttribute("uncertainAllocations", uncertainList);
        model.addAttribute("monthlyAllocations", allList);

        model.addAttribute(EMPLOYEES, employees);
        List<Workplace> workplaces = workplaceService.getWorkplaces();
        model.addAttribute("workplaces", workplaces);
        model.addAttribute("manage", manage);

        return "details/project_detail";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isProjectManager(#id) or @securityService.isWorkplaceManager(#id)")
    public String deleteProject(Model model, @PathVariable long id) {
        projectService.removeProject(id);
        return "redirect:/p?delete=success";
    }

    @GetMapping("/{id}/employee/add")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
    public String addSubordinate(Model model, @PathVariable long id, @ModelAttribute EmployeeVO userVO) {
        model.addAttribute("userVO", new EmployeeVO());

        var project = projectService.getProject(id);
        var restrictions = projectService.getProjectEmployees(project.getId());

        model.addAttribute(RESTRICTIONS, restrictions);
        model.addAttribute(EMPLOYEES, employeeService.getEmployees());

        return "forms/project/create_project_employee_form";
    }

    @PostMapping("/{id}/employee/add")
    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN)")
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

    @PreAuthorize("hasAnyAuthority(" +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).SECRETARIAT, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).PROJECT_ADMIN, " +
            "T(cz.zcu.kiv.mjakubas.piae.sem.webapplication.security.SecurityAuthority).ADMIN) " +
            " or @securityService.isProjectManager(#id) or @securityService.isWorkplaceManager(#id)")
    @GetMapping("/{id}/manage")
    public String manageProject(Model model, @PathVariable long id) {
        var project = projectService.getProject(id);
        project.getEmployees().addAll(projectService.getProjectEmployees(project.getId()));

        var payload = allocationService.getProjectAllocations(id);
        model.addAttribute("allocations", payload.getAllocations());
        model.addAttribute("projects", payload.getAssignmentsProjects());
        model.addAttribute(EMPLOYEES, payload.getAllocationsEmployees());

        model.addAttribute(PROJECT, project);
        return "views/project_management";
    }

    private ProjectVO newProject() {
        ProjectVO projectVO = new ProjectVO();
        projectVO.setState(ProjectState.NOVY.getValue());
        projectVO.setName("Nový projekt");
        return projectVO;
    }

    private AllocationVO newProjectAllocation(Project project) {
        AllocationVO newAllocation = new AllocationVO();
        newAllocation.setProjectId(project.getId());
        newAllocation.setWorkerId(1);
        newAllocation.setRole("nový");
        newAllocation.setIsCertain(1.0F);
        newAllocation.setAllocationScope(1);
        newAllocation.setDateFrom(utils.convertToLocalDateTime(project.getDateFrom()));
        newAllocation.setDateUntil(utils.convertToLocalDateTime(project.getDateUntil()));
        newAllocation.setDescription("description");

        return newAllocation;
    }

    private List<AllocationVO> mapAllocations(List<Allocation> allocations) {
        List<AllocationVO> allocationsVO = new LinkedList<>();
        for (Allocation allocation : allocations) {
            AllocationVO allocationVO = new AllocationVO();
            allocationVO.setId(allocation.getId());
            allocationVO.setProjectId(allocation.getProject().getId());
            allocationVO.setWorkerId(allocation.getWorker().getId());
            allocationVO.setRole(allocation.getRole());
            allocationVO.setDateFrom(utils.convertToLocalDateTime(allocation.getDateFrom()));
            allocationVO.setDateUntil(utils.convertToLocalDateTime(allocation.getDateUntil()));
            allocationVO.setAllocationScope((float) allocation.getAllocationScope() / (40 * 60));
            allocationVO.setIsCertain(allocation.getIsCertain());
            allocationVO.setDescription(allocation.getDescription());
            allocationsVO.add(allocationVO);
        }

        return allocationsVO;
    }
}
